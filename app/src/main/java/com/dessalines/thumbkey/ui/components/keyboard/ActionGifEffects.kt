package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import kotlin.math.min

data class ActionGifAsset(
    val id: String,
    val name: String,
    val uri: String,
)

data class ActionGifState(
    val enabled: Boolean = false,
    val assets: List<ActionGifAsset> = emptyList(),
    val mode: String = "single",
    val selectedId: String? = null,
    val directionAssignments: Map<String, String> = emptyMap(),
    val keyAssignments: Map<String, String> = emptyMap(),
    val atKey: Boolean = true,
    val aboveKeys: Boolean = true,
    val phase: String = "release",
    val durationMs: Int = 650,
    val cooldownMs: Int = 100,
    val opacity: Float = .8f,
    val size: Float = .35f,
    val maxOverlays: Int = 8,
)

object ActionGifPreferences {
    var current by mutableStateOf(ActionGifState())
        private set

    fun load(context: Context): ActionGifState {
        val prefs = context.getSharedPreferences("keywi_action_gifs", Context.MODE_PRIVATE)
        val saved = prefs.getString("state", null)
        current =
            if (saved == null) {
                val old = TypingOverlayPreferences.load(context)
                ActionGifState(
                    enabled = old.enabled,
                    assets =
                        old.uri?.let { listOf(ActionGifAsset("legacy", "Previous overlay", it)) } ?: emptyList(),
                    selectedId = if (old.uri != null) "legacy" else null,
                    durationMs = old.durationMs,
                    cooldownMs = old.cooldownMs,
                    opacity = old.opacity,
                    size = old.size,
                    atKey = false,
                )
            } else {
                runCatching {
                    val j = JSONObject(saved)
                    val a = j.optJSONArray("assets") ?: JSONArray()

                    fun mapping(key: String): Map<String, String> {
                        val obj = j.optJSONObject(key) ?: return emptyMap()
                        return obj.keys().asSequence().associateWith { obj.getString(it) }
                    }
                    ActionGifState(
                        j.optBoolean("enabled"),
                        (0 until a.length()).map {
                            a.getJSONObject(it).let { o ->
                                ActionGifAsset(o.getString("id"), o.getString("name"), o.getString("uri"))
                            }
                        },
                        j.optString("mode", "single"),
                        j.optString("selectedId").takeIf { it.isNotEmpty() },
                        mapping("directions"),
                        mapping("keys"),
                        j.optBoolean("atKey", true),
                        j.optBoolean("aboveKeys", true),
                        j.optString("phase", "release"),
                        j.optInt("duration", 650).coerceIn(50, 5000),
                        j.optInt("cooldown", 100).coerceIn(0, 3000),
                        j.optDouble("opacity", .8).toFloat().coerceIn(0f, 1f),
                        j.optDouble("size", .35).toFloat().coerceIn(.1f, 1f),
                        j.optInt("max", 8).coerceIn(1, 8),
                    )
                }.getOrDefault(ActionGifState())
            }
        return current
    }

    fun save(
        context: Context,
        state: ActionGifState,
    ) {
        val j =
            JSONObject()
                .put("enabled", state.enabled)
                .put("mode", state.mode)
                .put("selectedId", state.selectedId ?: "")
                .put(
                    "assets",
                    JSONArray().apply {
                        state.assets.forEach {
                            put(
                                JSONObject().put("id", it.id).put("name", it.name).put("uri", it.uri),
                            )
                        }
                    },
                ).put("directions", JSONObject(state.directionAssignments))
                .put("keys", JSONObject(state.keyAssignments))
                .put("atKey", state.atKey)
                .put("aboveKeys", state.aboveKeys)
                .put("phase", state.phase)
                .put("duration", state.durationMs)
                .put("cooldown", state.cooldownMs)
                .put("opacity", state.opacity)
                .put("size", state.size)
                .put("max", state.maxOverlays)
        context
            .getSharedPreferences("keywi_action_gifs", Context.MODE_PRIVATE)
            .edit()
            .putString("state", j.toString())
            .apply()
        current = state
    }

    suspend fun importAsset(
        context: Context,
        uri: Uri,
        name: String,
    ): ActionGifAsset =
        withContext(Dispatchers.IO) {
            require(current.assets.size < 32) { "A pack supports up to 32 images. Remove one before adding more." }
            // Validate before copying, and cap the copy independently in case the provider changes.
            loadTypingOverlayMedia(context, uri.toString())
            val dir = File(context.filesDir, "action-gifs").apply { mkdirs() }
            val id = UUID.randomUUID().toString()
            val file = File(dir, id)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        val bytes = ByteArray(8192)
                        var total = 0
                        while (true) {
                            val n = input.read(bytes)
                            if (n <
                                0
                            ) {
                                break
                            }
                            total += n
                            require(total <= 8 * 1024 * 1024) { "Choose an image smaller than 8 MB." }
                            output.write(bytes, 0, n)
                        }
                    }
                } ?: error("Unable to read this image.")
                ActionGifAsset(id, name, Uri.fromFile(file).toString())
            } catch (failure: Exception) {
                file.delete()
                throw failure
            }
        }

    fun remove(
        context: Context,
        id: String,
    ) {
        val state = current
        state.assets.firstOrNull { it.id == id }?.let { asset ->
            val file = File(Uri.parse(asset.uri).path ?: "")
            if (file.parentFile?.canonicalPath == File(context.filesDir, "action-gifs").canonicalPath) file.delete()
        }
        save(
            context,
            state.copy(
                assets = state.assets.filterNot { it.id == id },
                selectedId = state.selectedId.takeUnless { it == id },
                directionAssignments = state.directionAssignments.filterValues { it != id },
                keyAssignments =
                    state.keyAssignments.filterValues {
                        it !=
                            id
                    },
            ),
        )
    }
}

data class ActionGifPulse(
    val serial: Long,
    val uri: String,
    val x: Float,
    val y: Float,
    val started: Long,
)

object ActionGifRuntime {
    var pulses by mutableStateOf<List<ActionGifPulse>>(emptyList())
        private set
    private var sequence = 0
    private var lastAt = -1L
    private var serial = 0L

    /** Call on the UI thread. Coordinates are fractions of the keyboard bounds. */
    fun trigger(
        context: Context,
        keyId: String = "",
        direction: String = "CENTER",
        phase: String = "release",
        xFraction: Float = .5f,
        yFraction: Float = .5f,
        preview: Boolean = false,
    ) {
        val state = ActionGifPreferences.current
        if ((!state.enabled && !preview) || state.assets.isEmpty() || (!preview && state.phase != phase)) return
        if (KeywiPowerPreferences.load(context) == KeywiPowerMode.DESPERATE || (KeywiPowerPreferences.load(context) == KeywiPowerMode.RESTRICTED && !preview)) return
        val now = SystemClock.uptimeMillis()
        if (!preview && lastAt >= 0 && now - lastAt < state.cooldownMs) return
        val id =
            if (preview &&
                keyId.isEmpty()
            ) {
                state.selectedId
            } else {
                state.keyAssignments["$keyId:$direction"] ?: state.keyAssignments[keyId]
                    ?: state.directionAssignments[direction]
                    ?: when (state.mode) {
                        "sequence" -> state.assets[(sequence++ and Int.MAX_VALUE) % state.assets.size].id
                        "random" -> state.assets.random().id
                        "direction", "key" -> state.selectedId
                        else -> state.selectedId
                    }
            }
        val asset = state.assets.firstOrNull { it.id == id } ?: state.assets.first()
        if (!preview) lastAt = now
        pulses =
            (
                pulses.filter { now - it.started < state.durationMs } +
                    ActionGifPulse(
                        ++serial,
                        asset.uri,
                        if (state.atKey) xFraction.coerceIn(0f, 1f) else .5f,
                        if (state.atKey) yFraction.coerceIn(0f, 1f) else .5f,
                        now,
                    )
            ).takeLast(state.maxOverlays.coerceIn(1, 8))
    }

    fun clear() {
        pulses = emptyList()
        lastAt = -1L
    }
}

@Suppress("DEPRECATION")
private class ActionGifView(
    context: Context,
) : View(context) {
    var sources: Map<String, TypingOverlayMedia> = emptyMap()
    var pulses: List<ActionGifPulse> = emptyList()
    var options = ActionGifState()
    private val visibleStarts = mutableMapOf<Long, Long>()
    var frameMs = 33L
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = false
        isFocusable = false
        importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        var active = false
        for (pulse in pulses) {
            val media = sources[pulse.uri] ?: continue
            val started = visibleStarts.getOrPut(pulse.serial) { now }
            val elapsed = now - started
            if (elapsed !in 0 until options.durationMs.toLong()) continue
            active = true
            val scale = min(width.toFloat() / media.width, height.toFloat() / media.height) * options.size
            paint.alpha = (255 * options.opacity * min(1f, (options.durationMs - elapsed) / 100f)).toInt()
            val checkpoint = canvas.save()
            try {
                canvas.translate(width * pulse.x - media.width * scale / 2, height * pulse.y - media.height * scale / 2)
                canvas.scale(scale, scale)
                media.bitmap?.let { canvas.drawBitmap(it, 0f, 0f, paint) }
                media.movie?.let { movie ->
                    movie.setTime(elapsed.coerceAtMost((movie.duration().takeIf { it > 0 } ?: 1000) - 1L).toInt())
                    movie.draw(canvas, 0f, 0f, paint)
                }
            } catch (
                _: RuntimeException,
            ) {
                // Optional malformed artwork cannot crash input.
            } finally {
                canvas.restoreToCount(checkpoint)
            }
        }
        visibleStarts.keys.retainAll(pulses.map { it.serial }.toSet())
        if (active && isShown) postInvalidateDelayed(frameMs)
    }
}

@Composable
fun ActionGifOverlayLayer(
    aboveKeys: Boolean,
    modifier: Modifier = Modifier,
    preview: Boolean = false,
) {
    val context = LocalContext.current
    val state = ActionGifPreferences.current
    val power = KeywiPowerPreferences.load(context)
    if (power == KeywiPowerMode.DESPERATE) return
    if ((!state.enabled && !preview) || state.aboveKeys != aboveKeys || (power == KeywiPowerMode.RESTRICTED && !preview)) return
    val pulses = ActionGifRuntime.pulses
    // Only live sources are decoded. Keep at most eight cached images, never an entire pack.
    val requestedUris = pulses.takeLast(8).map { it.uri }.distinct()
    val cache = remember { LruCache<String, TypingOverlayMedia>(8) }
    val media by produceState<Map<String, TypingOverlayMedia>>(emptyMap(), requestedUris) {
        value =
            withContext(Dispatchers.IO) {
                requestedUris
                    .mapNotNull { uri ->
                        try {
                            val decoded =
                                cache.get(uri) ?: loadTypingOverlayMedia(context.applicationContext, uri)
                                    .let { media ->
                                        val bitmap = media.bitmap
                                        if (bitmap != null && maxOf(bitmap.width, bitmap.height) > 512) {
                                            val scale = 512f / maxOf(bitmap.width, bitmap.height)
                                            TypingOverlayMedia(
                                                bitmap =
                                                    Bitmap.createScaledBitmap(
                                                        bitmap,
                                                        (bitmap.width * scale).toInt().coerceAtLeast(1),
                                                        (
                                                            bitmap.height *
                                                                scale
                                                        ).toInt().coerceAtLeast(1),
                                                        true,
                                                    ),
                                            )
                                        } else {
                                            media
                                        }
                                    }.also { cache.put(uri, it) }
                            uri to decoded
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            null
                        }
                    }.toMap()
            }
    }
    AndroidView(factory = { ActionGifView(it) }, modifier = modifier, update = {
        it.options = state
        it.sources = media
        it.pulses = pulses
        it.frameMs =
            if (power ==
                KeywiPowerMode.UNBRIDLED
            ) {
                16L
            } else {
                33L
            }
        ; it.invalidate()
    }, onReset = null, onRelease = {
        it.sources = emptyMap()
        it.pulses = emptyList()
    })
}
