package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OklabReferenceGradientsTest {
    @Test fun sourceRampsHaveDenseOrderedStopsAndClosedSeams() {
        listOf(OklabReferenceGradients.broad, OklabReferenceGradients.tight).forEach { stops ->
            assertTrue(stops.size >= 65)
            assertEquals(0f, stops.first().position, 0f)
            assertEquals(1f, stops.last().position, 0f)
            assertEquals(stops.first().color, stops.last().color)
            assertTrue(stops.zipWithNext().all { (a, b) -> a.position < b.position })
            assertTrue(stops.all { it.color.alpha == 1f })
        }
    }

    @Test fun bothPublishedRampsRemainDistinctAndDeclareApproximation() {
        val presets = GradientLibrary.builtIns.filter { it.id.startsWith("builtin_oklab_") }
        assertEquals(listOf("Oklab Broad", "Oklab Tight"), presets.map { it.name })
        assertTrue(presets.all { it.builtIn && it.description.orEmpty().contains("Sampled approximation") })
        assertNotEquals(presets[0].stops, presets[1].stops)
        // Pin visibly different source quarter-points, guarding against swapped or duplicated ramps.
        assertTrue(presets[0].stops[24].color.blue < presets[1].stops[24].color.blue)
        assertTrue(presets[0].stops[24].color.green > presets[1].stops[24].color.green)
    }
}
