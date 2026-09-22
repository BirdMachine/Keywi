package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dessalines.thumbkey.utils.FontSizeVariant
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyC
import com.dessalines.thumbkey.utils.KeyDisplay
import com.dessalines.thumbkey.utils.KeyItemC
import com.dessalines.thumbkey.utils.KeyboardC
import com.dessalines.thumbkey.utils.KeyboardDefinition
import com.dessalines.thumbkey.utils.KeyboardDefinitionModes
import com.dessalines.thumbkey.utils.KeyboardDefinitionSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/** A slot holds a whole string: never split emoji, combining marks, or kaomoji into characters. */
@Serializable
data class BoardBinding(val kind: String = "text", val text: String = "") {
    fun key(large: Boolean = false): KeyC {
        val action = when (kind) {
            "backspace" -> KeyAction.DeleteKeyAction
            "enter" -> KeyAction.IMECompleteAction
            "space" -> KeyAction.CommitText(" ")
            "shift" -> KeyAction.ShiftAndCapsLock(true)
            "copy" -> KeyAction.Copy
            "paste" -> KeyAction.Paste
            "cut" -> KeyAction.Cut
            "undo" -> KeyAction.Undo
            "redo" -> KeyAction.Redo
            "selectAll" -> KeyAction.SelectAll
            "home" -> KeyAction.BoardHome
            "next" -> KeyAction.CycleBoard
            "none" -> KeyAction.Noop
            else -> if (text.isEmpty()) KeyAction.Noop else KeyAction.CommitText(text)
        }
        val label = when (kind) {
            "text" -> text
            "backspace" -> "⌫"
            "enter" -> "↵"
            "space" -> "␣"
            "shift" -> "⇧"
            "copy" -> "Copy"
            "paste" -> "Paste"
            "cut" -> "Cut"
            "undo" -> "Undo"
            "redo" -> "Redo"
            "selectAll" -> "All"
            "home" -> "ABC"
            "next" -> "→"
            else -> ""
        }
        return KeyC(action = action, display = KeyDisplay.TextDisplay(label), size = if (large) FontSizeVariant.LARGE else FontSizeVariant.SMALL)
    }
}

val BOARD_SLOTS = listOf("center", "topLeft", "top", "topRight", "left", "right", "bottomLeft", "bottom", "bottomRight", "hold")
val BOARD_ACTIONS = listOf("text", "none", "space", "backspace", "enter", "shift", "copy", "paste", "cut", "undo", "redo", "selectAll", "home", "next")

@Serializable
data class BoardCell(val bindings: Map<String, BoardBinding> = emptyMap()) {
    fun key(): KeyItemC = KeyItemC(
        center = (bindings["center"] ?: BoardBinding()).key(true),
        topLeft = bindings["topLeft"]?.key(), top = bindings["top"]?.key(), topRight = bindings["topRight"]?.key(),
        left = bindings["left"]?.key(), right = bindings["right"]?.key(),
        bottomLeft = bindings["bottomLeft"]?.key(), bottom = bindings["bottom"]?.key(), bottomRight = bindings["bottomRight"]?.key(),
        longPress = bindings["hold"]?.key()?.action,
    )
}

@Serializable
data class CustomBoard(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New room",
    val enabled: Boolean = true,
    val sideA: List<BoardCell> = List(9) { BoardCell() },
    val sideB: List<BoardCell> = List(9) { BoardCell() },
    val returnAfterInput: Boolean = false,
    val haptics: Boolean = true,
    val showHints: Boolean = true,
) {
    private fun side(cells: List<BoardCell>, second: Boolean): KeyboardC {
        val grid = List(9) { cells.getOrNull(it)?.key() ?: BoardCell().key() }.chunked(3)
        fun control(action: KeyAction, text: String) = KeyItemC(KeyC(action, display = KeyDisplay.TextDisplay(text), size = FontSizeVariant.LARGE))
        val navigation = control(KeyAction.ToggleNumericMode(!second), if (second) "B/A" else "A/B").copy(
            right = BoardBinding("next").key(), left = BoardBinding("home").key(), longPress = KeyAction.BoardHome,
        )
        return KeyboardC(grid + listOf(listOf(navigation, control(KeyAction.CommitText(" "), "␣"), control(KeyAction.DeleteKeyAction, "⌫").copy(top = BoardBinding("enter").key()))))
    }

    fun definition(): KeyboardDefinition {
        val a = side(sideA, false)
        val b = side(sideB, true)
        return KeyboardDefinition(name, KeyboardDefinitionModes(a, b, b, ctrled = a, alted = a), KeyboardDefinitionSettings(autoShift = false))
    }
}

object CustomBoardPreferences {
    private val json = Json { ignoreUnknownKeys = true }
    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences("keywi_custom_boards", Context.MODE_PRIVATE)
    fun load(context: Context): List<CustomBoard> = runCatching {
        json.decodeFromString<List<CustomBoard>>(prefs(context).getString("boards", "[]") ?: "[]")
    }.getOrDefault(emptyList())
    fun save(context: Context, boards: List<CustomBoard>) {
        prefs(context).edit().putString("boards", json.encodeToString(boards)).apply()
    }
    fun preset(emoji: Boolean): CustomBoard {
        val labels = if (emoji) listOf("🩷", "😊", "✨", "🐦", "😂", "🥹", "😍", "👍", "🫶") else listOf("°", "→", "∞", "×", "÷", "≠", "≤", "≥", "♡")
        return CustomBoard(name = if (emoji) "Emoji" else "Unicode", sideA = labels.map { BoardCell(mapOf("center" to BoardBinding(text = it))) })
    }
}

@Composable
fun rememberCustomBoards(): List<CustomBoard> {
    val context = androidx.compose.ui.platform.LocalContext.current
    var boards by remember(context) { mutableStateOf(CustomBoardPreferences.load(context)) }
    DisposableEffect(context) {
        val prefs = CustomBoardPreferences.prefs(context)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "boards") boards = CustomBoardPreferences.load(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return boards
}

/** Only reserve right-swipe when at least one custom room is enabled; tap remains #/ABC. */
fun KeyboardC.withBoardPaging(): KeyboardC = copy(arr = arr.map { row -> row.map { item ->
    if (item.center.action is KeyAction.ToggleNumericMode) item.copy(right = BoardBinding("next").key()) else item
} })
