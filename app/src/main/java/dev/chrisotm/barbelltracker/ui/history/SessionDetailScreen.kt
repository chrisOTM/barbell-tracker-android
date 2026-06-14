package dev.chrisotm.barbelltracker.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.SessionWithSets
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.ui.theme.Failure
import dev.chrisotm.barbelltracker.ui.theme.Success
import dev.chrisotm.barbelltracker.ui.util.formatDateTime
import dev.chrisotm.barbelltracker.ui.util.formatWeight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val repository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
    val session = repository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteSession(onDeleted: () -> Unit) = viewModelScope.launch {
        session.value?.let { repository.deleteSession(it.session) }
        onDeleted()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBack: () -> Unit,
    onOpenProgress: (Long) -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_session_title)) },
            text = { Text(stringResource(R.string.delete_session_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteSession(onDeleted = onBack)
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_session))
                    }
                }
            )
        }
    ) { padding ->
        val s = session
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            if (s != null) {
                item { SessionHeader(s) }
                val byExercise = s.sets.groupBy { it.exerciseId }
                byExercise.forEach { (exerciseId, sets) ->
                    item {
                        ExerciseBlock(
                            name = SeedCatalog.localizedName(LocalContext.current, sets.first().exerciseName),
                            sets = sets,
                            onOpenProgress = { onOpenProgress(exerciseId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(s: SessionWithSets) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(formatDateTime(s.session.startedAt), style = MaterialTheme.typography.titleLarge)
        val header = listOf(s.session.planName, s.session.workoutName)
            .filter { it.isNotBlank() }.joinToString(" · ")
        if (header.isNotBlank()) {
            Text(header, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ExerciseBlock(name: String, sets: List<SessionSet>, onOpenProgress: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenProgress) {
                    Icon(Icons.Filled.ShowChart, contentDescription = stringResource(R.string.progress))
                }
            }
            sets.sortedBy { it.setIndex }.forEach { set ->
                Text(
                    stringResource(
                        R.string.session_set_line,
                        set.setIndex + 1, set.actualReps, set.plannedReps, formatWeight(set.weightKg)
                    ) + (if (set.success) "  ✓" else "  ✗"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (set.success) Success else Failure
                )
            }
        }
    }
}
