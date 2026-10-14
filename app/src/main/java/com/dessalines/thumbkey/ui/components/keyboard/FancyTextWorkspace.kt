package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fancy Text occupies the same keyboard overlay slot as the Kaomoji palette.
 * Style selection currently transforms the editor selection; live preview follows.
 */
@Composable
fun FancyTextWorkspace(
    onApply: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles = listOf(
        "UPPERCASE" to "uppercase",
        "lowercase" to "lowercase",
        "Title Case" to "title",
        "aLtErNaTiNg" to "alternating",
        "ˢᵘᵖᵉʳ" to "superscript",
        "ₛᵤᵦ" to "subscript",
        "𝗕𝗼𝗹𝗱" to "bold",
        "𝘐𝘵𝘢𝘭𝘪𝘤" to "italic",
        "𝙼𝚘𝚗𝚘" to "monospace",
        "ⓒⓘⓡⓒⓛⓔ" to "circled",
        "Ｆｕｌｌ" to "fullwidth",
        "𝕱𝖗𝖆𝖐𝖙𝖚𝖗" to "fraktur",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("𝕱  Fancy Text", style = MaterialTheme.typography.titleMedium)
                Surface(onClick = onClose, shape = RoundedCornerShape(12.dp)) {
                    Text("✕", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }
            Text("Select text in the editor, then choose a style.", style = MaterialTheme.typography.bodySmall)
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                styles.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (label, style) ->
                            Surface(
                                onClick = { onApply(style) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(label, maxLines = 1, modifier = Modifier.padding(9.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
