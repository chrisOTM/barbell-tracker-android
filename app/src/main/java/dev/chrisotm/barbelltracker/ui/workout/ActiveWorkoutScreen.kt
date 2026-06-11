package dev.chrisotm.barbelltracker.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisotm.barbelltracker.ui.components.ConfirmDialog
import dev.chrisotm.barbelltracker.ui.components.SetProgressDots
import dev.chrisotm.barbelltracker.ui.theme.Failure
import dev.chrisotm.barbelltracker.ui.theme.Success
import dev.chrisotm.barbelltracker.ui.util.formatWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onExit: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Keep the screen awake during the workout.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    var confirmEnd by remember { mutableStateOf(false) }
    var editingWeight by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout ${state.workoutLabel}") },
                actions = {
                    if (state.phase != Phase.FINISHED) {
                        IconButton(onClick = { confirmEnd = true }) {
                            Icon(Icons.Filled.Close, contentDescription = "Workout beenden")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.phase) {
                Phase.LOADING -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                Phase.RUNNING -> RunningContent(
                    state = state,
                    onResult = viewModel::recordSet,
                    onEditWeight = { editingWeight = true }
                )
                Phase.CONFIRM_NEXT -> ConfirmNextContent(
                    state = state,
                    onStartNext = viewModel::startNextExercise
                )
                Phase.FINISHED -> FinishedContent(
                    state = state,
                    onChoice = viewModel::setProgressionChoice,
                    onApply = { viewModel.applyProgression(onExit) }
                )
            }
        }
    }

    if (confirmEnd) {
        ConfirmDialog(
            title = "Wirklich beenden?",
            message = "Bereits absolvierte Sätze werden gespeichert.",
            confirmLabel = "Beenden",
            onConfirm = { confirmEnd = false; viewModel.endEarly() },
            onDismiss = { confirmEnd = false }
        )
    }
    if (editingWeight) {
        WeightDialog(
            initial = state.weightKg,
            onConfirm = { viewModel.changeWeight(it); editingWeight = false },
            onDismiss = { editingWeight = false }
        )
    }
}

@Composable
private fun RunningContent(
    state: ActiveUiState,
    onResult: (Boolean, Int) -> Unit,
    onEditWeight: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var actualReps by remember(state.exerciseName, state.setNumber) {
        mutableStateOf(state.plannedReps)
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(state.exerciseName, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onEditWeight) {
            Text(formatWeight(state.weightKg), style = MaterialTheme.typography.titleLarge)
            Text("  ✎")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Satz ${state.setNumber} von ${state.setCount}",
            style = MaterialTheme.typography.titleLarge
        )
        Text("${state.plannedReps} Wdh.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        SetProgressDots(
            total = state.setCount,
            currentIndex = state.setNumber - 1,
            results = state.dotResults
        )
        Spacer(Modifier.height(32.dp))

        // Editable actually-achieved reps (US-2.3).
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { actualReps = (actualReps - 1).coerceAtLeast(0) }) { Text("−") }
            Text(
                "  $actualReps Wdh.  ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = { actualReps += 1 }) { Text("+") }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onResult(true, actualReps)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Success),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) { Text("✓ Geschafft", style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onResult(false, actualReps)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Failure),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) { Text("✗ Nicht geschafft", style = MaterialTheme.typography.titleLarge) }
    }
}

/** Acknowledgement gate shown between exercises — no rest timer, no break between sets. */
@Composable
private fun ConfirmNextContent(
    state: ActiveUiState,
    onStartNext: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.height(64.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (state.finishedExerciseName.isNotBlank()) {
            Text(
                "${state.finishedExerciseName} abgeschlossen",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(32.dp))
        Text("Nächste Übung", style = MaterialTheme.typography.titleLarge)
        Text(
            state.nextExerciseName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onStartNext,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) { Text("Nächste Übung starten", style = MaterialTheme.typography.titleLarge) }
    }
}
