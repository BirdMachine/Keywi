package com.dessalines.thumbkey.ui.components.settings.modifykeys

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dessalines.thumbkey.db.AppSettingsViewModel
import com.dessalines.thumbkey.db.DEFAULT_KEYBOARD_LAYOUT
import com.dessalines.thumbkey.db.DEFAULT_KEY_MODIFICATIONS
import com.dessalines.thumbkey.db.KeyModificationsUpdate
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import com.dessalines.thumbkey.effects.soundDirections
import com.dessalines.thumbkey.ui.components.keyboard.ActionGifPreferences
import com.dessalines.thumbkey.ui.components.keyboard.AdvancedKeyWordPreferences
import com.dessalines.thumbkey.ui.components.keyboard.MachineCutLayouts
import com.dessalines.thumbkey.ui.components.keyboard.VisualRemapSlot
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyC
import com.dessalines.thumbkey.utils.KeyDisplay
import com.dessalines.thumbkey.utils.KeyItemC
import com.dessalines.thumbkey.utils.KeyboardC
import com.dessalines.thumbkey.utils.KeyboardLayout
import com.dessalines.thumbkey.utils.KeyboardMode
import com.dessalines.thumbkey.utils.SimpleTopAppBar
import com.dessalines.thumbkey.utils.getModifiedKeyboardDefinition
import com.dessalines.thumbkey.utils.getVisualTextOverride
import com.dessalines.thumbkey.utils.updateVisualTextOverride

private val MATRIX_GREEN = Color(0xFF52FF52)
private val MATRIX_DIM = Color(0xFF1FAE3B)
private val MATRIX_BLACK = Color(0xFF001006)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedKeyWordSelectionScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
) {
    val settings by appSettingsViewModel.appSettings.observeAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    val layout =
        KeyboardLayout.entries.getOrElse(settings?.keyboardLayout ?: DEFAULT_KEYBOARD_LAYOUT) {
            KeyboardLayout.entries.first()
        }
    val yaml = settings?.keyModifications ?: DEFAULT_KEY_MODIFICATIONS
    var selectedMode by remember { mutableStateOf(KeyboardMode.MAIN) }
    var selectedRow by remember { mutableIntStateOf(0) }
    var selectedCol by remember { mutableIntStateOf(0) }
    var designGroupMode by remember { mutableStateOf(false) }
    var designKeys by remember { mutableStateOf(setOf("0:0")) }
    var showCurrentWord by remember { mutableStateOf(AdvancedKeyWordPreferences.showCurrentWord(context)) }
    var longPressAdd by remember { mutableStateOf(AdvancedKeyWordPreferences.longPressAddWord(context)) }
    var effectsHk by remember { mutableStateOf(false) }
    var editorMessage by remember { mutableStateOf<String?>(null) }

    val effectiveDefinition =
        if (yaml.isNotBlank()) {
            getModifiedKeyboardDefinition(layout, yaml) ?: layout.keyboardDefinition
        } else {
            layout.keyboardDefinition
        }
    val keyboard = effectiveDefinition.keyboardForMode(selectedMode)
    val selectedKey = keyboard?.arr?.getOrNull(selectedRow)?.getOrNull(selectedCol)
    val overrides = remember { mutableStateMapOf<VisualRemapSlot, String>() }

    LaunchedEffect(layout.name, selectedMode, selectedRow, selectedCol, yaml) {
        overrides.clear()
        VisualRemapSlot.entries.forEach { slot ->
            overrides[slot] =
                runCatching {
                    getVisualTextOverride(yaml, layout.name, selectedMode, selectedRow, selectedCol, slot)
                }.getOrNull().orEmpty()
        }
    }

    Scaffold(
        topBar = { SimpleTopAppBar(text = "Advanced Key & Word Selection", navController = navController) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MATRIX_BLACK),
                border = BorderStroke(1.dp, MATRIX_DIM),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "KEYWI // MATRIX REMAPPER",
                        color = MATRIX_GREEN,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    )
                    Text(
                        "Tap a key below, then override any of its nine MessageEase-style swipe positions. Blank fields inherit the original mapping.",
                        color = MATRIX_GREEN.copy(alpha = 0.78f),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Layout: ${layout.keyboardDefinition.title}",
                        color = MATRIX_GREEN,
                        fontFamily = FontFamily.Monospace,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(KeyboardMode.MAIN, KeyboardMode.SHIFTED, KeyboardMode.NUMERIC).forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = {
                                    selectedMode = mode
                                    selectedRow = 0
                                    selectedCol = 0
                                    designKeys = setOf("0:0")
                                },
                                label = { Text(mode.name.lowercase(), fontFamily = FontFamily.Monospace) },
                            )
                        }
                    }
                }
            }

            SettingSwitchRow("Select a key group for appearance", designGroupMode, onCheckedChange = {
                designGroupMode = it
                designKeys = setOf("$selectedRow:$selectedCol")
            })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !effectsHk, onClick = {
                    effectsHk = false
                    selectedRow = 0
                    selectedCol = 0
                    designKeys = setOf("0:0")
                }, label = { Text("ME-Like matrix") })
                FilterChip(selected = effectsHk, onClick = {
                    effectsHk = true
                    selectedRow = 0
                    selectedCol = 0
                    designKeys = setOf("0:0")
                }, label = { Text("HK-Like matrix") })
            }
            if (effectsHk) {
                HkEffectsMatrix(
                    selectedIds = if (designGroupMode) designKeys else setOf("$selectedRow:$selectedCol"),
                ) { row, col ->
                    selectedRow = row
                    selectedCol = col
                    val id = "$row:$col"
                    designKeys =
                        if (designGroupMode) {
                            if (id in designKeys) designKeys - id else designKeys + id
                        } else {
                            setOf(id)
                        }
                    editorMessage = null
                }
            } else {
                keyboard?.let {
                    MatrixKeyboardPreview(
                        keyboard = it,
                        selectedRow = selectedRow,
                        selectedCol = selectedCol,
                        selectedIds = if (designGroupMode) designKeys else setOf("$selectedRow:$selectedCol"),
                        onSelect = { row, col ->
                            selectedRow = row
                            selectedCol = col
                            val id = "$row:$col"
                            designKeys =
                                if (designGroupMode) {
                                    if (id in designKeys) designKeys - id else designKeys + id
                                } else {
                                    setOf(id)
                                }
                            editorMessage = null
                        },
                    )
                }
            }

            com.dessalines.thumbkey.ui.components.settings.lookandfeel.KeyDesignOverrideEditor(
                selectedIds = if (designGroupMode) designKeys else setOf("$selectedRow:$selectedCol"),
                groupMode = designGroupMode,
                onSelectGroup = { ids ->
                    designGroupMode = true
                    designKeys = ids
                },
            )

            KeyEffectsAssignments(
                keyId = if (effectsHk) "hk${selectedRow}_$selectedCol" else "key${selectedRow}_$selectedCol",
                navController = navController,
                hk = effectsHk,
            )

            if (!effectsHk) {
                selectedKey?.let { key ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MATRIX_BLACK),
                        border = BorderStroke(1.dp, MATRIX_GREEN),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "EDIT key${selectedRow}_$selectedCol",
                                color = MATRIX_GREEN,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            )
                            RemapEditorGrid(key, overrides)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        runCatching {
                                            var nextYaml = yaml
                                            VisualRemapSlot.entries.forEach { slot ->
                                                nextYaml =
                                                    updateVisualTextOverride(
                                                        yaml = nextYaml,
                                                        layoutName = layout.name,
                                                        mode = selectedMode,
                                                        row = selectedRow,
                                                        col = selectedCol,
                                                        slot = slot,
                                                        text = overrides[slot].orEmpty(),
                                                    )
                                            }
                                            appSettingsViewModel.updateKeyModifications(
                                                KeyModificationsUpdate(id = 1, keyModifications = nextYaml),
                                            )
                                        }.onSuccess {
                                            editorMessage = "Overrides saved ✓"
                                        }.onFailure {
                                            editorMessage = "Could not save: ${it.message}"
                                        }
                                    },
                                ) {
                                    Text("SAVE KEY")
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        VisualRemapSlot.entries.forEach { overrides[it] = "" }
                                        editorMessage = "Cleared in editor — tap SAVE KEY to restore originals."
                                    },
                                ) {
                                    Text("CLEAR")
                                }
                            }
                            editorMessage?.let {
                                Text(it, color = MATRIX_GREEN, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current word + personal dictionary", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "This adds the unfinished word (like “scr”) to the left side of the suggestion strip, MessageEase-style.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    SettingSwitchRow(
                        title = "Show unfinished word in suggestions",
                        checked = showCurrentWord,
                        onCheckedChange = {
                            showCurrentWord = it
                            AdvancedKeyWordPreferences.setShowCurrentWord(context, it)
                        },
                    )
                    SettingSwitchRow(
                        title = "Long-press unfinished word → add to personal dictionary",
                        checked = longPressAdd,
                        enabled = showCurrentWord,
                        onCheckedChange = {
                            longPressAdd = it
                            AdvancedKeyWordPreferences.setLongPressAddWord(context, it)
                        },
                    )
                    Text(
                        "Long-press uses Android's user dictionary provider from the keyboard itself.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("modifyKeys") },
            ) {
                Text("OPEN RAW YAML KEY EDITOR")
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MatrixKeyboardPreview(
    keyboard: KeyboardC,
    selectedRow: Int,
    selectedCol: Int,
    onSelect: (Int, Int) -> Unit,
    selectedIds: Set<String> = setOf("$selectedRow:$selectedCol"),
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, MATRIX_DIM),
    ) {
        Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            keyboard.arr.forEachIndexed { row, keys ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    keys.forEachIndexed { col, key ->
                        MatrixKey(
                            key = key,
                            selected = "$row:$col" in selectedIds,
                            modifier = Modifier.weight(key.widthMultiplier.toFloat().coerceAtLeast(1f)),
                            onClick = { onSelect(row, col) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixKey(
    key: KeyItemC,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MATRIX_GREEN else MATRIX_DIM.copy(alpha = 0.55f)
    Card(
        modifier = modifier.heightIn(min = 82.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF05240C) else Color(0xFF010804)),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(5.dp),
    ) {
        Column(Modifier.padding(3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            MatrixLabelRow(key.topLeft, key.top, key.topRight)
            MatrixLabelRow(key.left, key.center, key.right, centerBold = true)
            MatrixLabelRow(key.bottomLeft, key.bottom, key.bottomRight)
        }
    }
}

@Composable
private fun MatrixLabelRow(
    left: KeyC?,
    center: KeyC?,
    right: KeyC?,
    centerBold: Boolean = false,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        MatrixLabel(left, Modifier.weight(1f))
        MatrixLabel(center, Modifier.weight(1f), centerBold)
        MatrixLabel(right, Modifier.weight(1f))
    }
}

@Composable
private fun MatrixLabel(
    key: KeyC?,
    modifier: Modifier,
    bold: Boolean = false,
) {
    Text(
        text = keyLabel(key),
        modifier = modifier,
        color = if (bold) MATRIX_GREEN else MATRIX_GREEN.copy(alpha = 0.72f),
        fontFamily = FontFamily.Monospace,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (bold) 16.sp else 10.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun RemapEditorGrid(
    key: KeyItemC,
    overrides: MutableMap<VisualRemapSlot, String>,
) {
    val originals =
        mapOf(
            VisualRemapSlot.TOP_LEFT to keyLabel(key.topLeft),
            VisualRemapSlot.TOP to keyLabel(key.top),
            VisualRemapSlot.TOP_RIGHT to keyLabel(key.topRight),
            VisualRemapSlot.LEFT to keyLabel(key.left),
            VisualRemapSlot.CENTER to keyLabel(key.center),
            VisualRemapSlot.RIGHT to keyLabel(key.right),
            VisualRemapSlot.BOTTOM_LEFT to keyLabel(key.bottomLeft),
            VisualRemapSlot.BOTTOM to keyLabel(key.bottom),
            VisualRemapSlot.BOTTOM_RIGHT to keyLabel(key.bottomRight),
        )
    val rows =
        listOf(
            listOf(VisualRemapSlot.TOP_LEFT, VisualRemapSlot.TOP, VisualRemapSlot.TOP_RIGHT),
            listOf(VisualRemapSlot.LEFT, VisualRemapSlot.CENTER, VisualRemapSlot.RIGHT),
            listOf(VisualRemapSlot.BOTTOM_LEFT, VisualRemapSlot.BOTTOM, VisualRemapSlot.BOTTOM_RIGHT),
        )
    rows.forEach { slots ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            slots.forEach { slot ->
                OutlinedTextField(
                    value = overrides[slot].orEmpty(),
                    onValueChange = { overrides[slot] = it },
                    label = { Text(slot.name.lowercase().replace('_', ' '), fontSize = 9.sp) },
                    placeholder = { Text(originals[slot].orEmpty(), fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun keyLabel(key: KeyC?): String {
    if (key == null) return ""
    return when (val display = key.display) {
        is KeyDisplay.TextDisplay -> {
            display.text
        }

        is KeyDisplay.IconDisplay -> {
            "◈"
        }

        null -> {
            when (val action = key.action) {
                is KeyAction.CommitText -> action.text
                else -> action::class.simpleName?.take(3).orEmpty()
            }
        }
    }
}

private fun com.dessalines.thumbkey.utils.KeyboardDefinition.keyboardForMode(mode: KeyboardMode): KeyboardC? =
    when (mode) {
        KeyboardMode.MAIN -> {
            modes.main
        }

        KeyboardMode.SHIFTED -> {
            modes.shifted
        }

        KeyboardMode.NUMERIC -> {
            modes.numeric
        }

        KeyboardMode.CTRLED -> {
            modes.ctrled
        }

        KeyboardMode.ALTED -> {
            modes.alted
        }

        KeyboardMode.EMOJI -> {
            modes.emoji
        }

        KeyboardMode.KAOMOJI,
        KeyboardMode.CLIPBOARD,
        -> {
            null
        }
    }

/** Uses the live HK topology so the displayed cell IDs match runtime events. */
@Composable
private fun HkEffectsMatrix(
    selectedIds: Set<String>,
    onSelect: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("HK-Like key cells", style = MaterialTheme.typography.titleMedium)
        MachineCutLayouts.touchMain.rows.forEachIndexed { row, cells ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                cells.forEachIndexed { col, cell ->
                    Card(
                        modifier = Modifier.weight(cell.width).heightIn(min = 42.dp).clickable { onSelect(row, col) },
                        border =
                            BorderStroke(
                                if ("$row:$col" in selectedIds) 2.dp else 1.dp,
                                if ("$row:$col" in selectedIds) MATRIX_GREEN else MATRIX_DIM,
                            ),
                    ) {
                        Text(
                            cell.label,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Text(
            "Effects use HK-Like cell positions across symbol / control layers. Appearance uses the same row:column positions as other cuts.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun KeyEffectsAssignments(
    keyId: String,
    navController: NavController,
    hk: Boolean,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var sounds by remember { mutableStateOf(ActionSoundPreferences.get(context)) }
    remember(context) { ActionGifPreferences.load(context) }
    val gifs = ActionGifPreferences.current
    var direction by remember(hk) { mutableStateOf("CENTER") }
    val assignmentKey = "$keyId:$direction"
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ACTION FX // $keyId", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(
                "Choose a gesture, then assign a clip from either pack. Changes save immediately. Inherit follows your global direction / playback rules.",
                style = MaterialTheme.typography.bodySmall,
            )
            val rows = if (hk) listOf(listOf("CENTER", "HOLD")) else soundDirections.take(9).chunked(3) + listOf(listOf("HOLD"))
            rows.forEach { gestures ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    gestures.forEach { gesture ->
                        FilterChip(
                            selected = direction == gesture,
                            onClick = { direction = gesture },
                            modifier = Modifier.weight(1f),
                            label = { Text(gestureLabel(gesture), fontSize = 11.sp) },
                        )
                    }
                }
            }
            TaskerKeyAssignments(keyId = keyId, direction = direction)
            EffectAssignmentMenu(
                title = "Sound",
                selectedId = sounds.keys[assignmentKey],
                options = sounds.clips.mapIndexed { i, clip -> clip.id to "${i + 1}. ${clip.name}" },
                onSelect = { id ->
                    ActionSoundPreferences.assignKey(context, keyId, direction, id)
                    sounds = ActionSoundPreferences.get(context)
                },
            )
            EffectAssignmentMenu(
                title = "GIF / PNG",
                selectedId = gifs.keyAssignments[assignmentKey],
                options = gifs.assets.mapIndexed { i, asset -> asset.id to "${i + 1}. ${asset.name}" },
                onSelect = { id ->
                    val current = ActionGifPreferences.current
                    val assignments =
                        current.keyAssignments.toMutableMap().apply {
                            if (id == null) remove(assignmentKey) else put(assignmentKey, id)
                        }
                    ActionGifPreferences.save(context, current.copy(keyAssignments = assignments))
                },
            )
            Text(
                "Sound: ${if (sounds.enabled) "enabled" else "off"} · GIF overlays: ${if (gifs.enabled) "enabled" else "off"}",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { navController.navigate("keypressSounds") }) { Text("Sound pack") }
                TextButton(onClick = { navController.navigate("typingOverlay") }) { Text("GIF pack") }
            }
        }
    }
}

@Composable
private fun EffectAssignmentMenu(
    title: String,
    selectedId: String?,
    options: List<Pair<String, String>>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.first == selectedId }
    Box {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$title: ${if (selectedId == null) "Inherit" else selected?.second ?: "Missing clip"}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Inherit / clear assignment") }, onClick = {
                onSelect(null)
                expanded = false
            })
            options.forEach { (id, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onSelect(id)
                    expanded = false
                })
            }
        }
    }
    selectedId?.let { Text("Clip ID: $it", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall) }
    if (options.isEmpty()) Text("Import clips in the $title pack to assign them here.", style = MaterialTheme.typography.bodySmall)
}

private fun gestureLabel(direction: String): String =
    when (direction) {
        "TOP_LEFT" -> "↖ up left"
        "TOP" -> "↑ up"
        "TOP_RIGHT" -> "↗ up right"
        "LEFT" -> "← left"
        "CENTER" -> "● tap"
        "RIGHT" -> "→ right"
        "BOTTOM_LEFT" -> "↙ down left"
        "BOTTOM" -> "↓ down"
        "BOTTOM_RIGHT" -> "↘ down right"
        else -> "◷ hold"
    }
