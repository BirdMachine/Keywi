package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ThemeFont(
    val asset: String,
    val name: String,
)

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
    val font: ThemeFont? = null,
)

const val CURRENT_THEME_SCHEMA = 3
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
        if (!prefs.getBoolean(MIGRATED, false)) {
            store(context, captureCurrent(context, "Custom", CUSTOM_THEME_ID), CUSTOM_THEME_ID, allowMissing = true)
            prefs
                .edit()
                .putString(ACTIVE_THEME, CUSTOM_THEME_ID)
                .putBoolean(MIGRATED, true)
                .apply()
        }
        if (!prefs.getBoolean("owned_assets_migrated", false)) {
            val activeId = prefs.getString(ACTIVE_THEME, CUSTOM_THEME_ID) ?: CUSTOM_THEME_ID
            val liveCustom = if (activeId == CUSTOM_THEME_ID) captureCurrent(context, "Custom", CUSTOM_THEME_ID) else null
            ensureThemesDir(context)
            themeDir(context).listFiles().orEmpty().filter { it.isFile && it.extension == "json" }.forEach { file ->
                decode(file.readText()).getOrNull()?.let { old ->
                    val id = if (file.name == CUSTOM_FILE) CUSTOM_THEME_ID else old.id
                    if (!manifest(
                            context,
                            id,
                        ).exists()
                    ) {
                        store(context, old.copy(font = old.font ?: FontPreferences.snapshot(context)), id, allowMissing = true)
                    }
                }
            }
            // Custom's live preferences may contain edits newer than its last saved manifest.
            liveCustom?.let { store(context, it, CUSTOM_THEME_ID, allowMissing = true) }
            // Preserve the old flat JSON files as migration backups.
            prefs.edit().putBoolean("owned_assets_migrated", true).apply()
            val id = prefs.getString(ACTIVE_THEME, CUSTOM_THEME_ID) ?: CUSTOM_THEME_ID
            readStored(context, id)?.let { apply(context, it) }
        }
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
            font = FontPreferences.snapshot(context),
        )

    fun apply(
        context: Context,
        theme: ThemeDocument,
    ) {
        BackdropThemePreferences.save(
            context,
            theme.backdrop.copy(mediaUri = ThemeAssetStore.resolve(context, theme.id, theme.backdrop.mediaUri)),
        )
        ToolbarThemePreferences.save(
            context,
            theme.toolbar.copy(mediaUri = ThemeAssetStore.resolve(context, theme.id, theme.toolbar.mediaUri)),
        )
        ToolbarBorderPreferences.saveWidth(context, theme.toolbarBorderWidth)
        ToolbarBorderPreferences.saveColor(context, theme.toolbarBorderColor)
        ToolbarLayoutPreferences.saveKeyboardGap(context, theme.toolbarKeyboardGap)
        KeyThemePreferences.save(context, theme.keys)
        SuggestionLozengeThemePreferences.save(context, theme.suggestionLozenges)
        FontPreferences.applyTheme(
            context,
            theme.font?.copy(asset = requireNotNull(ThemeAssetStore.resolve(context, theme.id, theme.font.asset))),
        )
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ACTIVE_THEME, theme.id)
            .apply()
    }

    fun beginEditing(
        context: Context,
        source: ThemeDocument,
    ): ThemeDocument {
        val custom = store(context, source, CUSTOM_THEME_ID, allowMissing = true)
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
        return if (id ==
            CUSTOM_THEME_ID
        ) {
            loadCustom(context)
        } else {
            builtInThemes().firstOrNull { it.id == id } ?: loadTheme(context, id)
                ?: loadCustom(context)
        }
    }

    fun loadCustom(context: Context): ThemeDocument =
        readStored(context, CUSTOM_THEME_ID)
            ?: store(
                context,
                decode(
                    File(themeDir(context), CUSTOM_FILE)
                        .takeIf {
                            it.exists()
                        }?.readText(),
                ).getOrElse { captureCurrent(context, "Custom", CUSTOM_THEME_ID) },
                CUSTOM_THEME_ID,
                allowMissing = true,
            )

    /** Active Custom is edited live; previews must not read its older saved manifest. */
    fun customPreview(context: Context): ThemeDocument =
        if (activeThemeId(context) == CUSTOM_THEME_ID) {
            captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        } else {
            loadCustom(context)
        }

    fun saveCustom(
        context: Context,
        theme: ThemeDocument,
    ) {
        store(context, theme, CUSTOM_THEME_ID)
    }

    /** Built-ins are immutable templates. Editing one always forks through beginEditing(). */
    fun builtInThemes(): List<ThemeDocument> = listOf(neonAviaryTheme())

    fun listThemes(context: Context): List<ThemeDocument> {
        ensureMigrated(context)
        val local =
            themeDir(context)
                .listFiles()
                .orEmpty()
                .filter {
                    it.isDirectory && !it.name.startsWith(".") &&
                        it.name != CUSTOM_THEME_ID
                }.mapNotNull { runCatching { readStored(context, it.name) }.getOrNull() }
        return (builtInThemes() + local).distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }

    fun loadTheme(
        context: Context,
        id: String,
    ): ThemeDocument? {
        if (id == CUSTOM_THEME_ID) return loadCustom(context)
        builtInThemes().firstOrNull { it.id == id }?.let { return it }
        return readStored(context, id)
    }

    fun saveCustomAs(
        context: Context,
        name: String,
    ): ThemeDocument {
        val saved = store(context, loadCustom(context).copy(name = name.trim().ifBlank { "Untitled theme" }), UUID.randomUUID().toString())
        apply(context, saved)
        saveCustom(context, defaultCustom())
        return saved
    }

    fun importIntoCustom(
        context: Context,
        json: String,
    ): Result<ThemeDocument> =
        runCatching {
            val imported = decode(json).getOrThrow()
            require(
                assetReferences(imported).none { it.startsWith("assets/") },
            ) { "This theme needs its asset package; import the ZIP instead" }
            store(context, imported, CUSTOM_THEME_ID).also { apply(context, it) }
        }

    fun export(theme: ThemeDocument): String = encode(theme)

    fun resetCustom(context: Context): ThemeDocument = store(context, defaultCustom(), CUSTOM_THEME_ID).also { apply(context, it) }

    private fun manifest(
        context: Context,
        id: String,
    ) = File(ThemeAssetStore.directory(context, id), "theme.json")

    private fun readStored(
        context: Context,
        id: String,
    ): ThemeDocument? =
        decode(
            manifest(context, id)
                .takeIf {
                    it.exists()
                }?.readText(),
        ).getOrNull()?.copy(id = id)

    private fun store(
        context: Context,
        source: ThemeDocument,
        destinationId: String,
        sourceDirectory: File? = null,
        allowMissing: Boolean = false,
    ): ThemeDocument {
        fun asset(reference: String?): String? =
            reference?.let {
                try {
                    ThemeAssetStore.copy(context, destinationId, it, source.id, sourceDirectory)
                } catch (
                    error: Exception,
                ) {
                    if (allowMissing) it else throw IllegalArgumentException("Cannot copy theme asset; re-select the missing file", error)
                }
            }
        val stored =
            source.copy(
                schemaVersion = CURRENT_THEME_SCHEMA,
                id = destinationId,
                name = if (destinationId == CUSTOM_THEME_ID) "Custom" else source.name,
                backdrop = source.backdrop.copy(mediaUri = asset(source.backdrop.mediaUri)),
                toolbar = source.toolbar.copy(mediaUri = asset(source.toolbar.mediaUri)),
                font = source.font?.let { it.copy(asset = requireNotNull(asset(it.asset))) },
            )
        val target = manifest(context, destinationId)
        check(target.parentFile!!.mkdirs() || target.parentFile!!.isDirectory)
        val temporary = File.createTempFile("manifest-", ".tmp", target.parentFile)
        try {
            temporary.writeText(encode(stored))
            check(temporary.renameTo(target)) { "Cannot save theme" }
        } finally {
            temporary.delete()
        }
        return stored
    }

    private fun assetReferences(theme: ThemeDocument): List<String> =
        listOfNotNull(theme.backdrop.mediaUri, theme.toolbar.mediaUri, theme.font?.asset).distinct()

    fun missingAssets(
        context: Context,
        theme: ThemeDocument,
    ): List<String> =
        assetReferences(theme).filter {
            !ThemeAssetStore.readable(context, theme.id, it)
        }

    fun exportPackage(
        context: Context,
        theme: ThemeDocument,
        output: OutputStream,
    ) {
        require(missingAssets(context, theme).isEmpty()) { "A theme asset is missing; re-select it before exporting" }
        // Normalize legacy references before exporting; the archive never includes device-specific URIs.
        val owned = store(context, theme, theme.id)
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("theme.json"))
            zip.write(encode(owned).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            var total = 0L
            assetReferences(owned).forEach { reference ->
                val file = ThemeAssetStore.assetFile(ThemeAssetStore.directory(context, owned.id), reference)
                total += file.length()
                require(total <= ThemeAssetStore.MAX_PACKAGE_BYTES) { "Theme package exceeds 128 MB" }
                zip.putNextEntry(ZipEntry(reference))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun importPackage(
        context: Context,
        input: InputStream,
    ): Result<ThemeDocument> =
        runCatching {
            val staging = File(context.filesDir, "themes/.import-${UUID.randomUUID()}").apply { check(mkdirs()) }
            try {
                var total = 0L
                val seen = mutableSetOf<String>()
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        require(seen.add(entry.name) && seen.size <= 64) { "Duplicate or excessive package entries" }
                        if (entry.isDirectory) {
                            require(entry.name == "assets/") { "Invalid package directory" }
                            continue
                        }
                        val file =
                            if (entry.name ==
                                "theme.json"
                            ) {
                                File(staging, "theme.json")
                            } else {
                                ThemeAssetStore.assetFile(staging, entry.name)
                            }
                        file.parentFile!!.mkdirs()
                        var size = 0L
                        file.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val count = zip.read(buffer)
                                if (count < 0) break
                                total += count
                                size += count
                                require(
                                    total <= ThemeAssetStore.MAX_PACKAGE_BYTES && size <= ThemeAssetStore.MAX_ASSET_BYTES,
                                ) { "Theme package is too large" }
                                if (entry.name == "theme.json") require(size <= 1024 * 1024) { "Theme document is too large" }
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                }
                val imported = decode(File(staging, "theme.json").readText()).getOrThrow()
                assetReferences(
                    imported,
                ).forEach { require(ThemeAssetStore.assetFile(staging, it).isFile) { "Theme package is missing an asset" } }
                imported.font?.let { Typeface.createFromFile(ThemeAssetStore.assetFile(staging, it.asset)) }
                store(context, imported, CUSTOM_THEME_ID, staging).also { apply(context, it) }
            } finally {
                staging.deleteRecursively()
            }
        }

    private fun defaultCustom() =
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

    private fun neonAviaryTheme(): ThemeDocument {
        val night =
            KeyboardBackdrop(
                listOf(
                    KeyboardGradientStop(0f, Color(0xFF090818)),
                    KeyboardGradientStop(.46f, Color(0xFF19113A)),
                    KeyboardGradientStop(1f, Color(0xFF052B35)),
                ),
                132f,
            )
        val keys =
            KeyboardBackdrop(
                listOf(
                    KeyboardGradientStop(0f, Color(0xFF24104A)),
                    KeyboardGradientStop(.5f, Color(0xFF8D1E78)),
                    KeyboardGradientStop(1f, Color(0xFF087B82)),
                ),
                118f,
            )
        val neon =
            KeyboardBackdrop(
                listOf(
                    KeyboardGradientStop(0f, Color(0xFFFF4FCB)),
                    KeyboardGradientStop(.48f, Color(0xFFFFD85A)),
                    KeyboardGradientStop(1f, Color(0xFF45F6E8)),
                ),
                28f,
            )
        return ThemeDocument(
            id = NEON_AVIARY_THEME_ID,
            name = "Neon Aviary",
            backdrop = BackdropThemeState(BackdropPreset.CUSTOM, night.angleDegrees, night.stops, BackdropMode.COLORFUL, 1f, null),
            toolbar =
                BackdropThemeState(
                    BackdropPreset.CUSTOM,
                    105f,
                    listOf(KeyboardGradientStop(0f, Color(0xE61A0C35)), KeyboardGradientStop(1f, Color(0xE6053540))),
                    BackdropMode.COLORFUL,
                    .96f,
                    null,
                ),
            toolbarBorderWidth = 1.4f,
            toolbarBorderColor = Color(0xFFFF67D4),
            toolbarKeyboardGap = 3f,
            keys =
                KeyThemeState(
                    surfaceStyle = KeySurfaceStyle.GRADIENT,
                    surfaceGradient = keys,
                    surfaceColor = Color(0xFF24104A),
                    borderStyle = KeyBorderStyle.GRADIENT,
                    borderGradient = neon,
                    borderColor = Color(0xFFFFD85A),
                    shadowColor = Color(0xFF000000),
                    shadowAlpha = .62f,
                    shadowElevation = 5f,
                ),
            suggestionLozenges =
                SuggestionLozengeThemeState(
                    surfaceStyle = SuggestionLozengeSurfaceStyle.GRADIENT,
                    surfaceGradient =
                        KeyboardBackdrop(
                            listOf(KeyboardGradientStop(0f, Color(0xCC4A155E)), KeyboardGradientStop(1f, Color(0xCC07535C))),
                            90f,
                        ),
                    surfaceColor = Color(0xCC28113F),
                    borderStyle = SuggestionLozengeBorderStyle.GRADIENT,
                    borderGradient = neon,
                    borderColor = Color(0xFFFF67D4),
                    borderWidth = 1.2f,
                ),
        )
    }

    private fun themeDir(context: Context) = File(context.filesDir, THEMES_DIR)

    private fun ensureThemesDir(context: Context) {
        themeDir(context).mkdirs()
    }

    private fun encode(theme: ThemeDocument): String =
        JSONObject()
            .put(
                "schemaVersion",
                theme.schemaVersion,
            ).put(
                "id",
                theme.id,
            ).put(
                "name",
                theme.name,
            ).put(
                "backdrop",
                encodeBackdrop(theme.backdrop),
            ).put(
                "toolbar",
                encodeBackdrop(theme.toolbar),
            ).put(
                "toolbarBorderWidth",
                theme.toolbarBorderWidth,
            ).put(
                "toolbarBorderColor",
                theme.toolbarBorderColor.value.toString(),
            ).put(
                "toolbarKeyboardGap",
                theme.toolbarKeyboardGap,
            ).put(
                "keys",
                encodeKeys(theme.keys),
            ).put("suggestionLozenges", encodeLozenges(theme.suggestionLozenges))
            .put(
                "font",
                theme.font?.let {
                    JSONObject().put("asset", it.asset).put("name", it.name)
                },
            ).toString(2)

    private fun decode(raw: String?): Result<ThemeDocument> =
        runCatching {
            require(!raw.isNullOrBlank()) { "Empty theme document" }
            val json = JSONObject(raw)
            val version = json.optInt("schemaVersion", 1)
            require(version <= CURRENT_THEME_SCHEMA)
            ThemeDocument(
                version,
                json.optString("id").ifBlank {
                    UUID.randomUUID().toString()
                },
                json.optString(
                    "name",
                    "Imported theme",
                ),
                decodeBackdrop(
                    json.getJSONObject("backdrop"),
                ),
                decodeBackdrop(
                    json.getJSONObject("toolbar"),
                ),
                json
                    .optDouble(
                        "toolbarBorderWidth",
                        1.0,
                    ).toFloat(),
                decodeColor(
                    json.optString("toolbarBorderColor"),
                    Color(0xFFFFC247),
                ),
                json
                    .optDouble(
                        "toolbarKeyboardGap",
                        0.0,
                    ).toFloat(),
                decodeKeys(
                    json.getJSONObject("keys"),
                ),
                decodeLozenges(json.getJSONObject("suggestionLozenges")),
                json.optJSONObject("font")?.let {
                    ThemeFont(it.getString("asset"), it.optString("name", "Custom font"))
                },
            )
        }

    private fun encodeBackdrop(state: BackdropThemeState) =
        JSONObject()
            .put(
                "preset",
                state.preset.name,
            ).put(
                "angle",
                state.angleDegrees,
            ).put(
                "stops",
                encodeGradient(state.toBackdrop()).getJSONArray("stops"),
            ).put("mode", state.mode.name)
            .put("opacity", state.opacity)
            .put("mediaUri", state.mediaUri)

    private fun decodeBackdrop(json: JSONObject): BackdropThemeState {
        val gradient = decodeGradient(json, BIRDIE_RAINBOW_BACKDROP)
        return BackdropThemeState(
            enumValue(json.optString("preset"), BackdropPreset.CUSTOM),
            json.optDouble("angle", gradient.angleDegrees.toDouble()).toFloat(),
            gradient.stops,
            enumValue(json.optString("mode"), BackdropMode.COLORFUL),
            json.optDouble("opacity", 1.0).toFloat().coerceIn(0f, 1f),
            json.optString("mediaUri").takeIf {
                it.isNotBlank() &&
                    it != "null"
            },
        )
    }

    private fun encodeKeys(state: KeyThemeState) =
        JSONObject()
            .put(
                "surfaceStyle",
                state.surfaceStyle.name,
            ).put(
                "surfaceGradient",
                encodeGradient(state.surfaceGradient),
            ).put(
                "surfaceColor",
                state.surfaceColor.value.toString(),
            ).put(
                "borderStyle",
                state.borderStyle.name,
            ).put(
                "borderGradient",
                encodeGradient(state.borderGradient),
            ).put(
                "borderColor",
                state.borderColor.value.toString(),
            ).put(
                "shadowColor",
                state.shadowColor.value.toString(),
            ).put("shadowAlpha", state.shadowAlpha)
            .put("shadowElevation", state.shadowElevation)
            .put("borderDistribution", state.borderDistribution.name)
            .put("borderPhaseStepDegrees", state.borderPhaseStepDegrees)
            .put("borderRandomSeed", state.borderRandomSeed)
            .put("keyOverrides", JSONObject().apply { state.keyOverrides.forEach { (id, design) -> put(id, encodeDesign(design)) } })
            .put("keyGroups", JSONArray().apply { state.keyGroups.forEach { addGroup(it) } })

    private fun decodeKeys(json: JSONObject) =
        KeyThemeState(
            enumValue(json.optString("surfaceStyle"), KeySurfaceStyle.GRADIENT),
            decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT),
            decodeColor(json.optString("surfaceColor"), Color(0xFF242631)),
            enumValue(json.optString("borderStyle"), KeyBorderStyle.GRADIENT),
            decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER),
            decodeColor(json.optString("borderColor"), Color(0xFFFFD86B)),
            decodeColor(json.optString("shadowColor"), Color.Black),
            json.optDouble("shadowAlpha", .55).toFloat(),
            json.optDouble("shadowElevation", 4.0).toFloat(),
            borderDistribution = enumValue(json.optString("borderDistribution"), GradientDistribution.SAME),
            borderPhaseStepDegrees = json.optDouble("borderPhaseStepDegrees", 30.0).toFloat(),
            borderRandomSeed = json.optInt("borderRandomSeed", 0),
            keyOverrides = decodeOverrides(json.optJSONObject("keyOverrides")?.toString()),
            keyGroups = decodeGroups(json.optJSONArray("keyGroups")?.toString()),
        )

    private fun encodeLozenges(state: SuggestionLozengeThemeState) =
        JSONObject()
            .put(
                "surfaceStyle",
                state.surfaceStyle.name,
            ).put(
                "surfaceGradient",
                encodeGradient(state.surfaceGradient),
            ).put(
                "surfaceColor",
                state.surfaceColor.value.toString(),
            ).put(
                "borderStyle",
                state.borderStyle.name,
            ).put(
                "borderGradient",
                encodeGradient(state.borderGradient),
            ).put(
                "borderColor",
                state.borderColor.value.toString(),
            ).put(
                "borderWidth",
                state.borderWidth,
            ).put(
                "currentWordBackgroundColor",
                state.currentWordBackgroundColor.value.toString(),
            ).put("currentWordHighlightColor", state.currentWordHighlightColor?.value?.toString())

    private fun decodeLozenges(json: JSONObject) =
        SuggestionLozengeThemeState(
            enumValue(json.optString("surfaceStyle"), SuggestionLozengeSurfaceStyle.SOLID),
            decodeGradient(json.optJSONObject("surfaceGradient"), BIRDIE_KEY_GRADIENT),
            decodeColor(json.optString("surfaceColor"), Color(0xB8343540)),
            enumValue(json.optString("borderStyle"), SuggestionLozengeBorderStyle.SOLID),
            decodeGradient(json.optJSONObject("borderGradient"), BIRDIE_GOLD_BORDER),
            decodeColor(json.optString("borderColor"), Color(0x70FFFFFF)),
            json.optDouble("borderWidth", 1.0).toFloat(),
            decodeColor(json.optString("currentWordBackgroundColor"), Color(0xFF031005)),
            json
                .optString("currentWordHighlightColor")
                .takeIf {
                    it.isNotBlank() &&
                        it != "null"
                }?.let { decodeColor(it, Color(0xFF52FF52)) },
        )

    private fun encodeGradient(gradient: KeyboardBackdrop): JSONObject {
        val stops = JSONArray()
        gradient.stops.forEach { stops.put(JSONObject().put("position", it.position).put("color", it.color.value.toString())) }
        return JSONObject().put("angle", gradient.angleDegrees).put("stops", stops)
    }

    private fun decodeGradient(
        json: JSONObject?,
        fallback: KeyboardBackdrop,
    ): KeyboardBackdrop {
        if (json ==
            null
        ) {
            return fallback
        }
        val array =
            json.optJSONArray("stops") ?: return fallback
        val stops =
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val s = array.getJSONObject(i)
                    KeyboardGradientStop(s.getDouble("position").toFloat().coerceIn(0f, 1f), Color(s.getString("color").toULong()))
                }.getOrNull()
            }
        return if (stops.isEmpty()) {
            fallback
        } else {
            KeyboardBackdrop(
                stops.sortedBy {
                    it.position
                },
                json.optDouble("angle", fallback.angleDegrees.toDouble()).toFloat(),
            )
        }
    }

    private fun decodeColor(
        raw: String?,
        fallback: Color,
    ) = runCatching { Color(raw!!.toULong()) }.getOrDefault(fallback)

    private inline fun <reified T : Enum<T>> enumValue(
        raw: String?,
        fallback: T,
    ): T =
        runCatching {
            enumValueOf<T>(raw.orEmpty())
        }.getOrDefault(fallback)
}
