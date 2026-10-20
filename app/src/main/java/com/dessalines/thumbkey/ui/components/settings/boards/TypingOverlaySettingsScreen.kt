package com.dessalines.thumbkey.ui.components.settings.boards

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.ActionGifOverlayLayer
import com.dessalines.thumbkey.ui.components.keyboard.ActionGifPreferences
import com.dessalines.thumbkey.ui.components.keyboard.ActionGifRuntime
import com.dessalines.thumbkey.ui.components.keyboard.ActionGifState
import com.dessalines.thumbkey.ui.components.settings.GemButton
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTopAppBar
import kotlinx.coroutines.launch

private val effectDirections =
    listOf("CENTER", "TOP_LEFT", "TOP", "TOP_RIGHT", "LEFT", "RIGHT", "BOTTOM_LEFT", "BOTTOM", "BOTTOM_RIGHT", "HOLD")

@Composable
private fun GifChoice(
    label: String,
    value: String,
    choices: List<Pair<String, String>>,
    onChoose: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { open = true }) { Text(choices.firstOrNull { it.first == value }?.second ?: value) }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                choices.forEach { (id, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = {
                        onChoose(id)
                        open = false
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingOverlaySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    remember(context) { ActionGifPreferences.load(context) }
    val state = ActionGifPreferences.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var targetKey by remember { mutableStateOf("key0_0") }
    var targetDirection by remember { mutableStateOf("CENTER") }

    fun save(next: ActionGifState) = ActionGifPreferences.save(context, next)
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                scope.launch {
                    loading = true
                    error = null
                    var importedCount = 0
                    try {
                        for (uri in uris.take(32)) {
                            val name =
                                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                                    if (it.moveToFirst()) it.getString(0) else null
                                } ?: "Overlay ${ActionGifPreferences.current.assets.size + 1}"
                            val asset = ActionGifPreferences.importAsset(context.applicationContext, uri, name)
                            val latest = ActionGifPreferences.current
                            save(latest.copy(assets = latest.assets + asset, selectedId = latest.selectedId ?: asset.id, enabled = true))
                            importedCount++
                        }
                    } catch (
                        cancelled: kotlinx.coroutines.CancellationException,
                    ) {
                        throw cancelled
                    } catch (
                        failure: Exception,
                    ) {
                        error = "$importedCount imported. ${failure.message ?: "Unable to open this image."}"
                    } finally {
                        loading = false
                    }
                }
            }
        }
    val assetChoices =
        listOf("" to "Default overlay") + state.assets.mapIndexed { index, asset -> asset.id to "${index + 1}. ${asset.name}" }
    Scaffold(topBar = { GemSettingsTopAppBar("Keypress effects", navController) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Action artwork", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Build a pack of GIFs and PNGs, then give taps, swipes and holds their own little burst. Effects overlap, and never intercept your typing.",
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Enable action overlays", Modifier.weight(1f).padding(top = 12.dp))
                Switch(state.enabled, { save(state.copy(enabled = it)) }, enabled = state.assets.isNotEmpty())
            }
            GemButton(enabled = !loading, onClick = {
                picker.launch(arrayOf("image/gif", "image/png"))
            }) { Text(if (loading) "Importing artwork…" else "Add GIFs / PNGs") }
            Text(
                "Copied into Keywi so your pack survives moving the original files. Each image: up to 8 MB, 2048 × 2048. Restricted mode disables playback.",
                style = MaterialTheme.typography.bodySmall,
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.assets.forEachIndexed { index, asset ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${index + 1}. ${asset.name}", Modifier.weight(1f).padding(top = 12.dp))
                    TextButton(onClick = {
                        save(state.copy(selectedId = asset.id))
                        ActionGifRuntime.trigger(context, preview = true)
                    }) { Text("Play") }
                    TextButton(onClick = { ActionGifPreferences.remove(context, asset.id) }) { Text("Remove") }
                }
            }
            GifChoice(
                "Playback",
                state.mode,
                listOf(
                    "single" to "One image",
                    "sequence" to "In pack order",
                    "random" to "At random",
                    "direction" to "By gesture direction",
                    "key" to "By key + direction",
                ),
            ) { save(state.copy(mode = it)) }
            GifChoice(
                "Default image",
                state.selectedId ?: "",
                assetChoices,
            ) { save(state.copy(selectedId = it.takeIf(String::isNotEmpty))) }
            Text("Saved key and direction assignments take priority over pack playback order.", style = MaterialTheme.typography.bodySmall)
            if (state.mode == "direction" || state.mode == "key") {
                Text("Direction defaults", style = MaterialTheme.typography.titleMedium)
                effectDirections.forEach { direction ->
                    GifChoice(
                        direction.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                        state.directionAssignments[direction] ?: "",
                        assetChoices,
                    ) { id ->
                        save(
                            state.copy(
                                directionAssignments =
                                    if (id.isEmpty()) {
                                        state.directionAssignments - direction
                                    } else {
                                        state.directionAssignments +
                                            (direction to id)
                                    },
                            ),
                        )
                    }
                }
            }
            if (state.mode == "key") {
                Text("Per-key assignment", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Choose a matrix position, then its gesture and artwork. The matrix editor uses the same key IDs. Per-key rules override direction defaults.",
                    style = MaterialTheme.typography.bodySmall,
                )
                for (row in 0..3) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0..4) {
                            FilterChip(selected = targetKey == "key${row}_$col", onClick = {
                                targetKey = "key${row}_$col"
                            }, label = { Text("${row + 1}·${col + 1}") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                OutlinedTextField(value = targetKey, onValueChange = {
                    targetKey = it
                }, label = { Text("Matrix key ID (HK: hk0_0)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                GifChoice(
                    "Gesture for this key",
                    targetDirection,
                    listOf("ALL" to "Every gesture") + effectDirections.map { it to it.replace('_', ' ').lowercase() },
                ) {
                    targetDirection =
                        it
                }
                val assignmentKey = if (targetDirection == "ALL") targetKey else "$targetKey:$targetDirection"
                GifChoice("Assigned image", state.keyAssignments[assignmentKey] ?: "", assetChoices) { id ->
                    save(
                        state.copy(
                            keyAssignments =
                                if (id.isEmpty()) {
                                    state.keyAssignments - assignmentKey
                                } else {
                                    state.keyAssignments +
                                        (assignmentKey to id)
                                },
                        ),
                    )
                }
                if (state.keyAssignments.isNotEmpty()) {
                    Text(
                        "${state.keyAssignments.size} key rules saved",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            HorizontalDivider()
            GifChoice(
                "Origin",
                if (state.atKey) "key" else "center",
                listOf("key" to "At the activated key", "center" to "Keyboard center"),
            ) {
                save(
                    state.copy(
                        atKey =
                            it == "key",
                    ),
                )
            }
            GifChoice(
                "Layer",
                if (state.aboveKeys) "above" else "below",
                listOf(
                    "above" to "Above keys • touch-through",
                    "below" to "Below keys",
                ),
            ) { save(state.copy(aboveKeys = it == "above")) }
            GifChoice(
                "Trigger timing",
                state.phase,
                listOf("press" to "On action / tap", "release" to "On release"),
            ) { save(state.copy(phase = it)) }
            Text("Display duration: ${state.durationMs} ms")
            Slider(state.durationMs.toFloat(), { save(state.copy(durationMs = it.toInt())) }, valueRange = 50f..5000f)
            Text("Minimum interval: ${state.cooldownMs} ms")
            Slider(state.cooldownMs.toFloat(), { save(state.copy(cooldownMs = it.toInt())) }, valueRange = 0f..3000f)
            Text("Overlapping effects: ${state.maxOverlays}")
            Slider(state.maxOverlays.toFloat(), { save(state.copy(maxOverlays = it.toInt())) }, valueRange = 1f..8f, steps = 6)
            Text("Opacity: ${(state.opacity * 100).toInt()}%")
            Slider(state.opacity, { save(state.copy(opacity = it)) }, valueRange = 0f..1f)
            Text("Image size: ${(state.size * 100).toInt()}% of keyboard")
            Slider(state.size, { save(state.copy(size = it)) }, valueRange = .1f..1f)
            OutlinedButton(enabled = state.assets.isNotEmpty(), onClick = {
                ActionGifRuntime.trigger(context, keyId = targetKey, direction = targetDirection, preview = true)
            }) { Text("Preview assigned effect") }
            Box(Modifier.fillMaxWidth().height(180.dp)) {
                ActionGifOverlayLayer(aboveKeys = state.aboveKeys, modifier = Modifier.matchParentSize(), preview = true)
            }
        }
    }
}
