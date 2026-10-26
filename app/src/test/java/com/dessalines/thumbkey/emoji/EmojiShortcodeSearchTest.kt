package com.dessalines.thumbkey.emoji

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiShortcodeSearchTest {
    @Test fun prefixSuggestsFish() {
        val result = EmojiShortcodeSearch.suggest("hello :fi")
        assertEquals(":fi", result?.trigger)
        assertTrue(result!!.matches.any { it.alias == "fish" && it.emoji == "🐟" })
    }

    @Test fun punctuationAndUrlsDoNotTrigger() {
        assertNull(EmojiShortcodeSearch.suggest("https://example.com"))
        assertNull(EmojiShortcodeSearch.suggest("time:fi"))
        assertNull(EmojiShortcodeSearch.suggest("word :fi!"))
    }

    @Test fun emptyAndTooLongTriggersAreIgnored() {
        assertNull(EmojiShortcodeSearch.suggest(":"))
        assertNull(EmojiShortcodeSearch.suggest(":" + "a".repeat(36)))
    }
}
