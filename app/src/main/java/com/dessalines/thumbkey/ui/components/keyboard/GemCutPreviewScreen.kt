package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.utils.KeyboardDefinition

/** Activity-safe preview. ME legends come from the selected/modified character layout;
 * HK reuses its live renderer, including Shift, symbols and desktop controls. */
@Composable
fun GemCutPreviewScreen(
    facet: FacetId,
    definition: KeyboardDefinition,
    modifier: Modifier = Modifier,
    keyBorderWidth: Float = com.dessalines.thumbkey.db.DEFAULT_KEY_BORDER_WIDTH / 10f,
    keyRadius: Int = com.dessalines.thumbkey.db.DEFAULT_KEY_RADIUS,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (facet == FacetId.ME_LIKE) {
            var layer by remember { mutableStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ABC", "⇧", "123").forEachIndexed { index, label ->
                    FilterChip(selected = layer == index, onClick = { layer = index }, label = { Text(label) })
                }
            }
            val keyboard = when (layer) {
                1 -> definition.modes.shifted
                2 -> definition.modes.numeric
                else -> definition.modes.main
            }
            GemCutGeometryPreview(facet, keyboard, Modifier.fillMaxWidth())
            Text(definition.title, style = MaterialTheme.typography.labelSmall)
        } else {
            MachineCutSkeleton(keyBorderWidth = keyBorderWidth, keyRadius = keyRadius)
            Text("Try ⇧, ?123 or ⌘ to explore the HK layers.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
