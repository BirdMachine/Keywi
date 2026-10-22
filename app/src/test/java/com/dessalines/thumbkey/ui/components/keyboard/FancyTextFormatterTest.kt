package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FancyTextFormatterTest {
    @Test
    fun categoriesCoverEveryAvailableStyle() {
        assertEquals(12, FancyTextFormatter.styles.size)
        assertEquals(
            12,
            FancyTextFormatter.styles
                .map { it.id }
                .distinct()
                .size,
        )
        assertTrue(FancyTextFormatter.styles.all { it.category in FancyTextFormatter.Category.entries })
    }

    @Test
    fun casingAndTitleCasePreserveWhitespace() {
        assertEquals("HELLO  WORLD\nAGAIN", FancyTextFormatter.transform("hello  world\nagain", "uppercase"))
        assertEquals("Hello  World\nAgain", FancyTextFormatter.transform("hello  world\nagain", "title"))
        assertEquals("aBc! dE", FancyTextFormatter.transform("AbC! DE", "alternating"))
    }

    @Test
    fun mathematicalStylesPreservePunctuationAndSupplementaryCodepoints() {
        assertEquals("𝗔𝗯!", FancyTextFormatter.transform("Ab!", "bold"))
        assertEquals("ⓐⓑ 3", FancyTextFormatter.transform("ab 3", "circled"))
        assertEquals("Ａｂ！", FancyTextFormatter.transform("Ab!", "fullwidth"))
        assertEquals("🙂", FancyTextFormatter.transform("🙂", "fraktur"))
    }

    @Test
    fun unknownStyleLeavesTextUnchanged() {
        assertEquals("Hello 123", FancyTextFormatter.transform("Hello 123", "unknown"))
    }
}
