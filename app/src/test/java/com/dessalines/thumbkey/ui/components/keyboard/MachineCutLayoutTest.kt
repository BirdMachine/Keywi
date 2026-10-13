package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineCutLayoutTest {
    @Test
    fun qwertySkeletonHasFiveDenseRows() {
        val layout = MachineCutLayouts.qwertySkeleton
        assertEquals(5, layout.rows.size)
        assertTrue(layout.rows.take(4).all { it.size >= 13 })
    }

    @Test
    fun qwertySkeletonContainsPowerUserRegions() {
        val labels = MachineCutLayouts.qwertySkeleton.rows.flatten().map { it.label }.toSet()
        assertTrue(labels.containsAll(setOf("Esc", "Ctrl", "Alt", "Fn", "Space", "⌫", "↵")))
    }
}
