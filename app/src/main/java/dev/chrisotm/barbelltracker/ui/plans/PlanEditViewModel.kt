package dev.chrisotm.barbelltracker.ui.plans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.Exercise
import dev.chrisotm.barbelltracker.data.entity.Plan
import dev.chrisotm.barbelltracker.data.entity.PlanWithWorkouts
import dev.chrisotm.barbelltracker.data.entity.Workout
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.repo.ExerciseRepository
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanEditViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val planId: Long = savedStateHandle.get<Long>("planId") ?: 0L

    val plan = planRepository.observePlan(planId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val library = exerciseRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun renamePlan(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { planRepository.renamePlan(Plan(id = planId, name = name.trim())) }
    }

    fun deletePlan(onDone: () -> Unit) {
        viewModelScope.launch {
            plan.value?.let { planRepository.deletePlan(it.plan) }
            onDone()
        }
    }

    fun addWorkout() {
        viewModelScope.launch {
            val next = plan.value?.workouts?.size ?: 0
            val label = ('A' + next).toString()
            planRepository.addWorkout(planId, label)
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch { planRepository.deleteWorkout(workout) }
    }

    fun addExercise(workoutId: Long, exercise: Exercise) {
        viewModelScope.launch { planRepository.addExercise(workoutId, exercise.id, sets = 3, reps = 8) }
    }

    fun updateExercise(item: WorkoutExercise) {
        viewModelScope.launch { planRepository.updateExercise(item) }
    }

    fun deleteExercise(item: WorkoutExercise) {
        viewModelScope.launch { planRepository.deleteExercise(item) }
    }

    /** Move an exercise within its workout's ordered list (US-1.4 reordering). */
    fun moveExercise(orderedItems: List<WorkoutExercise>, index: Int, up: Boolean) {
        val target = if (up) index - 1 else index + 1
        if (target < 0 || target > orderedItems.lastIndex) return
        val mutable = orderedItems.toMutableList()
        val tmp = mutable[index]
        mutable[index] = mutable[target]
        mutable[target] = tmp
        viewModelScope.launch { planRepository.reorderExercises(mutable) }
    }
}
