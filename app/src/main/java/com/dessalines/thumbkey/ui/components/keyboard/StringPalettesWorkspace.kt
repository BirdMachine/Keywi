package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
internal fun StringPalettesWorkspace(onCommit: (String) -> Unit) {
    val context = LocalContext.current
    val collections = rememberToolPageConfig().snippets.filter { it.enabled }
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val current = collections.firstOrNull { it.id == chosen } ?: collections.firstOrNull()
    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("String palettes", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            collections.forEach { c ->
                ToolPageSurface(onClick = { chosen = c.id }, selected = current?.id == c.id) { Text("${c.glyph} ${c.title}", Modifier.padding(10.dp)) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val entries = current?.entries.orEmpty().filter { it.enabled }
            if (entries.isEmpty()) Text("Nothing here yet. Add text collections in Manage tool pages.")
            entries.forEachIndexed { i, entry ->
                PaletteEntryWithAlternates(entry.item(), onCommit, "snippet:${current?.id}:$i")
            }
        }
        TextButton(onClick = { openKeywiSettings(context, "advancedCharacters") }) { Text("Manage tool pages") }
    }
}

@Composable
internal fun PaletteEntryWithAlternates(entry: StringPaletteItem, onCommit: (String) -> Unit, designId: String) {
    var expanded by remember(entry) { mutableStateOf(false) }
    Box {
        ToolPageSurface(onClick = { onCommit(entry.text) }, onLongClick = if (entry.alternates.isEmpty()) null else ({ expanded = true }), designId = designId, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Text(entry.text, style = MaterialTheme.typography.bodyLarge)
                entry.label?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                if (entry.alternates.isNotEmpty()) Text("Hold for alternates", style = MaterialTheme.typography.labelSmall)
            }
        }
        DropdownMenu(expanded, { expanded = false }) {
            entry.alternates.forEach { alternate -> DropdownMenuItem(text = { Text(alternate) }, onClick = { expanded = false; onCommit(alternate) }) }
        }
    }
}
