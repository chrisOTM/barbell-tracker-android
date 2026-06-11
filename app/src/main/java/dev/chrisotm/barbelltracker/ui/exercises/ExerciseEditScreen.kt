package dev.chrisotm.barbelltracker.ui.exercises

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.SavedStateHandle
import dev.chrisotm.barbelltracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chrisotm.barbelltracker.data.db.SeedCatalog
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.repo.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseEditState(
    val id: Long = 0,
    val name: String = "",
    val muscleGroups: String = "",
    val description: String = "",
    val isCustom: Boolean = true,
    val isBodyweight: Boolean = false
)

@HiltViewModel
class ExerciseEditViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val repository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L
    val state = MutableStateFlow(ExerciseEditState())

    val isNew: Boolean get() = exerciseId == 0L

    init {
        if (exerciseId != 0L) {
            viewModelScope.launch {
                repository.getById(exerciseId)?.let { e ->
                    // Show built-in exercises in the active language.
                    state.value = ExerciseEditState(
                        id = e.id,
                        name = SeedCatalog.localizedName(context, e.name),
                        muscleGroups = SeedCatalog.localizedMuscles(context, e.name, e.muscleGroups),
                        description = SeedCatalog.localizedDescription(context, e.name, e.description),
                        isCustom = e.isCustom,
                        isBodyweight = e.isBodyweight
                    )
                }
            }
        }
    }

    fun onName(v: String) { state.value = state.value.copy(name = v) }
    fun onMuscle(v: String) { state.value = state.value.copy(muscleGroups = v) }
    fun onDescription(v: String) { state.value = state.value.copy(description = v) }

    val canSave: Boolean get() = state.value.name.isNotBlank()

    fun save(onDone: () -> Unit) {
        val s = state.value
        if (s.name.isBlank()) return
        viewModelScope.launch {
            repository.upsert(
                Exercise(
                    id = s.id,
                    name = s.name.trim(),
                    muscleGroups = s.muscleGroups.trim(),
                    description = s.description.trim(),
                    isCustom = if (s.id == 0L) true else s.isCustom,
                    isBodyweight = s.isBodyweight
                )
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val s = state.value
        if (s.id == 0L) return
        viewModelScope.launch {
            repository.delete(
                Exercise(
                    id = s.id, name = s.name, muscleGroups = s.muscleGroups,
                    description = s.description, isCustom = s.isCustom, isBodyweight = s.isBodyweight
                )
            )
            onDone()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditScreen(
    onBack: () -> Unit,
    viewModel: ExerciseEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isNew) R.string.new_exercise else R.string.edit_exercise)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!viewModel.isNew && state.isCustom) {
                        IconButton(onClick = { viewModel.delete(onBack) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = { Text(stringResource(R.string.name_required)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.muscleGroups,
                onValueChange = viewModel::onMuscle,
                label = { Text(stringResource(R.string.muscle_group)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescription,
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            Button(
                onClick = { viewModel.save(onBack) },
                enabled = state.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) { Text(stringResource(R.string.save)) }
        }
    }
}
