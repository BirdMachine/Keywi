package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dessalines.thumbkey.effects.ActionSoundConfig
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class QuickSettingsWorkspaceTest {
    @get:Rule val compose = createComposeRule()
    private var route = ""
    private var changes = 0
    private val context get() = RuntimeEnvironment.getApplication()

    private fun show() {
        SuggestionPreferences.setEnabled(context, true)
        ActionSoundPreferences.save(context, ActionSoundConfig())
        ActionGifPreferences.save(context, ActionGifState())
        KeywiPowerPreferences.save(context, KeywiPowerMode.CONSERVED)
        compose.setContent {
            MaterialTheme {
                QuickSettingsWorkspace(onPreferencesChanged = { changes++ }, onOpenSettings = { route = it })
            }
        }
    }

    @Test fun quickTogglesPersistAndPowerChangesWithoutTouchingEditor() {
        show()
        compose.onNodeWithContentDescription("Suggestions").performClick()
        compose.onNodeWithContentDescription("Key sounds").performClick()
        compose.onNodeWithContentDescription("Keypress images & GIFs").performClick()
        compose.onNodeWithContentDescription("Power mode Restricted").performClick()
        compose.runOnIdle {
            assertFalse(SuggestionPreferences.enabled(context))
            assertTrue(ActionSoundPreferences.get(context).enabled)
            assertTrue(ActionGifPreferences.current.enabled)
            assertEquals(KeywiPowerMode.RESTRICTED, KeywiPowerPreferences.load(context))
            assertEquals(4, changes)
        }
    }

    @Test fun externalChangesRefreshVisibleQuickControlsBothWays() {
        show()
        compose.runOnIdle {
            SuggestionPreferences.setEnabled(context, false)
            ActionSoundPreferences.save(context, ActionSoundPreferences.get(context).copy(enabled = true))
            KeywiPowerPreferences.save(context, KeywiPowerMode.RESTRICTED)
        }
        compose.onNodeWithContentDescription("Suggestions").assertIsOff()
        compose.onNodeWithContentDescription("Key sounds").assertIsOn()
        compose.onNodeWithContentDescription("Power mode Restricted").assertIsSelected()
        compose.runOnIdle {
            SuggestionPreferences.setEnabled(context, true)
            ActionSoundPreferences.save(context, ActionSoundPreferences.get(context).copy(enabled = false))
            KeywiPowerPreferences.save(context, KeywiPowerMode.UNBRIDLED)
        }
        compose.onNodeWithContentDescription("Suggestions").assertIsOn()
        compose.onNodeWithContentDescription("Key sounds").assertIsOff()
        compose.onNodeWithContentDescription("Power mode Unbridled").assertIsSelected()
    }

    @Test fun controlsStayReachableWhenHoldIsDisabled() {
        show()
        ToolbarViewPreferences.setAction(context, ToolbarGesture.HOLD, ToolbarGestureAction.NONE)
        compose.onNodeWithText("Toolbar controls").performClick()
        compose.onNodeWithText("Hold: Do nothing").assertExists()
        compose.onNodeWithText("Done").performClick()
    }

    @Test fun themeNavigationUsesExistingDestination() {
        show()
        compose.onNodeWithText("Themes & appearance").performClick()
        compose.runOnIdle { assertEquals("advancedLookAndFeel", route) }
    }
}
