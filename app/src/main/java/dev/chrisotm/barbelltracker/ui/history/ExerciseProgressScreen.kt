package dev.chrisotm.barbelltracker.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.ui.components.LineChart
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProgressData(
    val exerciseName: String = "",
    val points: List<Pair<Float, Float>> = emptyList(),
    val topWeights: List<Double> = emptyList()
)

@HiltViewModel
class ExerciseProgressViewModel @javax.inject.Inject constructor(
    repository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L

    val data = repository.observeSetsForExercise(exerciseId).map { sets ->
        // Top weight per session, in chronological order.
        val perSession = sets.groupBy { it.sessionId }
            .map { (_, s) -> s.maxOf { it.weightKg } }
        ProgressData(
            exerciseName = sets.firstOrNull()?.exerciseName ?: "",
            points = perSession.mapIndexed { i, w -> i.toFloat() to w.toFloat() },
            topWeights = perSession
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressData())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(
    onBack: () -> Unit,
    viewModel: ExerciseProgressViewModel = hiltViewModel()
) {
    val data by viewModel.data.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(data.exerciseName.ifBlank { stringResource(R.string.progress_title) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (data.points.size < 2) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.not_enough_data),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(stringResource(R.string.weight_over_time), style = MaterialTheme.typography.titleMedium)
                LineChart(points = data.points, modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    stringResource(
                        R.string.progress_from_to,
                        formatWeight(data.topWeights.first()), formatWeight(data.topWeights.last())
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
