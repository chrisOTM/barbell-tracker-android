package dev.chrisotm.barbelltracker.ui.nav

object Routes {
    const val PLANS = "plans"
    const val EXERCISES = "exercises"
    const val EXERCISE_EDIT = "exercise_edit"          // ?exerciseId={id}  (0 = new)
    const val PLAN_CREATE = "plan_create"
    const val PLAN_EDIT = "plan_edit"                  // /{planId}
    const val WORKOUT_OVERVIEW = "workout_overview"    // /{workoutId}
    const val ACTIVE_WORKOUT = "active_workout"        // /{workoutId}
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SESSION_DETAIL = "session_detail"        // /{sessionId}
    const val EXERCISE_PROGRESS = "exercise_progress"  // /{exerciseId}

    fun exerciseEdit(exerciseId: Long = 0) = "$EXERCISE_EDIT?exerciseId=$exerciseId"
    fun planEdit(planId: Long) = "$PLAN_EDIT/$planId"
    fun workoutOverview(workoutId: Long) = "$WORKOUT_OVERVIEW/$workoutId"
    fun activeWorkout(workoutId: Long) = "$ACTIVE_WORKOUT/$workoutId"
    fun sessionDetail(sessionId: Long) = "$SESSION_DETAIL/$sessionId"
    fun exerciseProgress(exerciseId: Long) = "$EXERCISE_PROGRESS/$exerciseId"
}
