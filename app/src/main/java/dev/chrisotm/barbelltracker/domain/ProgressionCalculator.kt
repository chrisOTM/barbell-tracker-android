package dev.chrisotm.barbelltracker.domain

/** Weight progression rule (US-3.2): all sets of an exercise succeeded → +2.5 kg,
 *  otherwise hold the current weight. Suggestions are editable in the UI. */
object ProgressionCalculator {
    const val DEFAULT_INCREMENT_KG = 2.5

    fun suggest(allSetsSuccessful: Boolean, currentWeightKg: Double): Double =
        if (allSetsSuccessful) currentWeightKg + DEFAULT_INCREMENT_KG else currentWeightKg
}
