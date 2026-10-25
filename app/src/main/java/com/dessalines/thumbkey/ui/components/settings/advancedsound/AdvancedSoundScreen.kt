package com.dessalines.thumbkey.ui.components.settings.advancedsound

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.ActionSoundConfig
import com.dessalines.thumbkey.effects.ActionSoundEngine
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import com.dessalines.thumbkey.effects.KeySoundSource
import com.dessalines.thumbkey.effects.source
import com.dessalines.thumbkey.effects.withSource
import com.dessalines.thumbkey.effects.NoteScale
import com.dessalines.thumbkey.effects.ScaleToneEngine
import com.dessalines.thumbkey.effects.noteRoots
import com.dessalines.thumbkey.effects.SoundClip
import com.dessalines.thumbkey.effects.SoundSelection
import com.dessalines.thumbkey.effects.SoundTrigger
import com.dessalines.thumbkey.effects.soundDirections
import com.dessalines.thumbkey.effects.starterSounds
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsCard
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsChoice
import com.dessalines.thumbkey.ui.components.settings.AdvancedSettingsSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdvancedSoundScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(ActionSoundPreferences.get(context)) }
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf<String?>(null) }
    var editingClip by remember { mutableStateOf<SoundClip?>(null) }
    var rename by remember { mutableStateOf("") }

    fun update(value: ActionSoundConfig) {
        ActionSoundPreferences.save(context, value)
        config = ActionSoundPreferences.get(context)
    }
    val files =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            scope.launch {
                val imported =
                    withContext(Dispatchers.IO) {
                        uris.map { uri ->
                            runCatching { ActionSoundPreferences.import(context, uri) }
                        }
                    }
                val added = imported.mapNotNull { it.getOrNull() }
                update(
                    config.copy(clips = config.clips + added, selectedId = config.selectedId.ifEmpty { added.firstOrNull()?.id.orEmpty() }),
                )
                feedback =
                    "${imported.count { it.isSuccess }} sounds copied into Keywi" +
                    if (imported.any { it.isFailure }) "; some files could not be imported (25 MB maximum each)." else "."
            }
        }
    val ringtone =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                @Suppress("DEPRECATION")
                val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    val title =
                        runCatching { RingtoneManager.getRingtone(context, uri)?.getTitle(context) }.getOrNull() ?: "Android notification"
                    scope.launch {
                        val imported =
                            withContext(Dispatchers.IO) { runCatching { ActionSoundPreferences.import(context, uri).copy(name = title) } }
                        imported
                            .onSuccess { clip ->
                                update(config.copy(clips = config.clips + clip, selectedId = config.selectedId.ifEmpty { clip.id }))
                            }.onFailure { feedback = "Unable to copy this Android sound. Try importing an audio file." }
                    }
                }
            }
        }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdvancedSettingsSection("Action sounds", "Build a named sound pack, then map its clips to your keys and gestures.") {
            Text("Key sound source", style = MaterialTheme.typography.titleMedium)
            KeySoundSource.entries.forEach { source ->
                AdvancedSettingsChoice(source.title, config.source == source, {
                    ActionSoundEngine.stopAll()
                    update(config.withSource(source))
                })
            }
            Text("Choose one source. Generated scale is a built-in instrument; your imported pack stays saved when you switch.", style = MaterialTheme.typography.bodySmall)
        }
        AdvancedSettingsCard {
            Text("Soft scale notes", style = MaterialTheme.typography.titleMedium)
            Text(
                "A quiet 170 ms tone, randomly chosen from your scale. Replaces default key clicks and key sound clips. " +
                    "Sound / Vibrate / Silent still applies; suggestion sounds keep their own assignments.",
                style = MaterialTheme.typography.bodySmall,
            )
            var rootExpanded by remember { mutableStateOf(false) }
            var scaleExpanded by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    TextButton(onClick = { rootExpanded = true }) { Text("Root: ${noteRoots[config.toneRoot]}") }
                    DropdownMenu(expanded = rootExpanded, onDismissRequest = { rootExpanded = false }) {
                        noteRoots.forEachIndexed { index, label ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                update(config.copy(toneRoot = index)); rootExpanded = false
                            })
                        }
                    }
                }
                Box {
                    TextButton(onClick = { scaleExpanded = true }) { Text(config.toneScale.title) }
                    DropdownMenu(expanded = scaleExpanded, onDismissRequest = { scaleExpanded = false }) {
                        NoteScale.entries.forEach { scale ->
                            DropdownMenuItem(text = { Text(scale.title) }, onClick = {
                                update(config.copy(toneScale = scale)); scaleExpanded = false
                            })
                        }
                    }
                }
            }
            Text("Tone level · ${(config.toneVolume * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            Slider(config.toneVolume, { update(config.copy(toneVolume = it)) })
            TextButton(onClick = { ScaleToneEngine.fire(context, preview = true) }) { Text("Preview a note") }
        }
        AdvancedSettingsCard {
            Text("Sound pack · ${config.clips.size} clips", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { files.launch(arrayOf("audio/*")) }) { Text("Import files") }
                TextButton(onClick = {
                    runCatching {
                        ringtone.launch(
                            Intent(
                                RingtoneManager.ACTION_RINGTONE_PICKER,
                            ).putExtra(
                                RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_NOTIFICATION,
                            ).putExtra(
                                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                                false,
                            ).putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Add a notification sound"),
                        )
                    }.onFailure { feedback = "This Android installation has no notification sound picker. Import an audio file instead." }
                }) { Text("Android sounds") }
            }
            Text(
                "Imported audio is stored inside Keywi. Tap a clip to rename it; ▶ previews it.",
                style = MaterialTheme.typography.bodySmall,
            )
            config.clips.forEachIndexed { index, clip ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = {
                        editingClip = clip
                        rename = clip.name
                    }, modifier = Modifier.weight(1f)) { Text("${index + 1}. ${clip.name}") }
                    TextButton(onClick = { ActionSoundEngine.preview(context, clip) }) { Text("▶") }
                    TextButton(onClick = {
                        config = ActionSoundPreferences.remove(context, clip.id)
                    }) { Text("×") }
                }
            }
            TextButton(onClick = {
                update(
                    config.copy(
                        clips =
                            config.clips +
                                starterSounds.filter { starter ->
                                    config.clips.none { it.id == starter.id }
                                },
                        selectedId = config.selectedId.ifEmpty { starterSounds.first().id },
                    ),
                )
            }) { Text("Restore subtle starter clips") }
            feedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        AdvancedSettingsCard {
            Text("Choose playback", style = MaterialTheme.typography.titleMedium)
            SoundSelection.entries.chunked(2).forEach { modes ->
                Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    modes.forEach { mode ->
                        AdvancedSettingsChoice(
                            mode.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            },
                            config.selection == mode,
                            { update(config.copy(selection = mode)) },
                            Modifier.weight(1f),
                        )
                    }
                }
            }
            when (config.selection) {
                SoundSelection.SINGLE -> {
                    ClipPicker("One sound", config.selectedId, config.clips) { id ->
                        update(
                            config.copy(
                                selectedId =
                                    id ?: "",
                            ),
                        )
                    }
                }

                SoundSelection.ORDERED -> {
                    Text("Cycles through the numbered pack for each action, regardless of key or direction.")
                }

                SoundSelection.RANDOM -> {
                    Text("Chooses a fresh random clip for each action.")
                }

                SoundSelection.DIRECTION -> {
                    soundDirections.forEach { direction ->
                        ClipPicker(direction.lowercase().replace('_', ' '), config.directions[direction], config.clips) { id ->
                            update(
                                config.copy(
                                    directions =
                                        config.directions.toMutableMap().apply {
                                            if (id ==
                                                null
                                            ) {
                                                remove(direction)
                                            } else {
                                                put(direction, id)
                                            }
                                        },
                                ),
                            )
                        }
                    }
                }
            }
            Text("Per-key assignments in the matrix override these rules.", style = MaterialTheme.typography.bodySmall)
        }
        AdvancedSettingsCard {
            Text("Timing & tails", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoundTrigger.entries.forEach { phase ->
                    AdvancedSettingsChoice(
                        phase.name.lowercase().replaceFirstChar { it.uppercase() },
                        config.trigger == phase,
                        { update(config.copy(trigger = phase)) },
                    )
                }
            }
            Text(
                "Press plays when the gesture resolves; release plays when that action is released. Swipes must resolve before their direction is known.",
                style = MaterialTheme.typography.bodySmall,
            )
            SoundToggle("Overlap clips / let tails stack", config.overlap) { update(config.copy(overlap = it)) }
            SoundToggle("Limit playback duration", config.maxDurationMs > 0) { update(config.copy(maxDurationMs = if (it) 50 else 0)) }
            if (config.maxDurationMs > 0) {
                Text("Cut off after ${config.maxDurationMs} ms (${config.maxDurationMs / 1000f} sec)")
                Slider(config.maxDurationMs.toFloat(), { update(config.copy(maxDurationMs = it.toInt())) }, valueRange = 10f..2000f)
            } else {
                Text("Each clip plays to completion.", style = MaterialTheme.typography.bodySmall)
            }
            Text("Volume · ${(config.volume * 100).toInt()}%")
            Slider(config.volume, { update(config.copy(volume = it)) })
        }
        AdvancedSettingsCard {
            Text("Suggestion strip", style = MaterialTheme.typography.titleMedium)
            ClipPicker("Scroll / slide", config.suggestionScrollId, config.clips) { update(config.copy(suggestionScrollId = it)) }
            ClipPicker("Select a suggestion", config.suggestionSelectedId, config.clips) { update(config.copy(suggestionSelectedId = it)) }
            Text(
                "Scroll sounds are spaced at least 65 ms apart. Try Suggestion brush for a gentle slide.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    editingClip?.let { clip ->
        AlertDialog(onDismissRequest = { editingClip = null }, title = { Text("Name this sound") }, text = {
            OutlinedTextField(rename, { rename = it }, singleLine = true, label = { Text("Sound name") })
        }, confirmButton = {
            TextButton(onClick = {
                update(
                    config.copy(
                        clips =
                            config.clips.map {
                                if (it.id ==
                                    clip.id
                                ) {
                                    it.copy(name = rename.trim().ifEmpty { "Sound" })
                                } else {
                                    it
                                }
                            },
                    ),
                )
                ; editingClip = null
            }) { Text("Save") }
        }, dismissButton = {
            TextButton(onClick = {
                editingClip =
                    null
            }) { Text("Cancel") }
        })
    }
}

@Composable
private fun SoundToggle(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f).padding(top = 12.dp))
        Switch(value, onChange)
    }
}

@Composable
private fun ClipPicker(
    label: String,
    selected: String?,
    clips: List<SoundClip>,
    onSelect: (String?) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = { open = true }) {
            val index = clips.indexOfFirst { it.id == selected }
            Text("$label · " + if (index >= 0) "${index + 1}. ${clips[index].name}" else "Off / unassigned")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Off / unassigned") }, onClick = {
                onSelect(null)
                open = false
            })
            clips.forEachIndexed { index, clip ->
                DropdownMenuItem(text = { Text("${index + 1}. ${clip.name}") }, onClick = {
                    onSelect(clip.id)
                    open =
                        false
                })
            }
        }
    }
}
