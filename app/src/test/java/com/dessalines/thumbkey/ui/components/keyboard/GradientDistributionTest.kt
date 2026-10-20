package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradientDistributionTest {
    private fun phase(
        mode: GradientDistribution,
        id: String = "0:0",
        index: Int = 0,
        step: Float = 30f,
        seed: Int = 0,
    ) = gradientPhaseDegrees(mode, id, index, step, seed)

    @Test fun defaultsAndSharedCanvasHaveNoLocalPhase() {
        assertEquals(0f, phase(GradientDistribution.SAME, index = 10), 0f)
        assertEquals(0f, phase(GradientDistribution.SHARED, index = 10), 0f)
    }

    @Test fun incrementalPhaseWrapsAndAllowsReverseDirection() {
        assertEquals(30f, phase(GradientDistribution.INCREMENTAL, index = 13), 0f)
        assertEquals(330f, phase(GradientDistribution.INCREMENTAL, index = 1, step = -30f), 0f)
        assertEquals(0f, phase(GradientDistribution.INCREMENTAL, step = Float.NaN), 0f)
    }

    @Test fun randomPhaseIsStablePerPositionAndSeedAndInsideCycle() {
        val a = phase(GradientDistribution.STABLE_RANDOM, "0:0", seed = 7)
        assertEquals(a, phase(GradientDistribution.STABLE_RANDOM, "0:0", index = 500, seed = 7), 0f)
        assertNotEquals(a, phase(GradientDistribution.STABLE_RANDOM, "0:1", seed = 7))
        assertNotEquals(a, phase(GradientDistribution.STABLE_RANDOM, "0:0", seed = 8))
        repeat(100) { assertTrue(phase(GradientDistribution.STABLE_RANDOM, "3:$it") in 0f..<360f) }
    }

    @Test fun specificDesignOverridesGroupAndGroupOverridesGlobalWithoutChangingOthers() {
        val theme =
            KeyThemeState(
                borderStyle = KeyBorderStyle.NONE,
                keyGroups = listOf(KeyDesignGroup("arrows", setOf("0:0", "0:1"), KeyDesignOverride(borderColor = Color.Red))),
                keyOverrides = mapOf("0:0" to KeyDesignOverride(borderGradient = BIRDIE_GOLD_BORDER)),
            )
        assertEquals(KeyBorderStyle.GRADIENT, theme.forKey("0:0").borderStyle)
        assertEquals(KeyBorderStyle.SOLID, theme.forKey("0:1").borderStyle)
        assertEquals(Color.Red, theme.forKey("0:1").borderColor)
        assertEquals(KeyBorderStyle.NONE, theme.forKey("1:0").borderStyle)
    }
}
