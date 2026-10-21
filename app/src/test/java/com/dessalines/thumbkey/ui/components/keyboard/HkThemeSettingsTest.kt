package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.dessalines.thumbkey.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HkThemeSettingsTest {
    @get:Rule val compose = createComposeRule()

    private val context: Application get() = ApplicationProvider.getApplicationContext()
    private val border = Color(0xFFFF00FF)

    private fun showKeyboard(
        width: Float = 3f,
        style: KeyBorderStyle = KeyBorderStyle.SOLID,
    ) {
        KeywiAppearancePreferences.save(context, true)
        KeyThemePreferences.save(
            context,
            KeyThemeState(
                surfaceStyle = KeySurfaceStyle.SOLID,
                surfaceColor = Color(0xFF142235),
                borderStyle = style,
                borderColor = border,
                borderGradient =
                    KeyboardBackdrop(
                        listOf(
                            KeyboardGradientStop(0f, border),
                            KeyboardGradientStop(1f, border),
                        ),
                        0f,
                    ),
            ),
        )
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(390.dp)) {
                    MachineCutSkeleton(keyBorderWidth = width, keyRadius = 20)
                }
            }
        }
        compose.waitForIdle()
    }

    private fun borderPixels(): Int {
        val pixels = compose.onNodeWithContentDescription("Keywi settings").captureToImage().toPixelMap()
        var count = 0
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                val color = pixels[x, y]
                if (color.red > .9f && color.green < .1f && color.blue > .9f) count++
            }
        }
        return count
    }

    private fun capture(name: String) {
        val file = File("build/outputs/hk-review/$name.png")
        file.parentFile.mkdirs()
        file.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun solidThemeBorderAndSettingsSurviveEveryLayer() {
        showKeyboard()
        assertTrue("Saved solid border must be painted", borderPixels() > 0)
        capture("hk-basic")
        compose.onNodeWithText("?123").performClick()
        compose.onNodeWithContentDescription("Keywi settings").assertIsDisplayed()
        capture("hk-symbols")
        compose.onNodeWithText("⌘").performClick()
        compose.onNodeWithText("Ctrl").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("Keywi settings").assertIsDisplayed()
        capture("hk-advanced")
        compose.onNodeWithContentDescription("Keywi settings").performClick()
        val intent = shadowOf(context).nextStartedActivity
        assertEquals(MainActivity::class.java.name, intent.component?.className)
        assertEquals("settings", intent.getStringExtra("startRoute"))
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test fun gradientBorderUsesSavedStops() {
        showKeyboard(style = KeyBorderStyle.GRADIENT)
        assertTrue("Saved gradient border must be painted", borderPixels() > 0)
    }

    @Test fun zeroWidthDisablesThemeBorder() {
        showKeyboard(width = 0f)
        assertEquals(0, borderPixels())
    }

    @Test fun noneBorderStyleRemainsBorderless() {
        showKeyboard(style = KeyBorderStyle.NONE)
        assertEquals(0, borderPixels())
    }
}
