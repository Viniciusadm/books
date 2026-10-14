package com.archieapps.books.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun `strips diacritics and lowercases`() {
        assertEquals("a cartografa", TextNormalizer.normalize("A Cartógrafa"))
        assertEquals("dona lua", TextNormalizer.normalize("Dona Lúa"))
        assertEquals("acao e coracao", TextNormalizer.normalize("Ação e Coração"))
    }

    @Test
    fun `collapses surrounding and repeated whitespace`() {
        assertEquals("tome", TextNormalizer.normalize("   Tome  "))
        assertEquals("jake walker", TextNormalizer.normalize("Jake\t\n  Walker"))
    }

    @Test
    fun `keeps parenthesised parts so distinct names stay distinct`() {
        assertEquals("jake (jacob walker)", TextNormalizer.normalize("Jake (Jacob Walker)"))
    }

    @Test
    fun `blank input normalizes to empty string`() {
        assertEquals("", TextNormalizer.normalize("   \n "))
    }
}
