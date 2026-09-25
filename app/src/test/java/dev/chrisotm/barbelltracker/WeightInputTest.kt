package dev.chrisotm.barbelltracker

import dev.chrisotm.barbelltracker.ui.util.parseWeight
import dev.chrisotm.barbelltracker.ui.util.sanitizeWeightInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeightInputTest {

    @Test fun decimalCommaIsKeptAsDot() {
        assertEquals("62.5", sanitizeWeightInput("62,5"))
        assertEquals(62.5, parseWeight("62,5")!!, 0.0)
    }

    @Test fun onlyFirstSeparatorSurvives() {
        assertEquals("62.5", sanitizeWeightInput("62.5,"))
        assertEquals("62.55", sanitizeWeightInput("62,5.5"))
    }

    @Test fun nonDigitsAreDropped() {
        assertEquals("625", sanitizeWeightInput("6 2-5kg"))
    }

    @Test fun trailingSeparatorStaysEditable() {
        assertEquals("62.", sanitizeWeightInput("62."))
        assertEquals(62.0, parseWeight("62.")!!, 0.0)
    }

    @Test fun emptyOrBareSeparatorIsNoWeight() {
        assertNull(parseWeight(""))
        assertNull(parseWeight("."))
        assertNull(parseWeight(","))
    }
}
