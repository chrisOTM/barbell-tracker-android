package dev.chrisotm.barbelltracker.ui.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutExerciseWithExercise
import dev.chrisotm.barbelltracker.data.entity.WorkoutWithExercises
import dev.chrisotm.barbelltracker.domain.RestDefaults
import dev.chrisotm.barbelltracker.ui.components.ConfirmDialog
import dev.chrisotm.barbelltracker.ui.util.formatDuration
import dev.chrisotm.barbelltracker.ui.util.formatWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditScreen(
    onBack: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    viewModel: PlanEditViewModel = hiltViewModel()
) {
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val library by viewModel.library.collectAsStateWithLifecycle()

    var renaming by remember { mutableStateOf(false) }
    var deletingPlan by remember { mutableStateOf(false) }
    var pickerForWorkout by remember { mutableStateOf<Long?>(null) }
    var editingConfig by remember { mutableStateOf<WorkoutExercise?>(null) }
    var replacingConfig by remember { mutableStateOf<WorkoutExercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.plan?.name ?: stringResource(R.string.plan)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { renaming = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.rename))
                    }
                    IconButton(onClick = { deletingPlan = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_plan))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(plan?.workouts ?: emptyList(), key = { it.workout.id }) { w ->
                WorkoutCard(
                    workout = w,
                    onStart = { onStartWorkout(w.workout.id) },
                    onDeleteWorkout = { viewModel.deleteWorkout(w.workout) },
                    onAddExercise = { pickerForWorkout = w.workout.id },
                    onEditConfig = { editingConfig = it },
                    onSwapConfig = { replacingConfig = it },
                    onDeleteConfig = { viewModel.deleteExercise(it) },
                    onMove = { index, up ->
                        viewModel.moveExercise(w.exercises.map { it.config }, index, up)
                    }
                )
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.addWorkout() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.add_workout)) }
            }
        }
    }

    if (renaming) {
        RenameDialog(
            initial = plan?.plan?.name ?: "",
            onConfirm = { viewModel.renamePlan(it); renaming = false },
            onDismiss = { renaming = false }
        )
    }
    if (deletingPlan) {
        ConfirmDialog(
            title = stringResource(R.string.delete_plan_title),
            message = stringResource(R.string.delete_plan_msg),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = { deletingPlan = false; viewModel.deletePlan(onBack) },
            onDismiss = { deletingPlan = false }
        )
    }
    pickerForWorkout?.let { workoutId ->
        ExercisePickerDialog(
            library = library,
            onPick = { viewModel.addExercise(workoutId, it); pickerForWorkout = null },
            onDismiss = { pickerForWorkout = null }
        )
    }
    replacingConfig?.let { config ->
        ExercisePickerDialog(
            library = library,
            onPick = { viewModel.swapExercise(config, it); replacingConfig = null },
            onDismiss = { replacingConfig = null }
        )
    }
    editingConfig?.let { config ->
        ExerciseConfigDialog(
            config = config,
            onConfirm = { viewModel.updateExercise(it); editingConfig = null },
            onDismiss = { editingConfig = null }
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutWithExercises,
    onStart: () -> Unit,
    onDeleteWorkout: () -> Unit,
    onAddExercise: () -> Unit,
    onEditConfig: (WorkoutExercise) -> Unit,
    onSwapConfig: (WorkoutExercise) -> Unit,
    onDeleteConfig: (WorkoutExercise) -> Unit,
    onMove: (index: Int, up: Boolean) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.workout_label, workout.workout.label),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteWorkout) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_workout))
                }
            }
            workout.exercises.forEachIndexed { index, item ->
                ExerciseConfigRow(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == workout.exercises.lastIndex,
                    onEdit = { onEditConfig(item.config) },
                    onSwap = { onSwapConfig(item.config) },
                    onDelete = { onDeleteConfig(item.config) },
                    onUp = { onMove(index, true) },
                    onDown = { onMove(index, false) }
                )
            }
            OutlinedButton(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text(stringResource(R.string.add_exercise)) }
            FilledTonalButton(
                onClick = onStart,
                enabled = workout.exercises.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("  " + stringResource(R.string.start))
            }
        }
    }
}

@Composable
private fun ExerciseConfigRow(
    item: WorkoutExerciseWithExercise,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onSwap: () -> Unit,
    onDelete: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    val c = item.config
    val rest = RestDefaults.effective(c.restSeconds, c.sets, c.reps)
    val weightText = c.targetWeightKg?.let { " · ${formatWeight(it)}" } ?: ""
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${c.sets}×${c.reps}$weightText · " +
                        stringResource(R.string.rest_value, formatDuration(rest)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onUp, enabled = !isFirst) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = stringResource(R.string.move_up))
            }
            IconButton(onClick = onDown, enabled = !isLast) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = stringResource(R.string.move_down))
            }
        }
        Row {
            IconButton(onClick = onSwap) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.swap_exercise))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove))
            }
        }
    }
}
