package com.dessalines.thumbkey.automation

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardGradientStop
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarThemePreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TaskerThemeAutomationTest {
    private val context get() = RuntimeEnvironment.getApplication()

    private fun intent(
        command: String,
        token: String = TaskerThemeAutomation.token(context),
    ) = Intent(TASKER_THEME_ACTION).putExtra("token", token).putExtra("command", command)

    private fun apply(
        mode: String = "temporary",
        target: String = "toolbar",
        color: String = "#FFEA2020",
    ) = """{"version":1,"operation":"apply","mode":"$mode","updates":[{"target":"$target","style":"color","color":"$color"}]}"""

    private fun handle(command: String) = runBlocking { TaskerThemeAutomation.handle(context, intent(command)) }

    @Before fun setup() {
        context
            .getSharedPreferences("keywi_tasker_themes", 0)
            .edit()
            .clear()
            .commit()
        ThemeEngine.ensureMigrated(context)
        runBlocking { TaskerThemeAutomation.setEnabled(context, true) }
    }

    @Test fun disabledAndIncorrectTokensCannotChangeTheme() {
        val before = ThemeEngine.export(ThemeEngine.captureCurrent(context, "Before", CUSTOM_THEME_ID))
        runBlocking { TaskerThemeAutomation.setEnabled(context, false) }
        assertFalse(handle(apply()).accepted)
        runBlocking { TaskerThemeAutomation.setEnabled(context, true) }
        assertFalse(runBlocking { TaskerThemeAutomation.handle(context, intent(apply(), "0".repeat(64))) }.accepted)
        val after = ThemeEngine.export(ThemeEngine.captureCurrent(context, "Before", CUSTOM_THEME_ID))
        assertEquals(before, after)
        assertFalse(TaskerThemeAutomation.temporaryActive(context))
    }

    @Test fun rotatingTokenRevokesPreviouslyPairedIntents() {
        val previous = TaskerThemeAutomation.token(context)
        TaskerThemeAutomation.rotateToken(context)
        assertFalse(runBlocking { TaskerThemeAutomation.handle(context, intent(apply(), previous)) }.accepted)
        assertTrue(handle(apply()).accepted)
    }

    @Test fun temporaryUpdatesStackAndClearRestoresLiveBaseWithoutSavingCustom() {
        val base =
            ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID).copy(
                toolbar =
                    ToolbarThemePreferences.load(context).copy(
                        mode = BackdropMode.COLORFUL,
                        stops = listOf(KeyboardGradientStop(0f, Color.Green), KeyboardGradientStop(1f, Color.Green)),
                    ),
            )
        ThemeEngine.saveCustom(context, base)
        ThemeEngine.apply(context, ThemeEngine.loadCustom(context))
        val saved = ThemeEngine.export(ThemeEngine.loadCustom(context))
        val beforeRevision = TaskerThemeAutomation.revision
        assertTrue(handle(apply()).accepted)
        assertEquals(
            Color(0xFFEA2020).toArgb(),
            ToolbarThemePreferences
                .load(context)
                .stops
                .first()
                .color
                .toArgb(),
        )
        assertTrue(handle(apply(target = "key_border", color = "#FF1212FF")).accepted)
        assertEquals(
            Color(0xFFEA2020).toArgb(),
            ToolbarThemePreferences
                .load(context)
                .stops
                .first()
                .color
                .toArgb(),
        )
        assertTrue(TaskerThemeAutomation.revision > beforeRevision)
        assertEquals(saved, ThemeEngine.export(ThemeEngine.loadCustom(context)))
        assertTrue(handle(TaskerThemeAutomation.clearCommand()).accepted)
        assertEquals(
            Color.Green.toArgb(),
            ToolbarThemePreferences
                .load(context)
                .stops
                .first()
                .color
                .toArgb(),
        )
        assertEquals(base.keys.borderColor.toArgb(), KeyThemePreferences.load(context).borderColor.toArgb())
        assertFalse(TaskerThemeAutomation.temporaryActive(context))
    }

    @Test fun persistentCustomCommandUsesBaseBeneathOverrideAndSurvivesClear() {
        val original = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        assertTrue(handle(apply()).accepted)
        assertTrue(handle(apply(mode = "custom", target = "key_border", color = "#FF00AAFF")).accepted)
        assertEquals(original.toolbar.stops, ToolbarThemePreferences.load(context).stops)
        assertEquals(Color(0xFF00AAFF).toArgb(), KeyThemePreferences.current.borderColor.toArgb())
        assertEquals(
            Color(0xFF00AAFF).toArgb(),
            ThemeEngine
                .loadCustom(context)
                .keys.borderColor
                .toArgb(),
        )
        assertTrue(handle(TaskerThemeAutomation.clearCommand()).accepted)
        assertEquals(Color(0xFF00AAFF).toArgb(), KeyThemePreferences.load(context).borderColor.toArgb())
    }

    @Test fun clearRestoresPreviouslySelectedSavedTheme() {
        val saved = ThemeEngine.builtInThemes().first()
        ThemeEngine.apply(context, saved)
        assertTrue(handle(apply()).accepted)
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertTrue(handle(TaskerThemeAutomation.clearCommand()).accepted)
        assertEquals(saved.id, ThemeEngine.activeThemeId(context))
    }

    @Test fun clearPreservesManualEditsToOtherSurfacesAndToAutomatedTarget() {
        val original = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        assertTrue(handle(apply()).accepted)
        val manualBackground =
            original.backdrop.copy(
                mode = BackdropMode.COLORFUL,
                stops = listOf(KeyboardGradientStop(0f, Color.Cyan), KeyboardGradientStop(1f, Color.Cyan)),
            )
        BackdropThemePreferences.save(context, manualBackground)
        assertTrue(handle(apply(target = "key_border", color = "#FF1111FF")).accepted)
        val manualToolbar =
            ToolbarThemePreferences.load(context).copy(
                stops = listOf(KeyboardGradientStop(0f, Color.Yellow), KeyboardGradientStop(1f, Color.Yellow)),
            )
        ToolbarThemePreferences.save(context, manualToolbar)
        assertTrue(handle(TaskerThemeAutomation.clearCommand()).accepted)
        assertEquals(manualBackground, BackdropThemePreferences.load(context))
        assertEquals(manualToolbar, ToolbarThemePreferences.load(context))
        assertEquals(original.keys.borderColor, KeyThemePreferences.load(context).borderColor)
    }

    @Test fun persistentCommandDuringOverrideKeepsUnrelatedManualEdit() {
        assertTrue(handle(apply()).accepted)
        val manualBackground = BackdropThemePreferences.load(context).copy(opacity = .42f)
        BackdropThemePreferences.save(context, manualBackground)
        assertTrue(handle(apply(mode = "custom", target = "key_border", color = "#FFAABBCC")).accepted)
        assertEquals(manualBackground, BackdropThemePreferences.load(context))
        assertFalse(TaskerThemeAutomation.temporaryActive(context))
    }

    @Test fun clearDoesNotOverwriteAnotherSavedThemeSelectedAfterOverride() {
        assertTrue(handle(apply()).accepted)
        val saved = ThemeEngine.builtInThemes().first()
        ThemeEngine.apply(context, saved)
        val before = ThemeEngine.captureCurrent(context, "Unchanged", saved.id)
        assertTrue(handle(TaskerThemeAutomation.clearCommand()).accepted)
        assertEquals(before, ThemeEngine.captureCurrent(context, "Unchanged", saved.id))
    }

    @Test fun strictSchemaRejectsUnknownKeysCoercionsPathsAndPartialBatches() {
        val invalid =
            listOf(
                apply().replace("\"color\":\"#FFEA2020\"", "\"color\":\"red\""),
                apply().replace("\"version\":1", "\"version\":\"1\""),
                apply().replace("\"updates\":", "\"preferences\":{},\"updates\":"),
                """{"version":1,"operation":"apply","mode":"temporary","updates":[{"target":"background",""" +
                    """"style":"image","uri":"file:///data/private"}]}""",
                """{"version":1,"operation":"apply","mode":"temporary","updates":[{"target":"toolbar",""" +
                    """"style":"color","color":"#FF0000"},{"target":"unknown","style":"color","color":"#000000"}]}""",
                """{"version":1,"operation":"apply","mode":"temporary","updates":[{"target":"toolbar",""" +
                    """"style":"gradient","angle":0,"stops":[{"position":0,"color":"#FF0000"},{"position":0,""" +
                    """"color":"#0000FF"}]}]}""",
            )
        val before = ThemeEngine.export(ThemeEngine.captureCurrent(context, "Base", CUSTOM_THEME_ID))
        invalid.forEach { assertFalse(it, handle(it).accepted) }
        assertEquals(before, ThemeEngine.export(ThemeEngine.captureCurrent(context, "Base", CUSTOM_THEME_ID)))
        assertFalse(TaskerThemeAutomation.temporaryActive(context))
    }

    @Test fun gradientsPreservePositionedStopsAndOtherTargets() {
        val background = BackdropThemePreferences.load(context)
        val command =
            """{"version":1,"operation":"apply","mode":"custom","updates":[{"target":"key_border",""" +
                """"style":"gradient","angle":45,"stops":[{"position":0,"color":"#FF0000"},{"position":0.2,""" +
                """"color":"#00FF00"},{"position":1,"color":"#0000FF"}]}]}"""
        assertTrue(handle(command).accepted)
        val keys = KeyThemePreferences.load(context)
        assertEquals(KeyBorderStyle.GRADIENT, keys.borderStyle)
        assertEquals(listOf(0f, .2f, 1f), keys.borderGradient.stops.map { it.position })
        assertEquals(45f, keys.borderGradient.angleDegrees, 0f)
        assertEquals(background, BackdropThemePreferences.load(context))
    }

    @Test fun contentImageIsCopiedBeforeProviderDisappearsAndInvalidImageDoesNotApply() {
        val uri = Uri.parse("content://tasker.test/image.png")
        val image = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.MAGENTA) }
        val bytes = ByteArrayOutputStream().also { image.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
        image.recycle()
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(bytes))
        val command =
            """{"version":1,"operation":"apply","mode":"custom","updates":[{"target":"background",""" +
                """"style":"image","uri":"$uri"}]}"""
        assertTrue(handle(command).accepted)
        val stored = Uri.parse(BackdropThemePreferences.load(context).mediaUri)
        assertEquals("file", stored.scheme)
        assertArrayEquals(bytes, File(requireNotNull(stored.path)).readBytes())
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(byteArrayOf(0, 1, 2)))
        val before = BackdropThemePreferences.load(context)
        assertFalse(handle(command).accepted)
        assertEquals(before, BackdropThemePreferences.load(context))
    }
}
