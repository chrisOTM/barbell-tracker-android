package dev.chrisotm.barbelltracker.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisotm.barbelltracker.ui.components.StepperField
import dev.chrisotm.barbelltracker.ui.util.formatWeight

@Composable
fun FinishedContent(
    state: ActiveUiState,
    onChoice: (Long, Double) -> Unit,
    onApply: () -> Unit
) {
    // Local editable copy of the suggested next-session weights.
    val weights = remember {
        mutableStateMapOf<Long, Double>().apply {
            state.progression.forEach { put(it.exerciseId, it.suggestedWeightKg) }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Workout abgeschlossen!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        if (state.progression.isEmpty()) {
            Text(
                "Keine Sätze protokolliert.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            Text(
                "Vorschlag für das nächste Mal:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.progression, key = { it.exerciseId }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Aktuell: ${formatWeight(item.currentWeightKg)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val w = weights[item.exerciseId] ?: item.suggestedWeightKg
                        StepperField(
                            label = "Nächstes Gewicht",
                            value = if (w % 1.0 == 0.0) w.toInt().toString() else w.toString(),
                            onValueChange = { txt ->
                                txt.toDoubleOrNull()?.let {
                                    weights[item.exerciseId] = it; onChoice(item.exerciseId, it)
                                }
                            },
                            onDecrement = {
                                val nv = (w - 2.5).coerceAtLeast(0.0)
                                weights[item.exerciseId] = nv; onChoice(item.exerciseId, nv)
                            },
                            onIncrement = {
                                val nv = w + 2.5
                                weights[item.exerciseId] = nv; onChoice(item.exerciseId, nv)
                            },
                            decimal = true,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) { Text("Übernehmen & Fertig") }
    }
}

@Composable
fun WeightDialog(
    initial: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember {
        mutableStateOf(if (initial % 1.0 == 0.0) initial.toInt().toString() else initial.toString())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gewicht anpassen") },
        text = {
            StepperField(
                label = "Gewicht (kg)",
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                onDecrement = {
                    value = ((value.toDoubleOrNull() ?: 0.0) - 2.5).coerceAtLeast(0.0).let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                    }
                },
                onIncrement = {
                    value = ((value.toDoubleOrNull() ?: 0.0) + 2.5).let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                    }
                },
                decimal = true
            )
        },
        confirmButton = {
            TextButton(onClick = { value.toDoubleOrNull()?.let(onConfirm) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
