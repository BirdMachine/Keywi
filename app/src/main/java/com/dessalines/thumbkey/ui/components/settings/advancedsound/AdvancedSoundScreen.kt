package com.dessalines.thumbkey.ui.components.settings.advancedsound

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsCard
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsChoice
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsSection
import com.dessalines.thumbkey.ui.components.settings.LocalAdvancedSettingsColors

@Composable
fun AdvancedSoundScreen(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(SoundMode.PLAYLIST) }

    Column(modifier = modifier) {
        AdvancedSettingsSection(
            title = "Advanced Sound",
            description = "Mix built-in clips and your own files, then decide how Keywi chooses what you hear.",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SoundMode.entries.forEach { soundMode ->
                    AdvancedSettingsChoice(
                        label = soundMode.label,
                        selected = mode == soundMode,
                        onClick = { mode = soundMode },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (mode) {
                SoundMode.PLAYLIST -> {
                    SoundPanel(
                        title = "Your sound playlist",
                        body = "Built-in + custom audio • order, loop, or random playback",
                    )
                }

                SoundMode.ACTION -> {
                    SoundPanel(
                        title = "Sounds by action",
                        body = "Letters, numbers, emoji, Unicode, Shift, Enter & tools",
                    )
                }

                SoundMode.POSITION -> {
                    SoundPanel(
                        title = "Sounds by touch position",
                        body = "4 sides + center • corners + sides + center",
                    )
                }

                SoundMode.KEY -> {
                    SoundPanel(
                        title = "Assign sounds on the keyboard",
                        body = "Per-key tap/swipe exceptions override broader rules.",
                    )
                }
            }
        }
    }
}

private enum class SoundMode(
    val label: String,
) {
    PLAYLIST("Playlist"),
    ACTION("Action"),
    POSITION("Position"),
    KEY("Key"),
}

@Composable
private fun SoundPanel(
    title: String,
    body: String,
) {
    val colors = LocalAdvancedSettingsColors.current

    AdvancedSettingsCard {
        Text(
            text = title,
            color = colors.text,
        )
        Text(
            text = body,
            color = colors.textMuted,
        )
    }
}
