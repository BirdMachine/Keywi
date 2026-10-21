package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardGradientStop
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLegibilityTest {
    @Test fun workspaceControlsRemainReadableOverBrightAndDarkKeyboardArt() {
        for (dark in listOf(false, true)) {
            val colors = gemSettingsColors(dark)
            val scheme = gemSettingsScheme(dark, colors)
            for (art in listOf(Color.Black, Color.White, Color.Magenta, Color.Yellow)) {
                val renderedWorkspace = scheme.surface.copy(alpha = .9f).compositeOver(art)
                for (foreground in listOf(scheme.onSurface, scheme.onSurfaceVariant, scheme.primary)) {
                    assertTrue("Workspace text needs contrast over $art", settingsContrastRatio(foreground, renderedWorkspace) >= 4.5f)
                }
                val panelTop = colors.surfaceRaised.copy(alpha = .84f).compositeOver(art)
                for (gloss in listOf(Color.White.copy(alpha = .04f), Color.Black.copy(alpha = .04f))) {
                    val renderedPanel = gloss.compositeOver(panelTop)
                    for (foreground in listOf(colors.text, colors.textMuted, colors.accent)) {
                        assertTrue("Panel text needs contrast over $art", settingsContrastRatio(foreground, renderedPanel) >= 4.5f)
                    }
                }
            }
        }
    }

    @Test fun themeBrightnessOverridesAnOppositeKeyboardColorScheme() {
        fun style(color: Color) =
            SettingsDripStyle(
                backdrop =
                    BackdropThemePreferences
                        .stateForPreset(BackdropPreset.BIRDIE_RAINBOW)
                        .copy(stops = listOf(KeyboardGradientStop(0f, color), KeyboardGradientStop(1f, color))),
                image = null,
                animated = null,
                frame = 0,
                keys = KeyThemeState(),
                power = KeywiPowerMode.CONSERVED,
                optics = null,
            )
        assertTrue(settingsArtworkIsDark(style(Color.Black), Color.White))
        assertFalse(settingsArtworkIsDark(style(Color.White), Color.Black))
        assertTrue(settingsArtworkIsDark(null, Color.Black))
        assertFalse(settingsArtworkIsDark(null, Color.White))
    }

    @Test fun chosenForegroundAlwaysMeetsNormalTextContrast() {
        for (value in 0..255) {
            val shade = Color(value, value, value)
            assertTrue(settingsContrastRatio(readableSettingsForeground(shade), shade) >= 4.5f)
        }
    }
}
