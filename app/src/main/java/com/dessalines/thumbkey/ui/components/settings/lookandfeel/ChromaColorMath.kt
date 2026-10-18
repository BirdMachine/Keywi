package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

internal fun discSelection(
    x: Float,
    y: Float,
    cx: Float,
    cy: Float,
    radius: Float,
    previousHue: Float,
): Pair<Float, Float> {
    val dx = x - cx
    val dy = y - cy
    val distance = hypot(dx, dy)
    val hue = if (distance < .001f) previousHue else ((Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() + 360f) % 360f)
    return hue to (distance / radius.coerceAtLeast(1f)).coerceIn(0f, 1f)
}

internal fun stripSelection(
    x: Float,
    width: Float,
    inset: Float,
): Float = ((x - inset) / (width - 2 * inset).coerceAtLeast(1f)).coerceIn(0f, 1f)

internal fun colorHex(color: Color): String =
    String.format(
        java.util.Locale.US,
        "#%02X%02X%02X",
        (color.red * 255).roundToInt(),
        (
            color.green *
                255
        ).roundToInt(),
        (color.blue * 255).roundToInt(),
    )

/** Six digits preserve opacity; eight digits explicitly use Android's AARRGGBB order. */
internal fun parseColorHex(
    text: String,
    alpha: Float,
): Color? {
    val digits = text.trim().removePrefix("#")
    if (digits.length !in listOf(6, 8) || !digits.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) return null
    val packed = digits.toLongOrNull(16) ?: return null
    val rgb = Color((0xFF000000L or (packed and 0xFFFFFF)).toInt())
    return rgb.copy(alpha = if (digits.length == 8) ((packed shr 24) and 255) / 255f else alpha)
}
