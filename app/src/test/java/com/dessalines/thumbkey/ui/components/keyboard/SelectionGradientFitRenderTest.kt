package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SelectionGradientFitRenderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun assignedFillReachesBothStopsForOneKeyTwoWeightedKeysAndAllKeys() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val gradient = KeyboardBackdrop.fromColors(listOf(Color.Red, Color.Blue), 0f)
        val design = KeyDesignOverride(surfaceGradient = gradient)
        val base =
            KeyThemeState(
                surfaceStyle = KeySurfaceStyle.SOLID,
                surfaceColor = Color.Black,
                borderStyle = KeyBorderStyle.NONE,
            )
        KeywiAppearancePreferences.save(context, true)
        KeyThemePreferences.save(context, base.copy(keyOverrides = mapOf("0:1" to design)))
        val layout =
            MachineCutLayout(
                LayoutId("selection-fill-fit"),
                listOf(listOf(MachineCutCell("A"), MachineCutCell("B"), MachineCutCell("C", width = 2f), MachineCutCell("D"))),
            )
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(390.dp)) {
                    MachineCutSkeleton(layout = layout, keyBorderWidth = 0f, keyRadius = 0)
                }
            }
        }
        compose.waitForIdle()

        fun pixels(label: String) = compose.onNodeWithText(label).captureToImage().toPixelMap()

        fun capture(name: String) {
            File("build/outputs/gradient-review/selection-fit-$name.png").apply { parentFile.mkdirs() }.outputStream().use {
                compose
                    .onRoot()
                    .captureToImage()
                    .asAndroidBitmap()
                    .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        val single = pixels("B")
        assertTrue("Specific-key fill must begin with its first stop", single[4, 5].red > .9f)
        assertTrue("Specific-key fill must reach its last stop inside that key", single[single.width - 5, 5].blue > .9f)
        assertTrue("Unselected key must keep the solid base fill", pixels("A")[4, 5].red < .05f)
        capture("single")

        compose.runOnIdle {
            KeyThemePreferences.save(
                context,
                base.copy(
                    keyGroups = listOf(KeyDesignGroup("Selected", setOf("0:1", "0:2"), design, sharedGradientCanvas = false)),
                ),
            )
        }
        compose.waitForIdle()
        val first = pixels("B")
        val second = pixels("C")
        assertTrue("Two-key selection must begin at the first selected key", first[4, 5].red > .9f)
        assertTrue("Two-key selection must finish at the last selected key", second[second.width - 5, 5].blue > .9f)
        assertTrue("Gradient must span the group instead of restarting per key", first[first.width - 5, 5].red > .6f)
        assertTrue("Outside key must remain unchanged", pixels("D")[4, 5].blue < .05f)
        capture("two")

        compose.runOnIdle {
            KeyThemePreferences.save(
                context,
                base.copy(
                    keyGroups = listOf(KeyDesignGroup("All", setOf("0:0", "0:1", "0:2", "0:3"), design, sharedGradientCanvas = false)),
                ),
            )
        }
        compose.waitForIdle()
        val allFirst = pixels("A")
        val allLast = pixels("D")
        assertTrue("All-key selection must start at its outer bounds", allFirst[4, 5].red > .9f)
        assertTrue("All-key selection must finish at its outer bounds", allLast[allLast.width - 5, 5].blue > .9f)
        capture("all")
    }
}
