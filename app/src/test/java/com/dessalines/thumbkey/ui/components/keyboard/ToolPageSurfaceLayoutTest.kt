package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToolPageSurfaceLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun weightedWellUsesItsAllocatedWidthAndCentersShortContent() {
        compose.setContent {
            MaterialTheme {
                ToolPageTheme {
                    Row(Modifier.width(280.dp).testTag("row")) {
                        ToolPageSurface(onClick = {}, modifier = Modifier.weight(1f)) {
                            Text("Short", Modifier.testTag("label"))
                        }
                    }
                }
            }
        }
        val row = compose.onNodeWithTag("row").fetchSemanticsNode().boundsInRoot
        val button = compose.onNode(hasClickAction()).fetchSemanticsNode().boundsInRoot
        val label = compose.onNodeWithTag("label", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals("Painted/tappable well must fill the weighted slot", row.width, button.width, .5f)
        assertEquals("Short text must be vertically centered", button.center.y, label.center.y, .5f)
    }

    @Test fun actualKaomojiSearchSpansThePageAndStillCapturesInput() {
        PaletteSearchCapture.release(clear = true)
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ToolPageTheme {
                    KaomojiRoom(
                        onCommit = {}, onBackToLetters = {}, showDismissButton = false,
                        modifier = Modifier.fillMaxSize().testTag("kaomoji"),
                    )
                }
            }
        }
        val page = compose.onNodeWithTag("kaomoji").fetchSemanticsNode().boundsInRoot
        val search = compose.onNode(hasClickAction() and hasText("Search…")).fetchSemanticsNode().boundsInRoot
        assertTrue("Search well must span the available row", search.width > page.width * .8f)
        val file = File("build/outputs/tool-page-review/kaomoji-dark.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use {
            compose.onNodeWithTag("kaomoji").captureToImage().asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Search…").performClick()
        compose.runOnIdle {
            assertTrue(PaletteSearchCapture.active)
            PaletteSearchCapture.release(clear = true)
        }
    }
}
