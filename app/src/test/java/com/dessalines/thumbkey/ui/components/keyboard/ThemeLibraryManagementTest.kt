package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ThemeLibraryManagementTest {
    private val context: Application get() = ApplicationProvider.getApplicationContext()

    private fun save(name: String): ThemeDocument {
        ThemeEngine.ensureMigrated(context)
        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        return ThemeEngine.saveCustomAs(context, name)
    }

    @Test
    fun duplicateNameNeedsExplicitOverwriteAndKeepsStableIdentity() {
        val first = save("OilSlyck")
        ThemeEngine.beginEditing(context, first)
        val edited = first.keys.copy(surfaceColor = Color.Magenta, shadowElevation = 11f)
        KeyThemePreferences.save(context, edited)
        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        val customBefore = ThemeEngine.export(ThemeEngine.loadCustom(context))
        assertTrue(runCatching { ThemeEngine.saveCustomAs(context, "  oilslyck  ") }.isFailure)
        assertEquals(first, ThemeEngine.loadTheme(context, first.id))
        assertEquals(customBefore, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        val replaced = ThemeEngine.saveCustomAs(context, "  oilslyck  ", first.id)
        assertEquals(first.id, replaced.id)
        assertEquals("oilslyck", replaced.name)
        assertEquals(edited, replaced.keys)
        assertEquals(1, ThemeEngine.themesNamed(context, "OILSLYCK").size)
    }

    @Test
    fun deletingActiveThemePreservesCustomAndDeletesOnlyItsOwnMedia() {
        ThemeEngine.ensureMigrated(context)
        val original = File(context.cacheDir, "sample.gif").apply { writeBytes(byteArrayOf(71, 73, 70, 56, 57, 97)) }
        val uri = ThemeAssetStore.copySelected(context, Uri.fromFile(original))
        BackdropThemePreferences.save(context, BackdropThemePreferences.load(context).copy(mode = BackdropMode.GIF, mediaUri = uri))
        val first = save("First")
        ThemeEngine.beginEditing(context, first)
        val second = save("Second")
        val preserved = ThemeEngine.loadCustom(context).copy(keys = second.keys.copy(surfaceColor = Color.Cyan))
        ThemeEngine.saveCustom(context, preserved)
        val before = ThemeEngine.export(ThemeEngine.loadCustom(context))
        ThemeEngine.apply(context, first)
        ThemeEngine.deleteSavedTheme(context, first.id)
        assertNull(ThemeEngine.loadTheme(context, first.id))
        assertFalse(ThemeAssetStore.directory(context, first.id).exists())
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertEquals(before, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        assertEquals(preserved.keys, KeyThemePreferences.load(context))
        assertTrue(ThemeEngine.missingAssets(context, second).isEmpty())
        assertEquals(second, ThemeEngine.loadTheme(context, second.id))
    }

    @Test
    fun deletingInactiveThemeDoesNotChangeLiveUnsavedCustom() {
        val saved = save("Delete me")
        ThemeEngine.apply(context, ThemeEngine.loadCustom(context))
        val live = KeyThemePreferences.load(context).copy(surfaceColor = Color.Yellow)
        KeyThemePreferences.save(context, live)
        val before = ThemeEngine.export(ThemeEngine.loadCustom(context))
        ThemeEngine.deleteSavedTheme(context, saved.id)
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertEquals(live, KeyThemePreferences.load(context))
        assertEquals(before, ThemeEngine.export(ThemeEngine.loadCustom(context)))
    }

    @Test
    fun updatingSelectedThemeCapturesLiveEditsAndDoesNotTouchCustom() {
        val saved = save("Update me")
        val customBefore = ThemeEngine.export(ThemeEngine.loadCustom(context))
        val flash = AccentFeedbackConfig(950, listOf(Color.Red, Color.Cyan), true)
        AccentFeedbackPreferences.save(context, flash)
        val live = saved.keys.copy(shadowElevation = 9f)
        KeyThemePreferences.save(context, live)
        val updated = ThemeEngine.updateSavedTheme(context, saved.id)
        assertEquals(saved.id, updated.id)
        assertEquals(live, updated.keys)
        assertEquals(flash, updated.accentFeedback)
        assertEquals(customBefore, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        assertEquals(saved.id, ThemeEngine.activeThemeId(context))
    }

    @Test
    fun reservedThemesAndStaleOverwriteTargetsAreProtected() {
        ThemeEngine.ensureMigrated(context)
        val builtin = ThemeEngine.builtInThemes().first()
        assertFalse(ThemeEngine.isSavedTheme(context, CUSTOM_THEME_ID))
        assertFalse(ThemeEngine.isSavedTheme(context, builtin.id))
        for (id in listOf(CUSTOM_THEME_ID, builtin.id)) {
            assertTrue(runCatching { ThemeEngine.deleteSavedTheme(context, id) }.isFailure)
            assertTrue(runCatching { ThemeEngine.updateSavedTheme(context, id) }.isFailure)
        }
        assertTrue(runCatching { ThemeEngine.saveCustomAs(context, builtin.name, builtin.id) }.isFailure)
        val saved = save("Stale")
        ThemeEngine.apply(context, ThemeEngine.loadCustom(context))
        assertTrue(runCatching { ThemeEngine.updateSavedTheme(context, saved.id) }.isFailure)
        ThemeEngine.deleteSavedTheme(context, saved.id)
        assertTrue(runCatching { ThemeEngine.saveCustomAs(context, "Stale", saved.id) }.isFailure)
    }

    @Test
    fun accentFramesFadeAndTrailSurvivePackageExportAndImport() {
        val expected = AccentFeedbackConfig(1250, listOf(Color(0x88FF2299), Color.Cyan, Color(0x556644FF)), true)
        AccentFeedbackPreferences.save(context, expected)
        val saved = save("Flash sequence")
        val bytes = ByteArrayOutputStream().also { ThemeEngine.exportPackage(context, saved, it) }.toByteArray()
        AccentFeedbackPreferences.save(context, AccentFeedbackConfig(0))
        val imported = ThemeEngine.importPackage(context, ByteArrayInputStream(bytes)).getOrThrow()
        assertEquals(4, imported.schemaVersion)
        assertEquals(expected, imported.accentFeedback)
        assertEquals(expected, AccentFeedbackPreferences.get(context))
    }

    @Test
    fun legacyThemesLeaveExistingFeedbackAloneAndExplicitDisabledFeedbackRestores() {
        ThemeEngine.ensureMigrated(context)
        val custom = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        val legacy = JSONObject(ThemeEngine.export(custom)).put("schemaVersion", 3).apply { remove("accentFeedback") }
        val existing = AccentFeedbackConfig(1400, listOf(Color.Green, Color.Blue), true)
        AccentFeedbackPreferences.save(context, existing)
        val imported = ThemeEngine.importIntoCustom(context, legacy.toString()).getOrThrow()
        assertNull(imported.accentFeedback)
        assertEquals(existing, AccentFeedbackPreferences.get(context))
        val disabled = AccentFeedbackConfig(0, emptyList(), false)
        ThemeEngine.importIntoCustom(context, ThemeEngine.export(custom.copy(accentFeedback = disabled))).getOrThrow()
        assertEquals(disabled, AccentFeedbackPreferences.get(context))
    }
}
