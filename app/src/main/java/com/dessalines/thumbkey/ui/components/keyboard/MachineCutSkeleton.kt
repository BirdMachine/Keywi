package com.dessalines.thumbkey.ui.components.keyboard

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.IMEService

/**
 * HK-Like: compact touch keyboard with an optional desktop-control layer.
 * Shares the ambient Keywi theme rather than introducing a separate palette.
 */
@Composable
fun MachineCutSkeleton(
    modifier: Modifier = Modifier,
    layout: MachineCutLayout = MachineCutLayouts.qwertySkeleton,
) {
    val ime = LocalContext.current as IMEService
    var advanced by remember { mutableStateOf(false) }
    var shifted by remember { mutableStateOf(false) }
    var symbols by remember { mutableStateOf(false) }
    var ctrl by remember { mutableStateOf(false) }
    var alt by remember { mutableStateOf(false) }
    var momentaryAdvanced by remember { mutableStateOf(false) }

    val basic = listOf(
        "1234567890".map { it.toString() },
        "qwertyuiop".map { it.toString() },
        "asdfghjkl".map { it.toString() },
        listOf("⇧") + "zxcvbnm".map { it.toString() } + listOf("⌫"),
        listOf("?123", ",", "☺", "Space", ".", "↵", "⌘"),
    )
    val symbolRows = listOf(
        "1234567890".map { it.toString() },
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?", "=", "_"),
        listOf("ABC", "[", "]", "{", "}", "<", ">", "⌫"),
        listOf("ABC", ",", "☺", "Space", ".", "↵", "⌘"),
    )
    val advancedRows = listOf(
        (1..12).map { "F$it" },
        listOf("Esc", "Tab", "Ctrl", "Alt", "Win", "Del", "Home", "End"),
        listOf("PgUp", "PgDn", "Ins", "↑", "←", "↓", "→", "⌫"),
        listOf("⇧") + "qwertyuiop".map { it.toString() },
        listOf("ABC", "☺", "Space", "↵", "⌘"),
    )
    val rows = if (advanced || momentaryAdvanced) advancedRows else if (symbols) symbolRows else basic

    fun keyCode(code: Int) {
        val connection = ime.currentInputConnection ?: return
        val meta = (if (ctrl) KeyEvent.META_CTRL_ON else 0) or (if (alt) KeyEvent.META_ALT_ON else 0)
        connection.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, code, 0, meta))
        connection.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_UP, code, 0, meta))
        ctrl = false
        alt = false
    }
    fun press(label: String) {
        val connection = ime.currentInputConnection ?: return
        when (label) {
            "⌘" -> { advanced = !advanced; momentaryAdvanced = false }
            "?123" -> symbols = true
            "ABC" -> { symbols = false; advanced = false }
            "⇧" -> shifted = !shifted
            "Ctrl" -> ctrl = !ctrl
            "Alt" -> alt = !alt
            "⌫" -> keyCode(KeyEvent.KEYCODE_DEL)
            "Del" -> keyCode(KeyEvent.KEYCODE_FORWARD_DEL)
            "↵" -> keyCode(KeyEvent.KEYCODE_ENTER)
            "Tab" -> keyCode(KeyEvent.KEYCODE_TAB)
            "Esc" -> keyCode(KeyEvent.KEYCODE_ESCAPE)
            "Win" -> keyCode(KeyEvent.KEYCODE_META_LEFT)
            "Home" -> keyCode(KeyEvent.KEYCODE_MOVE_HOME)
            "End" -> keyCode(KeyEvent.KEYCODE_MOVE_END)
            "PgUp" -> keyCode(KeyEvent.KEYCODE_PAGE_UP)
            "PgDn" -> keyCode(KeyEvent.KEYCODE_PAGE_DOWN)
            "Ins" -> keyCode(KeyEvent.KEYCODE_INSERT)
            "↑" -> keyCode(KeyEvent.KEYCODE_DPAD_UP)
            "↓" -> keyCode(KeyEvent.KEYCODE_DPAD_DOWN)
            "←" -> keyCode(KeyEvent.KEYCODE_DPAD_LEFT)
            "→" -> keyCode(KeyEvent.KEYCODE_DPAD_RIGHT)
            "☺" -> { connection.commitText("☺", 1) }
            "Space" -> connection.commitText(" ", 1)
            else -> {
                if (label.startsWith("F") && label.drop(1).toIntOrNull() in 1..12) {
                    keyCode(KeyEvent.KEYCODE_F1 + label.drop(1).toInt() - 1)
                } else {
                    val output = if (shifted) label.uppercase() else label
                    if (ctrl || alt) {
                        val code = KeyEvent.keyCodeFromString("KEYCODE_${label.uppercase()}")
                        if (code != KeyEvent.KEYCODE_UNKNOWN) keyCode(code) else connection.commitText(output, 1)
                    } else connection.commitText(output, 1)
                    shifted = false
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                row.forEach { label ->
                    val width = when (label) {
                        "Space" -> 4.4f
                        "⇧", "⌫", "↵", "⌘", "?123", "ABC" -> 1.5f
                        else -> 1f
                    }
                    HkKey(label, width, index == rows.lastIndex, { press(label) }) {
                        if (label == "⌘") momentaryAdvanced = !momentaryAdvanced
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.HkKey(label: String, width: Float, bottomRow: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(width)
            .height(if (bottomRow) 48.dp else 50.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f), RoundedCornerShape(5.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}
