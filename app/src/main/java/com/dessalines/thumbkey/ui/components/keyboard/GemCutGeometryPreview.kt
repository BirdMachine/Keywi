package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dessalines.thumbkey.utils.KeyC
import com.dessalines.thumbkey.utils.KeyDisplay
import com.dessalines.thumbkey.utils.KeyboardC

/** A read-only projection of actual rows, key spans and legends, safe in an Activity.
 * Does not construct an IMEService or emit input events from selector thumbnails. */
@Composable
fun GemCutGeometryPreview(
    facet: FacetId,
    keyboard: KeyboardC,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val appearance = remember(context) { KeywiAppearancePreferences.load(context) }
    remember(context) { KeyThemePreferences.load(context) }
    val theme = KeyThemePreferences.current
    val backdrop = remember(context) { BackdropThemePreferences.load(context) }
    val rows = if (facet == FacetId.ME_LIKE) {
        keyboard.arr.map { row -> row.map { PreviewCell(it.widthMultiplier.toFloat(), it.center, it) } }
    } else {
        MachineCutLayouts.touchMain.rows.map { row -> row.map {
            PreviewCell(it.width, KeyC(it.label), null)
        } }
    }
    // Use one unit for every ME row: a shorter row must not stretch its keys.
    val columns = rows.maxOfOrNull { row -> row.sumOf { it.width.toDouble() } }?.toFloat() ?: 1f
    val gap = if (compact) 2.dp else 4.dp
    BoxWithConstraints(modifier.semantics {
        contentDescription = "${if (facet == FacetId.ME_LIKE) "ME-Like" else "HK-Like"} keyboard geometry preview"
    }) {
        val availableWidth = maxWidth
        val unit = availableWidth / columns
        val rowHeight = if (facet == FacetId.ME_LIKE) unit else if (compact) 12.dp else 44.dp
        Box {
        if (!compact && appearance) BackdropVisualLayer(backdrop, Modifier.matchParentSize())
        Column(verticalArrangement = Arrangement.spacedBy(if (facet == FacetId.ME_LIKE) 0.dp else gap)) {
            rows.forEachIndexed { rowIndex, row ->
                // HK uses per-row weights, while ME uses fixed-size cells across all rows.
                val rowUnit = if (facet == FacetId.HK_LIKE) availableWidth / row.sumOf { it.width.toDouble() }.toFloat() else unit
                Row {
                    row.forEachIndexed { index, cell ->
                        val shape = if (compact) CutCornerShape(3.dp) else RoundedCornerShape(6.dp)
                        val appearanceModifier = if (compact || !appearance || facet == FacetId.HK_LIKE) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .65f), shape)
                        } else {
                            var visual: Modifier = Modifier
                            visual = when (theme.surfaceStyle) {
                                KeySurfaceStyle.GRADIENT -> visual.keyboardGradientSlice(
                                    theme.surfaceGradient, with(density) { availableWidth.toPx() }, with(density) { rowHeight.toPx() } * rows.size,
                                    row.take(index).sumOf { it.width.toDouble() }.toFloat() * with(density) { unit.toPx() },
                                    rowIndex * with(density) { rowHeight.toPx() },
                                )
                                KeySurfaceStyle.SOLID -> visual.background(theme.surfaceColor)
                                KeySurfaceStyle.NONE -> visual
                            }
                            when (theme.borderStyle) {
                                KeyBorderStyle.GRADIENT -> visual.keyboardGradientBorder(theme.borderGradient, 1.dp, 6.dp)
                                KeyBorderStyle.SOLID -> visual.border(1.dp, theme.borderColor, shape)
                                KeyBorderStyle.SHADOW -> visual.shadow(theme.shadowElevation.dp, shape,
                                    ambientColor = theme.shadowColor.copy(alpha = theme.shadowAlpha),
                                    spotColor = theme.shadowColor.copy(alpha = theme.shadowAlpha))
                                KeyBorderStyle.NONE -> visual
                            }
                        }
                        Box(
                            Modifier.width(rowUnit * cell.width).height(rowHeight).padding(gap / 2)
                                .clip(shape).then(appearanceModifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            PreviewLegend(cell.center, Modifier.align(Alignment.Center), if (compact) 8 else 18)
                            if (!compact) {
                                cell.key?.let { key ->
                                    listOf(
                                        key.topLeft to Alignment.TopStart, key.top to Alignment.TopCenter,
                                        key.topRight to Alignment.TopEnd, key.left to Alignment.CenterStart,
                                        key.right to Alignment.CenterEnd, key.bottomLeft to Alignment.BottomStart,
                                        key.bottom to Alignment.BottomCenter, key.bottomRight to Alignment.BottomEnd,
                                    ).forEach { (legend, alignment) ->
                                        legend?.let { PreviewLegend(it, Modifier.align(alignment).padding(4.dp), 10) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

private data class PreviewCell(
    val width: Float,
    val center: KeyC,
    val key: com.dessalines.thumbkey.utils.KeyItemC?,
)

@Composable
private fun PreviewLegend(key: KeyC, modifier: Modifier, fontSize: Int) {
    when (val display = key.display) {
        is KeyDisplay.TextDisplay -> Text(
            display.text, modifier, fontSize = fontSize.sp, maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface, fontFamily = display.fontFamily,
        )
        is KeyDisplay.IconDisplay -> Icon(
            display.icon, contentDescription = null, modifier = modifier.size(fontSize.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        null -> Unit
    }
}
