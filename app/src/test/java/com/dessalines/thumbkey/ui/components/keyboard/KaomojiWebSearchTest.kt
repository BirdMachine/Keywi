package com.dessalines.thumbkey.ui.components.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KaomojiWebSearchTest {
    private val source = KaomojiSearchPreferences.emojiCombos

    @Test fun searchTermsAreEncodedAsOneUrlComponent() {
        assertEquals("https://emojicombos.com/cat%20%26%20dog%2F%3F", source.searchUrl("cat & dog/?"))
        assertEquals("https://example.com/?q=%E2%99%A5", source.copy(url = "https://example.com/?q=%s").searchUrl("♥"))
    }

    @Test fun extractsOnlyResultTextWithEntitiesAndLineBreaksAndDeduplicates() {
        val html = """
            <div class="emojis">ignore outside list</div>
            <div id="comboListEl">
              <div class="emojis">(&gt;ᴗ&lt;)</div>
              <div class="emojis">(&gt;ᴗ&lt;)</div>
              <div class="emojis">a<br>b</div>
              <script>bad()</script><div class="ad">ignore ad</div>
            </div>
        """.trimIndent()
        val result = KaomojiWebSearch.parse(html, source)
        assertEquals(listOf("(>ᴗ<)", "a\nb"), result.map { it.text })
        assertTrue(result.all { it.label == "Emoji Combos" })
    }

    @Test fun customSelectorsAndResultLimitsWork() {
        val html = (0..150).joinToString("") { "<span class='face'>$it</span>" }
        assertEquals(100, KaomojiWebSearch.parse(html, source.copy(selector = ".face")).size)
        assertTrue(KaomojiWebSearch.parse("<div class='face'>${"x".repeat(513)}</div>", source.copy(selector = ".face")).isEmpty())
        assertTrue(KaomojiWebSearch.parse("<div>No matches</div>", source).isEmpty())
    }

    @Test fun rejectsMalformedSourcesAndNonHttpsDestinations() {
        listOf("http://example.com/%s", "https://user:password@example.com/%s", "https://example.com/", "file:///%s")
            .forEach { url -> assertTrue(url, runCatching { source.copy(url = url).validate() }.isFailure) }
        assertTrue(runCatching { source.copy(selector = "[").validate() }.isFailure)
        source.copy(selector = "").validate()
    }
}
