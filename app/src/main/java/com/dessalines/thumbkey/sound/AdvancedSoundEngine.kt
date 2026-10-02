package com.dessalines.thumbkey.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import kotlin.random.Random

/** Lightweight IME-safe sound router. Custom clips are streamed; Android effects remain zero-setup fallbacks. */
class AdvancedSoundEngine(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var player: MediaPlayer? = null
    private var cursor = 0

    fun play(event: SoundEvent) {
        val config = AdvancedSoundPreferences.load(context)
        if (!config.enabled) return
        val clip = resolve(config, event) ?: return
        clip.uri?.let { playUri(it, config.volume); return }
        clip.builtInEffect?.let { audioManager.playSoundEffect(it, config.volume) }
    }

    fun release() { player?.release(); player = null }

    private fun resolve(config: AdvancedSoundConfig, event: SoundEvent): SoundClip? {
        if (config.clips.isEmpty()) return null
        val assignment = when (config.mode) {
            AdvancedSoundMode.ACTION -> config.assignments[event.action]
            AdvancedSoundMode.POSITION -> config.assignments[event.position]
            AdvancedSoundMode.KEY -> config.assignments[event.key]
            AdvancedSoundMode.PLAYLIST -> null
        }
        assignment?.let { id -> config.clips.firstOrNull { it.id == id }?.let { return it } }
        return when (config.order) {
            SoundPlaybackOrder.RANDOM -> config.clips[Random.nextInt(config.clips.size)]
            SoundPlaybackOrder.ORDERED -> config.clips[(cursor++ % config.clips.size)]
        }
    }

    private fun playUri(uri: String, volume: Float) {
        runCatching {
            player?.release()
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(context, android.net.Uri.parse(uri))
                setVolume(volume, volume)
                setOnCompletionListener { it.release(); if (player === it) player = null }
                prepare()
                start()
            }
        }
    }
}

data class SoundEvent(
    val action: String = "tap",
    val position: String = "center",
    val key: String = "default",
)
