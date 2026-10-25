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
    @Test fun keySoundSourcesAreMutuallyExclusiveAndKeepTheirSavedOptions() {
        val original = ActionSoundConfig(enabled = true, toneRoot = 6, toneScale = NoteScale.MINOR)
        val generated = original.withSource(KeySoundSource.GENERATED)
        assertFalse(generated.enabled)
        assertTrue(generated.toneEnabled)
        val clips = generated.withSource(KeySoundSource.CLIPS)
        assertTrue(clips.enabled)
        assertFalse(clips.toneEnabled)
        assertEquals(6, clips.toneRoot)
        assertEquals(original.clips, clips.clips)
        val context = RuntimeEnvironment.getApplication()
        ActionSoundPreferences.save(context, original.copy(toneEnabled = true))
        assertFalse(ActionSoundPreferences.get(context).enabled)
        assertEquals(KeySoundSource.GENERATED, ActionSoundPreferences.get(context).source)
        val normal = clips.withSource(KeySoundSource.DEFAULT)
        assertFalse(normal.enabled)
        assertFalse(normal.toneEnabled)
    }

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
