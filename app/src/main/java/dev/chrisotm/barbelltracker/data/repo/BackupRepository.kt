package dev.chrisotm.barbelltracker.data.repo

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chrisotm.barbelltracker.data.dao.ExerciseDao
import dev.chrisotm.barbelltracker.data.dao.PlanDao
import dev.chrisotm.barbelltracker.data.dao.SessionDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutExerciseDao
import dev.chrisotm.barbelltracker.data.db.AppDatabase
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutSession
import dev.chrisotm.barbelltracker.data.io.BackupExercise
import dev.chrisotm.barbelltracker.data.io.BackupPlan
import dev.chrisotm.barbelltracker.data.io.BackupSession
import dev.chrisotm.barbelltracker.data.io.BackupSet
import dev.chrisotm.barbelltracker.data.io.BackupWorkout
import dev.chrisotm.barbelltracker.data.io.BackupWorkoutExercise
import dev.chrisotm.barbelltracker.data.io.HistoryBackup
import dev.chrisotm.barbelltracker.data.io.WorkoutsBackup
import dev.chrisotm.barbelltracker.domain.ExerciseNameIndex
import java.time.Instant
import javax.inject.Inject

enum class ImportMode { MERGE, REPLACE }

/** imported = top-level items added; skipped = entries dropped for unresolved references. */
data class ImportResult(val imported: Int, val skipped: Int)

interface BackupRepository {
    suspend fun buildWorkoutsBackup(): WorkoutsBackup
    suspend fun buildHistoryBackup(): HistoryBackup
    suspend fun importWorkouts(backup: WorkoutsBackup, mode: ImportMode): ImportResult
    suspend fun importHistory(backup: HistoryBackup, mode: ImportMode): ImportResult
}

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val exerciseDao: ExerciseDao,
    private val planDao: PlanDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val sessionDao: SessionDao
) : BackupRepository {

    override suspend fun buildWorkoutsBackup(): WorkoutsBackup {
        val exercises = exerciseDao.getAll().map {
            BackupExercise(it.name, it.muscleGroups, it.description, it.isCustom, it.isBodyweight)
        }
        val plans = planDao.getAllPlansWithWorkouts().map { pw ->
            BackupPlan(
                name = pw.plan.name,
                workouts = pw.workouts.map { w ->
                    BackupWorkout(
                        label = w.workout.label,
                        position = w.workout.position,
                        exercises = w.exercises.map { we ->
                            BackupWorkoutExercise(
                                exerciseName = we.exercise.name,
                                position = we.config.position,
                                sets = we.config.sets,
                                reps = we.config.reps,
                                targetWeightKg = we.config.targetWeightKg,
                                restSeconds = we.config.restSeconds
                            )
                        }
                    )
                }
            )
        }
        return WorkoutsBackup(exportedAt = now(), exercises = exercises, plans = plans)
    }

    override suspend fun buildHistoryBackup(): HistoryBackup {
        val sessions = sessionDao.getAllSessionsWithSets().map { sw ->
            BackupSession(
                planName = sw.session.planName,
                workoutName = sw.session.workoutName,
                startedAt = sw.session.startedAt,
                endedAt = sw.session.endedAt,
                sets = sw.sets.sortedBy { it.setIndex }.map { s ->
                    BackupSet(
                        exerciseName = s.exerciseName,
                        setIndex = s.setIndex,
                        plannedReps = s.plannedReps,
                        actualReps = s.actualReps,
                        weightKg = s.weightKg,
                        success = s.success,
                        restSeconds = s.restSeconds
                    )
                }
            )
        }
        return HistoryBackup(exportedAt = now(), sessions = sessions)
    }

    override suspend fun importWorkouts(backup: WorkoutsBackup, mode: ImportMode): ImportResult =
        db.withTransaction {
            if (mode == ImportMode.REPLACE) planDao.deleteAllPlans()

            // Resolve / create exercises. Built-ins match across languages, so a backup made
            // in German doesn't duplicate an English-seeded library as custom exercises.
            val library = libraryIndex()
            for (be in backup.exercises) {
                if (be.name.isBlank() || library.resolve(be.name) != null) continue
                val id = exerciseDao.insert(
                    Exercise(
                        name = be.name,
                        muscleGroups = be.muscleGroups,
                        description = be.description,
                        isCustom = true,
                        isBodyweight = be.isBodyweight
                    )
                )
                library.add(id, be.name)
            }

            var imported = 0
            var skipped = 0
            for (bp in backup.plans) {
                val planId = planDao.insert(Plan(name = bp.name))
                imported++
                for (bw in bp.workouts) {
                    val workoutId = workoutDao.insert(
                        Workout(planId = planId, label = bw.label, position = bw.position)
                    )
                    val items = bw.exercises.mapNotNull { bwe ->
                        val exId = library.resolve(bwe.exerciseName)
                        if (exId == null) { skipped++; null }
                        else WorkoutExercise(
                            workoutId = workoutId,
                            exerciseId = exId,
                            position = bwe.position,
                            sets = bwe.sets,
                            reps = bwe.reps,
                            targetWeightKg = bwe.targetWeightKg,
                            restSeconds = bwe.restSeconds
                        )
                    }
                    if (items.isNotEmpty()) workoutExerciseDao.insertAll(items)
                }
            }
            ImportResult(imported, skipped)
        }

    override suspend fun importHistory(backup: HistoryBackup, mode: ImportMode): ImportResult =
        db.withTransaction {
            if (mode == ImportMode.REPLACE) sessionDao.deleteAllSessions()

            // Exercise name -> id for progress linking (denormalized names are authoritative).
            // Logged names are in the UI language of the day, so match built-ins across languages.
            val library = libraryIndex()

            var imported = 0
            for (bs in backup.sessions) {
                val sessionId = sessionDao.insertSession(
                    WorkoutSession(
                        planId = 0,
                        workoutId = 0,
                        planName = bs.planName,
                        workoutName = bs.workoutName,
                        startedAt = bs.startedAt,
                        endedAt = bs.endedAt
                    )
                )
                imported++
                for (s in bs.sets) {
                    sessionDao.insertSet(
                        SessionSet(
                            sessionId = sessionId,
                            exerciseId = library.resolve(s.exerciseName) ?: 0L,
                            exerciseName = s.exerciseName,
                            setIndex = s.setIndex,
                            plannedReps = s.plannedReps,
                            actualReps = s.actualReps,
                            weightKg = s.weightKg,
                            success = s.success,
                            restSeconds = s.restSeconds
                        )
                    )
                }
            }
            ImportResult(imported, 0)
        }

    private suspend fun libraryIndex(): ExerciseNameIndex =
        ExerciseNameIndex(SeedCatalog.seedKeyByName(context)).apply {
            exerciseDao.getAll().forEach { add(it.id, it.name) }
        }

    private fun now(): String = Instant.now().toString()
}
