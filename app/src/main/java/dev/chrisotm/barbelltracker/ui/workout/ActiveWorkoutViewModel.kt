package dev.chrisotm.barbelltracker.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chrisotm.barbelltracker.data.entity.SessionSet
import dev.chrisotm.barbelltracker.data.entity.WorkoutExercise
import dev.chrisotm.barbelltracker.data.repo.PlanRepository
import dev.chrisotm.barbelltracker.data.repo.SessionRepository
import dev.chrisotm.barbelltracker.domain.ActiveExercise
import dev.chrisotm.barbelltracker.domain.ActiveSet
import dev.chrisotm.barbelltracker.domain.WorkoutEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Phase { LOADING, RUNNING, CONFIRM_NEXT, FINISHED }

data class ProgressionItem(
    val exerciseId: Long,
    val name: String,
    val currentWeightKg: Double,
    val suggestedWeightKg: Double
)

data class ActiveUiState(
    val phase: Phase = Phase.LOADING,
    val workoutLabel: String = "",
    val exerciseName: String = "",
    val weightKg: Double = 0.0,
    val setNumber: Int = 0,
    val setCount: Int = 0,
    val plannedReps: Int = 0,
    val dotResults: List<Boolean?> = emptyList(),
    val isLastSet: Boolean = false,
    val isLastExercise: Boolean = false,
    /** Name of the just-finished exercise, shown on the confirm screen. */
    val finishedExerciseName: String = "",
    /** Name of the upcoming exercise to acknowledge before starting (CONFIRM_NEXT). */
    val nextExerciseName: String = "",
    val progression: List<ProgressionItem> = emptyList()
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: 0L

    private val _state = MutableStateFlow(ActiveUiState())
    val state = _state.asStateFlow()

    private lateinit var engine: WorkoutEngine
    private var sessionId: Long = 0
    private var sessionPlanId: Long = 0
    private var sessionPlanName: String = ""
    private var sessionWorkoutName: String = ""
    private var sessionStartedAt: Long = System.currentTimeMillis()
    /** exerciseId -> plan's WorkoutExercise config, for writing back progression weights. */
    private val configByExercise = mutableMapOf<Long, WorkoutExercise>()
    /** Edited progression choices keyed by exerciseId. */
    private val progressionChoices = mutableMapOf<Long, Double>()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val workout = planRepository.getWorkout(workoutId) ?: return
        val actives = workout.exercises.map { item ->
            val c = item.config
            configByExercise[item.exercise.id] = c
            val startWeight = c.targetWeightKg
                ?: sessionRepository.lastWeightFor(item.exercise.id)
                ?: 0.0
            ActiveExercise(
                workoutExerciseId = c.id,
                exerciseId = item.exercise.id,
                name = item.exercise.name,
                plannedReps = c.reps,
                restSeconds = 0, // rest timer removed — sets flow with no break
                weightKg = startWeight,
                sets = (0 until c.sets).map { i ->
                    ActiveSet(setIndex = i, plannedReps = c.reps, actualReps = c.reps)
                }.toMutableList()
            )
        }
        if (actives.isEmpty()) return
        engine = WorkoutEngine(actives)
        sessionPlanId = workout.workout.planId
        sessionPlanName = planRepository.getPlan(sessionPlanId)?.name ?: ""
        sessionWorkoutName = "Workout ${workout.workout.label}"
        sessionStartedAt = System.currentTimeMillis()
        sessionId = sessionRepository.startSession(
            planId = sessionPlanId,
            workoutId = workoutId,
            planName = sessionPlanName,
            workoutName = sessionWorkoutName
        )
        _state.value = _state.value.copy(phase = Phase.RUNNING, workoutLabel = workout.workout.label)
        emitRunning()
    }

    private fun emitRunning() {
        val ex = engine.currentExercise()
        _state.value = _state.value.copy(
            phase = Phase.RUNNING,
            exerciseName = ex.name,
            weightKg = ex.weightKg,
            setNumber = engine.currentSetNumber,
            setCount = engine.currentSetCount,
            plannedReps = ex.plannedReps,
            dotResults = ex.sets.map { if (it.logged) it.success else null },
            isLastSet = engine.isLastSetOfExercise(),
            isLastExercise = engine.isLastExercise()
        )
    }

    private fun emitConfirmNext(finishedName: String) {
        val upcoming = engine.currentExercise()
        _state.value = _state.value.copy(
            phase = Phase.CONFIRM_NEXT,
            finishedExerciseName = finishedName,
            nextExerciseName = upcoming.name
        )
    }

    /**
     * Log the current set. Within an exercise the next set is shown immediately (no break).
     * After an exercise's last set, a confirmation gate is shown before the next exercise
     * (US: confirm before moving to the next exercise).
     */
    fun recordSet(success: Boolean, actualReps: Int) {
        val ex = engine.currentExercise()
        engine.recordCurrentSet(success, actualReps)
        viewModelScope.launch {
            sessionRepository.logSet(
                SessionSet(
                    sessionId = sessionId,
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.name,
                    setIndex = engine.setIndex,
                    plannedReps = ex.plannedReps,
                    actualReps = actualReps,
                    weightKg = ex.weightKg,
                    success = success,
                    restSeconds = 0
                )
            )
        }
        val finishedName = ex.name
        when {
            !engine.isLastSetOfExercise() -> {
                engine.advance()
                emitRunning()
            }
            !engine.isLastExercise() -> {
                engine.advance()
                emitConfirmNext(finishedName)
            }
            else -> {
                engine.advance()
                finish()
            }
        }
    }

    /** Acknowledge the confirmation screen and begin the next exercise. */
    fun startNextExercise() {
        emitRunning()
    }

    /** In-workout weight change for the current exercise (US-3.3). */
    fun changeWeight(weightKg: Double) {
        engine.setWeightForCurrentExercise(weightKg)
        if (_state.value.phase == Phase.RUNNING) emitRunning()
        else _state.value = _state.value.copy(weightKg = weightKg)
    }

    /** End early; already-logged sets are kept (US-2.7). */
    fun endEarly() {
        finish()
    }

    private fun finish() {
        viewModelScope.launch {
            val results = engine.progressionResults()
            results.forEach { progressionChoices[it.exerciseId] = it.suggestedNextWeightKg }
            endSession()
            _state.value = _state.value.copy(
                phase = Phase.FINISHED,
                progression = results.map {
                    ProgressionItem(it.exerciseId, it.name, it.weightKg, it.suggestedNextWeightKg)
                }
            )
        }
    }

    private suspend fun endSession() {
        sessionRepository.finishSession(
            dev.chrisotm.barbelltracker.data.entity.WorkoutSession(
                id = sessionId,
                planId = sessionPlanId,
                workoutId = workoutId,
                planName = sessionPlanName,
                workoutName = sessionWorkoutName,
                startedAt = sessionStartedAt,
                endedAt = System.currentTimeMillis()
            )
        )
    }

    fun setProgressionChoice(exerciseId: Long, weightKg: Double) {
        progressionChoices[exerciseId] = weightKg
    }

    /** Write the chosen next-session weights back into the plan (US-3.2). */
    fun applyProgression(onDone: () -> Unit) {
        viewModelScope.launch {
            progressionChoices.forEach { (exerciseId, weight) ->
                configByExercise[exerciseId]?.let { config ->
                    planRepository.updateExercise(config.copy(targetWeightKg = weight))
                }
            }
            onDone()
        }
    }
}
