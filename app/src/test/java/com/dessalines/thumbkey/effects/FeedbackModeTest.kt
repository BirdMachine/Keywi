package com.dessalines.thumbkey.effects

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class FeedbackModeTest {
    @Test fun cycleGatesBothChannelsAndPreservesThePack() {
        val context = RuntimeEnvironment.getApplication()
        val pack = ActionSoundConfig(enabled = true, selectedId = "starter-drop", trigger = SoundTrigger.RELEASE, overlap = false)
        ActionSoundPreferences.save(context, pack)
        var mode = FeedbackMode.SOUND
        for ((sound, haptic) in listOf(true to true, false to true, false to false)) {
            FeedbackPreferences.save(context, mode)
            assertEquals(mode, FeedbackPreferences.get(context))
            assertEquals(sound, FeedbackPreferences.soundAllowed(context))
            assertEquals(haptic, FeedbackPreferences.hapticsAllowed(context))
            assertEquals(pack, ActionSoundPreferences.get(context))
            mode = mode.next()
        }
        assertEquals(FeedbackMode.SOUND, mode)
    }
    @Test fun soundModeDoesNotEnableAnUntouchedCustomLibrary() {
        val context = RuntimeEnvironment.getApplication()
        ActionSoundPreferences.save(context, ActionSoundConfig())
        FeedbackPreferences.save(context, FeedbackMode.SOUND)
        assertTrue(FeedbackPreferences.soundAllowed(context))
        assertFalse(ActionSoundEngine.isEnabled(context))
    }
}
