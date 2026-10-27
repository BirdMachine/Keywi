package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.ui.graphics.Color
import com.dessalines.thumbkey.ui.components.keyboard.GradientDistribution
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeySurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardBackdrop

/**
 * Non-shader material starting points. Only global key material fields change:
 * per-key overrides, saved groups, and user-authored backdrop/media stay untouched.
 * Each application is an explicit user action with a one-step undo in the editor.
 */
internal enum class KeyMaterialRecipe(val label: String, val description: String) {
    OPAL("Opal enamel", "Dark iridescent glaze with a shared jewel-tone rim"),
    LAVA("Molten lacquer", "Black-cherry enamel with staggered ember edges"),
    COBALT("Cobalt glass", "Deep blue faceted finish with cool luminous borders"),
    ;

    fun applyTo(original: KeyThemeState): KeyThemeState =
        when (this) {
            OPAL ->
                original.copy(
                    surfaceStyle = KeySurfaceStyle.GRADIENT,
                    surfaceGradient = gradient(28f, 0xFF19152D, 0xFF46365F, 0xFF24505A, 0xFF181A37),
                    borderStyle = KeyBorderStyle.GRADIENT,
                    borderGradient = gradient(135f, 0xFFFC88D7, 0xFF8DE5F4, 0xFFD6A4FF, 0xFFFC88D7),
                    borderDistribution = GradientDistribution.SHARED,
                )
            LAVA ->
                original.copy(
                    surfaceStyle = KeySurfaceStyle.GRADIENT,
                    surfaceGradient = gradient(55f, 0xFF19090F, 0xFF6A211E, 0xFF391019, 0xFF16080E),
                    borderStyle = KeyBorderStyle.GRADIENT,
                    borderGradient = gradient(0f, 0xFFFFE2A0, 0xFFFF713D, 0xFFC72E52, 0xFFFFB35A),
                    borderDistribution = GradientDistribution.INCREMENTAL,
                    borderPhaseStepDegrees = 26f,
                )
            COBALT ->
                original.copy(
                    surfaceStyle = KeySurfaceStyle.GRADIENT,
                    surfaceGradient = gradient(18f, 0xFF0A152A, 0xFF2C507B, 0xFF132C4E, 0xFF101A32),
                    borderStyle = KeyBorderStyle.GRADIENT,
                    borderGradient = gradient(120f, 0xFF83D9FF, 0xFF527BFF, 0xFFBED5FF, 0xFF83D9FF),
                    borderDistribution = GradientDistribution.SAME,
                )
        }

    companion object {
        private fun gradient(angle: Float, vararg colors: Long): KeyboardBackdrop =
            KeyboardBackdrop.fromColors(colors.map { Color(it) }, angle)
    }
}
