package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ToolPagePreferencesTest {
    @Test fun defaultRotationPreservesTheExistingFourPages() {
        assertEquals(listOf(ToolbarPage.CHARACTERS, ToolbarPage.FORMATTING, ToolbarPage.KAOMOJI, ToolbarPage.QUICK_SETTINGS), ToolPageConfig().visiblePages())
    }

    @Test fun editedCollectionsAndOptionsPersistAndRestoreIndependently() {
        val c = ToolPageConfig(
            pages = listOf("SNIPPETS", "KAOMOJI", "CHARACTERS"), hiddenPages = setOf("FORMATTING"),
            formats = listOf("italic", "bold"), hiddenFormats = setOf("uppercase"),
            formatCategories = listOf("TYPE", "CASE", "SCRIPTS"), hiddenFormatCategories = setOf("CASE"),
            quickTools = listOf("FEEDBACK", "SUGGESTIONS"), hiddenQuickTools = setOf("POWER"),
            kaomoji = listOf(ManagedCollection("custom", "My birds", "🐦", listOf(ManagedEntry("(•ө•)♡", "bird", listOf("(•ө•)", "♡"), setOf("love"), false)), false)),
            snippets = listOf(ManagedCollection("art", "ASCII art", "▤", listOf(ManagedEntry("a\n b", "two lines", listOf("c"))))),
        )
        val context = RuntimeEnvironment.getApplication()
        ToolPagePreferences.save(context, c)
        assertEquals(c, ToolPagePreferences.load(context))
        val restored = c.copy(kaomoji = defaultKaomoji())
        ToolPagePreferences.save(context, restored)
        assertEquals(c.snippets, ToolPagePreferences.load(context).snippets)
        assertEquals(c.hiddenQuickTools, ToolPagePreferences.load(context).hiddenQuickTools)
        assertEquals(KaomojiLibrary.categories.size, restored.kaomoji.size)
    }

    @Test fun hiddenAndUnknownPagesCannotBreakCyclingOrRemoveCharacterInput() {
        val c = ToolPageConfig(pages = listOf("FUTURE", "KAOMOJI", "KAOMOJI", "CHARACTERS"), hiddenPages = ToolbarPage.entries.map { it.name }.toSet() - "KAOMOJI")
        assertEquals(listOf(ToolbarPage.KAOMOJI, ToolbarPage.CHARACTERS), c.visiblePages())
        assertEquals(ToolbarPage.CHARACTERS, c.next(ToolbarPage.KAOMOJI, 1))
        assertEquals(ToolbarPage.KAOMOJI, c.next(ToolbarPage.CHARACTERS, 1))
        assertEquals(ToolbarPage.KAOMOJI, c.next(ToolbarPage.SNIPPETS, 1))
        assertEquals(listOf(ToolbarPage.CHARACTERS), c.copy(hiddenPages = ToolbarPage.entries.map { it.name }.toSet()).visiblePages())
        assertEquals(ToolPageConfig(), ToolPagePreferences.decode("bad json"))
        assertEquals(ToolPageConfig(), ToolPagePreferences.decode(null))
    }

    @Test fun formattingAndQuickControlsFollowVisibilityAndOrder() {
        val c = ToolPageConfig(formats = listOf("italic", "bold"), hiddenFormats = setOf("bold"), hiddenFormatCategories = setOf("CASE"), quickTools = listOf("FEEDBACK", "POWER"), hiddenQuickTools = setOf("POWER"))
        assertEquals("italic", c.visibleFormats().first().id)
        assertFalse(c.visibleFormats().any { it.id == "bold" || it.category == FancyTextFormatter.Category.CASE })
        assertEquals(QuickTool.FEEDBACK, c.visibleQuickTools().first())
        assertFalse(QuickTool.POWER in c.visibleQuickTools())
        assertEquals(listOf("b", "a", "c"), listOf("a", "b", "c").moved(0, 1))
        assertTrue(emptyList<String>().moved(0, 1).isEmpty())
    }
}
