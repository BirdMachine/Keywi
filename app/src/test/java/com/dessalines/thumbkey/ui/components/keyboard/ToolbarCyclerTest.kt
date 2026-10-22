package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ToolbarCyclerTest {
    @get:Rule val compose = createComposeRule()
    private var current = ToolbarPage.CHARACTERS
    private var toggles = 0
    private var settingsOpened = 0

    private fun show() {
        RuntimeEnvironment
            .getApplication()
            .getSharedPreferences("toolbar_views", 0)
            .edit()
            .clear()
            .commit()
        compose.setContent {
            var page by remember { mutableStateOf(ToolbarPage.CHARACTERS) }
            MaterialTheme {
                Box(Modifier.height(80.dp)) {
                    ToolbarCycler(
                        page,
                        true,
                        { toggles++ },
                        {
                            page = it
                            current = it
                        },
                        onOpenSettings = { settingsOpened++ },
                    )
                }
            }
        }
    }

    private fun cycler() = compose.onNodeWithContentDescription("${current.title}, page ${current.ordinal + 1} of 4")

    private fun settleTaps() {
        compose.mainClock.advanceTimeBy(600)
        compose.waitForIdle()
    }

    @Test fun swipeWrapsBothWaysAndDoubleTapResetsWithoutToggling() {
        show()
        cycler().performTouchInput { swipeDown() }
        compose.runOnIdle { assertEquals(ToolbarPage.QUICK_SETTINGS, current) }
        cycler().performTouchInput { swipeUp() }
        compose.runOnIdle { assertEquals(ToolbarPage.CHARACTERS, current) }
        cycler().performTouchInput { swipeUp() }
        compose.runOnIdle { assertEquals(ToolbarPage.FORMATTING, current) }
        cycler().performTouchInput { doubleClick() }
        settleTaps()
        compose.runOnIdle {
            assertEquals(ToolbarPage.CHARACTERS, current)
            assertEquals(0, toggles)
        }
        cycler().performTouchInput { click() }
        settleTaps()
        compose.runOnIdle { assertEquals(1, toggles) }
    }

    @Test fun tripleTapOpensQuickSettingsWithoutSingleOrDoubleAction() {
        show()
        ToolbarViewPreferences.setAction(RuntimeEnvironment.getApplication(), ToolbarGesture.DOUBLE_TAP, ToolbarGestureAction.OPEN_SETTINGS)
        cycler().performTouchInput {
            click()
            advanceEventTime(80)
            click()
            advanceEventTime(80)
            click()
        }
        settleTaps()
        compose.runOnIdle {
            assertEquals(ToolbarPage.QUICK_SETTINGS, current)
            assertEquals(0, toggles)
            assertEquals(0, settingsOpened)
        }
    }

    @Test fun holdLetsUserChooseHome() {
        show()
        cycler().performTouchInput { longClick() }
        compose.onNodeWithText("Home view: Character input").performClick()
        compose.onNodeWithText("Kaomoji").performClick()
        compose.onNodeWithText("Done").performClick()
        cycler().performTouchInput { doubleClick() }
        settleTaps()
        compose.runOnIdle {
            assertEquals(ToolbarPage.KAOMOJI, current)
            assertEquals(0, toggles)
        }
    }

    @Test fun remappedHoldAndDisabledTripleDoNotToggleSuggestions() {
        show()
        val context = RuntimeEnvironment.getApplication()
        ToolbarViewPreferences.setAction(context, ToolbarGesture.HOLD, ToolbarGestureAction.OPEN_SETTINGS)
        ToolbarViewPreferences.setAction(context, ToolbarGesture.TRIPLE_TAP, ToolbarGestureAction.NONE)
        cycler().performTouchInput { longClick() }
        compose.runOnIdle { assertEquals(1, settingsOpened) }
        cycler().performTouchInput {
            click()
            advanceEventTime(80)
            click()
            advanceEventTime(80)
            click()
        }
        settleTaps()
        compose.runOnIdle {
            assertEquals(ToolbarPage.CHARACTERS, current)
            assertEquals(0, toggles)
        }
    }
}
