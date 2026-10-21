package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
import kotlin.math.abs

/** Native pixels verify the shader is distributed, rather than just testing its phase arithmetic. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GradientBorderRenderTest {
    @get:Rule val compose = createComposeRule()
    private val context: Application get() = ApplicationProvider.getApplicationContext()
    private val base =
        KeyThemeState(
            surfaceStyle = KeySurfaceStyle.SOLID,
            surfaceColor = Color.Black,
            borderStyle = KeyBorderStyle.GRADIENT,
            borderGradient = KeyboardBackdrop.fromColors(listOf(Color.Red, Color.Green, Color.Blue), 0f),
            borderPhaseStepDegrees = 180f,
            borderRandomSeed = 701,
        )

    private fun show() {
        KeyThemePreferences.save(context, base)
        compose.setContent {
            MaterialTheme {
                Row {
                    PaletteKeySurface(
                        onClick = {},
                        modifier = Modifier.size(100.dp, 60.dp).testTag("first"),
                        radius = 0,
                        designId = "0:0",
                        sequenceIndex = 0,
                    ) {}
                    PaletteKeySurface(
                        onClick = {},
                        modifier = Modifier.size(100.dp, 60.dp).testTag("second"),
                        radius = 0,
                        designId = "0:1",
                        sequenceIndex = 1,
                    ) {}
                }
            }
        }
        compose.waitForIdle()
    }

    private fun mode(
        distribution: GradientDistribution,
        name: String,
    ): Pair<ImageBitmap, ImageBitmap> {
        compose.runOnIdle { KeyThemePreferences.save(context, base.copy(borderDistribution = distribution)) }
        compose.waitForIdle()
        val file = File("build/outputs/gradient-review/$name.png").apply { parentFile.mkdirs() }
        file.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        return compose.onNodeWithTag("first").captureToImage() to compose.onNodeWithTag("second").captureToImage()
    }

    private fun difference(
        a: Color,
        b: Color,
    ): Float = abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue)

    private fun topBorderDifference(
        first: ImageBitmap,
        second: ImageBitmap,
    ): Float {
        val a = first.toPixelMap()
        val b = second.toPixelMap()
        // Flat top edge avoids corner rasterization and side-stroke overlap.
        return (3 until a.width - 3).map { x -> difference(a[x, 0], b[x, 0]) }.average().toFloat()
    }

    @Test fun nativePaletteBordersKeepDefaultAlignmentButDistributePhasesAndShareCanvas() {
        show()
        val same = mode(GradientDistribution.SAME, "same")
        val pixels = same.first.toPixelMap()
        assertTrue("Saved red/green/blue stops must actually paint the border", difference(pixels[5, 0], pixels[pixels.width - 6, 0]) > .8f)
        assertTrue("Default themes must keep matching borders", topBorderDifference(same.first, same.second) < .02f)

        val incremental = mode(GradientDistribution.INCREMENTAL, "incremental")
        assertTrue("180 degree phase must visibly offset the second tile", topBorderDifference(incremental.first, incremental.second) > .3f)

        val random = mode(GradientDistribution.STABLE_RANDOM, "stable-random")
        assertTrue("Stable random positions must use distinct painted phases", topBorderDifference(random.first, random.second) > .1f)
        val repeated = mode(GradientDistribution.STABLE_RANDOM, "stable-random-repeat")
        assertTrue("Recomposition must not reshuffle random colors", topBorderDifference(random.first, repeated.first) < .02f)

        val shared = mode(GradientDistribution.SHARED, "shared")
        val left = shared.first.toPixelMap()
        val right = shared.second.toPixelMap()
        assertTrue("Shared canvas must meet continuously across the tile boundary", difference(left[left.width - 2, 0], right[1, 0]) < .1f)
        assertTrue("Shared tiles must sample different parts of the common canvas", topBorderDifference(shared.first, shared.second) > .15f)
    }
}
