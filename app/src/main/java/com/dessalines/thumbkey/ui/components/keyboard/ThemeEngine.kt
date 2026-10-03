package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Portable, versioned snapshot of Keywi's appearance state.
 *
 * Keep this model independent from the settings UI: ThemeManager can change shape without
 * invalidating exported themes. Media URIs are retained for local themes, but callers should
 * treat them as device-local references when sharing an export.
 */
data class ThemeDocument(
    val schemaVersion: Int = CURRENT_THEME_SCHEMA,
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val backdrop: BackdropThemeState,
    val toolbar: BackdropThemeState,
    val toolbarBorderWidth: Float,
    val toolbarBorderColor: Color,
    val toolbarKeyboardGap: Float,
    val keys: KeyThemeState,
    val suggestionLozenges: SuggestionLozengeThemeState,
)

const val CURRENT_THEME_SCHEMA = 1
const val CUSTOM_THEME_ID = "custom"

/** Theme persistence + application boundary for the rest of Keywi. */
object ThemeEngine {
    private const val PREFS = "keywi_theme_engine"
    private const val ACTIVE_THEME = "active_theme_id"
    private const val MIGRATED = "theme_engine_migrated"
    private const val THEMES_DIR = "themes"
    private const val CUSTOM_FILE = "custom.json"

    /**
     * First launch preserves the user's existing appearance by adopting it as Custom.
     * Nothing visually changes merely because the theme engine was introduced.
     */
    fun ensureMigrated(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATED, false)) return
        saveCustom(context, captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        prefs.edit().putString(ACTIVE_THEME, CUSTOM_THEME_ID).putBoolean(MIGRATED, true).apply()
    }

    fun captureCurrent(
        context: Context,
        name: String,
        id: String = UUID.randomUUID().toString(),
    ): ThemeDocument =
        ThemeDocument(
            id = id,
            name = name,
            backdrop = BackdropThemePreferences.load(context),
            toolbar = ToolbarThemePreferences.load(context),
            toolbarBorderWidth = ToolbarBorderPreferences.loadWidth(context),
            toolbarBorderColor = ToolbarBorderPreferences.loadColor(context),
            toolbarKeyboardGap = ToolbarLayoutPreferences.loadKeyboardGap(context),
            keys = KeyThemePreferences.load(context),
            suggestionLozenges = SuggestionLozengeThemePreferences.load(context),
        )

    fun apply(context: Context, theme: ThemeDocument) {
        BackdropThemePreferences.save(context, theme.backdrop)
        ToolbarThemePreferences.save(context, theme.toolbar)
        ToolbarBorderPreferences.saveWidth(context, theme.toolbarBorderWidth)
        ToolbarBorderPreferences.saveColor(context, theme.toolbarBorderColor)
        ToolbarLayoutPreferences.saveKeyboardGap(context, theme.toolbarKeyboardGap)
        KeyThemePreferences.save(context, theme.keys)
        SuggestionLozengeThemePreferences.save(context, theme.suggestionLozenges)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ACTIVE_THEME, theme.id).apply()
    }

    /** Editing a named theme is deliberately non-destructive: fork it into Custom first. */
    fun beginEditing(context: Context, source: ThemeDocument): ThemeDocument {
        val custom = source.copy(id = CUSTOM_THEME_ID, name = "Custom")
        saveCustom(context, custom)
        apply(context, custom)
        return custom
    }

    fun activeThemeId(context: Context): String {
        ensureMigrated(context)
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ACTIVE_THEME, CUSTOM_THEME_ID)
            ?: CUSTOM_THEME_ID
    }

    fun loadActive(context: Context): ThemeDocument {
        ensureMigrated(context)
        val id = activeThemeId(context)
        return if (id == CUSTOM_THEME_ID) loadCustom(context) else loadTheme(context, id) ?: loadCustom(context)
    }

    fun loadCustom(context: Context): ThemeDocument {
        ensureThemesDir(context)
        val file = File(themeDir(context), CUSTOM_FILE)
        return decode(file.takeIf { it.exists() }?.readText()).getOrElse {
            captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        }.copy(id = CUSTOM_THEME_ID, name = "Custom")
    }

    fun saveCustom(context: Context, theme: ThemeDocument) {
        ensureThemesDir(context)
        File(themeDir(context), CUSTOM_FILE).writeText(encode(theme.copy(id = CUSTOM_THEME_ID, name = "Custom")))
    }

    fun listThemes(context: Context): List<ThemeDocument> {
        ensureThemesDir(context)
        return themeDir(context).listFiles()
            .orEmpty()
            .filter { it.extension == "json" && it.name != CUSTOM_FILE }
            .mapNotNull { decode(runCatching { it.readText() }.getOrNull()).getOrNull() }
            .sortedBy { it.name.lowercase() }
    }

    fun loadTheme(context: Context, id: String): ThemeDocument? {
        if (id == CUSTOM_THEME_ID) return loadCustom(context)
        val file = File(themeDir(context), "$id.json")
        return decode(file.takeIf { it.exists() }?.readText()).getOrNull()
    }

    /** Save Custom under a new identity, select it, then return Custom to defaults. */
    fun saveCustomAs(context: Context, name: String): ThemeDocument {
        val cleanName = name.trim().ifBlank { "Untitled theme" }
        val saved = loadCustom(context).copy(id = UUID.randomUUID().toString(), name = cleanName)
        ensureThemesDir(context)
        File(themeDir(context), "${saved.id}.json").writeText(encode(saved))
        apply(context, saved)
        saveCustom(context, defaultCustom())
        return saved
    }

    fun importIntoCustom(context: Context, json: String): Result<ThemeDocument> =
        decode(json).map { imported ->
            val custom = imported.copy(id = CUSTOM_THEME_ID, name = "Custom")
            saveCustom(context, custom)
            apply(context, custom)
            custom
        }

    fun export(theme: ThemeDocument): String = encode(theme)

    fun resetCustom(context: Context): ThemeDocument {
        val custom = defaultCustom()
        saveCustom(context, custom)
        apply(context, custom)
        return custom
    }

    private fun defaultCustom(): ThemeDocument =
        ThemeDocument(
            id = CUSTOM_THEME_ID,
            name = "Custom",
            backdrop = BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW),
            toolbar = BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW),
            toolbarBorderWidth = 1f,
            toolbarBorderColor = Color(0xFFFFC247),
            toolbarKeyboardGap = 0f,
            keys = KeyThemeState(),
            suggestionLozenges = SuggestionLozengeThemeState(),
        )

    private fun themeDir(context: Context) = File(context.filesDir, THEMES_DIR)
    private fun ensureThemesDir(context: Context) { themeDir(context).mkdirs() }

    private fun encode(theme: ThemeDocument): String =
        JSONObject()
            .put("schemaVersion", theme.schemaVersion)
            .put("id", theme.id)
            .put("name", theme.name)
            .put("backdrop", encodeBackdrop(theme.backdrop))
            .put("toolbar", encodeBackdrop(theme.toolbar))
            .put("toolbarBorderWidth", theme.toolbarBorderWidth)
            .put("toolbarBorderColor", theme.toolbarBorderColor.value.toString())
            .put("toolbarKeyboardGap", theme.toolbarKeyboardGap)
            .put("keys", encodeKeys(theme.keys))
            .put("suggestionLozenges", encodeLozenges(theme.suggestionLozenges))
            .toString(2)

    private fun decode(raw: String?): Result<ThemeDocument> = runCatching {
        require(!raw.isNullOrBlank()) { "Empty theme document" }
        val json = JSONObject(raw)
        val version = json.optInt("schemaVersion", 1)
        require(version <= CURRENT_THEME_SCHEMA) { "Theme schema $version is newer than this Keywi build" }
        ThemeDocument(
            schemaVersion = version,
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
            name = json.optString("name", "Imported theme"),
            backdrop = decodeBackdrop(json.getJSONObject("backdrop")),
            toolbar = decodeBackdrop(json.getJSONObject("toolbar")),
            toolbarBorderWidth = json.optDouble("toolbarBorderWidth", 1.0).toFloat(),
            toolbarBorderColor = decodeColor(json.optString("toolbarBorderColor"), Color(0xFFFFC247)),
            toolbarKeyboardGap = json.optDouble("toolbarKeyboardGap", 0.0).toFloat(),
            keys = decodeKeys(json.getJSONObject("keys")),
            suggestionLozenges = decodeLozenges(json.getJSONObject("suggestionLozenges")),
        )
    }

    private fun encodeBackdrop(state: BackdropThemeState) = JSONObject()
        .put("preset", state.preset.name).put("angle", state.angleDegrees)
        .put("stops", encodeGradient(state.toBackdrop()).getJSONArray("stops"))
        .put("mode", state.mode.name).put("opacity", state.opacity).put("mediaUri", state.mediaUri)

    private fun decodeBackdrop(json: JSONObject): BackdropThemeState {
        val gradient = decodeGradient(json, BIRDIE_RAINBOW_BACKDROP)
        return BackdropThemeState(
            preset = enumValue(json.optString("preset"), BackdropPreset.CUSTOM),
            angleDegrees = json.optDouble("angle", gradient.angleDegrees.toDouble()).toFloat(),
            stops = gradient.stops,
            mode = enumValue(json.optString("mode"), BackdropMode.COLORFUL),
            opacity = json.optDouble("opacity", 1.0).toFloat().coerceIn(0f, 1f),
            mediaUri = json.optString("mediaUri").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    private fun encodeKeys(state: KeyThemeState) = JSONObject()
        .put("surfaceStyle", state.surfaceStyle.name).put("surfaceGradient", encodeGradient(state.surfaceGradient))
        .put("surfaceColor", state.surfaceColor.value.toString()).put("borderStyle", state.borderStyle.name)
        .put("borderGradient", encodeGradient(state.borderGradient)).put("borderColor", state.borderColor.value.toString())
        .put("shadowColor", state.shadowColor.value.toString()).put("shadowAlpha", state.shadowAlpha)
        .put("shadowElevation", state.shadowElevation)

    private fun decodeKeys(json: JSONObject): KeyThemeState = KeyThemeState(
        surfaceStyle = enumValue(json.optString("surfaceStyle"), KeySurfaceStyle.GRADIENT),
        surfaceGradient = decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT),
        surfaceColor = decodeColor(json.optString("surfaceColor"), Color(0xFF242631)),
        borderStyle = enumValue(json.optString("borderStyle"), KeyBorderStyle.GRADIENT),
        borderGradient = decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER),
        borderColor = decodeColor(json.optString("borderColor"), Color(0xFFFFD86B)),
        shadowColor = decodeColor(json.optString("shadowColor"), Color.Black),
        shadowAlpha = json.optDouble("shadowAlpha", .55).toFloat(),
        shadowElevation = json.optDouble("shadowElevation", 4.0).toFloat(),
    )

    private fun encodeLozenges(state: SuggestionLozengeThemeState) = JSONObject()
        .put("surfaceStyle", state.surfaceStyle.name).put("surfaceGradient", encodeGradient(state.surfaceGradient))
        .put("surfaceColor", state.surfaceColor.value.toString()).put("borderStyle", state.borderStyle.name)
        .put("borderGradient", encodeGradient(state.borderGradient)).put("borderColor", state.borderColor.value.toString())
        .put("borderWidth", state.borderWidth)

    private fun decodeLozenges(json: JSONObject): SuggestionLozengeThemeState = SuggestionLozengeThemeState(
        surfaceStyle = enumValue(json.optString("surfaceStyle"), SuggestionLozengeSurfaceStyle.SOLID),
        surfaceGradient = decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT),
        surfaceColor = decodeColor(json.optString("surfaceColor"), Color(0xB8343540)),
        borderStyle = enumValue(json.optString("borderStyle"), SuggestionLozengeBorderStyle.SOLID),
        borderGradient = decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER),
        borderColor = decodeColor(json.optString("borderColor"), Color(0x70FFFFFF)),
        borderWidth = json.optDouble("borderWidth", 1.0).toFloat(),
    )

    private fun encodeGradient(gradient: KeyboardBackdrop): JSONObject {
        val stops = JSONArray()
        gradient.stops.forEach { stops.put(JSONObject().put("position", it.position).put("color", it.color.value.toString())) }
        return JSONObject().put("angle", gradient.angleDegrees).put("stops", stops)
    }

    private fun decodeGradient(json: JSONObject?, fallback: KeyboardBackdrop): KeyboardBackdrop {
        if (json == null) return fallback
        val array = json.optJSONArray("stops") ?: return fallback
        val stops = (0 until array.length()).mapNotNull { index ->
            runCatching {
                val stop = array.getJSONObject(index)
                KeyboardGradientStop(stop.getDouble("position").toFloat().coerceIn(0f, 1f), Color(stop.getString("color").toULong()))
            }.getOrNull()
        }
        return if (stops.isEmpty()) fallback else KeyboardBackdrop(stops.sortedBy { it.position }, json.optDouble("angle", fallback.angleDegrees.toDouble()).toFloat())
    }

    private fun decodeColor(raw: String?, fallback: Color): Color =
        runCatching { Color(raw!!.toULong()) }.getOrDefault(fallback)

    private inline fun <reified T : Enum<T>> enumValue(raw: String?, fallback: T): T =
        runCatching { enumValueOf<T>(raw.orEmpty()) }.getOrDefault(fallback)
}
