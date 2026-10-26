package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject

internal enum class QuickTool(val title: String) {
    SUGGESTIONS("Suggestions"), FEEDBACK("Feedback mode"), SOUNDS("Sound pack"), GIFS("Keypress images & GIFs"),
    POWER("Power mode"), TOOLBAR("Toolbar controls"), THEMES("Themes & appearance"), TASKER("Tasker theme updates"),
    AUDIO("Manage key sounds"), EFFECTS("Manage keypress images & GIFs"),
}

internal data class ManagedEntry(
    val text: String,
    val label: String = "",
    val alternates: List<String> = emptyList(),
    val tags: Set<String> = emptySet(),
    val enabled: Boolean = true,
) {
    fun item() = StringPaletteItem(text, label.ifBlank { null }, alternates, tags)
}

internal data class ManagedCollection(
    val id: String,
    val title: String,
    val glyph: String = "✧",
    val entries: List<ManagedEntry> = emptyList(),
    val enabled: Boolean = true,
)

internal fun defaultKaomoji() = KaomojiLibrary.categories.map { c ->
    ManagedCollection(c.id, c.title, c.glyph, c.items.map { ManagedEntry(it.text, it.label.orEmpty(), it.alternates, it.tags) })
}

internal data class ToolPageConfig(
    val pages: List<String> = listOf("CHARACTERS", "FORMATTING", "KAOMOJI", "QUICK_SETTINGS", "SNIPPETS"),
    val hiddenPages: Set<String> = setOf("SNIPPETS"),
    val formats: List<String> = FancyTextFormatter.styles.map { it.id },
    val hiddenFormats: Set<String> = emptySet(),
    val formatCategories: List<String> = FancyTextFormatter.Category.entries.map { it.name },
    val hiddenFormatCategories: Set<String> = emptySet(),
    val quickTools: List<String> = QuickTool.entries.map { it.name },
    val hiddenQuickTools: Set<String> = emptySet(),
    val kaomoji: List<ManagedCollection> = defaultKaomoji(),
    val snippets: List<ManagedCollection> = emptyList(),
) {
    fun visiblePages(): List<ToolbarPage> = ordered(pages, ToolbarPage.entries.map { it.name })
        .filter { it == "CHARACTERS" || it !in hiddenPages }.map { ToolbarPage.valueOf(it) }

    fun next(page: ToolbarPage, delta: Int): ToolbarPage {
        val visible = visiblePages()
        val index = visible.indexOf(page)
        return if (index < 0) visible.first() else visible[Math.floorMod(index + delta, visible.size)]
    }

    fun visibleFormats() = ordered(formats, FancyTextFormatter.styles.map { it.id })
        .filterNot { it in hiddenFormats }.mapNotNull { id -> FancyTextFormatter.styles.firstOrNull { it.id == id } }
        .filterNot { it.category.name in hiddenFormatCategories }

    fun visibleQuickTools() = ordered(quickTools, QuickTool.entries.map { it.name })
        .filterNot { it in hiddenQuickTools }.map { QuickTool.valueOf(it) }
}

internal fun ordered(saved: List<String>, known: List<String>) = (saved.filter { it in known } + known).distinct()
internal fun <T> List<T>.moved(index: Int, delta: Int): List<T> = toMutableList().apply {
    val target = index + delta
    if (index in indices && target in indices) add(target, removeAt(index))
}

internal object ToolPagePreferences {
    const val PREFS = "keywi_tool_pages"
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun load(context: Context): ToolPageConfig = decode(prefs(context).getString("config", null))
    fun save(context: Context, config: ToolPageConfig) { prefs(context).edit().putString("config", encode(config)).apply() }

    fun encode(c: ToolPageConfig): String = JSONObject().apply {
        put("version", 1)
        put("pages", JSONArray(c.pages)); put("hiddenPages", JSONArray(c.hiddenPages.toList()))
        put("formats", JSONArray(c.formats)); put("hiddenFormats", JSONArray(c.hiddenFormats.toList()))
        put("formatCategories", JSONArray(c.formatCategories)); put("hiddenFormatCategories", JSONArray(c.hiddenFormatCategories.toList()))
        put("quickTools", JSONArray(c.quickTools)); put("hiddenQuickTools", JSONArray(c.hiddenQuickTools.toList()))
        fun collections(values: List<ManagedCollection>) = JSONArray().apply {
            values.forEach { group -> put(JSONObject().apply {
                put("id", group.id); put("title", group.title); put("glyph", group.glyph); put("enabled", group.enabled)
                put("entries", JSONArray().apply { group.entries.forEach { entry -> put(JSONObject().apply {
                    put("text", entry.text); put("label", entry.label); put("enabled", entry.enabled)
                    put("alternates", JSONArray(entry.alternates)); put("tags", JSONArray(entry.tags.toList()))
                }) } })
            }) }
        }
        put("kaomoji", collections(c.kaomoji)); put("snippets", collections(c.snippets))
    }.toString()

    fun decode(raw: String?): ToolPageConfig = runCatching {
        if (raw == null) return ToolPageConfig()
        val j = JSONObject(raw)
        val d = ToolPageConfig()
        fun strings(key: String, fallback: List<String>) = j.optJSONArray(key)?.strings() ?: fallback
        fun collections(key: String, fallback: List<ManagedCollection>) = j.optJSONArray(key)?.let { array ->
            (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.let { g ->
                val id = g.optString("id")
                if (id.isBlank()) null else ManagedCollection(id, g.optString("title", "Collection"), g.optString("glyph", "✧"),
                    g.optJSONArray("entries")?.let { items -> (0 until items.length()).mapNotNull { n -> items.optJSONObject(n)?.let { e ->
                        e.optString("text").takeIf { it.isNotBlank() }?.let { text -> ManagedEntry(text, e.optString("label"), e.optJSONArray("alternates")?.strings().orEmpty(), e.optJSONArray("tags")?.strings().orEmpty().toSet(), e.optBoolean("enabled", true)) }
                    } } } ?: emptyList(), g.optBoolean("enabled", true))
            } }.distinctBy { it.id }
        } ?: fallback
        ToolPageConfig(strings("pages", d.pages), strings("hiddenPages", d.hiddenPages.toList()).toSet() - "CHARACTERS",
            strings("formats", d.formats), strings("hiddenFormats", emptyList()).toSet(),
            strings("formatCategories", d.formatCategories), strings("hiddenFormatCategories", emptyList()).toSet(),
            strings("quickTools", d.quickTools), strings("hiddenQuickTools", emptyList()).toSet(),
            collections("kaomoji", d.kaomoji), collections("snippets", d.snippets))
    }.getOrElse { ToolPageConfig() }

    private fun JSONArray.strings() = (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }.distinct()
}

@Composable
internal fun rememberToolPageConfig(): ToolPageConfig {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences(ToolPagePreferences.PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return remember(context, revision) { ToolPagePreferences.load(context) }
}
