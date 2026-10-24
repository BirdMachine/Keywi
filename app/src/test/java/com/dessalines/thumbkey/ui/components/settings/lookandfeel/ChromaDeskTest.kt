package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChromaDeskTest {
    @get:Rule val compose = createComposeRule()
    private val initial = Color(0x80C87BFF)
    private var applied: Color? = null
    private var dismissed = false

    private fun show() {
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                GemSettingsTheme(true) {
                    ChromaDeskDialog("Key color", initial, onDismiss = { dismissed = true }, onApply = { applied = it })
                }
            }
        }
    }

    @Test fun discClampsOutsideAndKeepsHueAtCenter() {
        assertEquals(0f, discSelection(300f, 100f, 100f, 100f, 90f, 240f).first, .001f)
        assertEquals(1f, discSelection(300f, 100f, 100f, 100f, 90f, 240f).second, .001f)
        assertEquals(90f, discSelection(100f, 190f, 100f, 100f, 90f, 240f).first, .001f)
        assertEquals(270f, discSelection(100f, 10f, 100f, 100f, 90f, 240f).first, .001f)
        assertEquals(240f, discSelection(100f, 100f, 100f, 100f, 90f, 240f).first, .001f)
        assertEquals(0f, discSelection(100f, 100f, 100f, 100f, 90f, 240f).second, .001f)
        assertEquals(0f, stripSelection(-100f, 200f, 12f), .001f)
        assertEquals(1f, stripSelection(400f, 200f, 12f), .001f)
    }

    @Test fun hexPreservesOpacityUnlessExplicitAndRejectsPartialInput() {
        assertEquals(initial.toArgb(), parseColorHex(" c87bff ", initial.alpha)!!.toArgb())
        assertEquals(0x40C87BFF, parseColorHex("#40C87BFF", 1f)!!.toArgb())
        listOf("#C87", "#GGFFFF", "1234567", "", "#123456789").forEach { assertNull(parseColorHex(it, 1f)) }
    }

    @Test fun cancelDoesNotPersistDraftAndApplyPreservesAlpha() {
        show()
        compose.onNodeWithText("HEX").performScrollTo().performTextReplacement("#12ABEF")
        compose.runOnIdle { assertNull(applied) }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle {
            assertTrue(dismissed)
            assertNull(applied)
        }
        compose.onNodeWithText("Apply").performClick()
        compose.runOnIdle { assertEquals(0x8012ABEF.toInt(), applied!!.toArgb()) }
    }

    @Test fun invalidHexCannotApplyAndRestoreResetsDraft() {
        show()
        compose.onNodeWithText("HEX").performScrollTo().performTextReplacement("#nope")
        compose.onNodeWithText("Apply").assertIsNotEnabled()
        compose.onNodeWithText("↶ Restore original").performScrollTo().performClick()
        compose.onNodeWithText("Apply").performClick()
        compose.runOnIdle { assertEquals(initial.toArgb(), applied!!.toArgb()) }
    }

    @Test fun blackRetainsDiscSelectionAndAlpha() {
        show()
        compose.onNodeWithContentDescription("Brightness").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        compose
            .onNodeWithContentDescription(
                "Hue and saturation disc",
            ).performScrollTo()
            .performTouchInput { click(Offset(width / 2f, 0f)) }
        compose.onNodeWithContentDescription("Brightness").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        compose.onNodeWithText("Apply").performClick()
        compose.runOnIdle { assertEquals(0x808000FF.toInt(), applied!!.toArgb()) }
    }

    @Test fun invalidRgbStillBlocksApplyWhenAnotherChannelChanges() {
        show()
        compose.onNodeWithText("RGB channels ▾").performScrollTo().performClick()
        compose.onNodeWithText("R").performScrollTo().performTextReplacement("999")
        compose.onNodeWithText("G").performScrollTo().performTextReplacement("100")
        compose.onNodeWithText("Apply").assertIsNotEnabled()
        compose.onNodeWithText("R").performScrollTo().performTextReplacement("80")
        compose.onNodeWithText("Apply").performClick()
        compose.runOnIdle { assertEquals(0x805064FF.toInt(), applied!!.toArgb()) }
    }

    /** A real app-owned image must survive the panel tint outside the opaque color disc. */
    private fun showWithStripedArtwork() {
        val context = RuntimeEnvironment.getApplication()
        val fixture = File(context.filesDir, "chroma-desk-artwork.png")
        val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                bitmap.setPixel(x, y, if ((x / 16) % 2 == 0) android.graphics.Color.RED else android.graphics.Color.BLUE)
            }
        }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        BackdropThemePreferences.save(
            context,
            BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW).copy(
                mode = BackdropMode.IMAGE,
                opacity = 1f,
                mediaUri = Uri.fromFile(fixture).toString(),
            ),
        )
        show()
        compose.waitUntil(timeoutMillis = 5_000) {
            val disc = compose.onNodeWithContentDescription("Hue and saturation disc").captureToImage().asAndroidBitmap()
            var redBands = 0
            var blueBands = 0
            // The color wheel itself is opaque. Its corners are transparent and expose artwork.
            for (y in 4 until disc.height - 4 step 4) {
                for (x in 4 until disc.width - 4 step 4) {
                    val dx = (x - disc.width / 2f) / disc.width
                    val dy = (y - disc.height / 2f) / disc.height
                    if (dx * dx + dy * dy > .25f) {
                        val pixel = disc.getPixel(x, y)
                        val red = android.graphics.Color.red(pixel)
                        val blue = android.graphics.Color.blue(pixel)
                        if (red - blue > 20) redBands++
                        if (blue - red > 20) blueBands++
                    }
                }
            }
            redBands > 20 && blueBands > 20
        }
    }

    @Test fun rendersActualChromaDesk() {
        showWithStripedArtwork()
        val file = File("build/outputs/color-review/chroma-desk.png")
        file.parentFile.mkdirs()
        compose.onNode(isDialog()).captureToImage().asAndroidBitmap().let { bitmap ->
            val samples = mutableSetOf<Int>()
            for (y in 0 until bitmap.height step 8) for (x in 0 until bitmap.width step 8) samples.add(bitmap.getPixel(x, y))
            assertTrue("Capture must contain the rendered color picker", samples.size > 100)
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
