package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import com.dessalines.thumbkey.ui.components.settings.gemSettingsColors
import com.dessalines.thumbkey.ui.components.settings.gemSettingsScheme

/** Local controls only: this page never reads or writes editor text, including in private fields. */
@Composable
fun QuickSettingsWorkspace(
    modifier: Modifier = Modifier,
    onPreferencesChanged: () -> Unit = {},
    onOpenSettings: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    var suggestions by remember { mutableStateOf(SuggestionPreferences.enabled(context)) }
    var power by remember { mutableStateOf(KeywiPowerPreferences.load(context)) }
    var sounds by remember { mutableStateOf(ActionSoundPreferences.get(context).enabled) }
    var showControls by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        // The stationary cycler and other settings surfaces can change these while this page stays open.
        val preferences =
            listOf("suggestion_preferences", "keywi_action_sounds", "keywi_power_preferences")
                .map { context.getSharedPreferences(it, Context.MODE_PRIVATE) }
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                suggestions = SuggestionPreferences.enabled(context)
                sounds = ActionSoundPreferences.get(context).enabled
                power = KeywiPowerPreferences.load(context)
            }
        preferences.forEach { it.registerOnSharedPreferenceChangeListener(listener) }
        onDispose { preferences.forEach { it.unregisterOnSharedPreferenceChangeListener(listener) } }
    }

    val gif = ActionGifPreferences.current

    fun open(route: String) {
        if (onOpenSettings != null) onOpenSettings(route) else openKeywiSettings(context, route)
    }

    // IME themes can independently override text and surface colors. Own a coherent
    // control palette here, while keeping the keyboard artwork visible underneath.
    val keyboardSurface = MaterialTheme.colorScheme.surface
    val palette =
        remember(keyboardSurface) {
            val dark = keyboardSurface.luminance() < .5f
            gemSettingsScheme(dark, gemSettingsColors(dark))
        }
    MaterialTheme(colorScheme = palette) {
        CompositionLocalProvider(LocalContentColor provides palette.onSurface) {
            Surface(
                color = palette.surface.copy(alpha = .9f),
                contentColor = palette.onSurface,
                modifier = modifier.fillMaxSize(),
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Quick Settings", style = MaterialTheme.typography.titleMedium)
                    QuickSettingToggle("Suggestions", suggestions) {
                        suggestions = it
                        SuggestionPreferences.setEnabled(context, it)
                        onPreferencesChanged()
                    }
                    QuickSettingToggle("Key sounds", sounds) {
                        sounds = it
                        ActionSoundPreferences.save(context, ActionSoundPreferences.get(context).copy(enabled = it))
                        onPreferencesChanged()
                    }
                    QuickSettingToggle("Keypress images & GIFs", gif.enabled) {
                        ActionGifPreferences.save(context, ActionGifPreferences.current.copy(enabled = it))
                        if (!it) ActionGifRuntime.clear()
                        onPreferencesChanged()
                    }
                    HorizontalDivider()
                    Text("Power mode", style = MaterialTheme.typography.titleSmall)
                    KeywiPowerMode.entries.forEach { mode ->
                        val title =
                            when (mode) {
                                KeywiPowerMode.UNBRIDLED -> "Unbridled"
                                KeywiPowerMode.CONSERVED -> "Conserved"
                                KeywiPowerMode.RESTRICTED -> "Restricted"
                            }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                modifier = Modifier.semantics { contentDescription = "Power mode $title" },
                                selected = power == mode,
                                onClick = {
                                    power = mode
                                    KeywiPowerPreferences.save(context, mode)
                                    if (mode == KeywiPowerMode.RESTRICTED) ActionGifRuntime.clear()
                                    onPreferencesChanged()
                                },
                            )
                            Text(title)
                        }
                    }
                    Text(
                        when (power) {
                            KeywiPowerMode.UNBRIDLED -> "Full motion and fastest suggestions."
                            KeywiPowerMode.CONSERVED -> "Animated keyboard, with less background work."
                            KeywiPowerMode.RESTRICTED -> "Still backgrounds, fewer updates, and no keypress GIF playback."
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider()
                    TextButton(onClick = { showControls = true }) { Text("Toolbar controls") }
                    TextButton(onClick = { open("advancedLookAndFeel") }) { Text("Themes & appearance") }
                    TextButton(onClick = { open("keypressSounds") }) { Text("Manage key sounds") }
                    TextButton(onClick = { open("keypressEffects") }) { Text("Manage keypress images & GIFs") }
                    TextButton(onClick = { open("settings") }) { Text("All Keywi settings") }
                }
            }
            if (showControls) ToolbarControlsDialog(onDismiss = { showControls = false })
        }
    }
}

@Composable
private fun QuickSettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.semantics { contentDescription = title })
    }
}
