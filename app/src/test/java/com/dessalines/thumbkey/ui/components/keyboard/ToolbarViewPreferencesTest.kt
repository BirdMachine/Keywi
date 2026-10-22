package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ToolbarViewPreferencesTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before fun reset() {
        context
            .getSharedPreferences("toolbar_views", 0)
            .edit()
            .clear()
            .commit()
    }

    @Test fun homesAndGestureChoicesRoundTripWithoutChangingOtherBindings() {
        ToolbarViewPreferences.setHome(context, ToolbarPage.KAOMOJI)
        ToolbarViewPreferences.setAction(context, ToolbarGesture.HOLD, ToolbarGestureAction.NONE)
        ToolbarViewPreferences.setAction(context, ToolbarGesture.TRIPLE_TAP, ToolbarGestureAction.PREVIOUS)
        assertEquals(ToolbarPage.KAOMOJI, ToolbarViewPreferences.home(context))
        assertEquals(ToolbarGestureAction.HOME, ToolbarViewPreferences.action(context, ToolbarGesture.DOUBLE_TAP))
        assertEquals(ToolbarGestureAction.NONE, ToolbarViewPreferences.action(context, ToolbarGesture.HOLD))
        assertEquals(ToolbarGestureAction.PREVIOUS, ToolbarViewPreferences.action(context, ToolbarGesture.TRIPLE_TAP))
    }

    @Test fun corruptChoicesRecoverToDefaultsAndOldHomeStillLoads() {
        context
            .getSharedPreferences("toolbar_views", 0)
            .edit()
            .putString("home", "FUTURE_PAGE")
            .putString("TRIPLE_TAP", "UNKNOWN")
            .commit()
        assertEquals(ToolbarPage.CHARACTERS, ToolbarViewPreferences.home(context))
        assertEquals(ToolbarGestureAction.QUICK_SETTINGS, ToolbarViewPreferences.action(context, ToolbarGesture.TRIPLE_TAP))
        assertEquals(ToolbarGestureAction.CONFIGURE, ToolbarViewPreferences.action(context, ToolbarGesture.HOLD))
        context
            .getSharedPreferences("toolbar_views", 0)
            .edit()
            .putString("home", "FORMATTING")
            .commit()
        assertEquals(ToolbarPage.FORMATTING, ToolbarViewPreferences.home(context))
    }

    @Test fun fourPagesCycleAndMapWithoutAmbiguity() {
        val ordered = listOf(ToolbarPage.CHARACTERS, ToolbarPage.FORMATTING, ToolbarPage.KAOMOJI, ToolbarPage.QUICK_SETTINGS)
        ordered.forEachIndexed { index, page ->
            assertEquals(ordered[(index + 1) % 4], page.step(1))
            assertEquals(page, page.step(1).step(-1))
            assertEquals(page, ToolbarPage.from(page.palette))
        }
    }
}
