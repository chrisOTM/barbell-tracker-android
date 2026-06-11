package dev.chrisotm.barbelltracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A movement in the library. Seeded set + user-created ones (US-1.1, US-1.2). */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroups: String = "",
    val description: String = "",
    val isCustom: Boolean = false,
    val isBodyweight: Boolean = false
)

/** A training plan; holds one or more workouts (US-1.4). */
@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/** A workout day (A, B, …) inside a plan (US-1.5). */
@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = Plan::class,
        parentColumns = ["id"],
        childColumns = ["planId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("planId")]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val label: String,
    val position: Int
)

/** Configuration of an exercise within a workout (US-1.4). */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val position: Int,
    val sets: Int,
    val reps: Int,
    val targetWeightKg: Double? = null,
    /** null = use RestDefaults derived from sets/reps. */
    val restSeconds: Int? = null
)

/** A logged training session header — diary entry (US-4.1). Names denormalized so
 *  history survives later edits/deletes of the plan. */
@Entity(tableName = "sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val workoutId: Long,
    val planName: String,
    val workoutName: String,
    val startedAt: Long,
    val endedAt: Long? = null
)

/** A single logged set inside a session (US-4.1). */
@Entity(
    tableName = "session_sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class SessionSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setIndex: Int,
    val plannedReps: Int,
    val actualReps: Int,
    val weightKg: Double,
    val success: Boolean,
    val restSeconds: Int
)
