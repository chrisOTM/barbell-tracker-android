package dev.chrisotm.barbelltracker.data.db

import dev.chrisotm.barbelltracker.data.entity.Exercise

/** Predefined barbell exercise library (US-1.1). */
object SeedData {
    val exercises: List<Exercise> = listOf(
        Exercise(
            name = "Langhantel-Kniebeuge",
            muscleGroups = "Beine, Gesäß, Rumpf",
            description = "Langhantel im Nacken, tief in die Hocke gehen bis die Hüfte unter Kniehöhe ist, " +
                "Rücken gerade, kontrolliert wieder hochdrücken."
        ),
        Exercise(
            name = "Bankdrücken",
            muscleGroups = "Brust, Trizeps, Schultern",
            description = "Auf der Flachbank liegend die Langhantel zur Brust senken und " +
                "gerade nach oben drücken. Schulterblätter zusammen."
        ),
        Exercise(
            name = "Kreuzheben",
            muscleGroups = "Rücken, Beine, Gesäß",
            description = "Langhantel vom Boden mit geradem Rücken und gestreckter Hüfte anheben, " +
                "bis du aufrecht stehst. Stange nah am Körper führen."
        ),
        Exercise(
            name = "Langhantel-Rudern",
            muscleGroups = "Rücken, Bizeps",
            description = "Vorgebeugt mit geradem Rücken die Langhantel zum unteren Brustkorb ziehen, " +
                "Ellenbogen eng am Körper."
        ),
        Exercise(
            name = "Schulterdrücken",
            muscleGroups = "Schultern, Trizeps",
            description = "Aus dem Stand die Langhantel von Schulterhöhe gerade über den Kopf drücken, " +
                "Rumpf fest. Auch Military / Overhead Press."
        ),
        Exercise(
            name = "Frontkniebeuge",
            muscleGroups = "Beine, Rumpf",
            description = "Langhantel auf den vorderen Schultern ablegen, aufrecht in die Hocke gehen " +
                "und wieder hochdrücken."
        ),
        Exercise(
            name = "Langhantel-Ausfallschritte",
            muscleGroups = "Beine, Gesäß",
            description = "Langhantel im Nacken, abwechselnd große Schritte nach vorn und tief absenken, " +
                "vorderes Knie über dem Fuß."
        ),
        Exercise(
            name = "Good Mornings",
            muscleGroups = "Beinbeuger, unterer Rücken",
            description = "Langhantel im Nacken, mit geradem Rücken aus der Hüfte nach vorn beugen " +
                "und wieder aufrichten."
        ),
        Exercise(
            name = "Rumänisches Kreuzheben",
            muscleGroups = "Beinbeuger, Gesäß, Rücken",
            description = "Aus dem Stand mit leicht gebeugten Knien die Stange entlang der Beine absenken, " +
                "Hüfte nach hinten, dann aufrichten."
        ),
        Exercise(
            name = "Kurzhantel-Rudern einarmig",
            muscleGroups = "Rücken, Bizeps",
            description = "Eine Hand und ein Knie auf der Bank, mit der freien Hand die Kurzhantel " +
                "zur Hüfte ziehen."
        ),
        Exercise(
            name = "Klimmzüge",
            muscleGroups = "Rücken, Bizeps",
            description = "An der Stange hängend den Körper hochziehen bis das Kinn über der Stange ist, " +
                "kontrolliert ablassen.",
            isBodyweight = true
        )
    )
}

/** Definition of one exercise slot inside a template workout. */
data class TemplateEntry(val exerciseName: String, val sets: Int, val reps: Int)

/** A workout (day) inside a template. */
data class TemplateWorkout(val label: String, val entries: List<TemplateEntry>)

/** A ready-to-copy plan template (US-1.3). */
data class PlanTemplate(val name: String, val workouts: List<TemplateWorkout>)

object PlanTemplates {
    val all: List<PlanTemplate> = listOf(
        PlanTemplate(
            name = "GK 5×5",
            workouts = listOf(
                TemplateWorkout(
                    "A",
                    listOf(
                        TemplateEntry("Langhantel-Kniebeuge", 5, 5),
                        TemplateEntry("Bankdrücken", 5, 5),
                        TemplateEntry("Langhantel-Rudern", 5, 5)
                    )
                ),
                TemplateWorkout(
                    "B",
                    listOf(
                        TemplateEntry("Langhantel-Kniebeuge", 5, 5),
                        TemplateEntry("Schulterdrücken", 5, 5),
                        TemplateEntry("Kreuzheben", 1, 5)
                    )
                )
            )
        ),
        PlanTemplate(
            name = "GK 3×8",
            workouts = listOf(
                TemplateWorkout(
                    "A",
                    listOf(
                        TemplateEntry("Langhantel-Kniebeuge", 3, 8),
                        TemplateEntry("Bankdrücken", 3, 8),
                        TemplateEntry("Langhantel-Rudern", 3, 8)
                    )
                ),
                TemplateWorkout(
                    "B",
                    listOf(
                        TemplateEntry("Langhantel-Kniebeuge", 3, 8),
                        TemplateEntry("Schulterdrücken", 3, 8),
                        TemplateEntry("Kreuzheben", 1, 8)
                    )
                )
            )
        )
    )
}
