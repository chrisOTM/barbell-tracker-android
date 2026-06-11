package dev.chrisotm.barbelltracker

import dev.chrisotm.barbelltracker.domain.ProgressionCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionCalculatorTest {
    @Test fun allSuccess_addsIncrement() {
        assertEquals(62.5, ProgressionCalculator.suggest(true, 60.0), 0.0)
    }

    @Test fun anyFailure_holdsWeight() {
        assertEquals(60.0, ProgressionCalculator.suggest(false, 60.0), 0.0)
    }
}
