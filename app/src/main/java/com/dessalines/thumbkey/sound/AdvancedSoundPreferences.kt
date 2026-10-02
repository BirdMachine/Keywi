package com.dessalines.thumbkey.sound

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class AdvancedSoundMode { PLAYLIST, ACTION, POSITION, KEY }
enum class SoundPlaybackOrder { ORDERED, RANDOM }

data class SoundClip(val id: String, val label: String, val uri: String? = null, val builtInEffect: Int? = null)

data class AdvancedSoundConfig(
    val enabled: Boolean = false,
    val mode: AdvancedSoundMode = AdvancedSoundMode.PLAYLIST,
    val order: SoundPlaybackOrder = SoundPlaybackOrder.RANDOM,
    val volume: Float = 0.25f,
    val clips: List<SoundClip> = emptyList(),
    val assignments: Map<String, String> = emptyMap(),
)

object AdvancedSoundPreferences {
    private const val PREFS = "keywi_advanced_sound"
    private const val CONFIG = "config"

    fun load(context: Context): AdvancedSoundConfig {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CONFIG, null) ?: return AdvancedSoundConfig()
        return runCatching {
            val root = JSONObject(raw)
            val clipsJson = root.optJSONArray("clips") ?: JSONArray()
            val clips = buildList {
                for (i in 0 until clipsJson.length()) {
                    val c = clipsJson.getJSONObject(i)
                    add(SoundClip(c.getString("id"), c.optString("label", "Sound"), c.optString("uri").takeIf { it.isNotBlank() }, c.optInt("effect", -1).takeIf { it >= 0 }))
                }
            }
            val assignmentJson = root.optJSONObject("assignments") ?: JSONObject()
            val assignments = buildMap { assignmentJson.keys().forEach { key -> put(key, assignmentJson.getString(key)) } }
            AdvancedSoundConfig(
                enabled = root.optBoolean("enabled", false),
                mode = runCatching { AdvancedSoundMode.valueOf(root.optString("mode")) }.getOrDefault(AdvancedSoundMode.PLAYLIST),
                order = runCatching { SoundPlaybackOrder.valueOf(root.optString("order")) }.getOrDefault(SoundPlaybackOrder.RANDOM),
                volume = root.optDouble("volume", .25).toFloat().coerceIn(0f, 1f),
                clips = clips,
                assignments = assignments,
            )
        }.getOrDefault(AdvancedSoundConfig())
    }

    fun save(context: Context, config: AdvancedSoundConfig) {
        val clips = JSONArray().apply {
            config.clips.forEach { clip -> put(JSONObject().apply { put("id", clip.id); put("label", clip.label); clip.uri?.let { put("uri", it) }; clip.builtInEffect?.let { put("effect", it) } }) }
        }
        val assignments = JSONObject().apply { config.assignments.forEach { (key, value) -> put(key, value) } }
        val root = JSONObject().apply {
            put("enabled", config.enabled); put("mode", config.mode.name); put("order", config.order.name); put("volume", config.volume.toDouble()); put("clips", clips); put("assignments", assignments)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CONFIG, root.toString()).apply()
    }
}
