package com.dessalines.thumbkey.effects

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Independent players preserve tails and overlap, including rapid swipes. */
object ActionSoundEngine {
    private val handler = Handler(Looper.getMainLooper())
    private val active = LinkedHashSet<MediaPlayer>()
    private val audioLoader = Executors.newSingleThreadExecutor()
    private var next = 0L
    private var lastScroll = 0L

    fun isEnabled(context: Context) = ActionSoundPreferences.get(context).enabled

    fun stopAll() { handler.post { active.toList().forEach { release(it) } } }

    fun fire(
        context: Context,
        direction: String = "CENTER",
        keyId: String? = null,
        phase: SoundTrigger = SoundTrigger.PRESS,
        event: SoundEvent = SoundEvent.KEY,
    ) {
        if (!FeedbackPreferences.soundAllowed(context)) return
        val config = ActionSoundPreferences.get(context)
        if (!config.enabled && event == SoundEvent.KEY && phase == SoundTrigger.PRESS) {
            (context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager)
                .playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, .1f)
            return
        }
        if (!config.enabled || (event == SoundEvent.KEY && phase != config.trigger)) return
        if (event == SoundEvent.SUGGESTION_SCROLL) {
            val now = android.os.SystemClock.uptimeMillis()
            if (now - lastScroll < 65) return
            lastScroll = now
        }
        val id =
            when (event) {
                SoundEvent.SUGGESTION_SCROLL -> {
                    config.suggestionScrollId ?: return
                }

                SoundEvent.SUGGESTION_SELECTED -> {
                    config.suggestionSelectedId ?: return
                }

                SoundEvent.KEY -> {
                    val index =
                        if (config.selection == SoundSelection.ORDERED &&
                            "$keyId:${direction.uppercase()}" !in config.keys
                        ) {
                            next++
                        } else {
                            next
                        }
                    config.clipForAction(direction, keyId, index)
                }
            }
        config.clips.find { it.id == id }?.let { play(context, it, config) }
    }

    fun preview(
        context: Context,
        clip: SoundClip,
    ) = play(context, clip, ActionSoundPreferences.get(context))

    private fun play(
        context: Context,
        clip: SoundClip,
        config: ActionSoundConfig,
    ) {
        audioLoader.execute {
            val uri =
                runCatching {
                    if (clip.uri.startsWith(
                            "builtin:",
                        )
                    ) {
                        Uri.fromFile(starterFile(context, clip.uri.substringAfter(':')))
                    } else {
                        Uri.parse(clip.uri)
                    }
                }.getOrNull()
                    ?: return@execute
            handler.post {
                if (!FeedbackPreferences.soundAllowed(context)) return@post
                if (!config.overlap) active.toList().forEach { release(it) }
                // Bound resources independently from the user's chosen tail duration.
                while (active.size >= 24) release(active.first())
                val player = MediaPlayer()
                active.add(player)
                runCatching {
                    player.setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setUsage(
                                AudioAttributes.USAGE_ASSISTANCE_SONIFICATION,
                            ).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    player.setVolume(config.volume.coerceIn(0f, 1f), config.volume.coerceIn(0f, 1f))
                    player.setDataSource(context.applicationContext, uri)
                    player.setOnCompletionListener { release(it) }
                    player.setOnErrorListener { p, _, _ ->
                        release(p)
                        true
                    }
                    player.setOnPreparedListener { p ->
                        if (p in active && FeedbackPreferences.soundAllowed(context)) {
                            p.start()
                            if (config.maxDurationMs >
                                0
                            ) {
                                handler.postAtTime({
                                    if (p in
                                        active
                                    ) {
                                        release(p)
                                    }
                                }, p, android.os.SystemClock.uptimeMillis() + config.maxDurationMs.coerceIn(1, 120_000))
                            }
                        }
                    }
                    player.prepareAsync()
                    handler.postAtTime({ if (player in active) release(player) }, player, android.os.SystemClock.uptimeMillis() + 120_000)
                }.onFailure { release(player) }
            }
        }
    }

    private fun release(player: MediaPlayer) {
        handler.removeCallbacksAndMessages(player)
        if (active.remove(player)) runCatching { player.release() }
    }

    private fun starterFile(
        context: Context,
        kind: String,
    ): File {
        val file = File(context.cacheDir, "keywi-starter-$kind.wav")
        if (file.exists()) return file
        val sampleRate = 22050
        val duration = if (kind == "drop") 0.13 else 0.055
        val samples = (sampleRate * duration).toInt()
        val b = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN)
        b
            .put("RIFF".toByteArray())
            .putInt(36 + samples * 2)
            .put("WAVEfmt ".toByteArray())
            .putInt(16)
            .putShort(1)
            .putShort(1)
            .putInt(sampleRate)
            .putInt(sampleRate * 2)
            .putShort(2)
            .putShort(16)
            .put("data".toByteArray())
            .putInt(samples * 2)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq =
                when (kind) {
                    "drop" -> 1250.0 - 2400 * t
                    "brush" -> 360.0
                    else -> 850.0
                }
            val fade = exp(-t * if (kind == "drop") 32 else 85) * (i / 30.0).coerceAtMost(1.0)
            val wave = sin(2 * PI * freq * t) + 0.2 * sin(2 * PI * freq * 2.1 * t)
            b.putShort((wave * fade * 8000).toInt().coerceIn(-32768, 32767).toShort())
        }
        file.writeBytes(b.array())
        return file
    }
}
