package dev.chrisotm.barbelltracker

import dev.chrisotm.barbelltracker.domain.ExerciseNameIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseNameIndexTest {

    // Seed names in both languages, normalized, mapped to their stable seed key.
    private val seedKeys = mapOf(
        "back squat" to "back_squat",
        "kniebeuge" to "back_squat",
        "bench press" to "bench_press",
        "bankdrücken" to "bench_press"
    )

    private fun index(vararg library: Pair<Long, String>) =
        ExerciseNameIndex(seedKeys).apply { library.forEach { (id, name) -> add(id, name) } }

    @Test fun exactNameMatchesIgnoringCaseAndWhitespace() {
        assertEquals(1L, index(1L to "Back Squat").resolve("  back squat "))
    }

    @Test fun builtInExerciseMatchesAcrossLanguages() {
        // Library seeded in English, template/backup names in German.
        val idx = index(1L to "Back Squat", 2L to "Bench Press")
        assertEquals(1L, idx.resolve("Kniebeuge"))
        assertEquals(2L, idx.resolve("Bankdrücken"))
    }

    @Test fun exactNameWinsOverSeedKey() {
        // A custom exercise literally named "Kniebeuge" beats the English built-in.
        assertEquals(7L, index(1L to "Back Squat", 7L to "Kniebeuge").resolve("Kniebeuge"))
    }

    @Test fun unknownNameResolvesToNull() {
        assertNull(index(1L to "Back Squat").resolve("Zercher Squat"))
    }

    @Test fun addedNamesAreResolvableAfterwards() {
        val idx = index()
        idx.add(9L, "Zercher Squat")
        assertEquals(9L, idx.resolve("zercher squat"))
    }
}
