package dev.chrisotm.barbelltracker

import dev.chrisotm.barbelltracker.domain.RestDefaults
import org.junit.Assert.assertEquals
import org.junit.Test

class RestDefaultsTest {
    @Test fun heavyLowRep_is3min() {
        assertEquals(180, RestDefaults.secondsFor(5, 5)) // 5×5
        assertEquals(180, RestDefaults.secondsFor(1, 5)) // Kreuzheben 1×5
    }

    @Test fun higherRep_is90s() {
        assertEquals(90, RestDefaults.secondsFor(3, 8))  // 3×8
        assertEquals(90, RestDefaults.secondsFor(3, 10)) // 3×10
    }

    @Test fun customValueWins() {
        assertEquals(120, RestDefaults.effective(120, 5, 5))
        assertEquals(180, RestDefaults.effective(null, 5, 5))
    }
}
