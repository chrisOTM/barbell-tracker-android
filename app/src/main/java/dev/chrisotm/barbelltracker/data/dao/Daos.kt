package dev.chrisotm.barbelltracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.entity.PlanWithWorkouts
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.SessionWithSets
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutSession
import dev.chrisotm.barbelltracker.data.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

    @Insert
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<Exercise>

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getById(id: Long): Plan?

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :id")
    fun observePlanWithWorkouts(id: Long): Flow<PlanWithWorkouts?>

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanWithWorkouts(id: Long): PlanWithWorkouts?

    @Transaction
    @Query("SELECT * FROM plans ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllPlansWithWorkouts(): List<PlanWithWorkouts>

    /** Wipes all plans; cascades to workouts + workout_exercises. */
    @Query("DELETE FROM plans")
    suspend fun deleteAllPlans()

    @Insert
    suspend fun insert(plan: Plan): Long

    @Update
    suspend fun update(plan: Plan)

    @Delete
    suspend fun delete(plan: Plan)
}

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutWithExercises(id: Long): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkoutWithExercises(id: Long): Flow<WorkoutWithExercises?>

    @Insert
    suspend fun insert(workout: Workout): Long

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM workouts WHERE planId = :planId")
    suspend fun nextPosition(planId: Long): Int
}

@Dao
interface WorkoutExerciseDao {
    @Insert
    suspend fun insert(item: WorkoutExercise): Long

    @Insert
    suspend fun insertAll(items: List<WorkoutExercise>)

    @Update
    suspend fun update(item: WorkoutExercise)

    @Update
    suspend fun updateAll(items: List<WorkoutExercise>)

    @Delete
    suspend fun delete(item: WorkoutExercise)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun nextPosition(workoutId: Long): Int
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Insert
    suspend fun insertSet(set: SessionSet): Long

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<WorkoutSession>>

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startedAt ASC")
    suspend fun getAllSessionsWithSets(): List<SessionWithSets>

    /** Wipes all sessions; cascades to session_sets. */
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id")
    fun observeSessionWithSets(id: Long): Flow<SessionWithSets?>

    /** Most recent logged weight for an exercise — used as next-session default (US-3.1). */
    @Query(
        "SELECT weightKg FROM session_sets WHERE exerciseId = :exerciseId " +
            "ORDER BY id DESC LIMIT 1"
    )
    suspend fun lastWeightFor(exerciseId: Long): Double?

    @Transaction
    @Query(
        "SELECT s.* FROM session_sets s INNER JOIN sessions ss ON s.sessionId = ss.id " +
            "WHERE s.exerciseId = :exerciseId ORDER BY ss.startedAt ASC, s.setIndex ASC"
    )
    fun observeSetsForExercise(exerciseId: Long): Flow<List<SessionSet>>
}
