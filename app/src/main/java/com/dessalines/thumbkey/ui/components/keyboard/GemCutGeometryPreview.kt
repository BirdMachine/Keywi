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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        val unit = maxWidth / columns
        val rowHeight = if (facet == FacetId.ME_LIKE) unit else if (compact) 12.dp else 44.dp
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            rows.forEach { row ->
                Row {
                    row.forEach { cell ->
                        val shape = RoundedCornerShape(if (compact) 2.dp else 6.dp)
                        Box(
                            Modifier.width(unit * cell.width).height(rowHeight).padding(gap / 2)
                                .clip(shape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .65f), shape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!compact) {
                                PreviewLegend(cell.center, Modifier.align(Alignment.Center), 18)
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
