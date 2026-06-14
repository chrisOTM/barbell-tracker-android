package dev.chrisotm.barbelltracker

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Persists the user's day/night choice. AppCompat keeps the night mode only in memory, so we
 * store it ourselves and re-apply it on app start. Values are AppCompatDelegate.MODE_NIGHT_* and
 * drive the Compose theme via the activity configuration (isSystemInDarkTheme reads it back).
 */
object ThemePreference {
    private const val PREFS = "barbell_prefs"
    private const val KEY_NIGHT_MODE = "night_mode"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Int =
        prefs(context).getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    /** Re-applies the saved mode; call once on startup. */
    fun apply(context: Context) {
        AppCompatDelegate.setDefaultNightMode(load(context))
    }

    /** Persists + applies a new mode (recreates the activity). */
    fun set(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_NIGHT_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
