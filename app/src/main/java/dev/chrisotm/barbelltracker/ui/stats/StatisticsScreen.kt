package dev.chrisotm.barbelltracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import dev.chrisotm.barbelltracker.data.entity.SessionWithSets
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.ui.util.formatHoursMinutes
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

data class ExerciseStatRow(
    val exerciseId: Long,
    val storedName: String,
    val sessions: Int,
    val prKg: Double
)

data class StatsUiState(
    val hasData: Boolean = false,
    val sessionsTotal: Int = 0,
    val sessions7d: Int = 0,
    val sessions30d: Int = 0,
    val streakWeeks: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0,
    val avgSetsPerSession: Double = 0.0,
    val successRatePct: Int = 0,
    val topExerciseStoredName: String = "",
    val totalTimeSeconds: Long = 0,
    val exercises: List<ExerciseStatRow> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    repository: SessionRepository
) : ViewModel() {
    val state = repository.observeSessionsWithSets()
        .map { sessions -> aggregate(sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
}

private const val DAY_MS = 24L * 60 * 60 * 1000

private fun aggregate(sessions: List<SessionWithSets>): StatsUiState {
    if (sessions.isEmpty()) return StatsUiState()
    val allSets = sessions.flatMap { it.sets }
    val now = System.currentTimeMillis()
    val totalSets = allSets.size
    val successful = allSets.count { it.success }

    // Per-exercise rows.
    val rows = allSets.groupBy { it.exerciseId }.map { (id, sets) ->
        ExerciseStatRow(
            exerciseId = id,
            storedName = sets.first().exerciseName,
            sessions = sets.map { it.sessionId }.distinct().size,
            prKg = sets.maxOf { it.weightKg }
        )
    }.sortedWith(compareByDescending<ExerciseStatRow> { it.sessions }.thenBy { it.storedName })

    val topExercise = rows.maxByOrNull { it.sessions }?.storedName ?: ""

    return StatsUiState(
        hasData = true,
        sessionsTotal = sessions.size,
        sessions7d = sessions.count { it.session.startedAt >= now - 7 * DAY_MS },
        sessions30d = sessions.count { it.session.startedAt >= now - 30 * DAY_MS },
        streakWeeks = weekStreak(sessions.map { it.session.startedAt }),
        totalVolumeKg = allSets.sumOf { it.weightKg * it.actualReps },
        totalSets = totalSets,
        avgSetsPerSession = if (sessions.isNotEmpty()) totalSets.toDouble() / sessions.size else 0.0,
        successRatePct = if (totalSets > 0) (successful * 100.0 / totalSets).roundToInt() else 0,
        topExerciseStoredName = topExercise,
        totalTimeSeconds = sessions.filter { it.session.endedAt != null }
            .sumOf { (it.session.endedAt!! - it.session.startedAt).coerceAtLeast(0) / 1000 },
        exercises = rows
    )
}

/** Count of consecutive ISO weeks (back from the current week) that contain ≥1 session. */
private fun weekStreak(startedAtMillis: List<Long>): Int {
    val zone = ZoneId.systemDefault()
    fun mondayEpochDay(millis: Long): Long {
        val d = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        return d.minusDays((d.dayOfWeek.value - 1).toLong()).toEpochDay()
    }
    val weeks = startedAtMillis.map { mondayEpochDay(it) }.toHashSet()
    if (weeks.isEmpty()) return 0
    val currentMonday = mondayEpochDay(System.currentTimeMillis())
    var streak = 0
    while (weeks.contains(currentMonday - streak * 7L)) streak++
    return streak
}

@Composable
fun StatisticsScreen(
    onOpenExercise: (Long) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!ui.hasData) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.stat_no_data), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(stringResource(R.string.stat_overview), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatLine(stringResource(R.string.stat_sessions_total), ui.sessionsTotal.toString())
                    StatLine(stringResource(R.string.stat_sessions_7d), ui.sessions7d.toString())
                    StatLine(stringResource(R.string.stat_sessions_30d), ui.sessions30d.toString())
                    StatLine(stringResource(R.string.stat_streak_weeks), ui.streakWeeks.toString())
                    StatLine(stringResource(R.string.stat_total_volume), formatWeight(ui.totalVolumeKg))
                    StatLine(stringResource(R.string.stat_total_sets), ui.totalSets.toString())
                    StatLine(
                        stringResource(R.string.stat_avg_sets),
                        "%.1f".format(ui.avgSetsPerSession)
                    )
                    StatLine(stringResource(R.string.stat_success_rate), "${ui.successRatePct}%")
                    if (ui.topExerciseStoredName.isNotBlank()) {
                        StatLine(
                            stringResource(R.string.stat_top_exercise),
                            SeedCatalog.localizedName(context, ui.topExerciseStoredName)
                        )
                    }
                    StatLine(
                        stringResource(R.string.stat_total_time),
                        formatHoursMinutes(ui.totalTimeSeconds)
                    )
                }
            }
        }
        item {
            Text(
                stringResource(R.string.stat_exercises_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(ui.exercises, key = { it.exerciseId }) { row ->
            Card(
                onClick = { onOpenExercise(row.exerciseId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        SeedCatalog.localizedName(context, row.storedName),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.stat_exercise_row, formatWeight(row.prKg), row.sessions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
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
