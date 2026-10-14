package com.archieapps.books.data.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterJsonParserTest {

    private val parser = ChapterJsonParser()

    @Test
    fun `parses the documented format`() {
        val result = parser.parse(
            """
            {
              "book": "A Cidade das Marés",
              "chapter": 2,
              "chapter_title": "A primeira maré",
              "characters": [
                {
                  "name": "A Cartógrafa",
                  "chapter_description": "Cartógrafa itinerante.",
                  "current_summary": "Cartógrafa a caminho de Porto Salgado."
                }
              ],
              "events": ["Tempestade no canal.", "Segue viagem."]
            }
            """.trimIndent(),
        )

        val chapter = (result as ChapterParseResult.Success).chapter
        assertEquals("A Cidade das Marés", chapter.bookTitle)
        assertEquals(2, chapter.number)
        assertEquals("A primeira maré", chapter.title)
        assertEquals(1, chapter.characters.size)
        assertEquals("A Cartógrafa", chapter.characters.single().name)
        assertEquals("Cartógrafa itinerante.", chapter.characters.single().chapterDescription)
        assertEquals(
            "Cartógrafa a caminho de Porto Salgado.",
            chapter.characters.single().currentSummary,
        )
        assertEquals(listOf("Tempestade no canal.", "Segue viagem."), chapter.events)
    }

    @Test
    fun `falls back to the legacy description field`() {
        val result = parser.parse(
            """
            {
              "book": "A Cidade das Marés",
              "chapter": 1,
              "chapter_title": "O farol apagado",
              "characters": [{ "name": "Iara", "description": "Faroleira da ilha." }],
              "events": []
            }
            """.trimIndent(),
        )

        val character = (result as ChapterParseResult.Success).chapter.characters.single()
        assertEquals("Faroleira da ilha.", character.chapterDescription)
        assertEquals("Faroleira da ilha.", character.currentSummary)
    }

    @Test
    fun `falls back to the chapter description when no current summary is given`() {
        val result = parser.parse(
            """
            {
              "book": "Livro",
              "chapter": 1,
              "characters": [{ "name": "Alguem", "chapter_description": "So no capitulo." }]
            }
            """.trimIndent(),
        )

        val character = (result as ChapterParseResult.Success).chapter.characters.single()
        assertEquals("So no capitulo.", character.currentSummary)
    }

    @Test
    fun `ignores unknown fields and accepts a missing chapter title`() {
        val result = parser.parse(
            """
            { "book": "Livro", "chapter": 7, "narrator": "Iara", "characters": [], "events": [] }
            """.trimIndent(),
        )

        val chapter = (result as ChapterParseResult.Success).chapter
        assertEquals(7, chapter.number)
        assertEquals("", chapter.title)
    }

    @Test
    fun `reports malformed json`() {
        assertTrue(parser.parse("{ nao e json") is ChapterParseResult.MalformedJson)
    }

    @Test
    fun `reports a missing book title`() {
        val result = parser.parse("""{ "chapter": 1, "characters": [], "events": [] }""")

        assertEquals("book", (result as ChapterParseResult.MissingField).field)
    }

    @Test
    fun `reports a blank book title`() {
        val result = parser.parse("""{ "book": "   ", "chapter": 1 }""")

        assertEquals("book", (result as ChapterParseResult.MissingField).field)
    }

    @Test
    fun `reports a missing chapter number`() {
        val result = parser.parse("""{ "book": "Livro", "characters": [] }""")

        assertEquals("chapter", (result as ChapterParseResult.MissingField).field)
    }

    @Test
    fun `accepts chapter zero so a prologue can be imported`() {
        val result = parser.parse(
            """
            {
              "book": "A Cidade das Marés",
              "chapter": 0,
              "chapter_title": "Prologo",
              "characters": [{ "name": "Iara", "chapter_description": "a maré que não voltou" }],
              "events": ["A maré não voltou ao porto."]
            }
            """.trimIndent(),
        )

        val chapter = (result as ChapterParseResult.Success).chapter
        assertEquals(0, chapter.number)
        assertEquals("Prologo", chapter.title)
        assertEquals("Iara", chapter.characters.single().name)
    }

    @Test
    fun `reports a negative chapter number`() {
        val result = parser.parse("""{ "book": "Livro", "chapter": -1 }""")

        assertEquals(-1, (result as ChapterParseResult.InvalidChapterNumber).value)
    }

    @Test
    fun `reports a character without a name`() {
        val result = parser.parse(
            """
            {
              "book": "Livro",
              "chapter": 1,
              "characters": [{ "chapter_description": "sem nome" }]
            }
            """.trimIndent(),
        )

        assertEquals("characters[0].name", (result as ChapterParseResult.MissingField).field)
    }

    @Test
    fun `collapses characters repeated in the same file keeping the last occurrence`() {
        val result = parser.parse(
            """
            {
              "book": "Livro",
              "chapter": 1,
              "characters": [
                { "name": "Iara", "chapter_description": "primeira mencao" },
                { "name": "Tome", "chapter_description": "aprendiz" },
                { "name": "iara", "chapter_description": "mencao corrigida" }
              ]
            }
            """.trimIndent(),
        )

        val characters = (result as ChapterParseResult.Success).chapter.characters
        assertEquals(listOf("Iara", "Tome"), characters.map { it.name })
        assertEquals("mencao corrigida", characters.first().chapterDescription)
    }

    @Test
    fun `trims surrounding whitespace from text fields and drops blank events`() {
        val result = parser.parse(
            """
            {
              "book": "  Livro  ",
              "chapter": 1,
              "chapter_title": "  Titulo  ",
              "characters": [{ "name": "  Iara  ", "chapter_description": "  desc  " }],
              "events": ["  algo aconteceu  ", "   "]
            }
            """.trimIndent(),
        )

        val chapter = (result as ChapterParseResult.Success).chapter
        assertEquals("Livro", chapter.bookTitle)
        assertEquals("Titulo", chapter.title)
        assertEquals("Iara", chapter.characters.single().name)
        assertEquals("desc", chapter.characters.single().chapterDescription)
        assertEquals(listOf("algo aconteceu"), chapter.events)
    }
}
