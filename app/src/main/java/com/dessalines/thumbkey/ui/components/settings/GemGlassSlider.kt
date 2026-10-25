package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences

/** Native slider input and semantics, with a fully custom inset glass face. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val theme = KeyThemePreferences.current
    val edges = when (theme.borderStyle) {
        KeyBorderStyle.GRADIENT -> theme.borderGradient.stops.sortedBy { it.position }.map { it.color }
        KeyBorderStyle.SOLID -> listOf(theme.borderColor, theme.borderColor)
        else -> listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = .35f), MaterialTheme.colorScheme.onSurface.copy(alpha = .35f))
    }.let { if (it.size < 2) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary) else it }
    val strength = if (enabled) 1f else .35f
    val channel = if (dark) Color(0xFF10151D) else Color(0xFFB8C0CA)
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        steps = steps,
        thumb = {
            Canvas(Modifier.size(24.dp, 30.dp)) {
                val rim = CornerRadius(8.dp.toPx())
                val face = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
                val origin = Offset(2.dp.toPx(), 1.dp.toPx())
                drawRoundRect(Color.Black.copy(alpha = .22f * strength), origin + Offset(0f, 3.dp.toPx()), face, rim)
                // Subpixel-like split edges imitate refraction without a blur pass.
                drawRoundRect(Color.Cyan.copy(alpha = .18f * strength), origin + Offset(-.6.dp.toPx(), 0f), face, rim, style = Stroke(1.dp.toPx()))
                drawRoundRect(Color.Magenta.copy(alpha = .16f * strength), origin + Offset(.6.dp.toPx(), 0f), face, rim, style = Stroke(1.dp.toPx()))
                drawRoundRect(Brush.verticalGradient(listOf(
                    (if (dark) Color(0xFF9BA6B3) else Color.White).copy(alpha = strength),
                    (if (dark) Color(0xFF46515F) else Color(0xFFD9DFE6)).copy(alpha = strength),
                    (if (dark) Color(0xFF252D38) else Color(0xFFA4AEBC)).copy(alpha = strength),
                )), origin, face, rim)
                drawRoundRect(Color.White.copy(alpha = .38f * strength), origin, face, rim, style = Stroke(.7.dp.toPx()))
                drawLine(Color.White.copy(alpha = .55f * strength), Offset(8.dp.toPx(), 4.dp.toPx()), Offset(size.width - 8.dp.toPx(), 4.dp.toPx()), .8.dp.toPx())
                drawLine(Color.Black.copy(alpha = .25f * strength), Offset(center.x, 11.dp.toPx()), Offset(center.x, 19.dp.toPx()), 1.dp.toPx())
                drawLine(Color.White.copy(alpha = .35f * strength), Offset(center.x + 1.dp.toPx(), 11.dp.toPx()), Offset(center.x + 1.dp.toPx(), 19.dp.toPx()), .7.dp.toPx())
            }
        },
        track = { state ->
            Canvas(Modifier.fillMaxWidth().height(14.dp)) {
                val h = 8.dp.toPx()
                val y = (size.height - h) / 2
                val radius = CornerRadius(h / 2)
                drawRoundRect(Color.White.copy(alpha = (if (dark) .1f else .6f) * strength), Offset(0f, y + 1.dp.toPx()), Size(size.width, h), radius)
                drawRoundRect(Brush.verticalGradient(listOf(lerp(channel, Color.Black, .4f), channel, lerp(channel, Color.White, .09f)), startY = y, endY = y + h), Offset(0f, y), Size(size.width, h), radius)
                drawRoundRect(Color.Black.copy(alpha = .3f * strength), Offset(0f, y), Size(size.width, h), radius, style = Stroke(.7.dp.toPx()))
                val fraction = ((state.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
                val start = if (layoutDirection == LayoutDirection.Rtl) size.width * (1f - fraction) else 0f
                val width = size.width * fraction
                if (width > 0f) {
                    val seam = Brush.horizontalGradient(edges.map { it.copy(alpha = .8f * strength) })
                    drawRoundRect(seam, Offset(start, center.y - 2.dp.toPx()), Size(width, 4.dp.toPx()), CornerRadius(2.dp.toPx()), alpha = .16f)
                    drawRoundRect(seam, Offset(start, center.y - .7.dp.toPx()), Size(width, 1.4.dp.toPx()), CornerRadius(1.dp.toPx()))
                }
                drawLine(Color.White.copy(alpha = .07f * strength), Offset(3.dp.toPx(), y + h - 1.dp.toPx()), Offset(size.width - 3.dp.toPx(), y + h - 1.dp.toPx()), .5.dp.toPx())
            }
        },
    )
}
