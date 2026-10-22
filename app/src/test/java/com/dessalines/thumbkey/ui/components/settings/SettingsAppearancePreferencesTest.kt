package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SettingsAppearancePreferencesTest {
    @get:Rule val compose = createComposeRule()

    @Test fun themeArtworkCanBeDisabledAndRestoredWithoutChangingKeyboardPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SettingsAppearancePreferences
            .store(context)
            .edit()
            .clear()
            .commit()
        assertTrue(SettingsAppearancePreferences.followsKeyboard(context))
        KeywiPowerPreferences.save(context, KeywiPowerMode.CONSERVED)
        val keyboardStores = listOf("birdie_backdrop_theme", "key_theme_preferences", "keywi_power_preferences")
        val before = keyboardStores.associateWith { context.getSharedPreferences(it, 0).all.toMap() }
        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(enabled = true, dripEnabled = true) {
                    androidx.compose.foundation.layout.Column {
                        SettingsAppearancePreference()
                        Text("Keyboard artwork: ${LocalSettingsDrip.current != null}")
                    }
                }
            }
        }
        compose.onNodeWithText("Keyboard artwork: true").assertIsDisplayed()
        compose.onNodeWithText("Follow keyboard theme in settings").performClick()
        compose.onNodeWithText("Keyboard artwork: false").assertIsDisplayed()
        assertFalse(SettingsAppearancePreferences.followsKeyboard(context))
        compose.onNodeWithText("Follow keyboard theme in settings").performClick()
        compose.onNodeWithText("Keyboard artwork: true").assertIsDisplayed()
        assertTrue(SettingsAppearancePreferences.followsKeyboard(context))
        assertEquals(before, keyboardStores.associateWith { context.getSharedPreferences(it, 0).all.toMap() })
    }
}
