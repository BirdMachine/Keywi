package com.dessalines.thumbkey.ui.components.settings.advancedinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.FancyTextFormatter
import com.dessalines.thumbkey.ui.components.keyboard.ManagedCollection
import com.dessalines.thumbkey.ui.components.keyboard.ManagedEntry
import com.dessalines.thumbkey.ui.components.keyboard.QuickTool
import com.dessalines.thumbkey.ui.components.keyboard.ToolPageConfig
import com.dessalines.thumbkey.ui.components.keyboard.ToolPagePreferences
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarPage
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarViewPreferences
import com.dessalines.thumbkey.ui.components.keyboard.defaultKaomoji
import com.dessalines.thumbkey.ui.components.keyboard.moved
import com.dessalines.thumbkey.ui.components.keyboard.openKeywiSettings
import com.dessalines.thumbkey.ui.components.keyboard.ordered
import com.dessalines.thumbkey.ui.components.keyboard.rememberToolPageConfig
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsCard
import com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolPageManagerScreen() {
    val context = LocalContext.current
    val config = rememberToolPageConfig()
    var home by remember { mutableStateOf(ToolbarViewPreferences.home(context)) }
    var section by rememberSaveable { mutableStateOf("home") }
    var collectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var restore by remember { mutableStateOf<(() -> Unit)?>(null) }
    var editCollection by remember { mutableStateOf<ManagedCollection?>(null) }
    var editEntry by remember { mutableStateOf<Pair<Int, ManagedEntry>?>(null) }
    var delete by remember { mutableStateOf<(() -> Unit)?>(null) }
    fun save(value: ToolPageConfig) = ToolPagePreferences.save(context, value)
    val groups = if (section == "snippets") config.snippets else config.kaomoji
    fun saveGroups(values: List<ManagedCollection>) = save(if (section == "snippets") config.copy(snippets = values) else config.copy(kaomoji = values))
    val group = groups.firstOrNull { it.id == collectionId }
    fun saveGroup(value: ManagedCollection) = saveGroups(groups.map { if (it.id == value.id) value else it })
    val heading = when (section) {
        "pages" -> "Tool page order"
        "kaomoji" -> "Kaomoji"
        "snippets" -> "String palettes"
        "formatting" -> "Formatting"
        "quick" -> "Quick Settings"
        else -> "Tool pages & palettes"
    }
    Scaffold(
        containerColor = if (LocalSettingsDrip.current != null) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = { CenterAlignedTopAppBar(title = { Text(heading) }) },
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (section != "home") TextButton(onClick = { if (collectionId != null) collectionId = null else section = "home" }) {
                Text(if (collectionId != null) "‹ Collections" else "‹ All tool pages")
            }
            if (section == "home") {
                Text("Make room for your tools", style = MaterialTheme.typography.titleLarge)
                Text("Choose what appears on each page. Changes save " +
"automatically; restore defaults is available in " +
"each section.")
                listOf("pages" to "Page order & visibility", "kaomoji" to "Kaomoji", "formatting" to "Formatting", "snippets" to "String palettes", "quick" to "Quick Settings").forEach { (id, title) ->
                    ManagerLink(title) { section = id; collectionId = null }
                }
                TextButton(onClick = { openKeywiSettings(context, "advancedBoards") }) { Text("Manage character boards & key actions") }
            }
            if (section == "pages") {
                Text("Hide pages from the swipe cycle, change their " +
"order, and choose the double-tap home. " +
"Character input always stays available.")
                val pages = ordered(config.pages, ToolbarPage.entries.map { it.name })
                pages.forEachIndexed { i, name ->
                    val page = ToolbarPage.valueOf(name)
                    ManagedRow(page.title, name !in config.hiddenPages || page == ToolbarPage.CHARACTERS, i, pages.size,
                        { delta -> save(config.copy(pages = pages.moved(i, delta))) },
                        { enabled -> save(config.copy(hiddenPages = if (enabled) config.hiddenPages - name else config.hiddenPages + name)) },
                        canHide = page != ToolbarPage.CHARACTERS) {
                        TextButton(enabled = name !in config.hiddenPages, onClick = { ToolbarViewPreferences.setHome(context, page); home = page }) {
                            Text(if (home == page) "✓ Home" else "Set home")
                        }
                    }
                }
                TextButton(onClick = { restore = { save(config.copy(pages = ToolPageConfig().pages, hiddenPages = ToolPageConfig().hiddenPages)); ToolbarViewPreferences.setHome(context, ToolbarPage.CHARACTERS); home = ToolbarPage.CHARACTERS } }) { Text("Restore page defaults") }
            }
            if (section == "kaomoji" || section == "snippets") {
                if (group == null) {
                    Text(if (section == "snippets") "Collections insert plain text, symbols, snippets " +
"or multiline ASCII art. They do not execute " +
"commands." else "Edit built-in categories or add your own. " +
"Favorites, recents and search sources stay " +
"separate.")
                    groups.forEachIndexed { i, c ->
                        ManagedRow("${c.glyph} ${c.title}", c.enabled, i, groups.size, { saveGroups(groups.moved(i, it)) }, { saveGroup(c.copy(enabled = it)) }) {
                            TextButton(onClick = { collectionId = c.id }) { Text("${c.entries.size} entries · Edit") }
                        }
                    }
                    TextButton(onClick = { editCollection = ManagedCollection(UUID.randomUUID().toString(), "") }) { Text("Add collection") }
                    if (section == "snippets") Text("Enable String palettes in Page order & visibility " +
"to put it in the toolbar cycle.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { restore = { saveGroups(if (section == "kaomoji") defaultKaomoji() else emptyList()) } }) { Text("Restore ${if (section == "kaomoji") "kaomoji" else "palette"} defaults") }
                } else {
                    Text("${group.glyph} ${group.title}", style = MaterialTheme.typography.titleLarge)
                    Text("Preview · tap an entry below to edit its text, " +
"label and alternates.", style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { editCollection = group }) { Text("Rename / symbol") }
                        TextButton(onClick = { delete = { saveGroups(groups.filterNot { it.id == group.id }); collectionId = null } }) { Text("Delete collection") }
                    }
                    group.entries.forEachIndexed { i, entry ->
                        ManagedRow(entry.label.ifBlank { entry.text.take(40) }, entry.enabled, i, group.entries.size,
                            { saveGroup(group.copy(entries = group.entries.moved(i, it))) },
                            { enabled -> saveGroup(group.copy(entries = group.entries.mapIndexed { n, e -> if (n == i) e.copy(enabled = enabled) else e })) }) {
                            Text(entry.text, style = MaterialTheme.typography.bodyLarge)
                            Row {
                                TextButton(onClick = { editEntry = i to entry }) { Text("Edit entry ${i + 1}") }
                                TextButton(onClick = { delete = { saveGroup(group.copy(entries = group.entries.filterIndexed { n, _ -> n != i })) } }) { Text("Delete entry ${i + 1}") }
                            }
                        }
                    }
                    TextButton(onClick = { editEntry = -1 to ManagedEntry("") }) { Text("Add entry") }
                    Text("Hidden entries stay saved. Deleted built-ins can " +
"be recovered with Restore kaomoji defaults.", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (section == "formatting") {
                Text("Choose and arrange category tabs and " +
"transformations. Their Unicode conversion rules " +
"stay built in.")
                val categories = ordered(config.formatCategories, FancyTextFormatter.Category.entries.map { it.name })
                categories.forEachIndexed { i, name ->
                    ManagedRow("${FancyTextFormatter.Category.valueOf(name).title} category", name !in config.hiddenFormatCategories, i, categories.size,
                        { save(config.copy(formatCategories = categories.moved(i, it))) },
                        { enabled -> save(config.copy(hiddenFormatCategories = if (enabled) config.hiddenFormatCategories - name else config.hiddenFormatCategories + name)) }) {}
                }
                val formats = ordered(config.formats, FancyTextFormatter.styles.map { it.id })
                formats.forEachIndexed { i, id ->
                    val style = FancyTextFormatter.styles.first { it.id == id }
                    ManagedRow(style.title, id !in config.hiddenFormats, i, formats.size, { save(config.copy(formats = formats.moved(i, it))) },
                        { enabled -> save(config.copy(hiddenFormats = if (enabled) config.hiddenFormats - id else config.hiddenFormats + id)) }) {
                        Text(FancyTextFormatter.transform("Keywi 123", id), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                TextButton(onClick = { restore = { val d = ToolPageConfig(); save(config.copy(formats = d.formats, hiddenFormats = emptySet(), formatCategories = d.formatCategories, hiddenFormatCategories = emptySet())) } }) { Text("Restore formatting defaults") }
            }
            if (section == "quick") {
                Text("Choose the controls and shortcuts shown in Quick " +
"Settings. All Keywi settings remains available at " +
"the bottom.")
                val tools = ordered(config.quickTools, QuickTool.entries.map { it.name })
                tools.forEachIndexed { i, name ->
                    ManagedRow(QuickTool.valueOf(name).title, name !in config.hiddenQuickTools, i, tools.size,
                        { save(config.copy(quickTools = tools.moved(i, it))) },
                        { enabled -> save(config.copy(hiddenQuickTools = if (enabled) config.hiddenQuickTools - name else config.hiddenQuickTools + name)) }) {}
                }
                TextButton(onClick = { restore = { save(config.copy(quickTools = ToolPageConfig().quickTools, hiddenQuickTools = emptySet())) } }) { Text("Restore Quick Settings defaults") }
            }
        }
    }
    restore?.let { action ->
        ConfirmManagerAction("Restore defaults?", "This resets only this section. Custom content in " +
"this section will be replaced; " +
"favorites, recents and other tool pages are kept.", "Restore", { restore = null }) { action(); restore = null }
    }
    delete?.let { action -> ConfirmManagerAction("Delete this content?", "This removes the selected entry or collection. " +
"Hide it instead if you want to keep it for later.", "Delete", { delete = null }) { action(); delete = null } }
    editCollection?.let { original ->
        var title by remember(original) { mutableStateOf(original.title) }
        var glyph by remember(original) { mutableStateOf(original.glyph) }
        AlertDialog(onDismissRequest = { editCollection = null }, title = { Text("Collection") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Collection name") }, singleLine = true)
                OutlinedTextField(glyph, { glyph = it }, label = { Text("Symbol") }, singleLine = true)
            }
        }, confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = {
            val value = original.copy(title = title.trim(), glyph = glyph.trim().ifBlank { "✧" })
            saveGroups(if (groups.any { it.id == original.id }) groups.map { if (it.id == original.id) value else it } else groups + value)
            editCollection = null
        }) { Text("Save collection") } }, dismissButton = { TextButton(onClick = { editCollection = null }) { Text("Cancel") } })
    }
    editEntry?.let { (index, original) ->
        var text by remember(original, index) { mutableStateOf(original.text) }
        var label by remember(original, index) { mutableStateOf(original.label) }
        var alternates by remember(original, index) { mutableStateOf(original.alternates.joinToString("\n")) }
        AlertDialog(onDismissRequest = { editEntry = null }, title = { Text("Text entry") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(text, { text = it }, label = { Text("Text to insert") }, minLines = 2, maxLines = 6)
                OutlinedTextField(label, { label = it }, label = { Text("Label (optional)") })
                OutlinedTextField(alternates, { alternates = it }, label = { Text("Alternates · one per line") }, minLines = 2, maxLines = 4)
            }
        }, confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = {
            group?.let { g ->
                val value = original.copy(text = text, label = label, alternates = alternates.lines().filter { it.isNotBlank() }.distinct())
                saveGroup(g.copy(entries = if (index < 0) g.entries + value else g.entries.mapIndexed { i, e -> if (i == index) value else e }))
            }
            editEntry = null
        }) { Text("Save entry") } }, dismissButton = { TextButton(onClick = { editEntry = null }) { Text("Cancel") } })
    }
}

@Composable
private fun ManagerLink(title: String, onClick: () -> Unit) {
    AdvancedSettingsCard(onClick = onClick) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title); Text("›") } }
}

@Composable
private fun ManagedRow(title: String, enabled: Boolean, index: Int, count: Int, move: (Int) -> Unit, toggle: (Boolean) -> Unit, canHide: Boolean = true, content: @Composable () -> Unit) {
    AdvancedSettingsCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Switch(enabled, toggle, enabled = canHide, modifier = Modifier.semantics { contentDescription = "Show $title" })
        }
        content()
        Row {
            TextButton(enabled = index > 0, onClick = { move(-1) }, modifier = Modifier.semantics { contentDescription = "Move $title up" }) { Text("↑ Up") }
            TextButton(enabled = index < count - 1, onClick = { move(1) }, modifier = Modifier.semantics { contentDescription = "Move $title down" }) { Text("↓ Down") }
        }
    }
}

@Composable
private fun ConfirmManagerAction(title: String, message: String, button: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = confirm) { Text(button) } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
