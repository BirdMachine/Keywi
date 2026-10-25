package com.dessalines.thumbkey.effects

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import java.io.File
import java.util.concurrent.Executors

/** A bounded twelve-note bank avoids MediaPlayer allocation and synthesis on each keypress. */
object ScaleToneEngine {
    private val handler = Handler(Looper.getMainLooper())
    private val loader = Executors.newSingleThreadExecutor()
    private var pool: SoundPool? = null
    private val ids = mutableMapOf<Int, Int>()
    private val ready = mutableSetOf<Int>()
    private val streams = ArrayDeque<Int>()
    private var pending: (() -> Unit)? = null

    fun stopAll() {
        handler.post {
            pending = null
            streams.forEach { pool?.stop(it) }
            streams.clear()
        }
    }

    fun fire(context: Context, preview: Boolean = false) {
        val app = context.applicationContext
        handler.post {
            if (!FeedbackPreferences.soundAllowed(app)) return@post
            val config = ActionSoundPreferences.get(app)
            if (!preview && !config.toneEnabled) return@post
            ensureBank(app)
            val note = ScaleToneSynth.notes(config.toneRoot, config.toneScale).random()
            val play = {
                val live = ActionSoundPreferences.get(app)
                if (FeedbackPreferences.soundAllowed(app) && (preview || live.toneEnabled) &&
                    live.toneRoot == config.toneRoot && live.toneScale == config.toneScale
                ) {
                    val id = ids[note]
                    if (id != null && id in ready) {
                        val volume = (live.volume * live.toneVolume).coerceIn(0f, 1f)
                        if (!live.overlap) { streams.forEach { pool?.stop(it) }; streams.clear() }
                        while (streams.size >= 8) pool?.stop(streams.removeFirst())
                        pool?.play(id, volume, volume, 1, 0, 1f)?.takeIf { it != 0 }?.let { streams.addLast(it) }
                    }
                }
                Unit
            }
            if (ids[note] in ready) play() else pending = play
        }
    }

    private fun ensureBank(context: Context) {
        if (pool != null) return
        val bank = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
        ).build()
        pool = bank
        bank.setOnLoadCompleteListener { _, id, status ->
            if (status == 0) ready.add(id)
            if (ready.size == 12) { val action = pending; pending = null; action?.invoke() }
        }
        loader.execute {
            val files = runCatching {
                (60..71).associateWith { midi ->
                    File(context.cacheDir, "keywi-scale-v2-$midi.wav").also { file ->
                        if (!file.exists() || file.length() != (44 + ScaleToneSynth.SAMPLE_RATE * ScaleToneSynth.DURATION_MS / 1000 * 2).toLong()) {
                            file.writeBytes(ScaleToneSynth.wave(midi))
                        }
                    }
                }
            }.getOrNull()
            handler.post {
                if (files == null) { bank.release(); pool = null; pending = null; return@post }
                files.forEach { (midi, file) -> ids[midi] = bank.load(file.absolutePath, 1) }
            }
        }
    }
}
