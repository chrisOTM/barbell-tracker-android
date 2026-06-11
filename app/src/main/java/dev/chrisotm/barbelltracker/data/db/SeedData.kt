package dev.chrisotm.barbelltracker.data.db

import android.content.Context
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.entity.Exercise

/**
 * Predefined barbell exercise library (US-1.1), localized via string resources.
 * Built from a [Context] so the library is seeded in the app's active language on first launch.
 */
object SeedData {
    /** name-res / muscles-res / desc-res / bodyweight for each starter exercise. */
    private data class SeedExercise(
        val nameRes: Int,
        val musclesRes: Int,
        val descRes: Int,
        val bodyweight: Boolean = false
    )

    private val defs = listOf(
        SeedExercise(R.string.seed_back_squat_name, R.string.seed_back_squat_muscles, R.string.seed_back_squat_desc),
        SeedExercise(R.string.seed_bench_press_name, R.string.seed_bench_press_muscles, R.string.seed_bench_press_desc),
        SeedExercise(R.string.seed_deadlift_name, R.string.seed_deadlift_muscles, R.string.seed_deadlift_desc),
        SeedExercise(R.string.seed_barbell_row_name, R.string.seed_barbell_row_muscles, R.string.seed_barbell_row_desc),
        SeedExercise(R.string.seed_ohp_name, R.string.seed_ohp_muscles, R.string.seed_ohp_desc),
        SeedExercise(R.string.seed_front_squat_name, R.string.seed_front_squat_muscles, R.string.seed_front_squat_desc),
        SeedExercise(R.string.seed_lunges_name, R.string.seed_lunges_muscles, R.string.seed_lunges_desc),
        SeedExercise(R.string.seed_good_mornings_name, R.string.seed_good_mornings_muscles, R.string.seed_good_mornings_desc),
        SeedExercise(R.string.seed_rdl_name, R.string.seed_rdl_muscles, R.string.seed_rdl_desc),
        SeedExercise(R.string.seed_one_arm_row_name, R.string.seed_one_arm_row_muscles, R.string.seed_one_arm_row_desc),
        SeedExercise(R.string.seed_pullups_name, R.string.seed_pullups_muscles, R.string.seed_pullups_desc, bodyweight = true)
    )

    fun exercises(context: Context): List<Exercise> = defs.map {
        Exercise(
            name = context.getString(it.nameRes),
            muscleGroups = context.getString(it.musclesRes),
            description = context.getString(it.descRes),
            isBodyweight = it.bodyweight
        )
    }
}

/** Definition of one exercise slot inside a template workout (exercise referenced by name res). */
data class TemplateEntry(val exerciseNameRes: Int, val sets: Int, val reps: Int)

/** A workout (day) inside a template. */
data class TemplateWorkout(val label: String, val entries: List<TemplateEntry>)

/** A ready-to-copy plan template (US-1.3), with all names resolved to the active language. */
data class PlanTemplate(
    val name: String,
    val workouts: List<ResolvedTemplateWorkout>
)

data class ResolvedTemplateEntry(val exerciseName: String, val sets: Int, val reps: Int)
data class ResolvedTemplateWorkout(val label: String, val entries: List<ResolvedTemplateEntry>)

object PlanTemplates {
    private val templates = listOf(
        R.string.seed_tpl_gk5x5 to listOf(
            TemplateWorkout(
                "A",
                listOf(
                    TemplateEntry(R.string.seed_back_squat_name, 5, 5),
                    TemplateEntry(R.string.seed_bench_press_name, 5, 5),
                    TemplateEntry(R.string.seed_barbell_row_name, 5, 5)
                )
            ),
            TemplateWorkout(
                "B",
                listOf(
                    TemplateEntry(R.string.seed_back_squat_name, 5, 5),
                    TemplateEntry(R.string.seed_ohp_name, 5, 5),
                    TemplateEntry(R.string.seed_deadlift_name, 1, 5)
                )
            )
        ),
        R.string.seed_tpl_gk3x8 to listOf(
            TemplateWorkout(
                "A",
                listOf(
                    TemplateEntry(R.string.seed_back_squat_name, 3, 8),
                    TemplateEntry(R.string.seed_bench_press_name, 3, 8),
                    TemplateEntry(R.string.seed_barbell_row_name, 3, 8)
                )
            ),
            TemplateWorkout(
                "B",
                listOf(
                    TemplateEntry(R.string.seed_back_squat_name, 3, 8),
                    TemplateEntry(R.string.seed_ohp_name, 3, 8),
                    TemplateEntry(R.string.seed_deadlift_name, 1, 8)
                )
            )
        )
    )

    fun all(context: Context): List<PlanTemplate> = templates.map { (nameRes, workouts) ->
        PlanTemplate(
            name = context.getString(nameRes),
            workouts = workouts.map { w ->
                ResolvedTemplateWorkout(
                    label = w.label,
                    entries = w.entries.map { e ->
                        ResolvedTemplateEntry(
                            exerciseName = context.getString(e.exerciseNameRes),
                            sets = e.sets,
                            reps = e.reps
                        )
                    }
                )
            }
        )
    }
}
