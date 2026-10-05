package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * First visible Machine Cut proof-of-geometry.
 *
 * This intentionally uses MaterialTheme colors instead of defining its own
 * palette. The active Keywi Theme remains outside the facet and therefore
 * paints this skeleton through the same theme machinery as ME-Like.
 *
 * It is not yet an input surface: wiring KeyAction / layers comes after the
 * topology boundary is proven without regressing ME-Like.
 */
@Composable
fun MachineCutSkeleton(
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("Esc", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "−", "=", "⌫"),
        listOf("↹", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\"),
        listOf("Ctrl", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "↵"),
        listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "△", "⇧"),
        listOf("Esc", "◉", "Alt", "◆", "Space", "Fn", "○", "◁", "▽", "▷"),
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                row.forEach { label ->
                    val weight = when {
                        label == "Space" -> 4.4f
                        label in setOf("Ctrl", "Alt", "Fn", "Esc") -> 1.35f
                        label in setOf("↹", "⌫", "↵", "⇧") -> 1.5f
                        else -> 1f
                    }
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(if (rowIndex == 4) 46.dp else 48.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
