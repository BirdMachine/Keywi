package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.AccentFeedbackConfig
import com.dessalines.thumbkey.ui.components.keyboard.AccentFeedbackPreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyAccentOverlay
import com.dessalines.thumbkey.ui.components.keyboard.rememberAccentFeedback
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.ColorEditor
import kotlin.math.roundToInt

@Composable
fun AccentFeedbackEditor() {
    val context = LocalContext.current
    val config = rememberAccentFeedback()
    val fallback = MaterialTheme.colorScheme.tertiaryContainer
    var preview by remember { mutableIntStateOf(0) }
    fun save(value: AccentFeedbackConfig) = AccentFeedbackPreferences.save(context, value)
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Key flash & drag trails", style = MaterialTheme.typography.titleMedium)
        Text("Frames are colors visited in order during one fade. Drag trails share the same colors and fade time.", style = MaterialTheme.typography.bodySmall)
        Text("Fade-out: ${config.durationMs} ms" + if (config.durationMs == 0) " · off" else "")
        Slider(value = config.durationMs.toFloat(), onValueChange = { save(config.copy(durationMs = (it / 25).roundToInt() * 25)) }, valueRange = 0f..5000f)
        if (config.colors.isEmpty()) Text("Using the active theme’s accent color.")
        config.colors.forEachIndexed { index, color ->
            ColorEditor("Frame ${index + 1}", color) { updated -> save(config.copy(colors = config.colors.toMutableList().also { it[index] = updated })) }
            Row {
                TextButton(enabled = index > 0, onClick = {
                    save(config.copy(colors = config.colors.toMutableList().also { val previous = it[index - 1]; it[index - 1] = it[index]; it[index] = previous }))
                }) { Text("Move earlier") }
                TextButton(onClick = { save(config.copy(colors = config.colors.filterIndexed { i, _ -> i != index })) }) { Text("Remove frame ${index + 1}") }
            }
        }
        Row {
            TextButton(enabled = config.colors.size < 12, onClick = { save(config.copy(colors = config.colors + (config.colors.lastOrNull() ?: fallback))) }) { Text("Add color frame") }
            TextButton(onClick = { save(config.copy(colors = emptyList())) }) { Text("Use theme accent") }
        }
        Row {
            Text("Drag trails", modifier = Modifier.weight(1f))
            Switch(checked = config.trailEnabled, onCheckedChange = { save(config.copy(trailEnabled = it)) })
        }
        TextButton(onClick = { preview++ }) { Text("Preview fade") }
        Box(Modifier.fillMaxWidth().height(48.dp)) {
            KeyAccentOverlay(pressed = false, trigger = preview, modifier = Modifier.matchParentSize())
        }
    }
}
