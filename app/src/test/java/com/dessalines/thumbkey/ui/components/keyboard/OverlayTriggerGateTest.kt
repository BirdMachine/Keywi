package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayTriggerGateTest {
    @Test
    fun fastTypingIsDroppedWithoutDelayingNextTrigger() {
        val gate = OverlayTriggerGate()
        assertTrue(gate.accept(0, 650))
        for (time in 1L..649L) assertFalse(gate.accept(time, 650))
        assertTrue(gate.accept(650, 650))
        assertFalse(gate.accept(651, 650))
        assertTrue(gate.accept(1300, 650))
    }

    @Test
    fun disabledCooldownAllowsEveryKeyAndChangesApplyImmediately() {
        val gate = OverlayTriggerGate()
        assertTrue(gate.accept(100, 650))
        assertTrue(gate.accept(101, 0))
        assertTrue(gate.accept(101, 0))
        assertFalse(gate.accept(102, 3000))
        assertTrue(gate.accept(3101, 3000))
    }
}
