package com.dessalines.thumbkey.ui.components.keyboard

import com.dessalines.thumbkey.utils.KeyboardMode

/** Coordinates use the caller's canvas units (dp in ME, pixels in HK). */
data class KeyCanvasRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    fun scaled(scale: Float) = KeyCanvasRect(left * scale, top * scale, width * scale, height * scale)
}

enum class KeyGradientLayer { SURFACE, BORDER }

/**
 * Rectangles of the painted keys, excluding exterior padding and including weighted key widths.
 * Row and column gaps stay inside the union canvas, so colors never restart across missing keys.
 */
fun keyCanvasRects(
    rowWidths: List<List<Float>>,
    rowHeights: List<Float>,
    columnGap: Float = 0f,
    rowGap: Float = 0f,
    padding: Float = 0f,
): Map<String, KeyCanvasRect> =
    buildMap {
        var y = 0f
        rowWidths.forEachIndexed { row, widths ->
            val height = rowHeights.getOrElse(row) { 0f }.coerceAtLeast(0f)
            var x = 0f
            widths.forEachIndexed { column, width ->
                val safeWidth = width.coerceAtLeast(0f)
                val insetX = padding.coerceIn(0f, safeWidth / 2f)
                val insetY = padding.coerceIn(0f, height / 2f)
                put("$row:$column", KeyCanvasRect(x + insetX, y + insetY, safeWidth - insetX * 2f, height - insetY * 2f))
                x += safeWidth + columnGap
            }
            y += height + rowGap
        }
    }

private fun KeyDesignOverride.specifies(layer: KeyGradientLayer): Boolean =
    when (layer) {
        KeyGradientLayer.SURFACE -> surfaceColor != null || surfaceGradient != null
        KeyGradientLayer.BORDER -> borderColor != null || borderGradient != null
    }

/**
 * Resolve each layer separately: later applicable groups win, then a specific key's explicit
 * design wins. Assigned fill gradients always fit the selection that owns them: one key for a
 * specific override, or the group bounding rectangle. The sharing switch governs inherited fill
 * gradients and border distribution; it is not needed to fit an explicitly assigned fill.
 * A group with no design and no shared canvas does not erase earlier layer settings.
 * Out-of-layout IDs are ignored; the persisted group naturally adapts to the current gem cut.
 */
fun KeyThemeState.groupCanvasForKey(
    keyId: String,
    layer: KeyGradientLayer,
    rectangles: Map<String, KeyCanvasRect>,
    mode: KeyboardMode = KeyboardMode.MAIN,
): KeyCanvasRect? {
    val specific = keyOverrides[keyId]
    if (specific?.specifies(layer) == true) {
        return if (layer == KeyGradientLayer.SURFACE && specific.surfaceGradient != null) {
            rectangles[keyId]?.takeIf { it.width > 0f && it.height > 0f }
        } else {
            null
        }
    }
    val group =
        keyGroups.lastOrNull {
            keyId in it.keyIds && it.designForMode(mode)?.let { design -> it.sharesCanvas(mode) || design.specifies(layer) } == true
        } ?: return null
    val design = group.designForMode(mode) ?: return null
    val assignedFill = layer == KeyGradientLayer.SURFACE && design.surfaceGradient != null
    if (!group.sharesCanvas(mode) && !assignedFill) return null
    val members = group.keyIds.mapNotNull { rectangles[it] }.filter { it.width > 0f && it.height > 0f }
    if (members.isEmpty() || keyId !in rectangles) return null
    val left = members.minOf { it.left }
    val top = members.minOf { it.top }
    val right = members.maxOf { it.left + it.width }
    val bottom = members.maxOf { it.top + it.height }
    return KeyCanvasRect(left, top, right - left, bottom - top)
}
