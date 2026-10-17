package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineCutTouchGeometryTest {
    @Test
    fun thumbnailsUseTheActualFiveRowTouchTopology() {
        val rows = MachineCutLayouts.touchMain.rows
        assertEquals(listOf(10, 10, 9, 9, 8), rows.map { it.size })
        assertEquals("qwertyuiop", rows[1].joinToString("") { it.label })
        assertEquals(listOf("?123", ",", "☺", "Space", ".", "⚙", "↵", "⌘"), rows.last().map { it.label })
    }

    @Test
    fun touchLayersKeepTheirRealControlSpans() {
        listOf(MachineCutLayouts.touchMain, MachineCutLayouts.touchSymbols, MachineCutLayouts.touchAdvanced)
            .forEach { layout ->
                assertEquals(5, layout.rows.size)
                assertTrue(layout.rows.flatten().all { it.width > 0f })
                val cells = layout.rows.flatten()
                assertEquals(1, cells.count { it.label == "⚙" })
                assertEquals(4.4f, cells.first { it.label == "Space" }.width, 0f)
                assertEquals(1.5f, cells.first { it.label == "⌫" }.width, 0f)
            }
    }

    @Test
    fun prototypeDesktopLayerRemainsAvailable() {
        val rows = MachineCutLayouts.touchAdvanced.rows
        assertEquals((1..12).map { "F$it" }, rows.first().map { it.label })
        assertTrue(rows.flatten().map { it.label }.containsAll(listOf("Ctrl", "Alt", "Home", "End", "↑", "←", "↓", "→")))
    }
}
