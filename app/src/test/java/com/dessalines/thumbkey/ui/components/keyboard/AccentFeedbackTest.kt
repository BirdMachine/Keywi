package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AccentFeedbackTest {
    @Test fun threeFramesBlendInOrderAndFadeWithoutLosingTheirAlpha() {
        val config = AccentFeedbackConfig(colors = listOf(Color.Red, Color.Green, Color.Blue))
        assertEquals(Color.Red, config.colorAt(0f, Color.White))
        assertEquals(Color.Green.copy(alpha = .5f), config.colorAt(.5f, Color.White))
        val middle = config.colorAt(.75f, Color.White)
        assertEquals(.25f, middle.alpha, .005f)
        assertTrue(middle.green > 0f && middle.blue > 0f)
        assertEquals(0f, config.colorAt(1f, Color.White).alpha, 0f)
        assertEquals(Color.Red, config.colorAt(-1f, Color.White))
        assertEquals(0f, config.colorAt(2f, Color.White).alpha, 0f)
    }
    @Test fun themeFallbackAndTranslucentColorsKeepTheirIntent() {
        assertEquals(Color.Magenta.copy(alpha = .5f), AccentFeedbackConfig().colorAt(.5f, Color.Magenta))
        assertEquals(.25f, AccentFeedbackConfig(colors = listOf(Color.Red.copy(alpha = .5f))).colorAt(.5f, Color.White).alpha, .005f)
    }
    @Test fun sequenceAndSharedTrailSettingsRoundTripAndClamp() {
        val context = RuntimeEnvironment.getApplication()
        val config = AccentFeedbackConfig(900, listOf(Color.Red, Color.Green, Color.Blue), true)
        AccentFeedbackPreferences.save(context, config)
        val restored = AccentFeedbackPreferences.get(context)
        assertEquals(900, restored.durationMs)
        assertTrue(restored.trailEnabled)
        assertEquals(config.colors.map { it.toArgb() }, restored.colors.map { it.toArgb() })
        AccentFeedbackPreferences.save(context, config.copy(durationMs = -10))
        assertEquals(0, AccentFeedbackPreferences.get(context).durationMs)
        AccentFeedbackPreferences.save(context, config.copy(durationMs = 90000, colors = List(50) { Color.Red }))
        assertEquals(5000, AccentFeedbackPreferences.get(context).durationMs)
        assertEquals(12, AccentFeedbackPreferences.get(context).colors.size)
    }
}
