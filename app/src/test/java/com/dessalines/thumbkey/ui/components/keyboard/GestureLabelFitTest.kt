package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dessalines.thumbkey.utils.FontSizeVariant
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyC
import org.junit.Assert.assertEquals
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
class GestureLabelFitTest {
    @get:Rule val compose = createComposeRule()

    @Test fun kaomojiStaysInItsThirdWithoutObscuringNeighbors() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.size(100.dp).background(Color.Black).testTag("key")) {
                    val labels = listOf(
                        Alignment.CenterStart to "a",
                        Alignment.Center to "(｡•̀ᴗ-)✧",
                        Alignment.CenterEnd to "b",
                    )
                    labels.forEach { (alignment, label) ->
                        GestureLabelSlot(alignment) {
                            CompositionLocalProvider(LocalKeyGlyphColor provides if (label == "a" || label == "b") Color.Green else Color.Red) {
                                KeyText(KeyC(KeyAction.CommitText(label), size = FontSizeVariant.SMALL), 100.dp, false, false, false)
                            }
                        }
                    }
                }
            }
        }
        val image = compose.onNodeWithTag("key").captureToImage().asAndroidBitmap()
        var redPixels = 0
        var leftGreen = 0
        var rightGreen = 0
        var minY = image.height
        var maxY = 0
        for (y in 0 until image.height) for (x in 0 until image.width) {
            val pixel = image.getPixel(x, y)
            if (android.graphics.Color.red(pixel) > 100) {
                redPixels++
                minY = minOf(minY, y)
                maxY = maxOf(maxY, y)
                assertTrue("Long legend leaked into a neighboring column at x=$x", x >= image.width / 3 && x < image.width * 2 / 3)
            }
            if (android.graphics.Color.green(pixel) > 100) {
                if (x < image.width / 3) leftGreen++
                if (x >= image.width * 2 / 3) rightGreen++
            }
        }
        assertTrue(redPixels > 10)
        assertTrue("The regression kaomoji must remain readable, rather than become an ellipsis", maxY - minY > image.height * .04f)
        assertTrue(leftGreen > 10 && rightGreen > 10)
        compose.onNodeWithText("(｡•̀ᴗ-)✧").assertExists()
    }

    @Test fun shortTextKeepsItsNaturalSize() {
        val style = TextStyle(fontSize = 16.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        compose.setContent {
            MaterialTheme {
                Box(Modifier.size(100.dp)) {
                    Text("a", style = style, modifier = Modifier.testTag("natural"))
                    FittedGestureText("a", style, Modifier.testTag("fitted"))
                }
            }
        }
        val natural = compose.onNodeWithTag("natural").fetchSemanticsNode().boundsInRoot
        val fitted = compose.onNodeWithTag("fitted").fetchSemanticsNode().boundsInRoot
        assertEquals(natural.width, fitted.width, .01f)
        assertEquals(natural.height, fitted.height, .01f)
    }

    @Test fun combiningEmojiAndExceptionalLabelsKeepCompleteAccessibleText() {
        val labels = listOf("e\u0301\u0327", "👩🏽‍💻", "👨‍👩‍👧‍👦", "🇺🇸", "(｡•̀ᴗ-)✧".repeat(100))
        compose.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    labels.forEach { label ->
                        Box(Modifier.size(24.dp)) { FittedGestureText(label, TextStyle(fontSize = 16.sp)) }
                    }
                }
            }
        }
        labels.forEach { compose.onNodeWithText(it).assertExists() }
    }

    @Test fun inheritedFontAndOrdinaryCharacterRenderingStayUnchanged() {
        val style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalTextStyle provides style, LocalKeyGlyphColor provides Color.Red) {
                    Row {
                        Box(Modifier.size(40.dp).background(Color.Black).testTag("actual")) {
                            KeyText(KeyC(KeyAction.CommitText("a")), 80.dp, false, false, false)
                        }
                        Box(Modifier.size(40.dp).background(Color.Black).testTag("expected")) {
                            Text("a", style = style)
                        }
                    }
                }
            }
        }
        val actual = compose.onNodeWithTag("actual").captureToImage().asAndroidBitmap()
        val expected = compose.onNodeWithTag("expected").captureToImage().asAndroidBitmap()
        assertTrue("Ordinary characters must retain their inherited font and size", actual.sameAs(expected))
    }

    @Test fun exceptionallyLongLabelUsesAnEntireEllipsisInsteadOfClippedGraphemes() {
        val label = "👩🏽‍💻e\u0301".repeat(100)
        val style = TextStyle(fontSize = 16.sp, color = Color.Red)
        compose.setContent {
            MaterialTheme {
                Row {
                    Box(Modifier.size(24.dp).background(Color.Black).testTag("actual")) { FittedGestureText(label, style) }
                    Box(Modifier.size(24.dp).background(Color.Black).testTag("expected")) { FittedGestureText("…", style) }
                }
            }
        }
        val actual = compose.onNodeWithTag("actual").captureToImage().asAndroidBitmap()
        val expected = compose.onNodeWithTag("expected").captureToImage().asAndroidBitmap()
        assertTrue("Below the minimum, replace the whole label visually", actual.sameAs(expected))
        compose.onNodeWithText(label).assertExists()
    }

    @Test fun scaleNeverEnlargesAndFitsBothAxes() {
        assertEquals(1f, gestureLabelScale(12, 20, 30, 80), 0f)
        assertEquals(.25f, gestureLabelScale(120, 20, 30, 80), 0f)
        assertEquals(.5f, gestureLabelScale(12, 160, 30, 80), 0f)
        assertEquals(0f, gestureLabelScale(12, 20, 0, 80), 0f)
    }
}
