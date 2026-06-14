package dev.chrisotm.barbelltracker.data.io

import kotlinx.serialization.Serializable

/**
 * Open JSON backup formats. Cross-row references use *names*, not DB ids, so a file
 * imports cleanly onto any install (built-in exercises resolve via their seed name).
 */

const val SCHEMA_VERSION = 1
const val TYPE_WORKOUTS = "barbell-tracker-workouts"
const val TYPE_HISTORY = "barbell-tracker-history"

@Serializable
data class WorkoutsBackup(
    val schemaVersion: Int = SCHEMA_VERSION,
    val type: String = TYPE_WORKOUTS,
    val exportedAt: String = "",
    val exercises: List<BackupExercise> = emptyList(),
    val plans: List<BackupPlan> = emptyList()
)

@Serializable
data class BackupExercise(
    val name: String,
    val muscleGroups: String = "",
    val description: String = "",
    val isCustom: Boolean = false,
    val isBodyweight: Boolean = false
)

@Serializable
data class BackupPlan(
    val name: String,
    val workouts: List<BackupWorkout> = emptyList()
)

@Serializable
data class BackupWorkout(
    val label: String,
    val position: Int,
    val exercises: List<BackupWorkoutExercise> = emptyList()
)

@Serializable
data class BackupWorkoutExercise(
    val exerciseName: String,
    val position: Int,
    val sets: Int,
    val reps: Int,
    val targetWeightKg: Double? = null,
    val restSeconds: Int? = null
)

@Serializable
data class HistoryBackup(
    val schemaVersion: Int = SCHEMA_VERSION,
    val type: String = TYPE_HISTORY,
    val exportedAt: String = "",
    val sessions: List<BackupSession> = emptyList()
)

@Serializable
data class BackupSession(
    val planName: String,
    val workoutName: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val sets: List<BackupSet> = emptyList()
)

@Serializable
data class BackupSet(
    val exerciseName: String,
    val setIndex: Int,
    val plannedReps: Int,
    val actualReps: Int,
    val weightKg: Double,
    val success: Boolean,
    val restSeconds: Int
)
