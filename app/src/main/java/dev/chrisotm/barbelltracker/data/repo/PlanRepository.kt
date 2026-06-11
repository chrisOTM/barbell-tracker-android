package dev.chrisotm.barbelltracker.data.repo

import dev.chrisotm.barbelltracker.data.dao.ExerciseDao
import dev.chrisotm.barbelltracker.data.dao.PlanDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutDao
import dev.chrisotm.barbelltracker.data.dao.WorkoutExerciseDao
import dev.chrisotm.barbelltracker.data.db.PlanTemplate
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.entity.PlanWithWorkouts
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PlanRepository {
    fun observeAll(): Flow<List<Plan>>
    fun observePlan(id: Long): Flow<PlanWithWorkouts?>
    suspend fun getPlan(id: Long): Plan?
    suspend fun getWorkout(id: Long): WorkoutWithExercises?

    suspend fun createPlan(name: String): Long
    suspend fun renamePlan(plan: Plan)
    suspend fun deletePlan(plan: Plan)
    /** Copies a template into a brand-new plan (US-1.3). */
    suspend fun createFromTemplate(template: PlanTemplate): Long

    suspend fun addWorkout(planId: Long, label: String): Long
    suspend fun deleteWorkout(workout: Workout)

    suspend fun addExercise(workoutId: Long, exerciseId: Long, sets: Int, reps: Int): Long
    suspend fun updateExercise(item: WorkoutExercise)
    suspend fun deleteExercise(item: WorkoutExercise)
    /** Persist a reordered list (positions assigned from list index). */
    suspend fun reorderExercises(items: List<WorkoutExercise>)
}

class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val exerciseDao: ExerciseDao
) : PlanRepository {

    override fun observeAll(): Flow<List<Plan>> = planDao.observeAll()

    override fun observePlan(id: Long): Flow<PlanWithWorkouts?> =
        planDao.observePlanWithWorkouts(id).map { it?.sorted() }

    override suspend fun getPlan(id: Long): Plan? = planDao.getById(id)

    override suspend fun getWorkout(id: Long): WorkoutWithExercises? =
        workoutDao.getWorkoutWithExercises(id)?.sorted()

    override suspend fun createPlan(name: String): Long = planDao.insert(Plan(name = name))

    override suspend fun renamePlan(plan: Plan) = planDao.update(plan)

    override suspend fun deletePlan(plan: Plan) = planDao.delete(plan)

    override suspend fun createFromTemplate(template: PlanTemplate): Long {
        val planId = planDao.insert(Plan(name = template.name))
        template.workouts.forEachIndexed { wIndex, tw ->
            val workoutId = workoutDao.insert(
                Workout(planId = planId, label = tw.label, position = wIndex)
            )
            val configs = tw.entries.mapIndexedNotNull { eIndex, entry ->
                val exercise = exerciseDao.getByName(entry.exerciseName) ?: return@mapIndexedNotNull null
                WorkoutExercise(
                    workoutId = workoutId,
                    exerciseId = exercise.id,
                    position = eIndex,
                    sets = entry.sets,
                    reps = entry.reps
                )
            }
            workoutExerciseDao.insertAll(configs)
        }
        return planId
    }

    override suspend fun addWorkout(planId: Long, label: String): Long {
        val pos = workoutDao.nextPosition(planId)
        return workoutDao.insert(Workout(planId = planId, label = label, position = pos))
    }

    override suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)

    override suspend fun addExercise(workoutId: Long, exerciseId: Long, sets: Int, reps: Int): Long {
        val pos = workoutExerciseDao.nextPosition(workoutId)
        return workoutExerciseDao.insert(
            WorkoutExercise(
                workoutId = workoutId,
                exerciseId = exerciseId,
                position = pos,
                sets = sets,
                reps = reps
            )
        )
    }

    override suspend fun updateExercise(item: WorkoutExercise) = workoutExerciseDao.update(item)

    override suspend fun deleteExercise(item: WorkoutExercise) = workoutExerciseDao.delete(item)

    override suspend fun reorderExercises(items: List<WorkoutExercise>) {
        val repositioned = items.mapIndexed { index, item -> item.copy(position = index) }
        workoutExerciseDao.updateAll(repositioned)
    }
}

private fun PlanWithWorkouts.sorted(): PlanWithWorkouts = copy(
    workouts = workouts.sortedBy { it.workout.position }.map { it.sorted() }
)

private fun WorkoutWithExercises.sorted(): WorkoutWithExercises = copy(
    exercises = exercises.sortedBy { it.config.position }
)
