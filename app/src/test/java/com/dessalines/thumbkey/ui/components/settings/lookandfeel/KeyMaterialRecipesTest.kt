package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.ui.graphics.Color
import com.dessalines.thumbkey.ui.components.keyboard.GradientDistribution
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyDesignGroup
import com.dessalines.thumbkey.ui.components.keyboard.KeyDesignOverride
import com.dessalines.thumbkey.ui.components.keyboard.KeySurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class KeyMaterialRecipesTest {
    private val individual = KeyDesignOverride(surfaceColor = Color.Red)
    private val group = KeyDesignGroup(
        id = "accent",
        keyIds = setOf("0,0", "0,1"),
        design = KeyDesignOverride(borderColor = Color.Cyan),
    )

    @Test fun recipesPreservePerKeyOverridesAndGroups() {
        val original = KeyThemeState(
            keyOverrides = mapOf("0,0" to individual),
            keyGroups = listOf(group),
            borderRandomSeed = 42,
            glyphColor = Color.White,
        )
        KeyMaterialRecipe.entries.forEach { recipe ->
            val result = recipe.applyTo(original)
            assertEquals(recipe.name, original.keyOverrides, result.keyOverrides)
            assertEquals(recipe.name, original.keyGroups, result.keyGroups)
            assertEquals(recipe.name, original.borderRandomSeed, result.borderRandomSeed)
            assertEquals(recipe.name, original.glyphColor, result.glyphColor)
            assertEquals(KeySurfaceStyle.GRADIENT, result.surfaceStyle)
            assertEquals(KeyBorderStyle.GRADIENT, result.borderStyle)
            assertEquals(4, result.surfaceGradient.stops.size)
            assertEquals(4, result.borderGradient.stops.size)
        }
        assertEquals(KeyThemeState().surfaceGradient, original.surfaceGradient)
    }

    @Test fun eachRecipeHasAVisuallyDistinctSurfaceAndDistribution() {
        val initial = KeyThemeState()
        val opal = KeyMaterialRecipe.OPAL.applyTo(initial)
        val lava = KeyMaterialRecipe.LAVA.applyTo(initial)
        val cobalt = KeyMaterialRecipe.COBALT.applyTo(initial)
        assertNotEquals(opal.surfaceGradient, lava.surfaceGradient)
        assertNotEquals(lava.surfaceGradient, cobalt.surfaceGradient)
        assertNotEquals(opal.borderGradient, cobalt.borderGradient)
        assertEquals(GradientDistribution.SHARED, opal.borderDistribution)
        assertEquals(GradientDistribution.INCREMENTAL, lava.borderDistribution)
        assertEquals(GradientDistribution.SAME, cobalt.borderDistribution)
        assertEquals(26f, lava.borderPhaseStepDegrees, 0.001f)
    }
}
