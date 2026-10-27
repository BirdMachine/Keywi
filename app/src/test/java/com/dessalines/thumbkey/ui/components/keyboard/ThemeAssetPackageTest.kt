package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.dessalines.thumbkey.R
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeAssetPackageTest {
    private val context: Application get() = ApplicationProvider.getApplicationContext()
    private val gif = android.util.Base64.decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7", android.util.Base64.DEFAULT)

    private fun selectedGif(): File = File(context.cacheDir, "original.gif").also { it.writeBytes(gif) }

    private fun ownedFile(
        theme: ThemeDocument,
        reference: String,
    ): File = ThemeAssetStore.assetFile(ThemeAssetStore.directory(context, theme.id), reference)

    private fun packageBytes(theme: ThemeDocument): ByteArray =
        ByteArrayOutputStream()
            .also {
                ThemeEngine.exportPackage(context, theme, it)
            }.toByteArray()

    private fun archive(entries: Map<String, ByteArray>): ByteArray =
        ByteArrayOutputStream()
            .also { output ->
                ZipOutputStream(output).use { zip ->
                    entries.forEach { (name, bytes) ->
                        zip.putNextEntry(ZipEntry(name))
                        zip.write(bytes)
                        zip.closeEntry()
                    }
                }
            }.toByteArray()

    @Test
    fun selectedMediaIsCopiedAndSavedThemeHasIndependentAssets() {
        ThemeEngine.ensureMigrated(context)
        val original = selectedGif()
        val uri = ThemeAssetStore.copySelected(context, Uri.fromFile(original))
        BackdropThemePreferences.save(context, BackdropThemePreferences.load(context).copy(mode = BackdropMode.GIF, mediaUri = uri))
        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        val custom = ThemeEngine.loadCustom(context)
        val saved = ThemeEngine.saveCustomAs(context, "Independent GIF")
        original.delete()
        assertArrayEquals(gif, ownedFile(custom, requireNotNull(custom.backdrop.mediaUri)).readBytes())
        assertArrayEquals(gif, ownedFile(saved, requireNotNull(saved.backdrop.mediaUri)).readBytes())
        assertNotEquals(ownedFile(custom, custom.backdrop.mediaUri!!), ownedFile(saved, saved.backdrop.mediaUri!!))
        ThemeAssetStore.directory(context, CUSTOM_THEME_ID).deleteRecursively()
        ThemeEngine.beginEditing(context, saved)
        assertArrayEquals(gif, ownedFile(ThemeEngine.loadCustom(context), saved.backdrop.mediaUri!!).readBytes())
        assertEquals(
            Uri.fromFile(ownedFile(ThemeEngine.loadCustom(context), saved.backdrop.mediaUri!!)).toString(),
            BackdropThemePreferences.load(context).mediaUri,
        )
    }

    @Test
    fun packageSurvivesSourceDeletionAndIncludesFont() {
        ThemeEngine.ensureMigrated(context)
        val original = selectedGif()
        val font =
            File(context.cacheDir, "example.otf").also { target ->
                context.resources.openRawResource(R.font.nasin_nanpa_4_0_2).use { input ->
                    target.outputStream().use { input.copyTo(it) }
                }
            }
        assertTrue(FontPreferences.importFont(context, Uri.fromFile(font)))
        val source = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        ThemeEngine.saveCustom(
            context,
            source.copy(backdrop = source.backdrop.copy(mode = BackdropMode.GIF, mediaUri = Uri.fromFile(original).toString())),
        )
        val saved = ThemeEngine.saveCustomAs(context, "Portable")
        val bytes = packageBytes(saved)
        val names = mutableListOf<String>()
        var document = ""
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry =
                    zip.nextEntry ?: break
                names += entry.name
                if (entry.name ==
                    "theme.json"
                ) {
                    document = zip.readBytes().toString(Charsets.UTF_8)
                }
            }
        }
        assertTrue(names.contains("theme.json"))
        assertEquals(3, names.size)
        assertFalse(document.contains("file://"))
        assertFalse(document.contains("content://"))
        original.delete()
        font.delete()
        ThemeAssetStore.directory(context, saved.id).deleteRecursively()
        ThemeAssetStore.directory(context, CUSTOM_THEME_ID).deleteRecursively()
        val imported = ThemeEngine.importPackage(context, ByteArrayInputStream(bytes)).getOrThrow()
        assertArrayEquals(gif, ownedFile(imported, imported.backdrop.mediaUri!!).readBytes())
        assertNotNull(FontPreferences.typeface(context))
        assertEquals("example.otf", FontPreferences.displayName(context))
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertTrue(ThemeEngine.missingAssets(context, imported).isEmpty())
    }

    @Test
    fun migrationCopiesLegacyAssetsAndReportsMissingOnesWithoutLosingOriginals() {
        val original = selectedGif()
        val source = ThemeEngine.captureCurrent(context, "Legacy", "legacy-theme")
        val legacy =
            source.copy(
                backdrop = source.backdrop.copy(mode = BackdropMode.GIF, mediaUri = Uri.fromFile(original).toString()),
                toolbar =
                    source.toolbar.copy(
                        mode = BackdropMode.IMAGE,
                        mediaUri = Uri.fromFile(File(context.filesDir, "themes/missing.gif")).toString(),
                    ),
            )
        val file =
            File(context.filesDir, "themes/legacy-theme.json").also {
                it.parentFile!!.mkdirs()
                it.writeText(JSONObject(ThemeEngine.export(legacy)).put("schemaVersion", 1).toString())
            }
        context
            .getSharedPreferences(
                "keywi_theme_engine",
                0,
            ).edit()
            .putBoolean("theme_engine_migrated", true)
            .putString("active_theme_id", legacy.id)
            .apply()
        ThemeEngine.ensureMigrated(context)
        val migrated = requireNotNull(ThemeEngine.loadTheme(context, legacy.id))
        assertTrue(file.exists())
        assertTrue(File(ThemeAssetStore.directory(context, legacy.id), "theme.json").exists())
        original.delete()
        assertArrayEquals(gif, ownedFile(migrated, migrated.backdrop.mediaUri!!).readBytes())
        assertEquals(
            listOf(Uri.fromFile(File(context.filesDir, "themes/missing.gif")).toString()),
            ThemeEngine.missingAssets(context, migrated),
        )
        assertEquals(legacy.id, ThemeEngine.activeThemeId(context))
        val edited = ThemeEngine.beginEditing(context, migrated)
        assertEquals(
            listOf(Uri.fromFile(File(context.filesDir, "themes/missing.gif")).toString()),
            ThemeEngine.missingAssets(context, edited),
        )
    }

    @Test
    fun migrationPreservesUnsavedCustomEdits() {
        val old = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        val file =
            File(context.filesDir, "themes/custom.json").also {
                it.parentFile!!.mkdirs()
                it.writeText(ThemeEngine.export(old))
            }
        context
            .getSharedPreferences(
                "keywi_theme_engine",
                0,
            ).edit()
            .putBoolean("theme_engine_migrated", true)
            .putString("active_theme_id", CUSTOM_THEME_ID)
            .apply()
        val editedKeys = old.keys.copy(shadowElevation = 9f)
        KeyThemePreferences.save(context, editedKeys)
        ThemeEngine.ensureMigrated(context)
        assertEquals(editedKeys, ThemeEngine.loadCustom(context).keys)
        assertEquals(editedKeys, KeyThemePreferences.load(context))
        assertEquals(ThemeEngine.export(old), file.readText())
    }

    @Test
    fun brokenPackagesCannotReplaceCustomOrWriteOutsideThemeFolders() {
        ThemeEngine.ensureMigrated(context)
        val before = ThemeEngine.export(ThemeEngine.loadCustom(context))
        val beforeLibraryIds = ThemeEngine.listThemes(context).map { it.id }
        val source = ThemeEngine.loadCustom(context)
        val missing = source.copy(backdrop = source.backdrop.copy(mode = BackdropMode.GIF, mediaUri = "assets/absent.gif"))
        val invalid =
            listOf(
                archive(mapOf("../escaped" to byteArrayOf(1))),
                archive(mapOf("theme.json" to ThemeEngine.export(missing).toByteArray())),
                archive(
                    mapOf(
                        "theme.json" to
                            ThemeEngine
                                .export(
                                    source.copy(backdrop = source.backdrop.copy(mediaUri = "file:///private/secret")),
                                ).toByteArray(),
                    ),
                ),
            )
        invalid.forEach { bytes ->
            assertTrue(ThemeEngine.importPackage(context, ByteArrayInputStream(bytes)).isFailure)
            assertTrue(ThemeEngine.importPackageIntoLibrary(context, ByteArrayInputStream(bytes)).isFailure)
            assertEquals(before, ThemeEngine.export(ThemeEngine.loadCustom(context)))
            assertEquals(beforeLibraryIds, ThemeEngine.listThemes(context).map { it.id })
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        }
        assertFalse(File(context.filesDir, "escaped").exists())
        assertTrue(File(context.filesDir, "themes").listFiles().orEmpty().none { it.name.startsWith(".import-") })
    }

    @Test
    fun legacyGradientOnlyJsonStillImportsAndFontIsThemeSpecific() {
        ThemeEngine.ensureMigrated(context)
        val theme = ThemeEngine.builtInThemes().first()
        val legacy = JSONObject(ThemeEngine.export(theme)).put("schemaVersion", 1).also { it.remove("font") }.toString()
        assertTrue(ThemeEngine.importIntoCustom(context, legacy).isSuccess)
        val imported = ThemeEngine.loadCustom(context)
        assertEquals(theme.keys, imported.keys)
        assertEquals(theme.backdrop, imported.backdrop)
        assertNull(imported.font)
        assertEquals("System default", FontPreferences.displayName(context))
    }

    @Test
    fun currentWordColorsTravelWithThemePackages() {
        ThemeEngine.ensureMigrated(context)
        val background = Color(0x66F8BBDD)
        val highlight = Color(0xFF7D3FB0)
        val original = SuggestionLozengeThemePreferences.load(context)
        SuggestionLozengeThemePreferences.save(
            context,
            original.copy(currentWordBackgroundColor = background, currentWordHighlightColor = highlight),
        )
        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        val saved = ThemeEngine.saveCustomAs(context, "Pink chip")
        val bytes = packageBytes(saved)
        SuggestionLozengeThemePreferences.save(
            context,
            original.copy(currentWordBackgroundColor = Color.Transparent, currentWordHighlightColor = Color.Red),
        )
        ThemeEngine.importPackage(context, ByteArrayInputStream(bytes)).getOrThrow()
        val restored = SuggestionLozengeThemePreferences.load(context)
        assertEquals(background, restored.currentWordBackgroundColor)
        assertEquals(highlight, restored.currentWordHighlightColor)
        assertEquals(original.surfaceColor, restored.surfaceColor)
    }

    @Test
    fun legacyThemesKeepExistingHighlightAndOriginalCurrentWordBackground() {
        val highlight = Color(0xFF9D79D8)
        AdvancedKeyWordPreferences.setNewWordHighlightColor(context, highlight)
        val legacy = JSONObject(ThemeEngine.export(ThemeEngine.builtInThemes().first())).put("schemaVersion", 2)
        legacy.getJSONObject("suggestionLozenges").apply {
            remove("currentWordBackgroundColor")
            remove("currentWordHighlightColor")
        }
        ThemeEngine.importIntoCustom(context, legacy.toString()).getOrThrow()
        val restored = SuggestionLozengeThemePreferences.load(context)
        assertEquals(Color(0xFF031005), restored.currentWordBackgroundColor)
        assertEquals(highlight, restored.currentWordHighlightColor)
    }

    @Test
    fun jsonLibraryImportsAreInactiveUniqueAndPreserveUnsavedCustomEdits() {
        // Complete legacy asset migration before recording the unsaved scratch baseline.
        ThemeEngine.ensureMigrated(context)
        ThemeEngine.resetCustom(context)
        val beforeCustom = ThemeEngine.export(ThemeEngine.loadCustom(context))
        KeyThemePreferences.save(context, KeyThemePreferences.load(context).copy(borderPhaseStepDegrees = 172f))
        val beforeLive = KeyThemePreferences.load(context)
        val source = ThemeEngine.builtInThemes().first()

        val first = ThemeEngine.importIntoLibrary(context, ThemeEngine.export(source)).getOrThrow()
        val second = ThemeEngine.importIntoLibrary(context, ThemeEngine.export(source)).getOrThrow()

        assertNotEquals(CUSTOM_THEME_ID, first.id)
        assertNotEquals(source.id, first.id)
        assertNotEquals(first.id, second.id)
        assertEquals(source.keys, first.keys)
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertEquals(beforeCustom, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        assertEquals(beforeLive, KeyThemePreferences.load(context))
        assertTrue(ThemeEngine.listThemes(context).any { it.id == first.id })
        assertTrue(ThemeEngine.listThemes(context).any { it.id == second.id })
    }

    @Test
    fun zipLibraryImportCopiesAssetsWithoutApplyingOrReplacingCustom() {
        ThemeEngine.ensureMigrated(context)
        val original = selectedGif()
        val source = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        ThemeEngine.saveCustom(
            context,
            source.copy(backdrop = source.backdrop.copy(mode = BackdropMode.GIF, mediaUri = Uri.fromFile(original).toString())),
        )
        val saved = ThemeEngine.saveCustomAs(context, "Portable library import")
        val bytes = packageBytes(saved)
        ThemeEngine.beginEditing(context, saved)
        KeyThemePreferences.save(context, KeyThemePreferences.load(context).copy(borderPhaseStepDegrees = 113f))
        val beforeCustom = ThemeEngine.export(ThemeEngine.loadCustom(context))
        val beforeLive = KeyThemePreferences.load(context)

        val imported = ThemeEngine.importPackageIntoLibrary(context, ByteArrayInputStream(bytes)).getOrThrow()

        assertNotEquals(CUSTOM_THEME_ID, imported.id)
        assertNotEquals(saved.id, imported.id)
        assertEquals(saved.name, imported.name)
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertEquals(beforeCustom, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        assertEquals(beforeLive, KeyThemePreferences.load(context))
        assertArrayEquals(gif, ownedFile(imported, requireNotNull(imported.backdrop.mediaUri)).readBytes())
        assertTrue(ThemeEngine.missingAssets(context, imported).isEmpty())
        assertTrue(ThemeEngine.listThemes(context).any { it.id == imported.id })
    }

    @Test
    fun customPreviewReflectsLiveEditsWithoutSavingOrReplacingInactiveCustom() {
        ThemeEngine.ensureMigrated(context)
        val savedCustom = ThemeEngine.loadCustom(context)
        val before = ThemeEngine.export(savedCustom)
        val live = savedCustom.backdrop.copy(mode = BackdropMode.IMAGE, mediaUri = "file:///live-background.png", opacity = .63f)
        BackdropThemePreferences.save(context, live)
        assertEquals(live, ThemeEngine.customPreview(context).backdrop)
        assertEquals(before, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        ThemeEngine.apply(context, ThemeEngine.builtInThemes().first())
        assertEquals(savedCustom, ThemeEngine.customPreview(context))
    }
}
