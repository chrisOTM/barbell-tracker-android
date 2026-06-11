package dev.chrisotm.barbelltracker.domain

/** Default rest times derived from the set/rep scheme when no custom value is set (US-2.4). */
object RestDefaults {
    const val HEAVY_SECONDS = 180   // 3:00 — heavy, low-rep work (5×5, 1×5)
    const val LIGHT_SECONDS = 90    // 1:30 — higher-rep work (3×8, 3×10)

    /** Heavy scheme = 5 or fewer reps per set. */
    fun secondsFor(sets: Int, reps: Int): Int =
        if (reps <= 5) HEAVY_SECONDS else LIGHT_SECONDS

    /** Effective rest for a configured exercise: custom value wins, else derived default. */
    fun effective(restSeconds: Int?, sets: Int, reps: Int): Int =
        restSeconds ?: secondsFor(sets, reps)
}
