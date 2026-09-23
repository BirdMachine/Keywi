package com.dessalines.thumbkey.ui.components.keyboard

import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyC
import com.dessalines.thumbkey.utils.KeyItemC
import com.dessalines.thumbkey.utils.KeyboardC
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomBoardsTest {
    @Test
    fun wholeUnicodeStringsSurviveBothSidesAndSerialization() {
        val emoji = "👩🏽‍💻"
        val kaomoji = "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧"
        val board = CustomBoard(
            sideA = listOf(BoardCell(mapOf("center" to BoardBinding(text = emoji)))),
            sideB = listOf(BoardCell(mapOf("center" to BoardBinding(text = kaomoji)))),
            returnAfterInput = true,
            haptics = false,
        )
        val restored = Json.decodeFromString<CustomBoard>(Json.encodeToString(board))
        assertEquals(board, restored)
        val definition = restored.definition()
        assertEquals(emoji, (definition.modes.main.arr[0][0].center.action as KeyAction.CommitText).text)
        assertEquals(kaomoji, (definition.modes.numeric.arr[0][0].center.action as KeyAction.CommitText).text)
        assertFalse(definition.settings.autoShift)
    }

    @Test
    fun pagingPreservesTapAndOtherSwipeAssignments() {
        val tap = KeyAction.ToggleNumericMode(true)
        val up = KeyC("copy")
        val key = KeyItemC(center = KeyC(tap), top = up)
        val original = KeyboardC(listOf(listOf(key, KeyItemC(KeyC("x")))))
        val paged = original.withBoardPaging()
        assertSame(tap, paged.arr[0][0].center.action)
        assertSame(up, paged.arr[0][0].top)
        assertSame(KeyAction.CycleBoard, paged.arr[0][0].right?.action)
        assertSame(original.arr[0][1], paged.arr[0][1])
    }

    @Test
    fun emptyBoardsAlwaysKeepNavigationAndTwoSides() {
        val modes = CustomBoard(sideA = emptyList(), sideB = emptyList()).definition().modes
        listOf(modes.main, modes.numeric).forEach { keyboard ->
            assertEquals(4, keyboard.arr.size)
            assertTrue(keyboard.arr.all { it.size == 3 })
            val navigation = keyboard.arr.last().first()
            assertSame(KeyAction.BoardHome, navigation.left?.action)
            assertSame(KeyAction.BoardHome, navigation.longPress)
            assertSame(KeyAction.CycleBoard, navigation.right?.action)
        }
        assertTrue((modes.main.arr.last().first().center.action as KeyAction.ToggleNumericMode).enable)
        assertFalse((modes.numeric.arr.last().first().center.action as KeyAction.ToggleNumericMode).enable)
    }

    @Test
    fun swipeAndHoldActionsAreIndependent() {
        val key = BoardCell(mapOf("center" to BoardBinding(text = "°"), "right" to BoardBinding(text = "→"), "hold" to BoardBinding("paste"))).key()
        assertEquals("°", (key.center.action as KeyAction.CommitText).text)
        assertEquals("→", (key.right?.action as KeyAction.CommitText).text)
        assertSame(KeyAction.Paste, key.longPress)
    }
}
