package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PaletteWorkspaceHeader(
    selected: InputPalette,
    onSelect: (InputPalette) -> Unit,
    onClose: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(
            InputPalette.FANCY_TEXT to "𝕱  Fancy Text",
            InputPalette.KAOMOJI to "(｡•̀ᴗ-)✧  Kaomoji",
        ).forEach { (palette, title) ->
            val active = selected == palette
            PaletteKeySurface(
                onClick = { onSelect(palette) },
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp),
                radius = 6,
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        PaletteKeySurface(
            onClick = onClose,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 44.dp),
            radius = 6,
        ) {
            Text(
                "✕",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
