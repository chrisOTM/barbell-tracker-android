package dev.chrisotm.barbelltracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisotm.barbelltracker.ui.theme.Failure
import dev.chrisotm.barbelltracker.ui.theme.Success

/** Visual progress for the sets of the current exercise (US-2.2). */
@Composable
fun SetProgressDots(
    total: Int,
    currentIndex: Int,
    results: List<Boolean?>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val outline = MaterialTheme.colorScheme.outline
        val primary = MaterialTheme.colorScheme.primary
        repeat(total) { i ->
            val color: Color = when {
                results.getOrNull(i) == true -> Success
                results.getOrNull(i) == false -> Failure
                i == currentIndex -> primary
                else -> outline
            }
            Box(Modifier.size(16.dp).background(color, CircleShape))
        }
    }
}
