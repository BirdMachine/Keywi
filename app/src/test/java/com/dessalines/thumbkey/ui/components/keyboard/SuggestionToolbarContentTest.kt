package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SuggestionToolbarContentTest {
    @get:Rule val compose = createComposeRule()
    private val page = mutableStateOf(ToolbarPage.CHARACTERS)
    private val enabled = mutableStateOf(true)
    private val privateField = mutableStateOf(false)

    @Test fun toolsReplaceSuggestionsWithAccessibleTitlesAndCharactersRestoreTheContent() {
        var selected = 0
        compose.setContent {
            MaterialTheme {
                Column {
                    Box(Modifier.height(42.dp)) {
                        SuggestionToolbarContent(page.value) {
                            TextButton(onClick = { selected++ }) { Text("Current word suggestion") }
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("Current word suggestion").assertIsDisplayed().performClick()
        ToolbarPage.entries.filter { it != ToolbarPage.CHARACTERS }.forEach { tool ->
            compose.runOnIdle { page.value = tool }
            compose.onNodeWithText("Current word suggestion").assertDoesNotExist()
            compose
                .onNodeWithText(tool.title)
                .assertIsDisplayed()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        }
        compose.runOnIdle { page.value = ToolbarPage.CHARACTERS }
        ToolbarPage.entries.filter { it != ToolbarPage.CHARACTERS }.forEach { tool ->
            compose.onNodeWithText(tool.title).assertDoesNotExist()
        }
        compose.onNodeWithText("Current word suggestion").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(2, selected) }
    }

    @Test fun pollingStopsAndClearsForEveryToolThenRestartsOnlyForEnabledNonPrivateCharacters() {
        var reads = 0
        var clears = 0
        compose.setContent {
            MaterialTheme {
                SuggestionPollingEffect(
                    page = page.value,
                    enabled = enabled.value,
                    privateField = privateField.value,
                    onInactive = { clears++ },
                    onPoll = { reads++ },
                )
                Text(page.value.title)
            }
        }
        compose.mainClock.advanceTimeBy(240)
        compose.runOnIdle { assertTrue(reads > 0) }
        ToolbarPage.entries.filter { it != ToolbarPage.CHARACTERS }.forEach { tool ->
            val beforeClears = clears
            compose.runOnIdle { page.value = tool }
            compose.waitForIdle()
            val before = reads
            compose.mainClock.advanceTimeBy(240)
            compose.runOnIdle {
                assertEquals("No source reads while $tool is open", before, reads)
                assertTrue("Opening $tool clears cached suggestions", clears > beforeClears)
            }
        }
        val beforeReturn = reads
        compose.runOnIdle { page.value = ToolbarPage.CHARACTERS }
        compose.mainClock.advanceTimeBy(240)
        compose.runOnIdle { assertTrue(reads > beforeReturn) }
        compose.runOnIdle { enabled.value = false }
        compose.waitForIdle()
        val beforeDisabled = reads
        compose.mainClock.advanceTimeBy(240)
        compose.runOnIdle { assertEquals(beforeDisabled, reads) }
        compose.runOnIdle {
            privateField.value = true
            enabled.value = true
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(240)
        compose.runOnIdle { assertEquals(beforeDisabled, reads) }
    }
}
