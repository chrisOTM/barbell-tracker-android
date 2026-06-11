package dev.chrisotm.barbelltracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BarbellApp : Application() {

    override fun onCreate() {
        super.onCreate()
        forceEnglishOnFirstLaunch()
    }

    /**
     * The app defaults to English on the very first launch (even on a non-English device).
     * After that, AppCompat auto-store restores the user's chosen language, so we only set
     * it once, guarded by a one-time flag.
     */
    private fun forceEnglishOnFirstLaunch() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_LOCALE_INITIALIZED, false)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            prefs.edit().putBoolean(KEY_LOCALE_INITIALIZED, true).apply()
        }
    }

    companion object {
        private const val PREFS = "barbell_prefs"
        private const val KEY_LOCALE_INITIALIZED = "locale_initialized"
    }
}
