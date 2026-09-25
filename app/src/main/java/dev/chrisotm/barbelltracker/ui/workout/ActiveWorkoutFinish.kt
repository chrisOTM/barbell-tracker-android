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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.ui.components.StepperField
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import dev.chrisotm.barbelltracker.ui.util.formatWeightPlain
import dev.chrisotm.barbelltracker.ui.util.parseWeight
import dev.chrisotm.barbelltracker.ui.util.sanitizeWeightInput

@Composable
fun FinishedContent(
    state: ActiveUiState,
    onChoice: (Long, Double) -> Unit,
    onApply: () -> Unit
) {
    // Raw field text per slot, so partial input like "62." or an emptied field stays editable;
    // every parseable value is reported to the ViewModel as the current choice.
    val texts = remember {
        mutableStateMapOf<Long, String>().apply {
            state.progression.forEach { put(it.workoutExerciseId, formatWeightPlain(it.suggestedWeightKg)) }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            stringResource(R.string.workout_complete),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        if (state.progression.isEmpty()) {
            Text(
                stringResource(R.string.no_sets_logged),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            Text(
                stringResource(R.string.suggestion_next),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.progression, key = { it.workoutExerciseId }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.current_weight, formatWeight(item.currentWeightKg)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val slot = item.workoutExerciseId
                        val text = texts[slot] ?: formatWeightPlain(item.suggestedWeightKg)
                        val w = parseWeight(text) ?: item.suggestedWeightKg
                        fun choose(nv: Double) {
                            texts[slot] = formatWeightPlain(nv); onChoice(slot, nv)
                        }
                        StepperField(
                            label = stringResource(R.string.next_weight),
                            value = text,
                            onValueChange = { raw ->
                                val clean = sanitizeWeightInput(raw)
                                texts[slot] = clean
                                parseWeight(clean)?.let { onChoice(slot, it) }
                            },
                            onDecrement = { choose((w - 2.5).coerceAtLeast(0.0)) },
                            onIncrement = { choose(w + 2.5) },
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
        ) { Text(stringResource(R.string.apply_finish)) }
    }
}

@Composable
fun WeightDialog(
    initial: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(formatWeightPlain(initial)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adjust_weight)) },
        text = {
            StepperField(
                label = stringResource(R.string.weight_kg),
                value = value,
                onValueChange = { value = sanitizeWeightInput(it) },
                onDecrement = { value = formatWeightPlain(((parseWeight(value) ?: 0.0) - 2.5).coerceAtLeast(0.0)) },
                onIncrement = { value = formatWeightPlain((parseWeight(value) ?: 0.0) + 2.5) },
                decimal = true
            )
        },
        confirmButton = {
            TextButton(onClick = { parseWeight(value)?.let(onConfirm) }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
