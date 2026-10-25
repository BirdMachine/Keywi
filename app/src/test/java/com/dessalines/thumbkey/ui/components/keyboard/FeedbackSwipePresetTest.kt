package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.FeedbackMode
import com.dessalines.thumbkey.effects.FeedbackPreferences
import com.dessalines.thumbkey.keyboards.EMOJI_KEY_ITEM
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.SwipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class FeedbackSwipePresetTest {
    @get:Rule val compose = createComposeRule()

    @Test fun presetUsesTheOrdinarySwipeSlotAndKeepsItsOtherActions() {
        org.junit.Assert.assertNull(EMOJI_KEY_ITEM.topRight)
        assertNotNull(EMOJI_KEY_ITEM.getSwipe(SwipeDirection.TOP)?.action)
        assertNotNull(EMOJI_KEY_ITEM.getSwipe(SwipeDirection.LEFT)?.action)
        org.junit.Assert.assertTrue(EMOJI_KEY_ITEM.center.action is KeyAction.ToggleEmojiMode)
    }

    @Test fun feedbackControlWorksInSettingsWithoutAKeyCorner() {
        val context = RuntimeEnvironment.getApplication()
        FeedbackPreferences.save(context, FeedbackMode.SOUND)
        compose.setContent { MaterialTheme { com.dessalines.thumbkey.effects.FeedbackModeControl() } }
        compose.onNodeWithText("Feedback: Sound → Vibrate only").performClick()
        compose.runOnIdle { assertEquals(FeedbackMode.VIBRATE, FeedbackPreferences.get(context)) }
        compose.onNodeWithText("Feedback: Vibrate only → Silent").performClick()
        compose.runOnIdle { assertEquals(FeedbackMode.SILENT, FeedbackPreferences.get(context)) }
    }
}
