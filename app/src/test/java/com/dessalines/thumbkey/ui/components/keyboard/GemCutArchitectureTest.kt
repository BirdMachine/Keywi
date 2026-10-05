package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GemCutArchitectureTest {
    @Test
    fun existingGeometryIsPreservedAsMeLike() {
        assertEquals(FacetId.ME_LIKE, KeywiFacets.meLike.id)
        assertTrue(KeywiFacets.meLike.supportsCustomBoards)
    }

    @Test
    fun machineCutIsASeparateFacet() {
        assertEquals(FacetId.MACHINE_CUT, KeywiFacets.machineCut.id)
        assertTrue(KeywiFacets.machineCut.supportsLayers)
    }

    @Test
    fun themeLayoutAndFacetAreIndependentSelectionAxes() {
        val selection = GemCutSelection(
            facet = FacetId.MACHINE_CUT,
            layout = LayoutId("en-qwerty"),
            theme = ThemeId("neon-aviary"),
        )
        assertEquals(FacetId.MACHINE_CUT, selection.facet)
        assertEquals("en-qwerty", selection.layout.value)
        assertEquals("neon-aviary", selection.theme.value)
    }
}
