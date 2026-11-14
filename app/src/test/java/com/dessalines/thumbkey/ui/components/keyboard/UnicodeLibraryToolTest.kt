package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnicodeLibraryToolTest {
    @get:Rule val compose = createComposeRule()

    @After fun clearSearchCapture() {
        PaletteSearchCapture.release(clear = true)
    }

    @Test fun invisibleCharacterRequiresConfirmationAndInsertionBypassesSearchCapture() {
        PaletteSearchCapture.release(clear = true)
        var inserted: String? = null
        var capturedDuringInsertion = true
        val visible = mutableStateOf(true)
        compose.setContent {
            MaterialTheme {
                if (visible.value) {
                    ToolPageTheme {
                        UnicodeLibraryTool(
                            onInsert = {
                                inserted = it
                                capturedDuringInsertion = PaletteSearchCapture.shouldCapture()
                            },
                            onBack = { visible.value = false },
                            modifier = Modifier.width(320.dp).height(420.dp),
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Search names, aliases, blocks, or U+ codes…").performClick()
        compose.runOnIdle {
            assertTrue(PaletteSearchCapture.active)
            PaletteSearchCapture.consumeCommitText("U+200D")
        }
        compose.waitUntil(timeoutMillis = 15000) {
            compose.onAllNodesWithContentDescription("ZERO WIDTH JOINER, U+200D").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("ZERO WIDTH JOINER, U+200D").performClick()
        compose.onNodeWithText("Insert character").assertIsDisplayed()
        compose.runOnIdle { assertEquals(null, inserted) }
        compose.onNodeWithText("Insert character").performClick()
        compose.runOnIdle {
            assertEquals("\u200D", inserted)
            assertFalse("Insertion must reach the host instead of changing search", capturedDuringInsertion)
            assertEquals("U+200D", PaletteSearchCapture.query)
        }
        compose.onNodeWithText("Keyboard").performClick()
        compose.runOnIdle {
            assertFalse(PaletteSearchCapture.active)
            assertEquals("", PaletteSearchCapture.query)
        }
    }
}
