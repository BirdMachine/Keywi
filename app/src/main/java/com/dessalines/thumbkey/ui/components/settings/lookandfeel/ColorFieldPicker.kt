package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Two-dimensional HSV saturation/value field and continuous hue strip.
 * All coordinates are clamped, including drags beyond the edges.
 * Uses the supplied state rather than maintaining a second color internally.
 */
@Composable
internal fun ColorFieldPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (Float, Float, Float) -> Unit,
) {
    val latestOnChange by rememberUpdatedState(onColorChange)
    val latestHue by rememberUpdatedState(hue)
    val latestSaturation by rememberUpdatedState(saturation)
    val latestValue by rememberUpdatedState(value)
    val pureHue = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    Column {
        Text("Saturation / brightness", style = MaterialTheme.typography.labelSmall)
        Canvas(
            Modifier.fillMaxWidth().height(190.dp).padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { point ->
                        latestOnChange(
                            latestHue,
                            (point.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                            (1f - point.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f),
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { point ->
                            latestOnChange(latestHue,
                                (point.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                                (1f - point.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f))
                        },
                        onDrag = { change, _ ->
                            latestOnChange(latestHue,
                                (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                                (1f - change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f))
                            change.consume()
                        },
                    )
                },
        ) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, pureHue)))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val center = Offset(saturation * size.width, (1f - value) * size.height)
            drawCircle(Color.Black.copy(alpha = 0.65f), radius = 10.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
            drawCircle(Color.White, radius = 8.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
        }
        Text("Hue • ${hue.toInt()}°", style = MaterialTheme.typography.labelSmall)
        val spectrum = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map {
            Color(android.graphics.Color.HSVToColor(floatArrayOf(it, 1f, 1f)))
        }
        Canvas(
            Modifier.fillMaxWidth().height(38.dp).padding(vertical = 7.dp)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures { point ->
                        latestOnChange((point.x / size.width.coerceAtLeast(1) * 360f).coerceIn(0f, 360f), latestSaturation, latestValue)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { point ->
                            latestOnChange((point.x / size.width.coerceAtLeast(1) * 360f).coerceIn(0f, 360f), latestSaturation, latestValue)
                        },
                        onDrag = { change, _ ->
                            latestOnChange((change.position.x / size.width.coerceAtLeast(1) * 360f).coerceIn(0f, 360f), latestSaturation, latestValue)
                            change.consume()
                        },
                    )
                },
        ) {
            drawRect(Brush.horizontalGradient(spectrum))
            val x = (hue / 360f).coerceIn(0f, 1f) * size.width
            drawLine(Color.Black, Offset(x, 0f), Offset(x, size.height), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
