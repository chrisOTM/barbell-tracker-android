package dev.chrisotm.barbelltracker.ui.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlansListViewModel @Inject constructor(
    repository: PlanRepository
) : ViewModel() {
    val plans = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansListScreen(
    onOpenPlan: (Long) -> Unit,
    onCreatePlan: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: PlansListViewModel = hiltViewModel()
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barbell Tracker") },
                actions = {
                    IconButton(onClick = onOpenExercises) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Übungen")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Verlauf")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Filled.Add, contentDescription = "Plan erstellen")
            }
        }
    ) { padding ->
        if (plans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        "Noch kein Trainingsplan.\nTippe auf +, um zu starten.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(plans, key = { it.id }) { plan ->
                    PlanRow(plan) { onOpenPlan(plan.id) }
                }
            }
        }
    }
}

@Composable
private fun PlanRow(plan: Plan, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            plan.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(20.dp)
        )
    }
}
