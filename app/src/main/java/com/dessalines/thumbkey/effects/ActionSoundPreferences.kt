package com.dessalines.thumbkey.effects

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class SoundSelection { SINGLE, ORDERED, RANDOM, DIRECTION }

enum class SoundTrigger { PRESS, RELEASE }

enum class SoundEvent { KEY, SUGGESTION_SCROLL, SUGGESTION_SELECTED }

data class SoundClip(
    val id: String,
    val name: String,
    val uri: String,
)

data class ActionSoundConfig(
    val enabled: Boolean = false,
    val clips: List<SoundClip> = starterSounds,
    val selection: SoundSelection = SoundSelection.SINGLE,
    val selectedId: String = "starter-soft",
    val trigger: SoundTrigger = SoundTrigger.PRESS,
    val overlap: Boolean = true,
    val maxDurationMs: Int = 0,
    val volume: Float = 0.6f,
    val toneEnabled: Boolean = false,
    val toneRoot: Int = 0,
    val toneScale: NoteScale = NoteScale.MAJOR,
    val toneVolume: Float = .35f,
    val directions: Map<String, String> = emptyMap(),
    val keys: Map<String, String> = emptyMap(),
    val suggestionScrollId: String? = null,
    val suggestionSelectedId: String? = null,
)

/** Removing a selected clip keeps single playback usable and clears every stale assignment. */
fun ActionSoundConfig.withoutClip(id: String): ActionSoundConfig {
    val remaining = clips.filterNot { it.id == id }
    return copy(
        clips = remaining,
        selectedId = selectedId.takeUnless { it == id } ?: remaining.firstOrNull()?.id.orEmpty(),
        directions = directions.filterValues { it != id },
        keys = keys.filterValues { it != id },
        suggestionScrollId = suggestionScrollId?.takeUnless { it == id },
        suggestionSelectedId = suggestionSelectedId?.takeUnless { it == id },
    )
}

/** Per-cell gestures take priority; direction defaults apply only in direction playback mode. */
internal fun ActionSoundConfig.clipForAction(
    direction: String,
    keyId: String?,
    orderedIndex: Long,
): String? =
    keys["$keyId:${direction.uppercase()}"] ?: when (selection) {
        SoundSelection.SINGLE -> selectedId
        SoundSelection.ORDERED -> clips.getOrNull(Math.floorMod(orderedIndex, clips.size.coerceAtLeast(1).toLong()).toInt())?.id
        SoundSelection.RANDOM -> clips.randomOrNull()?.id
        SoundSelection.DIRECTION -> directions[direction.uppercase()]
    }

val starterSounds =
    listOf(
        SoundClip("starter-soft", "Soft tick", "builtin:soft"),
        SoundClip("starter-drop", "Glass drop", "builtin:drop"),
        SoundClip("starter-brush", "Suggestion brush", "builtin:brush"),
    )
val soundDirections = listOf("TOP_LEFT", "TOP", "TOP_RIGHT", "LEFT", "CENTER", "RIGHT", "BOTTOM_LEFT", "BOTTOM", "BOTTOM_RIGHT", "HOLD")

object ActionSoundPreferences {
    private const val KEY = "config"

    @Volatile private var cached: ActionSoundConfig? = null

    @Volatile private var cachedJson: String? = null

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("keywi_action_sounds", Context.MODE_PRIVATE)

    private fun JSONObject.stringMap(): Map<String, String> = keys().asSequence().associateWith { getString(it) }

    fun get(context: Context): ActionSoundConfig {
        val raw = prefs(context).getString(KEY, null)
        cached?.let { if (cachedJson == raw) return it }
        val value = parse(raw)
        cachedJson = raw
        cached = value
        return value
    }

    private fun parse(raw: String?): ActionSoundConfig =
        runCatching {
            val j = JSONObject(raw ?: return ActionSoundConfig())
            val array = j.optJSONArray("clips") ?: JSONArray()
            ActionSoundConfig(
                enabled = j.optBoolean("enabled"),
                clips =
                    (0 until array.length()).map { i ->
                        array.getJSONObject(i).let { SoundClip(it.getString("id"), it.getString("name"), it.getString("uri")) }
                    },
                selection = SoundSelection.valueOf(j.optString("selection", "SINGLE")),
                selectedId = j.optString("selectedId", "starter-soft"),
                trigger = SoundTrigger.valueOf(j.optString("trigger", "PRESS")),
                overlap = j.optBoolean("overlap", true),
                maxDurationMs = j.optInt("maxDurationMs", 0).coerceIn(0, 120_000),
                volume = j.optDouble("volume", 0.6).toFloat().coerceIn(0f, 1f),
                toneEnabled = j.optBoolean("toneEnabled", false),
                toneRoot = j.optInt("toneRoot", 0).coerceIn(0, 11),
                toneScale = runCatching { NoteScale.valueOf(j.optString("toneScale", "MAJOR")) }.getOrDefault(NoteScale.MAJOR),
                toneVolume = j.optDouble("toneVolume", .35).toFloat().coerceIn(0f, 1f),
                directions = j.optJSONObject("directions")?.stringMap().orEmpty(),
                keys = j.optJSONObject("keys")?.stringMap().orEmpty(),
                suggestionScrollId = j.optString("suggestionScrollId").takeIf { it.isNotEmpty() },
                suggestionSelectedId = j.optString("suggestionSelectedId").takeIf { it.isNotEmpty() },
            )
        }.getOrDefault(ActionSoundConfig())

    fun save(
        context: Context,
        value: ActionSoundConfig,
    ) {
        val j =
            JSONObject()
                .put("enabled", value.enabled)
                .put("selection", value.selection.name)
                .put("selectedId", value.selectedId)
                .put("trigger", value.trigger.name)
                .put("overlap", value.overlap)
                .put("maxDurationMs", value.maxDurationMs)
                .put("volume", value.volume.toDouble())
                .put("toneEnabled", value.toneEnabled)
                .put("toneRoot", value.toneRoot.coerceIn(0, 11))
                .put("toneScale", value.toneScale.name)
                .put("toneVolume", value.toneVolume.coerceIn(0f, 1f).toDouble())
                .put("directions", JSONObject(value.directions))
                .put("keys", JSONObject(value.keys))
                .put("suggestionScrollId", value.suggestionScrollId ?: "")
                .put("suggestionSelectedId", value.suggestionSelectedId ?: "")
        j.put(
            "clips",
            JSONArray().also { a ->
                value.clips.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("uri", it.uri)) }
            },
        )
        val raw = j.toString()
        cachedJson = raw
        cached = value
        prefs(context).edit().putString(KEY, raw).apply()
    }

    /** Imports into app storage, so moving or deleting the source never breaks the pack. */
    fun import(
        context: Context,
        uri: Uri,
    ): SoundClip {
        val id = UUID.randomUUID().toString()
        var name = "Imported sound"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) {
                name =
                    it.getString(0)
            }
        }
        val target = File(File(context.filesDir, "action-sounds").apply { mkdirs() }, id)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { out ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= 25 * 1024 * 1024) { "Sound files must be 25 MB or smaller" }
                        out.write(buffer, 0, read)
                    }
                }
            } ?: error("Unable to open sound")
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        return SoundClip(id, name, Uri.fromFile(target).toString())
    }

    fun remove(
        context: Context,
        id: String,
    ): ActionSoundConfig {
        val previous = get(context)
        val value = previous.withoutClip(id)
        save(context, value)
        previous.clips.firstOrNull { it.id == id }?.let { clip ->
            val uri = Uri.parse(clip.uri)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                val directory = File(context.filesDir, "action-sounds")
                if (runCatching { file.parentFile?.canonicalFile == directory.canonicalFile }.getOrDefault(false)) file.delete()
            }
        }
        return value
    }

    fun assignKey(
        context: Context,
        keyId: String,
        direction: String,
        soundId: String?,
    ) {
        val value = get(context)
        val key = "$keyId:${direction.uppercase()}"
        save(context, value.copy(keys = value.keys.toMutableMap().apply { if (soundId == null) remove(key) else put(key, soundId) }))
    }
}
