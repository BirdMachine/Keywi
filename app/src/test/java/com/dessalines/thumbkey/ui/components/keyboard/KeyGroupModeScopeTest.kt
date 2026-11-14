package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.dessalines.thumbkey.utils.KeyboardMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class KeyGroupModeScopeTest {
    private val main = KeyDesignOverride(surfaceColor = Color.Blue)
    private val shifted = KeyDesignOverride(surfaceColor = Color.Green)
    private val numeric = KeyDesignOverride(surfaceGradient = BIRDIE_RAINBOW_BACKDROP, borderGradient = BIRDIE_GOLD_BORDER)
    private val group = KeyDesignGroup("Modes", setOf("0:0", "0:1"), KeyDesignOverride(),
        modeDesigns = mapOf(KeyboardMode.MAIN to main, KeyboardMode.SHIFTED to shifted, KeyboardMode.NUMERIC to numeric),
        modeSharedCanvases = mapOf(KeyboardMode.NUMERIC to true))
    private val rectangles = mapOf("0:0" to KeyCanvasRect(0f, 0f, 100f, 100f), "0:1" to KeyCanvasRect(100f, 0f, 100f, 100f))

    @Test fun numericGradientCannotOverwriteMainOrShiftedGroupStyles() {
        val theme = KeyThemeState(keyGroups = listOf(group))
        assertEquals(Color.Blue, theme.forKey("0:0", KeyboardMode.MAIN).surfaceColor)
        assertEquals(KeySurfaceStyle.SOLID, theme.forKey("0:0", KeyboardMode.MAIN).surfaceStyle)
        assertEquals(Color.Green, theme.forKey("0:0", KeyboardMode.SHIFTED).surfaceColor)
        assertEquals(numeric.surfaceGradient, theme.forKey("0:0", KeyboardMode.NUMERIC).surfaceGradient)
        assertEquals(Color.Blue, theme.forKey("0:0", KeyboardMode.CTRLED).surfaceColor)
        assertNull(theme.groupCanvasForKey("0:0", KeyGradientLayer.SURFACE, rectangles, KeyboardMode.MAIN))
        assertNull(theme.groupCanvasForKey("0:0", KeyGradientLayer.BORDER, rectangles, KeyboardMode.SHIFTED))
        assertEquals(KeyCanvasRect(0f, 0f, 200f, 100f), theme.groupCanvasForKey("0:0", KeyGradientLayer.SURFACE, rectangles, KeyboardMode.NUMERIC))
    }

    @Test fun numericOnlyGroupHasNoEffectOnOtherModesEvenWhenItSharesTheCanvas() {
        val numericOnly = group.copy(modeDesigns = mapOf(KeyboardMode.NUMERIC to numeric), sharedGradientCanvas = true)
        val baseline = KeyThemeState()
        val theme = baseline.copy(keyGroups = listOf(numericOnly))
        assertEquals(baseline.surfaceGradient, theme.forKey("0:0", KeyboardMode.MAIN).surfaceGradient)
        assertEquals(baseline.surfaceGradient, theme.forKey("0:0", KeyboardMode.SHIFTED).surfaceGradient)
        assertNull(theme.groupCanvasForKey("0:0", KeyGradientLayer.SURFACE, rectangles, KeyboardMode.MAIN))
        assertNull(theme.groupCanvasForKey("0:0", KeyGradientLayer.BORDER, rectangles, KeyboardMode.SHIFTED))
    }

    @Test fun modesRoundTripAndLegacyGroupsKeepTheirExistingBehavior() {
        val context = RuntimeEnvironment.getApplication()
        val theme = KeyThemeState(keyGroups = listOf(group))
        KeyThemePreferences.save(context, theme)
        assertEquals(theme, KeyThemePreferences.load(context))
        val legacy = group.copy(design = main, modeDesigns = emptyMap(), modeSharedCanvases = emptyMap())
        assertEquals(main, legacy.designForMode(KeyboardMode.NUMERIC))
        val edited = legacy.copy(modeDesigns = mapOf(KeyboardMode.NUMERIC to numeric))
        assertEquals(main, edited.designForMode(KeyboardMode.MAIN))
        assertEquals(numeric, edited.designForMode(KeyboardMode.NUMERIC))
        val resetNumeric = edited.copy(modeDesigns = edited.modeDesigns + (KeyboardMode.NUMERIC to KeyDesignOverride()))
        assertEquals(main, resetNumeric.designForMode(KeyboardMode.MAIN))
        assertEquals(KeyDesignOverride(), resetNumeric.designForMode(KeyboardMode.NUMERIC))
    }
}
