package dev.chrisotm.barbelltracker.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.SessionWithSets
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.ui.util.formatDateTime
import dev.chrisotm.barbelltracker.ui.util.formatDay
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<SessionWithSets> = emptyList(),
    val plans: List<String> = emptyList(),
    val planFilter: String? = null,
    val dateQuery: String = ""
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: SessionRepository
) : ViewModel() {
    val planFilter = MutableStateFlow<String?>(null)
    val dateQuery = MutableStateFlow("")

    val uiState = combine(
        repository.observeSessionsWithSets(), planFilter, dateQuery
    ) { sessions, plan, date ->
        val plans = sessions.map { it.session.planName }.filter { it.isNotBlank() }.distinct()
        val filtered = sessions.filter { s ->
            (plan == null || s.session.planName == plan) &&
                (date.isBlank() || formatDay(s.session.startedAt).contains(date))
        }
        HistoryUiState(filtered, plans, plan, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    onOpenSession: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = ui.dateQuery,
                onValueChange = { viewModel.dateQuery.value = it },
                label = { Text(stringResource(R.string.search_date)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            if (ui.plans.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = ui.planFilter == null,
                        onClick = { viewModel.planFilter.value = null },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                    ui.plans.forEach { p ->
                        FilterChip(
                            selected = ui.planFilter == p,
                            onClick = { viewModel.planFilter.value = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.sessions, key = { it.session.id }) { s ->
                    SessionRow(s) { onOpenSession(s.session.id) }
                }
            }
        }
    }

@Composable
private fun SessionRow(item: SessionWithSets, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(formatDateTime(item.session.startedAt), style = MaterialTheme.typography.titleMedium)
            val header = listOf(item.session.planName, item.session.workoutName)
                .filter { it.isNotBlank() }.joinToString(" · ")
            if (header.isNotBlank()) {
                Text(header, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
            val ctx = LocalContext.current
            val byExercise = item.sets.groupBy { it.exerciseName }
            byExercise.forEach { (name, sets) ->
                val top = sets.maxByOrNull { it.weightKg }?.weightKg ?: 0.0
                Text(
                    stringResource(
                        R.string.session_summary,
                        SeedCatalog.localizedName(ctx, name), sets.size, formatWeight(top)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
