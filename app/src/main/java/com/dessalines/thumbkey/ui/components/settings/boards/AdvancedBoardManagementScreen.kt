package com.dessalines.thumbkey.ui.components.settings.boards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.BOARD_ACTIONS
import com.dessalines.thumbkey.ui.components.keyboard.BOARD_SLOTS
import com.dessalines.thumbkey.ui.components.keyboard.BoardBinding
import com.dessalines.thumbkey.ui.components.keyboard.BoardCell
import com.dessalines.thumbkey.ui.components.keyboard.CustomBoard
import com.dessalines.thumbkey.ui.components.keyboard.CustomBoardPreferences
import com.dessalines.thumbkey.ui.components.keyboard.rememberCustomBoards
import com.dessalines.thumbkey.utils.SimpleTopAppBar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedBoardManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val boards = rememberCustomBoards()
    var selectedId by remember { mutableStateOf<String?>(null) }
    var second by remember { mutableStateOf(false) }
    var cellIndex by remember { mutableIntStateOf(0) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val selected = boards.firstOrNull { it.id == selectedId }
    fun save(board: CustomBoard) = CustomBoardPreferences.save(context, boards.map { if (it.id == board.id) board else it })
    fun add(board: CustomBoard) {
        CustomBoardPreferences.save(context, boards + board)
        selectedId = board.id
        second = false
        cellIndex = 0
    }
    Scaffold(topBar = { SimpleTopAppBar("Advanced Board Management", navController) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Swipe right on #/ABC to visit enabled rooms in this order. In a room, tap A/B to change sides; swipe left or hold it to return to ABC. Swipe up on ⌫ for Enter.")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { add(CustomBoard()) }) { Text("Add") }
                OutlinedButton(onClick = { add(CustomBoardPreferences.preset(true)) }) { Text("Emoji") }
                OutlinedButton(onClick = { add(CustomBoardPreferences.preset(false)) }) { Text("Unicode") }
            }
            boards.forEachIndexed { index, board ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { selectedId = board.id; cellIndex = 0 }, modifier = Modifier.weight(1f)) { Text((if (board.id == selectedId) "▸ " else "") + board.name) }
                    Switch(checked = board.enabled, onCheckedChange = { save(board.copy(enabled = it)) })
                    TextButton(enabled = index > 0, onClick = {
                        val reordered = boards.toMutableList()
                        reordered[index] = boards[index - 1]
                        reordered[index - 1] = board
                        CustomBoardPreferences.save(context, reordered)
                    }) { Text("↑") }
                }
            }
            selected?.let { board ->
                HorizontalDivider()
                OutlinedTextField(board.name, { save(board.copy(name = it)) }, label = { Text("Room name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row {
                    TextButton(onClick = { add(board.copy(id = UUID.randomUUID().toString(), name = board.name + " copy")) }) { Text("Duplicate") }
                    TextButton(onClick = { deleteId = board.id }) { Text("Delete") }
                }
                BoardSwitch("Return to ABC after input", board.returnAfterInput) { save(board.copy(returnAfterInput = it)) }
                BoardSwitch("Allow key vibration", board.haptics) { save(board.copy(haptics = it)) }
                BoardSwitch("Show swipe hints", board.showHints) { save(board.copy(showHints = it)) }
                Row {
                    FilterChip(selected = !second, onClick = { second = false }, label = { Text("Side A") })
                    Spacer(Modifier.width(12.dp))
                    FilterChip(selected = second, onClick = { second = true }, label = { Text("Side B") })
                }
                val cells = if (second) board.sideB else board.sideA
                repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { col ->
                            val index = row * 3 + col
                            val binding = cells.getOrNull(index)?.bindings?.get("center")
                            OutlinedButton(onClick = { cellIndex = index }, modifier = Modifier.weight(1f)) {
                                Text((if (cellIndex == index) "▸ " else "") + (binding?.text?.takeIf { it.isNotEmpty() } ?: binding?.kind ?: "${index + 1}"))
                            }
                        }
                    }
                }
                Text("Key ${cellIndex + 1} · tap, eight swipe directions, and hold", style = MaterialTheme.typography.titleMedium)
                val cell = cells.getOrNull(cellIndex) ?: BoardCell()
                fun update(slot: String, value: BoardBinding) {
                    val newCells = List(9) { index ->
                        if (index == cellIndex) cell.copy(bindings = cell.bindings + (slot to value)) else cells.getOrNull(index) ?: BoardCell()
                    }
                    save(if (second) board.copy(sideB = newCells) else board.copy(sideA = newCells))
                }
                BOARD_SLOTS.forEach { slot ->
                    val binding = cell.bindings[slot] ?: BoardBinding()
                    key(board.id, second, cellIndex, slot) {
                        var expanded by remember { mutableStateOf(false) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box {
                                TextButton(onClick = { expanded = true }) { Text("$slot: ${binding.kind}") }
                                DropdownMenu(expanded, { expanded = false }) {
                                    BOARD_ACTIONS.forEach { kind ->
                                        DropdownMenuItem(text = { Text(kind) }, onClick = { update(slot, binding.copy(kind = kind)); expanded = false })
                                    }
                                }
                            }
                            if (binding.kind == "text") OutlinedTextField(binding.text, { update(slot, binding.copy(text = it)) }, modifier = Modifier.weight(1f), label = { Text("Text / emoji") })
                        }
                    }
                }
                Text("The bottom navigation row stays available even if all editable keys are cleared. Shift selects side B; editing actions include copy, paste, undo, and selection.", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    deleteId?.let { id ->
        AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Delete room?") }, text = { Text("This removes both sides and their key assignments.") }, confirmButton = {
            TextButton(onClick = { CustomBoardPreferences.save(context, boards.filterNot { it.id == id }); if (selectedId == id) selectedId = null; deleteId = null }) { Text("Delete") }
        }, dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } })
    }
}

@Composable
private fun BoardSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f).padding(top = 12.dp))
        Switch(checked, onChange)
    }
}
