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
 * Appearance comes only from the ambient Keywi/Material theme; geometry and
 * content come from a MachineCutLayout.
 */
@Composable
fun MachineCutSkeleton(
    modifier: Modifier = Modifier,
    layout: MachineCutLayout = MachineCutLayouts.qwertySkeleton,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        layout.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                row.forEach { cell ->
                    val width = if (cell.width != 1f) cell.width else when (cell.label) {
                        "↹", "⌫", "↵", "⇧" -> 1.5f
                        "Ctrl", "Alt", "Fn", "Esc" -> 1.35f
                        else -> 1f
                    }
                    Box(
                        modifier = Modifier
                            .weight(width)
                            .height(if (rowIndex == layout.rows.lastIndex) 46.dp else 48.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cell.label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
