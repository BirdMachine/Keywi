package com.dessalines.thumbkey.effects

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

enum class FeedbackMode(val title: String) {
    SOUND("Sound"), VIBRATE("Vibrate only"), SILENT("Silent");
    fun next() = entries[(ordinal + 1) % entries.size]
}

/** A master gate, independent of saved packs and per-board preferences. */
object FeedbackPreferences {
    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("keywi_feedback", Context.MODE_PRIVATE)
    fun get(context: Context): FeedbackMode = runCatching {
        FeedbackMode.valueOf(prefs(context).getString("mode", "SOUND")!!)
    }.getOrDefault(FeedbackMode.SOUND)
    fun save(context: Context, mode: FeedbackMode) {
        prefs(context).edit().putString("mode", mode.name).apply()
        if (mode != FeedbackMode.SOUND) ActionSoundEngine.stopAll()
    }
    fun soundAllowed(context: Context) = get(context) == FeedbackMode.SOUND
    fun hapticsAllowed(context: Context) = get(context) != FeedbackMode.SILENT
}

@Composable
fun rememberFeedbackMode(): FeedbackMode {
    val context = LocalContext.current
    var mode by remember(context) { mutableStateOf(FeedbackPreferences.get(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("keywi_feedback", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> mode = FeedbackPreferences.get(context) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return mode
}

@Composable
fun FeedbackModeControl(modifier: Modifier = Modifier, compact: Boolean = false, onChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val mode = rememberFeedbackMode()
    val cycle = { FeedbackPreferences.save(context, mode.next()); onChanged() }
    if (compact) {
        IconButton(onClick = cycle, modifier = modifier) {
            Icon(when (mode) {
                FeedbackMode.SOUND -> Icons.AutoMirrored.Outlined.VolumeUp
                FeedbackMode.VIBRATE -> Icons.Outlined.Vibration
                FeedbackMode.SILENT -> Icons.Outlined.VolumeOff
            }, contentDescription = "Feedback: "+mode.title+". Switch to "+mode.next().title)
        }
    } else {
        TextButton(onClick = cycle, modifier = modifier) { Text("Feedback: "+mode.title+" → "+mode.next().title) }
    }
}
