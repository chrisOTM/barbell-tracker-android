package dev.chrisotm.barbelltracker.domain

/** One planned set during a live workout. */
data class ActiveSet(
    val setIndex: Int,
    val plannedReps: Int,
    var actualReps: Int,
    var success: Boolean = false,
    var logged: Boolean = false,
    var weightKg: Double = 0.0
)

/** One exercise during a live workout, with its sets and current target weight. */
data class ActiveExercise(
    val workoutExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val plannedReps: Int,
    val restSeconds: Int,
    var weightKg: Double,
    val sets: MutableList<ActiveSet>
)

/** Result of one exercise once the workout ends — drives progression (US-3.2). */
data class ExerciseProgression(
    val exerciseId: Long,
    val name: String,
    val weightKg: Double,
    val allSuccessful: Boolean
) {
    val suggestedNextWeightKg: Double = ProgressionCalculator.suggest(allSuccessful, weightKg)
}

/**
 * Pure in-memory state machine for a running workout (US-2.2 … US-2.5). No Android
 * dependencies, so it is fully unit-testable. The ViewModel mirrors its state into a
 * StateFlow and owns the timer/persistence around it.
 */
class WorkoutEngine(val exercises: List<ActiveExercise>) {

    var exerciseIndex: Int = 0
        private set
    var setIndex: Int = 0
        private set
    var finished: Boolean = false
        private set

    init {
        require(exercises.isNotEmpty()) { "Workout has no exercises" }
    }

    fun currentExercise(): ActiveExercise = exercises[exerciseIndex]
    fun currentSet(): ActiveSet = currentExercise().sets[setIndex]

    val currentSetNumber: Int get() = setIndex + 1
    val currentSetCount: Int get() = currentExercise().sets.size

    fun isLastSetOfExercise(): Boolean = setIndex == currentExercise().sets.lastIndex
    fun isLastExercise(): Boolean = exerciseIndex == exercises.lastIndex

    /** Log the current set's outcome. Returns the rest duration to count down. */
    fun recordCurrentSet(success: Boolean, actualReps: Int): Int {
        val ex = currentExercise()
        currentSet().apply {
            this.success = success
            this.actualReps = actualReps
            this.weightKg = ex.weightKg
            this.logged = true
        }
        return ex.restSeconds
    }

    /** Advance after the rest timer. Returns false when the workout is complete. */
    fun advance(): Boolean {
        if (!isLastSetOfExercise()) {
            setIndex++
        } else if (!isLastExercise()) {
            exerciseIndex++
            setIndex = 0
        } else {
            finished = true
            return false
        }
        return true
    }

    /** Change the weight of the current exercise; applies to current + remaining sets (US-3.3). */
    fun setWeightForCurrentExercise(weightKg: Double) {
        currentExercise().weightKg = weightKg
    }

    /** Per-exercise progression results for sets that were actually logged. */
    fun progressionResults(): List<ExerciseProgression> = exercises.mapNotNull { ex ->
        val logged = ex.sets.filter { it.logged }
        if (logged.isEmpty()) null
        else ExerciseProgression(
            exerciseId = ex.exerciseId,
            name = ex.name,
            weightKg = ex.weightKg,
            allSuccessful = logged.size == ex.sets.size && logged.all { it.success }
        )
    }

    fun loggedSets(): List<ActiveSetRecord> = exercises.flatMap { ex ->
        ex.sets.filter { it.logged }.map { s ->
            ActiveSetRecord(
                exerciseId = ex.exerciseId,
                exerciseName = ex.name,
                setIndex = s.setIndex,
                plannedReps = s.plannedReps,
                actualReps = s.actualReps,
                weightKg = s.weightKg,
                success = s.success,
                restSeconds = ex.restSeconds
            )
        }
    }
}

/** Flat record of a logged set, ready to be persisted as a SessionSet. */
data class ActiveSetRecord(
    val exerciseId: Long,
    val exerciseName: String,
    val setIndex: Int,
    val plannedReps: Int,
    val actualReps: Int,
    val weightKg: Double,
    val success: Boolean,
    val restSeconds: Int
)
