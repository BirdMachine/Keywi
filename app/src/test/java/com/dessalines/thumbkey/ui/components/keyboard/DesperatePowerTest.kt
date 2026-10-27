package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTheme
import com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DesperatePowerTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun desperateSuppressesVisualsAndRestoresTheSavedAppearance() {
        KeywiAppearancePreferences.save(context, true)
        val original = BackdropThemePreferences.load(context)
        val keyTheme = KeyThemePreferences.load(context)
        KeywiPowerPreferences.save(context, KeywiPowerMode.DESPERATE)
        assertEquals(KeywiPowerMode.DESPERATE, KeywiPowerPreferences.load(context))
        assertFalse(KeywiPowerPreferences.animationsEnabled(context))
        assertFalse(KeywiPowerPreferences.gifsEnabled(context))
        assertFalse(KeywiAppearancePreferences.currentEnabled)
        assertTrue(KeywiAppearancePreferences.load(context))
        assertEquals(original, BackdropThemePreferences.load(context))
        assertEquals(keyTheme, KeyThemePreferences.load(context))
        assertEquals(500L, KeywiPowerPreferences.suggestionPollMillis(context))
        KeywiPowerPreferences.save(context, KeywiPowerMode.CONSERVED)
        assertTrue(KeywiAppearancePreferences.currentEnabled)
        assertTrue(KeywiPowerPreferences.animationsEnabled(context))
        assertEquals(original, BackdropThemePreferences.load(context))
    }

    @Test fun desperateBypassesSettingsAndToolDecorationsButRetainsClickableControls() {
        KeywiPowerPreferences.save(context, KeywiPowerMode.DESPERATE)
        var clicks = 0
        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(enabled = true, dripEnabled = true) {
                    assertNull(LocalSettingsDrip.current)
                    ToolPageTheme {
                        assertNull(LocalToolPageStyle.current)
                        Box(Modifier.size(150.dp)) {
                            ToolPageSurface(onClick = { clicks++ }, selected = true, modifier = Modifier.testTag("plain-control")) { Text("Power options") }
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("plain-control").performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
