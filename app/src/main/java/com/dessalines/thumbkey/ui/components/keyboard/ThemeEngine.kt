package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

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
private const val NEON_AVIARY_THEME_ID = "builtin-neon-aviary"

object ThemeEngine {
    private const val PREFS = "keywi_theme_engine"
    private const val ACTIVE_THEME = "active_theme_id"
    private const val MIGRATED = "theme_engine_migrated"
    private const val THEMES_DIR = "themes"
    private const val CUSTOM_FILE = "custom.json"

    fun ensureMigrated(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATED, false)) return
        saveCustom(context, captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        prefs.edit().putString(ACTIVE_THEME, CUSTOM_THEME_ID).putBoolean(MIGRATED, true).apply()
    }

    fun captureCurrent(context: Context, name: String, id: String = UUID.randomUUID().toString()): ThemeDocument =
        ThemeDocument(id = id, name = name, backdrop = BackdropThemePreferences.load(context), toolbar = ToolbarThemePreferences.load(context), toolbarBorderWidth = ToolbarBorderPreferences.loadWidth(context), toolbarBorderColor = ToolbarBorderPreferences.loadColor(context), toolbarKeyboardGap = ToolbarLayoutPreferences.loadKeyboardGap(context), keys = KeyThemePreferences.load(context), suggestionLozenges = SuggestionLozengeThemePreferences.load(context))

    fun apply(context: Context, theme: ThemeDocument) {
        BackdropThemePreferences.save(context, theme.backdrop); ToolbarThemePreferences.save(context, theme.toolbar)
        ToolbarBorderPreferences.saveWidth(context, theme.toolbarBorderWidth); ToolbarBorderPreferences.saveColor(context, theme.toolbarBorderColor); ToolbarLayoutPreferences.saveKeyboardGap(context, theme.toolbarKeyboardGap)
        KeyThemePreferences.save(context, theme.keys); SuggestionLozengeThemePreferences.save(context, theme.suggestionLozenges)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ACTIVE_THEME, theme.id).apply()
    }

    fun beginEditing(context: Context, source: ThemeDocument): ThemeDocument { val custom = source.copy(id = CUSTOM_THEME_ID, name = "Custom"); saveCustom(context, custom); apply(context, custom); return custom }
    fun activeThemeId(context: Context): String { ensureMigrated(context); return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ACTIVE_THEME, CUSTOM_THEME_ID) ?: CUSTOM_THEME_ID }
    fun loadActive(context: Context): ThemeDocument { ensureMigrated(context); val id = activeThemeId(context); return if (id == CUSTOM_THEME_ID) loadCustom(context) else builtInThemes().firstOrNull { it.id == id } ?: loadTheme(context, id) ?: loadCustom(context) }
    fun loadCustom(context: Context): ThemeDocument { ensureThemesDir(context); val file = File(themeDir(context), CUSTOM_FILE); return decode(file.takeIf { it.exists() }?.readText()).getOrElse { captureCurrent(context, "Custom", CUSTOM_THEME_ID) }.copy(id = CUSTOM_THEME_ID, name = "Custom") }
    fun saveCustom(context: Context, theme: ThemeDocument) { ensureThemesDir(context); File(themeDir(context), CUSTOM_FILE).writeText(encode(theme.copy(id = CUSTOM_THEME_ID, name = "Custom"))) }

    /** Built-ins are immutable templates. Editing one always forks through beginEditing(). */
    fun builtInThemes(): List<ThemeDocument> = listOf(neonAviaryTheme())

    fun listThemes(context: Context): List<ThemeDocument> {
        ensureThemesDir(context)
        val local = themeDir(context).listFiles().orEmpty().filter { it.extension == "json" && it.name != CUSTOM_FILE }.mapNotNull { decode(runCatching { it.readText() }.getOrNull()).getOrNull() }
        return (builtInThemes() + local).distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }
    fun loadTheme(context: Context, id: String): ThemeDocument? { if (id == CUSTOM_THEME_ID) return loadCustom(context); builtInThemes().firstOrNull { it.id == id }?.let { return it }; return decode(File(themeDir(context), "$id.json").takeIf { it.exists() }?.readText()).getOrNull() }
    fun saveCustomAs(context: Context, name: String): ThemeDocument { val saved = loadCustom(context).copy(id = UUID.randomUUID().toString(), name = name.trim().ifBlank { "Untitled theme" }); ensureThemesDir(context); File(themeDir(context), "${saved.id}.json").writeText(encode(saved)); apply(context, saved); saveCustom(context, defaultCustom()); return saved }
    fun importIntoCustom(context: Context, json: String): Result<ThemeDocument> = decode(json).map { imported -> imported.copy(id = CUSTOM_THEME_ID, name = "Custom").also { saveCustom(context, it); apply(context, it) } }
    fun export(theme: ThemeDocument): String = encode(theme)
    fun resetCustom(context: Context): ThemeDocument = defaultCustom().also { saveCustom(context, it); apply(context, it) }

    private fun defaultCustom() = ThemeDocument(id = CUSTOM_THEME_ID, name = "Custom", backdrop = BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW), toolbar = BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW), toolbarBorderWidth = 1f, toolbarBorderColor = Color(0xFFFFC247), toolbarKeyboardGap = 0f, keys = KeyThemeState(), suggestionLozenges = SuggestionLozengeThemeState())

    private fun neonAviaryTheme(): ThemeDocument {
        val night = KeyboardBackdrop(132f, listOf(KeyboardGradientStop(0f, Color(0xFF090818)), KeyboardGradientStop(.46f, Color(0xFF19113A)), KeyboardGradientStop(1f, Color(0xFF052B35))))
        val keys = KeyboardBackdrop(118f, listOf(KeyboardGradientStop(0f, Color(0xFF24104A)), KeyboardGradientStop(.5f, Color(0xFF8D1E78)), KeyboardGradientStop(1f, Color(0xFF087B82))))
        val neon = KeyboardBackdrop(28f, listOf(KeyboardGradientStop(0f, Color(0xFFFF4FCB)), KeyboardGradientStop(.48f, Color(0xFFFFD85A)), KeyboardGradientStop(1f, Color(0xFF45F6E8))))
        return ThemeDocument(
            id = NEON_AVIARY_THEME_ID, name = "Neon Aviary",
            backdrop = BackdropThemeState(BackdropPreset.CUSTOM, night.angleDegrees, night.stops, BackdropMode.COLORFUL, 1f, null),
            toolbar = BackdropThemeState(BackdropPreset.CUSTOM, 105f, listOf(KeyboardGradientStop(0f, Color(0xE61A0C35)), KeyboardGradientStop(1f, Color(0xE6053540))), BackdropMode.COLORFUL, .96f, null),
            toolbarBorderWidth = 1.4f, toolbarBorderColor = Color(0xFFFF67D4), toolbarKeyboardGap = 3f,
            keys = KeyThemeState(surfaceStyle = KeySurfaceStyle.GRADIENT, surfaceGradient = keys, surfaceColor = Color(0xFF24104A), borderStyle = KeyBorderStyle.GRADIENT, borderGradient = neon, borderColor = Color(0xFFFFD85A), shadowColor = Color(0xFF000000), shadowAlpha = .62f, shadowElevation = 5f),
            suggestionLozenges = SuggestionLozengeThemeState(surfaceStyle = SuggestionLozengeSurfaceStyle.GRADIENT, surfaceGradient = KeyboardBackdrop(90f, listOf(KeyboardGradientStop(0f, Color(0xCC4A155E)), KeyboardGradientStop(1f, Color(0xCC07535C)))), surfaceColor = Color(0xCC28113F), borderStyle = SuggestionLozengeBorderStyle.GRADIENT, borderGradient = neon, borderColor = Color(0xFFFF67D4), borderWidth = 1.2f),
        )
    }

    private fun themeDir(context: Context) = File(context.filesDir, THEMES_DIR)
    private fun ensureThemesDir(context: Context) { themeDir(context).mkdirs() }
    private fun encode(theme: ThemeDocument): String = JSONObject().put("schemaVersion", theme.schemaVersion).put("id", theme.id).put("name", theme.name).put("backdrop", encodeBackdrop(theme.backdrop)).put("toolbar", encodeBackdrop(theme.toolbar)).put("toolbarBorderWidth", theme.toolbarBorderWidth).put("toolbarBorderColor", theme.toolbarBorderColor.value.toString()).put("toolbarKeyboardGap", theme.toolbarKeyboardGap).put("keys", encodeKeys(theme.keys)).put("suggestionLozenges", encodeLozenges(theme.suggestionLozenges)).toString(2)
    private fun decode(raw: String?): Result<ThemeDocument> = runCatching { require(!raw.isNullOrBlank()) { "Empty theme document" }; val json = JSONObject(raw); val version = json.optInt("schemaVersion", 1); require(version <= CURRENT_THEME_SCHEMA); ThemeDocument(version, json.optString("id").ifBlank { UUID.randomUUID().toString() }, json.optString("name", "Imported theme"), decodeBackdrop(json.getJSONObject("backdrop")), decodeBackdrop(json.getJSONObject("toolbar")), json.optDouble("toolbarBorderWidth", 1.0).toFloat(), decodeColor(json.optString("toolbarBorderColor"), Color(0xFFFFC247)), json.optDouble("toolbarKeyboardGap", 0.0).toFloat(), decodeKeys(json.getJSONObject("keys")), decodeLozenges(json.getJSONObject("suggestionLozenges"))) }
    private fun encodeBackdrop(state: BackdropThemeState) = JSONObject().put("preset", state.preset.name).put("angle", state.angleDegrees).put("stops", encodeGradient(state.toBackdrop()).getJSONArray("stops")).put("mode", state.mode.name).put("opacity", state.opacity).put("mediaUri", state.mediaUri)
    private fun decodeBackdrop(json: JSONObject): BackdropThemeState { val gradient = decodeGradient(json, BIRDIE_RAINBOW_BACKDROP); return BackdropThemeState(enumValue(json.optString("preset"), BackdropPreset.CUSTOM), json.optDouble("angle", gradient.angleDegrees.toDouble()).toFloat(), gradient.stops, enumValue(json.optString("mode"), BackdropMode.COLORFUL), json.optDouble("opacity", 1.0).toFloat().coerceIn(0f, 1f), json.optString("mediaUri").takeIf { it.isNotBlank() && it != "null" }) }
    private fun encodeKeys(state: KeyThemeState) = JSONObject().put("surfaceStyle", state.surfaceStyle.name).put("surfaceGradient", encodeGradient(state.surfaceGradient)).put("surfaceColor", state.surfaceColor.value.toString()).put("borderStyle", state.borderStyle.name).put("borderGradient", encodeGradient(state.borderGradient)).put("borderColor", state.borderColor.value.toString()).put("shadowColor", state.shadowColor.value.toString()).put("shadowAlpha", state.shadowAlpha).put("shadowElevation", state.shadowElevation)
    private fun decodeKeys(json: JSONObject) = KeyThemeState(enumValue(json.optString("surfaceStyle"), KeySurfaceStyle.GRADIENT), decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT), decodeColor(json.optString("surfaceColor"), Color(0xFF242631)), enumValue(json.optString("borderStyle"), KeyBorderStyle.GRADIENT), decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER), decodeColor(json.optString("borderColor"), Color(0xFFFFD86B)), decodeColor(json.optString("shadowColor"), Color.Black), json.optDouble("shadowAlpha", .55).toFloat(), json.optDouble("shadowElevation", 4.0).toFloat())
    private fun encodeLozenges(state: SuggestionLozengeThemeState) = JSONObject().put("surfaceStyle", state.surfaceStyle.name).put("surfaceGradient", encodeGradient(state.surfaceGradient)).put("surfaceColor", state.surfaceColor.value.toString()).put("borderStyle", state.borderStyle.name).put("borderGradient", encodeGradient(state.borderGradient)).put("borderColor", state.borderColor.value.toString()).put("borderWidth", state.borderWidth)
    private fun decodeLozenges(json: JSONObject) = SuggestionLozengeThemeState(enumValue(json.optString("surfaceStyle"), SuggestionLozengeSurfaceStyle.SOLID), decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT), decodeColor(json.optString("surfaceColor"), Color(0xB8343540)), enumValue(json.optString("borderStyle"), SuggestionLozengeBorderStyle.SOLID), decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER), decodeColor(json.optString("borderColor"), Color(0x70FFFFFF)), json.optDouble("borderWidth", 1.0).toFloat())
    private fun encodeGradient(gradient: KeyboardBackdrop): JSONObject { val stops = JSONArray(); gradient.stops.forEach { stops.put(JSONObject().put("position", it.position).put("color", it.color.value.toString())) }; return JSONObject().put("angle", gradient.angleDegrees).put("stops", stops) }
    private fun decodeGradient(json: JSONObject?, fallback: KeyboardBackdrop): KeyboardBackdrop { if (json == null) return fallback; val array = json.optJSONArray("stops") ?: return fallback; val stops = (0 until array.length()).mapNotNull { i -> runCatching { val s = array.getJSONObject(i); KeyboardGradientStop(s.getDouble("position").toFloat().coerceIn(0f, 1f), Color(s.getString("color").toULong())) }.getOrNull() }; return if (stops.isEmpty()) fallback else KeyboardBackdrop(stops.sortedBy { it.position }, json.optDouble("angle", fallback.angleDegrees.toDouble()).toFloat()) }
    private fun decodeColor(raw: String?, fallback: Color) = runCatching { Color(raw!!.toULong()) }.getOrDefault(fallback)
    private inline fun <reified T : Enum<T>> enumValue(raw: String?, fallback: T): T = runCatching { enumValueOf<T>(raw.orEmpty()) }.getOrDefault(fallback)
}
