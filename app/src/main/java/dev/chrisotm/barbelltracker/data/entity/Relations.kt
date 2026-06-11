package dev.chrisotm.barbelltracker.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/** A workout-exercise row joined with its library Exercise (for display). */
data class WorkoutExerciseWithExercise(
    @Embedded val config: WorkoutExercise,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: Exercise
)

/** A workout with its ordered, resolved exercises. */
data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = WorkoutExercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithExercise>
)

/** A plan with all of its workouts (each with exercises). */
data class PlanWithWorkouts(
    @Embedded val plan: Plan,
    @Relation(
        entity = Workout::class,
        parentColumn = "id",
        entityColumn = "planId"
    )
    val workouts: List<WorkoutWithExercises>
)

/** A session header with all logged sets. */
data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<SessionSet>
)
