package dev.chrisotm.barbelltracker.ui.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import dev.chrisotm.barbelltracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.WorkoutWithExercises
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import dev.chrisotm.barbelltracker.domain.RestDefaults
import dev.chrisotm.barbelltracker.ui.util.formatDuration
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutOverviewViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: 0L
    val workout = MutableStateFlow<WorkoutWithExercises?>(null)

    init {
        viewModelScope.launch { workout.value = planRepository.getWorkout(workoutId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutOverviewScreen(
    onBack: () -> Unit,
    onStart: (Long) -> Unit,
    viewModel: WorkoutOverviewViewModel = hiltViewModel()
) {
    val workout by viewModel.workout.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_label, workout?.workout?.label ?: "")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(workout?.exercises ?: emptyList(), key = { it.config.id }) { item ->
                    val c = item.config
                    val rest = RestDefaults.effective(c.restSeconds, c.sets, c.reps)
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(item.exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${c.sets}×${c.reps}" +
                                    (c.targetWeightKg?.let { " · ${formatWeight(it)}" }
                                        ?: " · ${stringResource(R.string.weight_open)}") +
                                    " · " + stringResource(R.string.rest_value, formatDuration(rest)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            val id = workout?.workout?.id
            Button(
                onClick = { id?.let(onStart) },
                enabled = id != null && (workout?.exercises?.isNotEmpty() == true),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("  " + stringResource(R.string.start_workout))
            }
        }
    }
}
