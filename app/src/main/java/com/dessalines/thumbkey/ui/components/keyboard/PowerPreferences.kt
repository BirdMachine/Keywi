package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val POWER_PREFS = "keywi_power_preferences"
private const val POWER_MODE = "mode"

enum class KeywiPowerMode {
    UNBRIDLED,
    CONSERVED,
    RESTRICTED,
    DESPERATE,
}

object KeywiPowerPreferences {
    var currentMode by mutableStateOf(KeywiPowerMode.CONSERVED)
        private set
    val advancedThemingEnabled get() = currentMode != KeywiPowerMode.DESPERATE
    fun load(context: Context): KeywiPowerMode {
        val stored =
            context
                .getSharedPreferences(POWER_PREFS, Context.MODE_PRIVATE)
                .getString(POWER_MODE, KeywiPowerMode.CONSERVED.name)
        currentMode = runCatching { KeywiPowerMode.valueOf(stored!!) }.getOrDefault(KeywiPowerMode.CONSERVED)
        return currentMode
    }

    fun save(
        context: Context,
        mode: KeywiPowerMode,
    ) {
        currentMode = mode
        if (mode == KeywiPowerMode.RESTRICTED || mode == KeywiPowerMode.DESPERATE) ActionGifRuntime.clear()
        context
            .getSharedPreferences(POWER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(POWER_MODE, mode.name)
            .apply()
    }

    fun animationsEnabled(context: Context): Boolean = load(context) in setOf(KeywiPowerMode.UNBRIDLED, KeywiPowerMode.CONSERVED)

    fun gifsEnabled(context: Context): Boolean = load(context) in setOf(KeywiPowerMode.UNBRIDLED, KeywiPowerMode.CONSERVED)

    fun suggestionPollMillis(context: Context): Long =
        when (load(context)) {
            KeywiPowerMode.UNBRIDLED -> 80L
            KeywiPowerMode.CONSERVED -> 180L
            KeywiPowerMode.RESTRICTED -> 350L
            KeywiPowerMode.DESPERATE -> 500L
        }
}

@Composable
fun rememberKeywiPowerMode(): KeywiPowerMode {
    val context = LocalContext.current
    var mode by remember(context) { mutableStateOf(KeywiPowerPreferences.load(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences(POWER_PREFS, Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            mode = KeywiPowerPreferences.load(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return mode
}
