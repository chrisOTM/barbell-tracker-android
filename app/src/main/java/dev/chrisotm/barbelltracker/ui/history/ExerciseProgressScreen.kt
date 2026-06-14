package dev.chrisotm.barbelltracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.ui.components.LineChart
import dev.chrisotm.barbelltracker.ui.util.formatDay
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

data class ProgressData(
    val exerciseName: String = "",
    val points: List<Pair<Float, Float>> = emptyList(),
    val firstStartedAt: Long = 0,
    val lastStartedAt: Long = 0,
    val minWeight: Double = 0.0,
    val maxWeight: Double = 0.0,
    val firstTop: Double = 0.0,
    val lastTop: Double = 0.0,
    val estimated1Rm: Double = 0.0,
    val totalVolumeKg: Double = 0.0,
    val sessions: Int = 0,
    val successRatePct: Int = 0
)

@HiltViewModel
class ExerciseProgressViewModel @javax.inject.Inject constructor(
    repository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L

    val data = repository.observeSessionsWithSets().map { sessions ->
        val zone = ZoneId.systemDefault()
        fun epochDay(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate().toEpochDay()

        // Sessions that contain this exercise, with its sets + the session's start time.
        val relevant = sessions.mapNotNull { sw ->
            val sets = sw.sets.filter { it.exerciseId == exerciseId }
            if (sets.isEmpty()) null else sw.session.startedAt to sets
        }
        if (relevant.isEmpty()) return@map ProgressData()

        val allSets = relevant.flatMap { it.second }
        val name = allSets.first().exerciseName

        // Top weight per calendar day → chart points (x = days since first day).
        val perDay = relevant
            .groupBy { epochDay(it.first) }
            .mapValues { (_, list) -> list.flatMap { it.second }.maxOf { s -> s.weightKg } }
            .toSortedMap()
        val firstDay = perDay.firstKey()
        val points = perDay.map { (day, top) -> (day - firstDay).toFloat() to top.toFloat() }
        val tops = perDay.values.toList()

        ProgressData(
            exerciseName = name,
            points = points,
            firstStartedAt = relevant.minOf { it.first },
            lastStartedAt = relevant.maxOf { it.first },
            minWeight = tops.min(),
            maxWeight = allSets.maxOf { it.weightKg },
            firstTop = tops.first(),
            lastTop = tops.last(),
            estimated1Rm = (allSets.maxOf { it.weightKg * (1 + it.actualReps / 30.0) } * 10).roundToInt() / 10.0,
            totalVolumeKg = allSets.sumOf { it.weightKg * it.actualReps },
            sessions = relevant.size,
            successRatePct = (allSets.count { it.success } * 100.0 / allSets.size).roundToInt()
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
                title = {
                    val name = SeedCatalog.localizedName(LocalContext.current, data.exerciseName)
                    Text(name.ifBlank { stringResource(R.string.progress_title) })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (data.sessions == 0) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.not_enough_data), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // KPI card.
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatLine(stringResource(R.string.stat_pr), formatWeight(data.maxWeight))
                    StatLine(stringResource(R.string.stat_e1rm), formatWeight(data.estimated1Rm))
                    StatLine(
                        stringResource(R.string.stat_progress),
                        stringResource(
                            R.string.progress_from_to,
                            formatWeight(data.firstTop), formatWeight(data.lastTop)
                        )
                    )
                    StatLine(stringResource(R.string.stat_volume), formatWeight(data.totalVolumeKg))
                    StatLine(stringResource(R.string.stat_exercise_sessions), data.sessions.toString())
                    StatLine(stringResource(R.string.stat_success_rate), "${data.successRatePct}%")
                }
            }

            // Chart (needs ≥2 days).
            if (data.points.size < 2) {
                Text(stringResource(R.string.not_enough_data), style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(stringResource(R.string.weight_over_time), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatWeight(data.maxWeight), style = MaterialTheme.typography.bodySmall)
                    Text(formatWeight(data.minWeight), style = MaterialTheme.typography.bodySmall)
                }
                LineChart(points = data.points)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDay(data.firstStartedAt), style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatDay(data.lastStartedAt),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}
