package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ThemeEditorExpandableSectionTest {
    @get:Rule val compose = createComposeRule()
    private var applied = 0

    private fun show(mode: KeywiPowerMode = KeywiPowerMode.UNBRIDLED) {
        KeywiPowerPreferences.save(RuntimeEnvironment.getApplication(), mode)
        compose.setContent {
            var expanded by remember { mutableStateOf(false) }
            MaterialTheme {
                ThemeEditorExpandableSection(
                    title = "Keys",
                    subtitle = "Borders and surfaces",
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    summary = { Text("Shared preview") },
                ) {
                    Box(Modifier.height(160.dp)) {
                        TextButton(onClick = { applied++ }) { Text("Apply border") }
                    }
                }
            }
        }
    }

    private fun header() = compose.onNodeWithTag("theme-section-header:Keys")

    private fun height() =
        compose
            .onNodeWithTag("theme-section:Keys")
            .fetchSemanticsNode()
            .boundsInRoot.height

    @Test fun expandingAndCollapsingGrowTheSameContainerThroughIntermediateSizes() {
        show()
        val collapsed = height()
        compose.mainClock.autoAdvance = false
        header().performClick()
        compose.mainClock.advanceTimeBy(80)
        val opening = height()
        compose.mainClock.advanceTimeBy(400)
        val opened = height()
        assertTrue("Section should grow gradually instead of jumping to its full height", opening > collapsed && opening < opened)
        header().performClick()
        compose.mainClock.advanceTimeBy(80)
        val closing = height()
        compose.mainClock.advanceTimeBy(400)
        assertTrue("Section should shrink gradually", closing > collapsed && closing < opened)
        assertEquals(collapsed, height(), .5f)
    }

    @Test fun summaryAndControlsStayConnectedAndExpansionStateIsAccessible() {
        show()
        header().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
        compose.onNodeWithText("Shared preview").assertIsDisplayed()
        header().performClick()
        header().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        compose.onNodeWithText("Apply border").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, applied) }
        header().performClick()
        compose.onNodeWithText("Apply border").assertDoesNotExist()
        compose.onNodeWithText("Shared preview").assertIsDisplayed()
    }

    @Test fun restrictedPowerSkipsTheExpansionAnimation() {
        assertImmediateExpansion(KeywiPowerMode.RESTRICTED)
    }

    @Test fun conservedPowerKeepsSettingsExpansionImmediate() {
        assertImmediateExpansion(KeywiPowerMode.CONSERVED)
    }

    private fun assertImmediateExpansion(mode: KeywiPowerMode) {
        show(mode)
        val collapsed = height()
        compose.mainClock.autoAdvance = false
        header().performClick()
        compose.mainClock.advanceTimeBy(32)
        val opened = height()
        assertTrue(opened > collapsed)
        compose.mainClock.advanceTimeBy(300)
        assertEquals(opened, height(), .5f)
    }
}
