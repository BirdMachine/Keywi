package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaletteWorkspaceHeader(
    selected: InputPalette,
    onSelect: (InputPalette) -> Unit,
    onClose: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        TextButton(onClick = { onSelect(InputPalette.FANCY_TEXT) }, modifier = Modifier.weight(1f)) {
            Text(if (selected == InputPalette.FANCY_TEXT) "◆ Fancy" else "◇ Fancy")
        }
        TextButton(onClick = { onSelect(InputPalette.KAOMOJI) }, modifier = Modifier.weight(1f)) {
            Text(if (selected == InputPalette.KAOMOJI) "◆ Kaomoji" else "◇ Kaomoji")
        }
        TextButton(onClick = onClose) { Text("✕") }
    }
}
