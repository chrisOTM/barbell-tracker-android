package dev.chrisotm.barbelltracker.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Render a weight without a trailing ".0" (e.g. 52.5 kg, 60 kg). */
fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()} kg" else "$kg kg"

fun formatWeightPlain(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()}" else kg.toString()

/** Seconds → m:ss. */
fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

// Built per call so they follow the app's active locale (date/time still use the
// numeric dd.MM.yyyy layout the history search expects).
fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(epochMillis))

fun formatDay(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(epochMillis))
