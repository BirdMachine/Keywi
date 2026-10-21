package com.dessalines.thumbkey.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ActionSoundSelectionTest {
    private val clips = listOf(SoundClip("a", "First", "builtin:soft"), SoundClip("b", "Second", "builtin:drop"))

    @Test
    fun removedSelectionFallsBackAndClearsAssignmentsWithoutDeletingOtherRules() {
        val config =
            ActionSoundConfig(
                clips = clips,
                selectedId = "a",
                keys = mapOf("key0_0:TOP" to "a", "key1_1:CENTER" to "b"),
                directions = mapOf("TOP" to "a", "BOTTOM" to "b"),
                suggestionScrollId = "a",
                suggestionSelectedId = "b",
            )
        val next = config.withoutClip("a")
        assertEquals("b", next.selectedId)
        assertFalse(next.keys.containsKey("key0_0:TOP"))
        assertEquals("b", next.keys["key1_1:CENTER"])
        assertNull(next.directions["TOP"])
        assertEquals("b", next.directions["BOTTOM"])
        assertNull(next.suggestionScrollId)
        assertEquals("b", next.suggestionSelectedId)
        assertEquals("", next.withoutClip("b").selectedId)
    }

    @Test
    fun perKeyGesturesOverrideOrderAndDirectionButUnassignedKeysInherit() {
        val config =
            ActionSoundConfig(
                clips = clips,
                selection = SoundSelection.ORDERED,
                keys = mapOf("key0_0:TOP" to "b"),
                directions = mapOf("TOP" to "b"),
            )
        assertEquals("b", config.clipForAction("top", "key0_0", 0))
        assertEquals("a", config.clipForAction("TOP", "key1_1", 0))
        assertEquals("b", config.copy(selection = SoundSelection.DIRECTION).clipForAction("top", "key1_1", 0))
        assertNull(config.copy(selection = SoundSelection.DIRECTION).clipForAction("BOTTOM", "key1_1", 0))
    }

    @Test
    fun orderedPacksWrapSafelyForEmptyPacksAndCounterOverflow() {
        val config = ActionSoundConfig(clips = clips, selection = SoundSelection.ORDERED)
        assertEquals("a", config.clipForAction("CENTER", null, 2))
        assertEquals("b", config.clipForAction("CENTER", null, Long.MAX_VALUE))
        assertEquals("a", config.clipForAction("CENTER", null, Long.MIN_VALUE))
        assertNull(config.copy(clips = emptyList()).clipForAction("CENTER", null, 5))
    }
}
