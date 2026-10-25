package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

enum class KeySurfaceStyle { GRADIENT, SOLID, NONE }

enum class KeyBorderStyle { GRADIENT, SOLID, SHADOW, NONE }

data class KeyDesignOverride(
    val surfaceColor: Color? = null,
    val borderColor: Color? = null,
    val surfaceGradient: KeyboardBackdrop? = null,
    val borderGradient: KeyboardBackdrop? = null,
    val glyphColor: Color? = null,
)

data class KeyDesignGroup(
    val id: String,
    val keyIds: Set<String>,
    val design: KeyDesignOverride,
    val sharedGradientCanvas: Boolean = false,
)

data class KeyThemeState(
    val surfaceStyle: KeySurfaceStyle = KeySurfaceStyle.GRADIENT,
    val surfaceGradient: KeyboardBackdrop = BIRDIE_KEY_GRADIENT,
    val surfaceColor: Color = Color(0xFF242631),
    val borderStyle: KeyBorderStyle = KeyBorderStyle.GRADIENT,
    val borderGradient: KeyboardBackdrop = BIRDIE_GOLD_BORDER,
    val borderColor: Color = Color(0xFFFFD86B),
    val shadowColor: Color = Color.Black,
    val shadowAlpha: Float = 0.55f,
    val shadowElevation: Float = 4f,
    val borderDistribution: GradientDistribution = GradientDistribution.SAME,
    val borderPhaseStepDegrees: Float = 30f,
    val borderRandomSeed: Int = 0,
    val keyOverrides: Map<String, KeyDesignOverride> = emptyMap(),
    val keyGroups: List<KeyDesignGroup> = emptyList(),
    val glyphColor: Color? = null,
)

object KeyThemePreferences {
    private const val PREFS = "key_theme_preferences"

    var current: KeyThemeState by mutableStateOf(KeyThemeState())
        private set

    fun load(context: Context): KeyThemeState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val defaults = KeyThemeState()
        current =
            KeyThemeState(
                surfaceStyle = enumValue(prefs.getString("surface_style", null), KeySurfaceStyle.GRADIENT),
                surfaceGradient = readGradient(prefs, "surface", BIRDIE_KEY_GRADIENT),
                surfaceColor = readColor(prefs, "surface_color", defaults.surfaceColor),
                borderStyle = enumValue(prefs.getString("border_style", null), KeyBorderStyle.GRADIENT),
                borderGradient = readGradient(prefs, "border", BIRDIE_GOLD_BORDER),
                borderColor = readColor(prefs, "border_color", defaults.borderColor),
                shadowColor = readColor(prefs, "shadow_color", defaults.shadowColor),
                shadowAlpha = prefs.getFloat("shadow_alpha", 0.55f),
                shadowElevation = prefs.getFloat("shadow_elevation", 4f),
                borderDistribution = enumValue(prefs.getString("border_distribution", null), GradientDistribution.SAME),
                borderPhaseStepDegrees = prefs.getFloat("border_phase_step", 30f),
                borderRandomSeed = prefs.getInt("border_random_seed", 0),
                keyOverrides = decodeOverrides(prefs.getString("key_overrides", null)),
                keyGroups = decodeGroups(prefs.getString("key_groups", null)),
            )
        return current
    }

    fun save(
        context: Context,
        state: KeyThemeState,
    ) {
        current = state
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putString("surface_style", state.surfaceStyle.name)
                writeGradient(this, "surface", state.surfaceGradient)
                putLong("surface_color", state.surfaceColor.value.toLong())
                putString("border_style", state.borderStyle.name)
                writeGradient(this, "border", state.borderGradient)
                putLong("border_color", state.borderColor.value.toLong())
                putLong("shadow_color", state.shadowColor.value.toLong())
                putFloat("shadow_alpha", state.shadowAlpha)
                putFloat("shadow_elevation", state.shadowElevation)
                putString("border_distribution", state.borderDistribution.name)
                putFloat("border_phase_step", state.borderPhaseStepDegrees)
                putInt("border_random_seed", state.borderRandomSeed)
                putString(
                    "key_overrides",
                    JSONObject()
                        .apply {
                            state.keyOverrides.forEach { (id, design) -> put(id, encodeDesign(design)) }
                        }.toString(),
                )
                putString(
                    "key_groups",
                    JSONArray()
                        .apply {
                            state.keyGroups.forEach { group -> addGroup(group) }
                        }.toString(),
                )
            }.apply()
    }

    private fun readColor(
        prefs: android.content.SharedPreferences,
        key: String,
        fallback: Color,
    ): Color =
        runCatching {
            Color(prefs.getLong(key, fallback.value.toLong()).toULong())
        }.getOrDefault(fallback)

    private fun readGradient(
        prefs: android.content.SharedPreferences,
        prefix: String,
        fallback: KeyboardBackdrop,
    ): KeyboardBackdrop {
        val raw = prefs.getString("${prefix}_stops", null) ?: return fallback
        val stops =
            raw.split(";").mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size != 2) {
                    null
                } else {
                    runCatching {
                        KeyboardGradientStop(parts[0].toFloat(), Color(parts[1].toULong()))
                    }.getOrNull()
                }
            }
        return if (stops.isEmpty()) {
            fallback
        } else {
            KeyboardBackdrop(stops, prefs.getFloat("${prefix}_angle", fallback.angleDegrees))
        }
    }

    private fun writeGradient(
        editor: android.content.SharedPreferences.Editor,
        prefix: String,
        gradient: KeyboardBackdrop,
    ) {
        editor.putFloat("${prefix}_angle", gradient.angleDegrees)
        editor.putString(
            "${prefix}_stops",
            gradient.stops.joinToString(";") { "${it.position}:${it.color.value}" },
        )
    }

    private inline fun <reified T : Enum<T>> enumValue(
        value: String?,
        fallback: T,
    ): T = runCatching { enumValueOf<T>(value ?: "") }.getOrDefault(fallback)
}

/** Later groups override earlier groups, then the specific key overrides the group. */
fun KeyThemeState.forKey(keyId: String): KeyThemeState {
    var theme = this

    fun applyDesign(design: KeyDesignOverride) {
        theme =
            theme.copy(
                surfaceStyle =
                    if (design.surfaceGradient !=
                        null
                    ) {
                        KeySurfaceStyle.GRADIENT
                    } else if (design.surfaceColor != null) {
                        KeySurfaceStyle.SOLID
                    } else {
                        theme.surfaceStyle
                    },
                surfaceColor = design.surfaceColor ?: theme.surfaceColor,
                surfaceGradient = design.surfaceGradient ?: theme.surfaceGradient,
                borderStyle =
                    if (design.borderGradient !=
                        null
                    ) {
                        KeyBorderStyle.GRADIENT
                    } else if (design.borderColor != null) {
                        KeyBorderStyle.SOLID
                    } else {
                        theme.borderStyle
                    },
                glyphColor = design.glyphColor ?: theme.glyphColor,
                borderColor = design.borderColor ?: theme.borderColor,
                borderGradient = design.borderGradient ?: theme.borderGradient,
            )
    }
    keyGroups.filter { keyId in it.keyIds }.forEach { applyDesign(it.design) }
    keyOverrides[keyId]?.let { applyDesign(it) }
    return theme
}

internal fun encodeDesign(design: KeyDesignOverride): JSONObject =
    JSONObject().apply {
        design.glyphColor?.let { put("glyphColor", it.value.toString()) }
        design.surfaceColor?.let { put("surfaceColor", it.value.toString()) }
        design.borderColor?.let { put("borderColor", it.value.toString()) }

        fun gradient(
            name: String,
            value: KeyboardBackdrop?,
        ) {
            value ?: return
            put(
                name,
                JSONObject().apply {
                    put("angle", value.angleDegrees.toDouble())
                    put(
                        "stops",
                        JSONArray().apply {
                            value.stops.forEach { stop ->
                                put(JSONObject().put("position", stop.position.toDouble()).put("color", stop.color.value.toString()))
                            }
                        },
                    )
                },
            )
        }
        gradient("surfaceGradient", design.surfaceGradient)
        gradient("borderGradient", design.borderGradient)
    }

internal fun decodeDesign(json: JSONObject): KeyDesignOverride {
    fun color(name: String): Color? = json.optString(name).toULongOrNull()?.let { Color(it) }

    fun gradient(name: String): KeyboardBackdrop? {
        val value = json.optJSONObject(name) ?: return null
        val stops = value.optJSONArray("stops") ?: return null
        val parsed =
            (0 until stops.length()).mapNotNull { i ->
                runCatching {
                    stops.getJSONObject(i).let {
                        KeyboardGradientStop(it.getDouble("position").toFloat(), Color(it.getString("color").toULong()))
                    }
                }.getOrNull()
            }
        return parsed.takeIf { it.isNotEmpty() }?.let { KeyboardBackdrop(it, value.optDouble("angle", 135.0).toFloat()) }
    }
    return KeyDesignOverride(color("surfaceColor"), color("borderColor"), gradient("surfaceGradient"), gradient("borderGradient"), color("glyphColor"))
}

internal fun decodeOverrides(raw: String?): Map<String, KeyDesignOverride> =
    runCatching {
        val json = JSONObject(raw ?: "{}")
        json
            .keys()
            .asSequence()
            .mapNotNull { id -> runCatching { id to decodeDesign(json.getJSONObject(id)) }.getOrNull() }
            .toMap()
    }.getOrDefault(emptyMap())

internal fun JSONArray.addGroup(group: KeyDesignGroup) {
    put(
        JSONObject()
            .put(
                "id",
                group.id,
            ).put(
                "keys",
                JSONArray(group.keyIds.toList()),
            ).put("design", encodeDesign(group.design))
            .put("sharedGradientCanvas", group.sharedGradientCanvas),
    )
}

internal fun decodeGroups(raw: String?): List<KeyDesignGroup> =
    runCatching {
        val array = JSONArray(raw ?: "[]")
        (0 until array.length()).mapNotNull { i ->
            runCatching {
                val json = array.getJSONObject(i)
                val keys = json.getJSONArray("keys")
                KeyDesignGroup(
                    json.getString("id"),
                    (0 until keys.length()).map { keys.getString(it) }.toSet(),
                    decodeDesign(json.getJSONObject("design")),
                    sharedGradientCanvas = json.optBoolean("sharedGradientCanvas", false),
                )
            }.getOrNull()
        }
    }.getOrDefault(emptyList())
