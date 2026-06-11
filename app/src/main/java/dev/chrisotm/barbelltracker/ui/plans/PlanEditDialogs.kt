package dev.chrisotm.barbelltracker.ui.plans

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.domain.RestDefaults
import dev.chrisotm.barbelltracker.ui.components.StepperField

@Composable
fun RenameDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan umbenennen") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Speichern") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun ExercisePickerDialog(
    library: List<Exercise>,
    onPick: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Übung wählen") },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                items(library, key = { it.id }) { ex ->
                    Card(
                        onClick = { onPick(ex) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ex.name, style = MaterialTheme.typography.titleMedium)
                            if (ex.muscleGroups.isNotBlank()) {
                                Text(ex.muscleGroups, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
fun ExerciseConfigDialog(
    config: WorkoutExercise,
    onConfirm: (WorkoutExercise) -> Unit,
    onDismiss: () -> Unit
) {
    var sets by remember { mutableStateOf(config.sets.toString()) }
    var reps by remember { mutableStateOf(config.reps.toString()) }
    var weight by remember { mutableStateOf(config.targetWeightKg?.let { trimNumber(it) } ?: "") }
    val defaultRest = RestDefaults.secondsFor(config.sets, config.reps)
    var rest by remember { mutableStateOf((config.restSeconds ?: defaultRest).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Übung konfigurieren") },
        text = {
            Column {
                StepperField(
                    label = "Sätze",
                    value = sets,
                    onValueChange = { sets = it.filter(Char::isDigit) },
                    onDecrement = { sets = ((sets.toIntOrNull() ?: 1) - 1).coerceAtLeast(1).toString() },
                    onIncrement = { sets = ((sets.toIntOrNull() ?: 0) + 1).toString() }
                )
                StepperField(
                    label = "Wdh.",
                    value = reps,
                    onValueChange = { reps = it.filter(Char::isDigit) },
                    onDecrement = { reps = ((reps.toIntOrNull() ?: 1) - 1).coerceAtLeast(1).toString() },
                    onIncrement = { reps = ((reps.toIntOrNull() ?: 0) + 1).toString() },
                    modifier = Modifier.padding(top = 8.dp)
                )
                StepperField(
                    label = "Gewicht (kg)",
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    onDecrement = { weight = trimNumber(((weight.toDoubleOrNull() ?: 0.0) - 2.5).coerceAtLeast(0.0)) },
                    onIncrement = { weight = trimNumber((weight.toDoubleOrNull() ?: 0.0) + 2.5) },
                    decimal = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
                StepperField(
                    label = "Pause (Sek.)",
                    value = rest,
                    onValueChange = { rest = it.filter(Char::isDigit) },
                    onDecrement = { rest = ((rest.toIntOrNull() ?: 30) - 15).coerceAtLeast(0).toString() },
                    onIncrement = { rest = ((rest.toIntOrNull() ?: 0) + 15).toString() },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    config.copy(
                        sets = sets.toIntOrNull()?.coerceAtLeast(1) ?: config.sets,
                        reps = reps.toIntOrNull()?.coerceAtLeast(1) ?: config.reps,
                        targetWeightKg = weight.toDoubleOrNull(),
                        restSeconds = rest.toIntOrNull()
                    )
                )
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

private fun trimNumber(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
