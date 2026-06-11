package dev.chrisotm.barbelltracker

import dev.chrisotm.barbelltracker.domain.ActiveExercise
import dev.chrisotm.barbelltracker.domain.ActiveSet
import dev.chrisotm.barbelltracker.domain.WorkoutEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEngineTest {

    private fun exercise(id: Long, sets: Int, reps: Int, weight: Double) = ActiveExercise(
        workoutExerciseId = id,
        exerciseId = id,
        name = "Ex$id",
        plannedReps = reps,
        restSeconds = 180,
        weightKg = weight,
        sets = (0 until sets).map { ActiveSet(it, reps, reps) }.toMutableList()
    )

    @Test fun advancesThroughSetsThenExercises() {
        val engine = WorkoutEngine(listOf(exercise(1, 2, 5, 60.0), exercise(2, 1, 5, 40.0)))

        assertEquals(1, engine.currentSetNumber)
        assertEquals(2, engine.currentSetCount)
        engine.recordCurrentSet(true, 5)
        assertTrue(engine.advance())          // -> set 2 of ex1
        assertEquals(2, engine.currentSetNumber)
        engine.recordCurrentSet(true, 5)
        assertTrue(engine.advance())          // -> ex2 set 1
        assertEquals("Ex2", engine.currentExercise().name)
        engine.recordCurrentSet(true, 5)
        assertFalse(engine.advance())         // workout done
        assertTrue(engine.finished)
    }

    @Test fun progressionReflectsSuccess() {
        val engine = WorkoutEngine(listOf(exercise(1, 2, 5, 60.0)))
        engine.recordCurrentSet(true, 5)
        engine.advance()
        engine.recordCurrentSet(false, 3)     // one failed set

        val result = engine.progressionResults().single()
        assertFalse(result.allSuccessful)
        assertEquals(60.0, result.suggestedNextWeightKg, 0.0) // hold weight
    }

    @Test fun weightChangeAppliesToCurrentExercise() {
        val engine = WorkoutEngine(listOf(exercise(1, 2, 5, 60.0)))
        engine.setWeightForCurrentExercise(65.0)
        engine.recordCurrentSet(true, 5)
        assertEquals(65.0, engine.loggedSets().first().weightKg, 0.0)
    }
}
