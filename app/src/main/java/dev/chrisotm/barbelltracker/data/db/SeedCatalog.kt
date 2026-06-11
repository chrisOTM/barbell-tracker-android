package dev.chrisotm.barbelltracker.data.db

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import dev.chrisotm.barbelltracker.R
import java.util.Locale

/**
 * Catalog of the built-in (seeded) exercises. Built-in exercises are stored in the DB with a
 * localized name at seed time, but their displayed name/muscles/description are re-resolved from
 * string resources in the *currently selected* app language — so changing the language updates the
 * exercise texts everywhere (US: language affects the whole app incl. exercises).
 *
 * A stored exercise is recognized as built-in by matching its stored name against the known seed
 * names in any supported language. Custom (user-created) exercises don't match and are shown as-is.
 */
object SeedCatalog {
    data class Entry(
        val nameRes: Int,
        val musclesRes: Int,
        val descRes: Int,
        val bodyweight: Boolean = false
    )

    /** Stable key → string resources. Order defines the seeded library order. */
    val entries: Map<String, Entry> = linkedMapOf(
        "back_squat" to Entry(R.string.seed_back_squat_name, R.string.seed_back_squat_muscles, R.string.seed_back_squat_desc),
        "bench_press" to Entry(R.string.seed_bench_press_name, R.string.seed_bench_press_muscles, R.string.seed_bench_press_desc),
        "deadlift" to Entry(R.string.seed_deadlift_name, R.string.seed_deadlift_muscles, R.string.seed_deadlift_desc),
        "barbell_row" to Entry(R.string.seed_barbell_row_name, R.string.seed_barbell_row_muscles, R.string.seed_barbell_row_desc),
        "ohp" to Entry(R.string.seed_ohp_name, R.string.seed_ohp_muscles, R.string.seed_ohp_desc),
        "front_squat" to Entry(R.string.seed_front_squat_name, R.string.seed_front_squat_muscles, R.string.seed_front_squat_desc),
        "lunges" to Entry(R.string.seed_lunges_name, R.string.seed_lunges_muscles, R.string.seed_lunges_desc),
        "good_mornings" to Entry(R.string.seed_good_mornings_name, R.string.seed_good_mornings_muscles, R.string.seed_good_mornings_desc),
        "rdl" to Entry(R.string.seed_rdl_name, R.string.seed_rdl_muscles, R.string.seed_rdl_desc),
        "one_arm_row" to Entry(R.string.seed_one_arm_row_name, R.string.seed_one_arm_row_muscles, R.string.seed_one_arm_row_desc),
        "pullups" to Entry(R.string.seed_pullups_name, R.string.seed_pullups_muscles, R.string.seed_pullups_desc, bodyweight = true)
    )

    /** Supported UI languages whose seed names are used for recognition. */
    private val SUPPORTED = listOf("en", "de")

    @Volatile
    private var nameToKey: Map<String, String>? = null

    /** stored exercise name (any supported language, case-insensitive) → seed key. */
    private fun nameToKey(context: Context): Map<String, String> {
        nameToKey?.let { return it }
        val map = HashMap<String, String>()
        for (lang in SUPPORTED) {
            val ctx = localizedContext(context, Locale.forLanguageTag(lang))
            for ((key, e) in entries) {
                map[ctx.getString(e.nameRes).trim().lowercase()] = key
            }
        }
        nameToKey = map
        return map
    }

    private fun localizedContext(context: Context, locale: Locale): Context {
        val cfg = Configuration(context.resources.configuration)
        cfg.setLocale(locale)
        return context.createConfigurationContext(cfg)
    }

    /** Context resolving strings in the *currently selected* app language (API-safe via AppCompat). */
    private fun selectedContext(context: Context): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        val locale = if (locales.isEmpty) Locale.getDefault() else locales[0] ?: Locale.getDefault()
        return localizedContext(context, locale)
    }

    private fun keyFor(context: Context, storedName: String): String? =
        nameToKey(context)[storedName.trim().lowercase()]

    fun localizedName(context: Context, storedName: String): String {
        val key = keyFor(context, storedName) ?: return storedName
        return selectedContext(context).getString(entries.getValue(key).nameRes)
    }

    fun localizedMuscles(context: Context, storedName: String, fallback: String): String {
        val key = keyFor(context, storedName) ?: return fallback
        return selectedContext(context).getString(entries.getValue(key).musclesRes)
    }

    fun localizedDescription(context: Context, storedName: String, fallback: String): String {
        val key = keyFor(context, storedName) ?: return fallback
        return selectedContext(context).getString(entries.getValue(key).descRes)
    }
}
