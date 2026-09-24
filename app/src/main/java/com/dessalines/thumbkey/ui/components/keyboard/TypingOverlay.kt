package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import android.graphics.Paint
import android.net.Uri
import android.os.SystemClock
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

private const val OVERLAY_PREFS = "keywi_typing_overlay"
private const val MAX_MEDIA_BYTES = 8 * 1024 * 1024

data class TypingOverlayState(
    val enabled: Boolean = false,
    val uri: String? = null,
    val durationMs: Int = 650,
    val cooldownMs: Int = 650,
    val opacity: Float = 0.8f,
    val size: Float = 0.65f,
)

object TypingOverlayPreferences {
    var current by mutableStateOf(TypingOverlayState())
        private set
    fun load(context: Context): TypingOverlayState {
        val prefs = context.getSharedPreferences(OVERLAY_PREFS, Context.MODE_PRIVATE)
        current = TypingOverlayState(
            enabled = prefs.getBoolean("enabled", false), uri = prefs.getString("uri", null),
            durationMs = prefs.getInt("duration", 650).coerceIn(100, 3000),
            cooldownMs = prefs.getInt("cooldown", 650).coerceIn(0, 3000),
            opacity = prefs.getFloat("opacity", 0.8f).coerceIn(0f, 1f),
            size = prefs.getFloat("size", 0.65f).coerceIn(0.1f, 1f),
        )
        return current
    }
    fun save(context: Context, state: TypingOverlayState) {
        context.getSharedPreferences(OVERLAY_PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", state.enabled).putString("uri", state.uri).putInt("duration", state.durationMs)
            .putInt("cooldown", state.cooldownMs.coerceIn(0, 3000))
            .putFloat("opacity", state.opacity).putFloat("size", state.size).apply()
        current = state
    }
}

/** Decoded once per source, off the input/UI thread. One instance plays regardless of typing rate. */
@Suppress("DEPRECATION")
class TypingOverlayMedia(val bitmap: Bitmap? = null, val movie: Movie? = null) {
    val width: Int get() = bitmap?.width ?: movie!!.width()
    val height: Int get() = bitmap?.height ?: movie!!.height()
}

@Suppress("DEPRECATION")
suspend fun loadTypingOverlayMedia(context: Context, uri: String): TypingOverlayMedia = withContext(Dispatchers.IO) {
    val bytes = context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
        val out = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
            val count = input.read(chunk)
            if (count < 0) break
            require(out.size() + count <= MAX_MEDIA_BYTES) { "Choose an image smaller than 8 MB." }
            out.write(chunk, 0, count)
        }
        out.toByteArray()
    } ?: error("The selected image is no longer available. Choose it again.")
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    require(bounds.outWidth in 1..2048 && bounds.outHeight in 1..2048) { "Choose an image up to 2048 × 2048 pixels." }
    require(bounds.outMimeType == "image/gif" || bounds.outMimeType == "image/png") { "Choose a GIF or PNG image." }
    if (bounds.outMimeType == "image/gif") {
        val movie = Movie.decodeByteArray(bytes, 0, bytes.size) ?: error("This GIF could not be decoded.")
        TypingOverlayMedia(movie = movie)
    } else {
        val opts = BitmapFactory.Options().apply { inSampleSize = if (maxOf(bounds.outWidth, bounds.outHeight) > 1024) 2 else 1 }
        TypingOverlayMedia(bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: error("This PNG could not be decoded."))
    }
}

@Suppress("DEPRECATION")
private class TypingOverlayView(context: Context) : View(context) {
    var media: TypingOverlayMedia? = null
        set(value) {
            if (value == null || value !== failedMedia) field = value
        }
    private var failedMedia: TypingOverlayMedia? = null
    var options = TypingOverlayState()
    var frameMillis = 33L
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var started = -1L
    private var lastPulse = 0L
    private val triggerGate = OverlayTriggerGate()
    init {
        // Movie's GIF renderer requires a software canvas on some Android versions.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable = false
        isFocusable = false
    }
    fun pulse(value: Long) {
        if (value != lastPulse) {
            lastPulse = value
            val now = SystemClock.uptimeMillis()
            if (media != null && triggerGate.accept(now, options.cooldownMs)) {
                started = now
                invalidate()
            }
        }
    }
    fun stop() { started = -1L; invalidate() }
    override fun onDetachedFromWindow() { stop(); super.onDetachedFromWindow() }
    override fun onWindowVisibilityChanged(visibility: Int) { super.onWindowVisibilityChanged(visibility); if (visibility != VISIBLE) stop() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val image = media ?: return
        if (started < 0 || !isShown) return
        val elapsed = SystemClock.uptimeMillis() - started
        if (elapsed >= options.durationMs) { started = -1L; return }
        val scale = min(width.toFloat() / image.width, height.toFloat() / image.height) * options.size
        val fade = min(1f, (options.durationMs - elapsed) / 120f)
        paint.alpha = (255 * options.opacity * fade).toInt()
        val checkpoint = canvas.save()
        try {
        canvas.translate((width - image.width * scale) / 2f, (height - image.height * scale) / 2f)
        canvas.scale(scale, scale)
        image.bitmap?.let { canvas.drawBitmap(it, 0f, 0f, paint) }
        image.movie?.let {
            // Play once; keep the last frame until the configured display duration ends.
            it.setTime(elapsed.coerceAtMost((it.duration().takeIf { n -> n > 0 } ?: 1000) - 1L).toInt())
            it.draw(canvas, 0f, 0f, paint)
        }
        } catch (failure: RuntimeException) {
            // A broken optional effect must not close the keyboard.
            android.util.Log.e("KeywiOverlay", "Overlay drawing failed; disabling this media instance", failure)
            failedMedia = image
            media = null
            started = -1L
            return
        } finally {
            canvas.restoreToCount(checkpoint)
        }
        // Static PNGs need no redraw until their fade begins.
        val delay = if (image.bitmap != null && elapsed < options.durationMs - 120) {
            options.durationMs - 120L - elapsed
        } else frameMillis
        postInvalidateDelayed(delay)
    }
}

@Composable
fun TypingOverlayLayer(pulse: State<Long>, state: TypingOverlayState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val power = KeywiPowerPreferences.load(context)
    if (!state.enabled || state.uri == null || power == KeywiPowerMode.RESTRICTED) return
    val media by produceState<TypingOverlayMedia?>(null, state.uri) {
        value = null
        value = try { loadTypingOverlayMedia(context.applicationContext, state.uri) } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) { null }
    }
    val decoded = media ?: return
    AndroidView(
        factory = { TypingOverlayView(it) }, modifier = modifier,
        update = { view ->
            view.media = decoded
            view.options = state
            view.frameMillis = if (power == KeywiPowerMode.UNBRIDLED) 16L else 33L
            view.pulse(pulse.value)
        },
        onReset = null,
        onRelease = { it.stop(); it.media = null },
    )
}
