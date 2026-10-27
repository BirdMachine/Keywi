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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
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
    railPattern: GlassRailPattern? = null,
    railDensity: GlassRailDensity? = null,
) {
    val context = LocalContext.current
    rememberGlassRailPreferences(context)
    val selectedRailPattern = railPattern ?: GlassRailPreferences.currentPattern
    val selectedRailDensity = railDensity ?: GlassRailPreferences.currentDensity
    if (!com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences.advancedThemingEnabled) {
        Slider(value, onValueChange, modifier, enabled, valueRange = valueRange, steps = steps, onValueChangeFinished = onValueChangeFinished)
        return
    }
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
            Canvas(Modifier.size(26.dp, 32.dp)) {
                val rim = CornerRadius(9.dp.toPx())
                val face = Size(size.width - 6.dp.toPx(), size.height - 5.dp.toPx())
                val origin = Offset(3.dp.toPx(), 2.dp.toPx())
                // Keep the middle clear: the backdrop and rail remain visible through the lens.
                drawRoundRect(Color.Black.copy(alpha = .12f * strength), origin + Offset(0f, 2.dp.toPx()), face, rim)
                drawRoundRect(Brush.verticalGradient(listOf(
                    Color.White.copy(alpha = .26f * strength),
                    Color.White.copy(alpha = .035f * strength),
                    Color.White.copy(alpha = .07f * strength),
                    Color.Black.copy(alpha = .10f * strength),
                )), origin, face, rim)
                // A bent, magnified rail fragment supplies a cheap lens/refraction cue.
                drawRoundRect(Brush.horizontalGradient(edges.map { it.copy(alpha = .48f * strength) }),
                    Offset(origin.x + 1.dp.toPx(), center.y - .8.dp.toPx()),
                    Size(face.width - 2.dp.toPx(), 2.dp.toPx()), CornerRadius(1.dp.toPx()))
                // Draw separated spectral rims last, so the glass face cannot cover them up.
                drawRoundRect(Color.Cyan.copy(alpha = .65f * strength), origin + Offset(-1.dp.toPx(), -.2.dp.toPx()), face, rim, style = Stroke(1.dp.toPx()))
                drawRoundRect(Color.Magenta.copy(alpha = .60f * strength), origin + Offset(1.dp.toPx(), .3.dp.toPx()), face, rim, style = Stroke(1.dp.toPx()))
                drawRoundRect(Color.White.copy(alpha = .50f * strength), origin, face, rim, style = Stroke(.65.dp.toPx()))
                drawLine(Color.White.copy(alpha = .8f * strength), Offset(9.dp.toPx(), 4.dp.toPx()), Offset(size.width - 9.dp.toPx(), 4.dp.toPx()), 1.dp.toPx())
                drawLine(Color.White.copy(alpha = .5f * strength), Offset(5.dp.toPx(), 10.dp.toPx()), Offset(5.dp.toPx(), 14.dp.toPx()), .8.dp.toPx())
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
                // Pattern marks decorate only the unfilled inset rail, including in RTL.
                if (selectedRailPattern != GlassRailPattern.PLAIN) {
                    val inset = 3.dp.toPx()
                    val left = if (layoutDirection == LayoutDirection.Rtl) inset else size.width * fraction + inset
                    val right = if (layoutDirection == LayoutDirection.Rtl) size.width * (1f - fraction) - inset else size.width - inset
                    if (right > left) {
                        clipRect(left = left, top = y + 1.dp.toPx(), right = right, bottom = y + h - 1.dp.toPx()) {
                            val ink = (if (dark) Color.White else Color.Black).copy(alpha = (if (dark) .34f else .28f) * strength)
                            val step = selectedRailDensity.spacingDp.dp.toPx()
                            val stroke = .7.dp.toPx()
                            var x = inset
                            var index = 0
                            while (x < size.width) {
                                when (selectedRailPattern) {
                                    GlassRailPattern.TICKS -> {
                                        val height = if (index % 4 == 0) 5.dp.toPx() else 2.5.dp.toPx()
                                        drawLine(ink, Offset(x, center.y - height / 2), Offset(x, center.y + height / 2), stroke)
                                    }
                                    GlassRailPattern.DOTS -> {
                                        drawCircle(ink, .65.dp.toPx(), Offset(x, center.y - 1.5.dp.toPx()))
                                        drawCircle(ink, .65.dp.toPx(), Offset(x + step / 2, center.y + 1.5.dp.toPx()))
                                    }
                                    GlassRailPattern.MESH -> {
                                        drawLine(ink, Offset(x, y + 1.dp.toPx()), Offset(x + step, y + h - 1.dp.toPx()), stroke)
                                        drawLine(ink, Offset(x, y + h - 1.dp.toPx()), Offset(x + step, y + 1.dp.toPx()), stroke)
                                    }
                                    GlassRailPattern.DIAGONAL -> {
                                        drawLine(ink, Offset(x, y + h - 1.dp.toPx()), Offset(x + step, y + 1.dp.toPx()), stroke)
                                    }
                                    GlassRailPattern.PLAIN -> Unit
                                }
                                x += step
                                index++
                            }
                        }
                    }
                }
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
