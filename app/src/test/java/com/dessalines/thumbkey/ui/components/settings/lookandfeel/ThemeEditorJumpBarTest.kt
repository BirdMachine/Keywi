package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ThemeEditorJumpBarTest {
    @get:Rule val compose = createComposeRule()

    private fun show() {
        compose.setContent {
            var target by remember { mutableStateOf<String?>(null) }
            var revision by remember { mutableIntStateOf(0) }
            MaterialTheme {
                Column {
                    ThemeEditorJumpBar(selectedTitle = target) {
                        target = it
                        revision++
                    }
                    ThemeEditorFold(
                        title = "Keyboard background",
                        subtitle = "Backdrop controls",
                        jumpTarget = target,
                        jumpRevision = revision,
                    ) { Text("Backdrop opacity") }
                    ThemeEditorFold(
                        title = "Keys",
                        subtitle = "Key surface controls",
                        jumpTarget = target,
                        jumpRevision = revision,
                    ) { Text("Key border intensity") }
                }
            }
        }
    }

    private fun assertExpanded(title: String, expanded: Boolean) {
        compose.onNodeWithTag("theme-section-header:$title").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                if (expanded) "Expanded" else "Collapsed",
            ),
        )
    }

    @Test fun shortcutOpensOnlyRequestedSectionWithoutResettingOtherFolds() {
        show()
        assertExpanded("Keyboard background", false)
        assertExpanded("Keys", false)
        compose.onNodeWithTag("theme-jump:Keys").performClick()
        assertExpanded("Keys", true)
        assertExpanded("Keyboard background", false)
        compose.onNodeWithText("Key border intensity").assertIsDisplayed()
        compose.onNodeWithTag("theme-jump:Keyboard background").performClick()
        assertExpanded("Keyboard background", true)
        assertExpanded("Keys", true)
    }

    @Test fun shortcutCanReopenManuallyCollapsedSection() {
        show()
        compose.onNodeWithTag("theme-jump:Keys").performClick()
        assertExpanded("Keys", true)
        compose.onNodeWithTag("theme-section-header:Keys").performClick()
        assertExpanded("Keys", false)
        compose.onNodeWithText("Key border intensity").assertDoesNotExist()
        compose.onNodeWithTag("theme-jump:Keys").performClick()
        assertExpanded("Keys", true)
        compose.onNodeWithText("Key border intensity").assertIsDisplayed()
    }
}
