package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsArtworkBoundsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun portraitBitmapInFixedBarDoesNotCoverScrollingSettings() {
        verifyArtworkBounds(animated = false)
    }

    @Test fun portraitDrawableInFixedBarDoesNotCoverScrollingSettings() {
        verifyArtworkBounds(animated = true)
    }

    private fun verifyArtworkBounds(animated: Boolean) {
        // A portrait source on a short, wide top bar requires a tall center crop.
        // The bar is intentionally drawn last, as Scaffold draws it above content.
        val image =
            Bitmap.createBitmap(20, 200, Bitmap.Config.ARGB_8888).apply {
                eraseColor(android.graphics.Color.RED)
            }
        val style =
            SettingsDripStyle(
                backdrop =
                    BackdropThemePreferences
                        .stateForPreset(BackdropPreset.BIRDIE_RAINBOW)
                        .copy(mode = if (animated) BackdropMode.GIF else BackdropMode.IMAGE),
                image = image.asImageBitmap(),
                animated = if (animated) PortraitDrawable() else null,
                frame = 0,
                keys = KeyThemeState(),
                power = if (animated) KeywiPowerMode.UNBRIDLED else KeywiPowerMode.CONSERVED,
                optics = null,
            )
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CompositionLocalProvider(
                    LocalSettingsDrip provides style,
                    LocalAdvancedSettingsColors provides gemSettingsColors(true),
                ) {
                    Box(Modifier.size(240.dp, 320.dp).background(Color.Green)) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            repeat(20) {
                                Text(
                                    "Settings row $it",
                                    Modifier.fillMaxWidth().height(64.dp).padding(start = 24.dp),
                                )
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(48.dp).settingsDripSurface())
                    }
                }
            }
        }

        fun assertRowsUncovered() {
            val rendered = compose.onRoot().captureToImage().asAndroidBitmap()
            // Far below the 48dp toolbar, no red source artwork may escape.
            assertEquals(android.graphics.Color.GREEN, rendered.getPixel(3, rendered.height / 2))
            assertEquals(android.graphics.Color.GREEN, rendered.getPixel(3, rendered.height - 3))
        }
        assertRowsUncovered()
        compose.onNodeWithText("Settings row 19").performScrollTo()
        assertRowsUncovered()
    }

    /** Exercises the same native Canvas path used by AnimatedImageDrawable playback. */
    private class PortraitDrawable : Drawable() {
        private val paint = Paint().apply { color = android.graphics.Color.RED }

        override fun getIntrinsicWidth(): Int = 20

        override fun getIntrinsicHeight(): Int = 200

        override fun draw(canvas: Canvas) {
            canvas.drawRect(bounds, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Android")
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }
}
