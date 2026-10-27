package com.dessalines.thumbkey.unicode

import java.io.File
import java.util.concurrent.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnicodeCatalogTest {
    companion object {
        private val catalog by lazy {
            val file = listOf(
                File("src/main/assets/unicode/catalog.tsv.gz"),
                File("app/src/main/assets/unicode/catalog.tsv.gz"),
            ).first { it.exists() }
            file.inputStream().use { UnicodeCatalog.readGzip(it) }
        }
    }

    @Test fun completePinnedCatalogIncludesAlgorithmicAndSupplementaryNames() {
        assertEquals("18.0.0", catalog.version)
        assertEquals(172873, catalog.entries.size)
        assertEquals("CJK UNIFIED IDEOGRAPH-4E00", catalog.search("U+4E00").single().name)
        assertEquals("HANGUL SYLLABLE GA", catalog.search("U+AC00").single().name)
        assertEquals("TANGUT IDEOGRAPH-17000", catalog.search("U+17000").single().name)
        assertEquals("😀", catalog.search("U+1F600").single().text)
    }

    @Test fun aliasesLiteralAndCategorySearchWorkOffline() {
        assertEquals(0x000A, catalog.search("LINE FEED").first().codePoint)
        assertTrue(catalog.search("😀").any { it.codePoint == 0x1F600 })
        assertTrue(catalog.search("benzene").any { it.codePoint == 0x232C })
        assertTrue(catalog.search("", "symbols").all { it.category.startsWith("S") })
        assertTrue(catalog.search("", block = "Greek and Coptic").all { it.block == "Greek and Coptic" })
        assertTrue(catalog.search("combining acute").any { it.codePoint == 0x0301 })
    }

    @Test fun pagesAreBoundedAndExcludedCodepointsNeverLeak() {
        val page = catalog.search("", limit = 40)
        assertEquals(40, page.size)
        assertEquals(catalog.entries[40], catalog.search("", limit = 40, offset = 40).first())
        assertFalse(catalog.entries.any { it.category in listOf("Co", "Cs", "Cn") })
        assertTrue(catalog.search("U+D800").isEmpty())
        assertTrue(catalog.search("U+E000").isEmpty())
    }

    @Test fun controlsMarksAndSpacesKeepHonestDisplayMetadata() {
        assertTrue(catalog.search("U+0000").single().isInvisible)
        assertTrue(catalog.search("U+0020").single().isInvisible)
        assertTrue(catalog.search("U+0301").single().isCombining)
        assertEquals("U+1F600", catalog.search("U+1F600").single().codePointLabel)
    }

    @Test(expected = CancellationException::class)
    fun staleSearchCanCancelWhileScanningTheFullCatalog() {
        var checkpoints = 0
        catalog.search("a deliberately absent search term", ensureActive = {
            if (++checkpoints == 2) throw CancellationException("Superseded query")
        })
    }

}
