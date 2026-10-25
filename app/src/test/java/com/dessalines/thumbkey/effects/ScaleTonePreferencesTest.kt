package com.dessalines.thumbkey.effects

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ScaleTonePreferencesTest {
    @Test fun scaleChoiceRoundTripsWithoutEnablingOrChangingTheClipPack() {
        val context = RuntimeEnvironment.getApplication()
        val config = ActionSoundConfig(
            enabled = false, toneEnabled = true, toneRoot = 11, toneScale = NoteScale.MINOR,
            toneVolume = .22f, keys = mapOf("key0_0:CENTER" to "starter-drop"),
        )
        ActionSoundPreferences.save(context, config)
        val prefs = context.getSharedPreferences("keywi_action_sounds", 0)
        prefs.edit().putString("config", prefs.getString("config", "") + " ").commit()
        assertEquals(config, ActionSoundPreferences.get(context))
        assertTrue(ActionSoundEngine.isEnabled(context))
        FeedbackPreferences.save(context, FeedbackMode.SILENT)
        assertFalse(FeedbackPreferences.soundAllowed(context))
        FeedbackPreferences.save(context, FeedbackMode.SOUND)
        assertTrue(FeedbackPreferences.soundAllowed(context))
    }
}
