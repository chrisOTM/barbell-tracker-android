package dev.chrisotm.barbelltracker.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Render a weight without a trailing ".0" (e.g. 52.5 kg, 60 kg). */
fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()} kg" else "$kg kg"

fun formatWeightPlain(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()}" else kg.toString()

/** Filter for weight text fields: keeps digits and the first decimal separator. German
 *  keyboards send ',' — it is accepted and normalized to '.' instead of being dropped
 *  (which turned "62,5" into "625"). */
fun sanitizeWeightInput(text: String): String {
    val out = StringBuilder()
    var hasSeparator = false
    for (c in text) {
        when {
            c.isDigit() -> out.append(c)
            (c == '.' || c == ',') && !hasSeparator -> {
                out.append('.')
                hasSeparator = true
            }
        }
    }
    return out.toString()
}

/** Parses a weight typed with either decimal separator; null for empty/incomplete input. */
fun parseWeight(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/** Seconds → m:ss. */
fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

/** Seconds → "12 h 30 min" / "45 min" for cumulative training time. */
fun formatHoursMinutes(totalSeconds: Long): String {
    val totalMin = totalSeconds / 60
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h h $m min" else "$m min"
}

// Built per call so they follow the app's active locale (date/time still use the
// numeric dd.MM.yyyy layout the history search expects).
fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(epochMillis))

fun formatDay(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(epochMillis))
