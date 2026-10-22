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
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class KeyGroupGradientRenderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun hkWeightedGroupUsesItsOwnBoundsWhileOutsideKeysKeepTheirOwnBorders() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val gradient = KeyboardBackdrop.fromColors(listOf(Color.Red, Color.Blue), 0f)
        KeywiAppearancePreferences.save(context, true)
        KeyThemePreferences.save(
            context,
            KeyThemeState(
                surfaceStyle = KeySurfaceStyle.GRADIENT,
                surfaceGradient = gradient,
                borderStyle = KeyBorderStyle.GRADIENT,
                borderGradient = gradient,
                keyGroups = listOf(KeyDesignGroup("Middle", setOf("0:1", "0:2"), KeyDesignOverride(), true)),
            ),
        )
        val layout =
            MachineCutLayout(
                LayoutId("group-render-test"),
                listOf(
                    listOf(
                        MachineCutCell("Outside left"),
                        MachineCutCell("Group left"),
                        MachineCutCell("Group wide", width = 2f),
                        MachineCutCell("Outside right"),
                    ),
                ),
            )
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(390.dp)) { MachineCutSkeleton(layout = layout, keyBorderWidth = 3f, keyRadius = 0) }
            }
        }
        compose.waitForIdle()

        fun pixels(label: String) = compose.onNodeWithText(label).captureToImage().toPixelMap()
        val left = pixels("Group left")
        val wide = pixels("Group wide")
        val outsideA = pixels("Outside left")
        val outsideB = pixels("Outside right")
        assertTrue("Selected group must start at its own red stop", left[4, 1].red > .9f && left[4, 1].blue < .1f)
        assertTrue("Weighted group must finish at its own blue stop", wide[wide.width - 5, 1].blue > .9f)
        assertTrue("Second selected key must continue the first key gradient", wide[3, 1].red in 0.55f..0.75f)
        assertTrue("Unselected keys must retain their individual border gradients", abs(outsideA[8, 1].red - outsideB[8, 1].red) < .04f)
        // Away from key labels: fill samples prove group canvas applies to both layers.
        assertTrue("Group fill must begin red", left[4, left.height / 2].red > .9f)
        assertTrue("Group fill must finish blue", wide[wide.width - 5, wide.height / 2].blue > .9f)
        File("build/outputs/gradient-review/group-weighted-hk.png").apply { parentFile.mkdirs() }.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
