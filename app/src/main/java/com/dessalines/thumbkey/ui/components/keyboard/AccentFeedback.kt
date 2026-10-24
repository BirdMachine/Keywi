package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

/** Frames are evenly spaced color stops, not a rendering FPS setting. */
data class AccentFeedbackConfig(
    val durationMs: Int = 300,
    val colors: List<Color> = emptyList(),
    val trailEnabled: Boolean = false,
) {
    fun colorAt(progress: Float, fallback: Color): Color {
        val stops = colors.ifEmpty { listOf(fallback) }
        val t = progress.coerceIn(0f, 1f)
        val position = t * (stops.size - 1)
        val index = position.toInt().coerceAtMost(stops.lastIndex)
        val color = if (position == index.toFloat()) stops[index] else lerp(stops[index], stops[(index + 1).coerceAtMost(stops.lastIndex)], position - index)
        return color.copy(alpha = color.alpha * (1f - t))
    }
}

object AccentFeedbackPreferences {
    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("keywi_accent_feedback", Context.MODE_PRIVATE)
    fun get(context: Context): AccentFeedbackConfig = runCatching {
        val json = JSONObject(prefs(context).getString("config", "{}")!!)
        val colors = json.optJSONArray("colors") ?: JSONArray()
        AccentFeedbackConfig(
            json.optInt("durationMs", 300).coerceIn(0, 5000),
            (0 until colors.length().coerceAtMost(12)).map { Color(colors.getInt(it)) },
            json.optBoolean("trailEnabled", false),
        )
    }.getOrDefault(AccentFeedbackConfig())
    fun save(context: Context, config: AccentFeedbackConfig) {
        val json = JSONObject().put("durationMs", config.durationMs.coerceIn(0, 5000))
            .put("colors", JSONArray(config.colors.take(12).map { it.toArgb() }))
            .put("trailEnabled", config.trailEnabled)
        prefs(context).edit().putString("config", json.toString()).apply()
    }
}

@Composable
fun rememberAccentFeedback(): AccentFeedbackConfig {
    val context = LocalContext.current
    var config by remember(context) { mutableStateOf(AccentFeedbackPreferences.get(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("keywi_accent_feedback", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> config = AccentFeedbackPreferences.get(context) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return config
}

data class AccentTrailPoint(val position: Offset, val createdAt: Long = SystemClock.uptimeMillis())

/** Lives inside the key surface; the keyboard's GIF layer is composited above it. */
@Composable
fun KeyAccentOverlay(
    pressed: Boolean,
    modifier: Modifier,
    points: List<AccentTrailPoint> = emptyList(),
    suppressed: Boolean = false,
    trigger: Int = 0,
    onProgress: ((Float) -> Unit)? = null,
) {
    val config = rememberAccentFeedback()
    val fallback = MaterialTheme.colorScheme.tertiaryContainer
    var progress by remember { mutableFloatStateOf(1f) }
    var pulse by remember { mutableIntStateOf(0) }
    var clock by remember { mutableLongStateOf(0L) }
    LaunchedEffect(pressed, trigger, suppressed) {
        if (!suppressed && (pressed || trigger > 0)) pulse++
    }
    // Finger lift does not change this effect's keys. Each new press restarts the
    // sequence on the frame clock, independently of system transition scaling.
    LaunchedEffect(pulse, suppressed) {
        progress = 1f
        if (pulse > 0 && !suppressed && config.durationMs > 0) {
            val durationNanos = config.durationMs * 1_000_000L
            val started = withFrameNanos { it }
            progress = 0f
            do {
                withFrameNanos { frame -> progress = ((frame - started).toFloat() / durationNanos).coerceIn(0f, 1f) }
            } while (progress < 1f)
        }
    }
    LaunchedEffect(points, config, suppressed) {
        if (!suppressed && config.trailEnabled && config.durationMs > 0 && points.isNotEmpty()) {
            val until = points.last().createdAt + config.durationMs
            do { withFrameNanos { clock = SystemClock.uptimeMillis() } } while (clock < until)
        }
    }
    if (onProgress != null) {
        val sampled = progress
        SideEffect { onProgress(sampled) }
    }
    Canvas(modifier) {
        if (!suppressed && config.durationMs > 0) {
            if (progress < 1f) drawRect(config.colorAt(progress, fallback))
            if (config.trailEnabled) {
                points.zipWithNext().forEach { (from, to) ->
                    val age = ((clock - to.createdAt).coerceAtLeast(0L).toFloat() / config.durationMs).coerceIn(0f, 1f)
                    if (age < 1f) drawLine(config.colorAt(age, fallback), from.position, to.position, 5.dp.toPx(), StrokeCap.Round)
                }
            }
        }
    }
}
