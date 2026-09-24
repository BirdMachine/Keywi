package com.dessalines.thumbkey.ui.components.settings.boards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.BOARD_ACTIONS
import com.dessalines.thumbkey.ui.components.keyboard.BOARD_SLOTS
import com.dessalines.thumbkey.ui.components.keyboard.BoardBinding
import com.dessalines.thumbkey.ui.components.keyboard.BoardCell
import com.dessalines.thumbkey.ui.components.keyboard.CustomBoard
import com.dessalines.thumbkey.ui.components.keyboard.CustomBoardPreferences
import com.dessalines.thumbkey.ui.components.keyboard.rememberCustomBoards
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedBoardManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val boards = rememberCustomBoards()
    var page by rememberSaveable { mutableStateOf("list") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var second by rememberSaveable { mutableStateOf(false) }
    var cellIndex by rememberSaveable { mutableIntStateOf(0) }
    var gesture by rememberSaveable { mutableStateOf("center") }
    var template by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var help by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var removed by remember { mutableStateOf<Pair<Int, CustomBoard>?>(null) }
    val selected = boards.firstOrNull { it.id == selectedId }
    fun save(board: CustomBoard) = CustomBoardPreferences.save(context, boards.map { if (it.id == board.id) board else it })
    fun open(board: CustomBoard) {
        selectedId = board.id
        second = false
        cellIndex = 0
        gesture = "center"
        page = "edit"
    }
    fun add(board: CustomBoard) {
        CustomBoardPreferences.save(context, boards + board)
        open(board)
    }
    fun back() {
        page = when (page) {
            "settings" -> "edit"
            "list" -> { navController.popBackStack(); "list" }
            else -> "list"
        }
    }
    fun move(id: String, delta: Int) {
        val from = boards.indexOfFirst { it.id == id }
        val to = from + delta
        if (from >= 0 && to in boards.indices) {
            val reordered = boards.toMutableList()
            reordered.add(to, reordered.removeAt(from))
            CustomBoardPreferences.save(context, reordered)
        }
    }
    BackHandler(page != "list") { back() }
    Scaffold(topBar = {
        TopAppBar(title = { Text(when (page) {
            "add" -> "Add board"
            "edit" -> selected?.name ?: "Board"
            "settings" -> "Board settings"
            "reorder" -> "Reorder boards"
            else -> "Boards"
        }, maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
            TextButton(onClick = { back() }, modifier = Modifier.semantics { contentDescription = "Back" }) { Text("←", fontSize = 24.sp) }
        }, actions = {
            if (page == "reorder") TextButton(onClick = { page = "list" }) { Text("Done") }
            if (page == "edit") TextButton(onClick = { page = "settings" }) { Text("Settings") }
            TextButton(onClick = { help = true }, modifier = Modifier.semantics { contentDescription = "Board help" }) { Text("?") }
        })
    }) { padding ->
        key(page, selectedId) {
            Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (page) {
                    "list", "reorder" -> {
                        if (page == "list") {
                            Button(onClick = { template = 0; name = ""; page = "add" }, modifier = Modifier.fillMaxWidth()) { Text("+ Add board") }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Enabled boards appear in this order", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                TextButton(enabled = boards.size > 1, onClick = { page = "reorder" }) { Text("Reorder") }
                            }
                        } else Text("Hold a grip and drag, or use the arrows to change the swipe order.")
                        if (boards.isEmpty()) Text("No custom boards yet. Add a blank board or start with Emoji or Unicode.")
                        boards.forEachIndexed { index, board ->
                            key(board.id) {
                                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            if (page == "reorder") {
                                                val latestMove by rememberUpdatedState<(Int) -> Unit>({ move(board.id, it) })
                                                val threshold = with(LocalDensity.current) { 64.dp.toPx() }
                                                Text("⠿", Modifier.size(48.dp).wrapContentSize().semantics { contentDescription = "Drag ${board.name} to reorder" }.pointerInput(board.id) {
                                                    var distance = 0f
                                                    detectDragGesturesAfterLongPress(onDragStart = { distance = 0f }, onDrag = { change, amount ->
                                                        change.consume()
                                                        distance += amount.y
                                                        if (kotlin.math.abs(distance) >= threshold) {
                                                            latestMove(if (distance > 0) 1 else -1)
                                                            distance = 0f
                                                        }
                                                    })
                                                }, fontSize = 28.sp)
                                            }
                                            Row(Modifier.weight(1f).clickable { if (page == "list") open(board) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                BoardThumbnail(board)
                                                Column(Modifier.weight(1f)) {
                                                    Text(board.name, style = MaterialTheme.typography.titleMedium)
                                                    Text(if (board.enabled) "Sides A + B" else "Disabled", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                            if (page == "list") Switch(board.enabled, { save(board.copy(enabled = it)) }, modifier = Modifier.semantics { contentDescription = "Enable ${board.name}" })
                                        }
                                        if (page == "reorder") Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(enabled = index > 0, onClick = { move(board.id, -1) }) { Text("↑ Move up") }
                                            TextButton(enabled = index < boards.lastIndex, onClick = { move(board.id, 1) }) { Text("↓ Move down") }
                                        }
                                    }
                                }
                            }
                        }
                        if (page == "list") Text("Tap a board to edit its keys.", style = MaterialTheme.typography.bodySmall)
                        removed?.let { (index, board) ->
                            OutlinedButton(onClick = {
                                CustomBoardPreferences.save(context, boards.toMutableList().apply { add(index.coerceAtMost(size), board) })
                                removed = null
                            }) { Text("Undo deletion of ${board.name}") }
                        }
                    }
                    "add" -> {
                        Text("Start fresh or use a template", style = MaterialTheme.typography.headlineSmall)
                        Text("Choose a starting point for your new board.")
                        val templates = remember { listOf(CustomBoard(name = "Blank board"), CustomBoardPreferences.preset(true), CustomBoardPreferences.preset(false)) }
                        templates.forEachIndexed { index, board ->
                            Surface(onClick = { template = index }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, border = if (template == index) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    BoardThumbnail(board)
                                    Column(Modifier.weight(1f)) {
                                        Text(board.name, style = MaterialTheme.typography.titleMedium)
                                        Text(listOf("Build your own layout", "Hearts, faces and reactions", "Symbols, arrows and characters")[index], style = MaterialTheme.typography.bodyMedium)
                                    }
                                    RadioButton(selected = template == index, onClick = { template = index })
                                }
                            }
                        }
                        OutlinedTextField(name, { name = it }, label = { Text("Board name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { add(templates[template].copy(id = UUID.randomUUID().toString(), name = name.trim().ifBlank { if (template == 0) "New board" else templates[template].name })) }, modifier = Modifier.fillMaxWidth()) { Text("Create board") }
                    }
                    "edit" -> selected?.let { board ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(!second, { second = false }, label = { Text("Side A") }, modifier = Modifier.weight(1f))
                            FilterChip(second, { second = true }, label = { Text("Side B") }, modifier = Modifier.weight(1f))
                        }
                        val cells = if (second) board.sideB else board.sideA
                        repeat(3) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) { col ->
                                    val index = row * 3 + col
                                    Surface(onClick = { cellIndex = index }, modifier = Modifier.weight(1f).aspectRatio(1.25f), shape = RoundedCornerShape(16.dp), color = if (cellIndex == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer, border = if (cellIndex == index) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                                        Box(Modifier.padding(8.dp).semantics { contentDescription = "Edit key ${index + 1}" }, contentAlignment = Alignment.Center) {
                                            Text(bindingLabel(cells.getOrNull(index)?.bindings?.get("center")), fontSize = 26.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Text("A/B")
                            Text("space")
                            Text("⌫")
                        }
                        Text("Navigation row · always available", style = MaterialTheme.typography.labelSmall)
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Edit key ${cellIndex + 1}", style = MaterialTheme.typography.titleLarge)
                                val tab = when (gesture) { "center" -> 0; "hold" -> 2; else -> 1 }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Tap", "Swipes", "Hold").forEachIndexed { index, title ->
                                        FilterChip(tab == index, { gesture = listOf("center", "top", "hold")[index] }, label = { Text(title) }, modifier = Modifier.weight(1f))
                                    }
                                }
                                if (tab == 1) {
                                    val slots = BOARD_SLOTS.filter { it != "center" && it != "hold" }
                                    slots.chunked(3).forEach { chunk ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            chunk.forEach { slot -> FilterChip(gesture == slot, { gesture = slot }, label = { Text(slotLabel(slot)) }) }
                                        }
                                    }
                                }
                                val cell = cells.getOrNull(cellIndex) ?: BoardCell()
                                val binding = cell.bindings[gesture] ?: BoardBinding()
                                fun update(value: BoardBinding) {
                                    val updated = List(9) { index -> if (index == cellIndex) cell.copy(bindings = cell.bindings + (gesture to value)) else cells.getOrNull(index) ?: BoardCell() }
                                    save(if (second) board.copy(sideB = updated) else board.copy(sideA = updated))
                                }
                                key(board.id, second, cellIndex, gesture) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("${slotLabel(gesture)} action: ${actionLabel(binding.kind)} ▾") }
                                        DropdownMenu(expanded, { expanded = false }) {
                                            BOARD_ACTIONS.forEach { kind -> DropdownMenuItem(text = { Text(actionLabel(kind)) }, onClick = { update(binding.copy(kind = kind)); expanded = false }) }
                                        }
                                    }
                                    if (binding.kind == "text") OutlinedTextField(binding.text, { update(binding.copy(text = it)) }, label = { Text("Text to insert") }, modifier = Modifier.fillMaxWidth())
                                }
                                Text("Changes save automatically", style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { page = "settings" }, modifier = Modifier.fillMaxWidth()) { Text("Board settings →") }
                            }
                        }
                    }
                    "settings" -> selected?.let { board ->
                        Text("Changes save automatically", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(board.name, { save(board.copy(name = it)) }, label = { Text("Board name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        BoardSwitch("Enabled", board.enabled) { save(board.copy(enabled = it)) }
                        HorizontalDivider()
                        BoardSwitch("Return to ABC after input", board.returnAfterInput) { save(board.copy(returnAfterInput = it)) }
                        BoardSwitch("Key vibration", board.haptics) { save(board.copy(haptics = it)) }
                        BoardSwitch("Show swipe hints", board.showHints) { save(board.copy(showHints = it)) }
                        HorizontalDivider()
                        OutlinedButton(onClick = { add(board.copy(id = UUID.randomUUID().toString(), name = board.name + " copy")) }, modifier = Modifier.fillMaxWidth()) { Text("Duplicate board") }
                        TextButton(onClick = { delete = true }) { Text("Delete board", color = MaterialTheme.colorScheme.error) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (help) AlertDialog(onDismissRequest = { help = false }, title = { Text("Using custom boards") }, text = { Text("Swipe right on #/ABC to visit enabled boards in order. Tap A/B to change sides; swipe left or hold it to return to ABC. Swipe up on ⌫ for Enter.\n\nTap a key in the editor, then choose Tap, Swipes, or Hold to assign text or an action.\n\nToolbar panels such as Kaomoji belong in Advanced Input Management.") }, confirmButton = { TextButton(onClick = { help = false }) { Text("Got it") } })
    if (delete && selected != null) AlertDialog(onDismissRequest = { delete = false }, title = { Text("Delete ${selected.name}?") }, text = { Text("This removes both sides and their key assignments. You can undo from the board list.") }, confirmButton = {
        TextButton(onClick = {
            removed = boards.indexOf(selected) to selected
            CustomBoardPreferences.save(context, boards.filterNot { it.id == selected.id })
            selectedId = null
            page = "list"
            delete = false
        }) { Text("Delete") }
    }, dismissButton = { TextButton(onClick = { delete = false }) { Text("Cancel") } })
}

private fun actionLabel(kind: String): String = when (kind) {
    "text" -> "Insert text"
    "none" -> "No action"
    "selectAll" -> "Select all"
    "home" -> "Return to ABC"
    "next" -> "Next board"
    "shift" -> "Select side B"
    else -> kind.replaceFirstChar { it.uppercase() }
}

private fun slotLabel(slot: String): String = when (slot) {
    "center" -> "Tap"
    "hold" -> "Hold"
    "topLeft" -> "↖"
    "top" -> "↑"
    "topRight" -> "↗"
    "left" -> "←"
    "right" -> "→"
    "bottomLeft" -> "↙"
    "bottom" -> "↓"
    else -> "↘"
}

private fun bindingLabel(binding: BoardBinding?): String = when {
    binding == null -> "·"
    binding.kind == "text" -> binding.text.ifEmpty { "·" }
    binding.kind == "none" -> "·"
    else -> actionLabel(binding.kind)
}

@Composable
private fun BoardThumbnail(board: CustomBoard) {
    Column(Modifier.width(66.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { col ->
                    Surface(modifier = Modifier.size(20.dp), shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Box(contentAlignment = Alignment.Center) { Text(bindingLabel(board.sideA.getOrNull(row * 3 + col)?.bindings?.get("center")), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Clip) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onChange, modifier = Modifier.semantics { contentDescription = label })
    }
}
