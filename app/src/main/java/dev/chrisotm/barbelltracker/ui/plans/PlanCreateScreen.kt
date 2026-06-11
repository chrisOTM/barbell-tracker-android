package dev.chrisotm.barbelltracker.ui.plans

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.db.PlanTemplate
import dev.chrisotm.barbelltracker.data.db.PlanTemplates
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanCreateViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val repository: PlanRepository
) : ViewModel() {
    val name = MutableStateFlow("")
    val templates: List<PlanTemplate> = PlanTemplates.all(context)

    fun createEmpty(onCreated: (Long) -> Unit) {
        val n = name.value.trim().ifBlank { context.getString(R.string.new_plan_default) }
        viewModelScope.launch { onCreated(repository.createPlan(n)) }
    }

    fun createFromTemplate(template: PlanTemplate, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repository.createFromTemplate(template)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreateScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: PlanCreateViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plan_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.custom_plan), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.name.value = it },
                label = { Text(stringResource(R.string.plan_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = { viewModel.createEmpty(onCreated) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text(stringResource(R.string.create_empty_plan)) }

            HorizontalDivider(Modifier.padding(vertical = 20.dp))

            Text(stringResource(R.string.from_template), style = MaterialTheme.typography.titleMedium)
            viewModel.templates.forEach { template ->
                Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(template.name, style = MaterialTheme.typography.titleLarge)
                        template.workouts.forEach { w ->
                            Text(
                                stringResource(
                                    R.string.template_workout_line,
                                    w.label,
                                    w.entries.joinToString { "${it.exerciseName} ${it.sets}×${it.reps}" }
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.createFromTemplate(template, onCreated) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) { Text(stringResource(R.string.use_template)) }
                    }
                }
            }
        }
    }
}
