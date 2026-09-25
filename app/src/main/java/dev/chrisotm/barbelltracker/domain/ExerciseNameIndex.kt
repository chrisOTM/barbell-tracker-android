package dev.chrisotm.barbelltracker.domain

/**
 * Resolves an exercise name from a plan template or a backup file to a library exercise id.
 *
 * Built-in exercises are stored under the name of the language active at seed time, while
 * templates and logged history use the name of the language active when they were created.
 * An exact (case-insensitive) name match wins; otherwise a built-in exercise is matched in any
 * supported language via its stable seed key, so "Kniebeuge" finds a library stored as
 * "Back Squat".
 *
 * @param seedKeyByName normalized seed name (every supported language) → stable seed key.
 */
class ExerciseNameIndex(private val seedKeyByName: Map<String, String>) {
    private val idByName = HashMap<String, Long>()
    private val idBySeedKey = HashMap<String, Long>()

    fun add(id: Long, name: String) {
        val normalized = normalize(name)
        idByName.putIfAbsent(normalized, id)
        seedKeyByName[normalized]?.let { idBySeedKey.putIfAbsent(it, id) }
    }

    fun resolve(name: String): Long? {
        val normalized = normalize(name)
        return idByName[normalized] ?: seedKeyByName[normalized]?.let { idBySeedKey[it] }
    }

    companion object {
        fun normalize(name: String): String = name.trim().lowercase()
    }
}
