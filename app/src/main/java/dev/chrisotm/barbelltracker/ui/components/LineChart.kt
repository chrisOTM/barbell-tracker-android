package dev.chrisotm.barbelltracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Minimal dependency-free weight-over-time chart (US-4.3). [points] are (x, y) in
 *  data space, ascending by x; rendered as a connected line with dots. */
@Composable
fun LineChart(
    points: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (points.isEmpty()) return@Canvas
        val padding = 24f
        val w = size.width - padding * 2
        val h = size.height - padding * 2

        val xs = points.map { it.first }
        val ys = points.map { it.second }
        val minX = xs.min(); val maxX = xs.max()
        val minY = ys.min(); val maxY = ys.max()
        val spanX = (maxX - minX).takeIf { it != 0f } ?: 1f
        val spanY = (maxY - minY).takeIf { it != 0f } ?: 1f

        fun px(x: Float) = padding + (x - minX) / spanX * w
        fun py(y: Float) = padding + h - (y - minY) / spanY * h

        val path = Path()
        points.forEachIndexed { i, p ->
            val cx = px(p.first); val cy = py(p.second)
            if (i == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 5f))
        points.forEach { p ->
            drawCircle(color = lineColor, radius = 7f, center = Offset(px(p.first), py(p.second)))
        }
    }
}
