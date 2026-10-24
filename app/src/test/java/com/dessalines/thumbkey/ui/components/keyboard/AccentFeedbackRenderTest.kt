package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.settings.AccentFeedbackEditor
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
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AccentFeedbackRenderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun releasingDoesNotFreezeFadeAndPasswordSuppressionClearsIt() {
        AccentFeedbackPreferences.save(RuntimeEnvironment.getApplication(), AccentFeedbackConfig(1000, listOf(Color.Red, Color.Green, Color.Blue)))
        val pressed = mutableStateOf(false)
        val suppressed = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                Box(Modifier.size(100.dp).background(Color.Black).testTag("flash")) {
                    KeyAccentOverlay(pressed.value, Modifier.matchParentSize(), suppressed = suppressed.value)
                }
            }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { pressed.value = true }
        compose.mainClock.advanceTimeBy(200)
        compose.runOnIdle { pressed.value = false }
        compose.mainClock.advanceTimeBy(250)
        fun pixel() = compose.onNodeWithTag("flash").captureToImage().toPixelMap()[50, 50]
        val middle = pixel()
        assertTrue("Second color must appear after release: $middle", middle.green > middle.red && middle.green > middle.blue)
        compose.mainClock.advanceTimeBy(1500)
        assertTrue("Fade must finish even after finger lift", pixel().green < .02f)
        compose.runOnIdle { pressed.value = true }
        compose.mainClock.advanceTimeBy(100)
        compose.runOnIdle { suppressed.value = true }
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Password fields must suppress the accent", pixel().red < .02f)
    }

    @Test fun rendersTheFrameEditor() {
        AccentFeedbackPreferences.save(RuntimeEnvironment.getApplication(), AccentFeedbackConfig(750, listOf(Color.Red, Color.Green, Color.Blue), true))
        compose.setContent { MaterialTheme { Column { AccentFeedbackEditor() } } }
        val file = File("build/outputs/color-review/feedback-frame-editor.png")
        file.parentFile.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
