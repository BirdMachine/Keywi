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
    fun hkLikeIsASeparateFacet() {
        assertEquals(FacetId.HK_LIKE, KeywiFacets.hkLike.id)
        assertTrue(KeywiFacets.hkLike.supportsLayers)
    }

    @Test
    fun themeLayoutAndFacetAreIndependentSelectionAxes() {
        val selection = GemCutSelection(
            facet = FacetId.HK_LIKE,
            layout = LayoutId("en-qwerty"),
            theme = ThemeId("neon-aviary"),
        )
        assertEquals(FacetId.HK_LIKE, selection.facet)
        assertEquals("en-qwerty", selection.layout.value)
        assertEquals("neon-aviary", selection.theme.value)
    }
}
