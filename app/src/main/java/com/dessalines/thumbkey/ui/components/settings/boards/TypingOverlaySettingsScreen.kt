package com.dessalines.thumbkey.ui.components.settings.boards

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.TypingOverlayLayer
import com.dessalines.thumbkey.ui.components.keyboard.TypingOverlayPreferences
import com.dessalines.thumbkey.ui.components.keyboard.TypingOverlayState
import com.dessalines.thumbkey.ui.components.keyboard.loadTypingOverlayMedia
import com.dessalines.thumbkey.utils.SimpleTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingOverlaySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    remember(context) { TypingOverlayPreferences.load(context) }
    val state = TypingOverlayPreferences.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val pulse = remember { mutableLongStateOf(0L) }
    fun save(next: TypingOverlayState) = TypingOverlayPreferences.save(context, next)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            loading = true
            error = null
            try {
                loadTypingOverlayMedia(context.applicationContext, uri.toString())
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                save(TypingOverlayPreferences.current.copy(uri = uri.toString(), enabled = true))
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled
            } catch (failure: Exception) { error = failure.message ?: "Unable to open this image."
            } finally { loading = false }
        }
    }
    Scaffold(topBar = { SimpleTopAppBar("On-type GIF / PNG overlay", navController) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("A touch-through image over the keyboard. Typed keys trigger one overlay. Keys during the cooldown are skipped, never queued. PNGs flash and fade; GIFs play once.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Enable typing overlay", Modifier.weight(1f).padding(top = 12.dp))
                Switch(state.enabled, { save(state.copy(enabled = it)) }, enabled = state.uri != null)
            }
            Button(enabled = !loading, onClick = { picker.launch(arrayOf("image/gif", "image/png")) }) { Text(if (loading) "Checking image…" else "Choose GIF / PNG") }
            Text("Up to 8 MB and 2048 × 2048 pixels. Small transparent images work best. Restricted power mode disables overlays.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text(if (state.cooldownMs == 0) "Trigger cooldown: off (every key)" else "Trigger cooldown: ${state.cooldownMs} ms")
            Slider(state.cooldownMs.toFloat(), { save(state.copy(cooldownMs = (it / 50).toInt() * 50)) }, valueRange = 0f..3000f, steps = 59)
            Text("Minimum time between animation starts. Increase this if fast typing feels slow. 0 triggers on every key.", style = MaterialTheme.typography.bodySmall)
            Text("Display duration: ${state.durationMs} ms")
            Slider(state.durationMs.toFloat(), { save(state.copy(durationMs = it.toInt())) }, valueRange = 100f..3000f)
            Text("Opacity: ${(state.opacity * 100).toInt()}%")
            Slider(state.opacity, { save(state.copy(opacity = it)) }, valueRange = 0f..1f)
            Text("Size: ${(state.size * 100).toInt()}%")
            Slider(state.size, { save(state.copy(size = it)) }, valueRange = 0.1f..1f)
            OutlinedButton(enabled = state.uri != null, onClick = { pulse.longValue++ }) { Text("Preview keypress") }
            Box(Modifier.fillMaxWidth().height(180.dp)) {
                TypingOverlayLayer(pulse, state.copy(enabled = true), Modifier.matchParentSize())
            }
            TextButton(enabled = state.uri != null, onClick = { save(state.copy(enabled = false, uri = null)) }) { Text("Remove overlay") }
        }
    }
}
