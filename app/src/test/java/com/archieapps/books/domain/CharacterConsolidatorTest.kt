package com.archieapps.books.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterConsolidatorTest {

    private fun appearance(
        id: Long,
        chapterNumber: Int,
        importedAt: Long = 0L,
        summary: String = "resumo $chapterNumber",
    ) = AppearanceSnapshot(
        id = id,
        chapterNumber = chapterNumber,
        chapterImportedAt = importedAt,
        incomingSummary = summary,
    )

    @Test
    fun `no appearances yields no consolidation`() {
        assertNull(CharacterConsolidator.consolidate(emptyList()))
    }

    @Test
    fun `highest chapter number wins regardless of import order`() {
        val result = CharacterConsolidator.consolidate(
            listOf(
                appearance(id = 1, chapterNumber = 5, importedAt = 100, summary = "visao do cap 5"),
                appearance(id = 2, chapterNumber = 1, importedAt = 900, summary = "visao do cap 1"),
            ),
        )

        assertEquals("visao do cap 5", result?.currentSummary)
        assertEquals(5, result?.currentChapterNumber)
        assertEquals(2, result?.appearanceCount)
    }

    @Test
    fun `same chapter reimported resolves by most recent import`() {
        val result = CharacterConsolidator.consolidate(
            listOf(
                appearance(id = 1, chapterNumber = 3, importedAt = 100, summary = "antigo"),
                appearance(id = 2, chapterNumber = 3, importedAt = 400, summary = "corrigido"),
            ),
        )

        assertEquals("corrigido", result?.currentSummary)
    }

    @Test
    fun `flags change when previous chapter carried a different summary`() {
        val result = CharacterConsolidator.consolidate(
            listOf(
                appearance(id = 1, chapterNumber = 1, summary = "so estalajadeiro"),
                appearance(id = 2, chapterNumber = 4, summary = "estalajadeiro com passado"),
            ),
        )

        assertTrue(result!!.changedInLatestChapter)
    }

    @Test
    fun `does not flag change when the summary was carried over untouched`() {
        val result = CharacterConsolidator.consolidate(
            listOf(
                appearance(id = 1, chapterNumber = 1, summary = "mesma coisa"),
                appearance(id = 2, chapterNumber = 4, summary = "mesma coisa"),
            ),
        )

        assertFalse(result!!.changedInLatestChapter)
    }

    @Test
    fun `single appearance is never flagged as changed`() {
        val result = CharacterConsolidator.consolidate(listOf(appearance(id = 1, chapterNumber = 2)))

        assertFalse(result!!.changedInLatestChapter)
        assertEquals(1, result.appearanceCount)
    }
}
