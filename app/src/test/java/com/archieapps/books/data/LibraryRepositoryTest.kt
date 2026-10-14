package com.archieapps.books.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.archieapps.books.data.importer.ChapterImporter
import com.archieapps.books.data.importer.ChapterJsonParser
import com.archieapps.books.data.importer.CharacterRecalculator
import com.archieapps.books.data.local.BooksDatabase
import com.archieapps.books.data.repository.RoomLibraryRepository
import com.archieapps.books.domain.ImportOutcome
import com.archieapps.books.domain.ImportRejection
import com.archieapps.books.domain.ImportRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class LibraryRepositoryTest {

    private lateinit var database: BooksDatabase
    private lateinit var repository: RoomLibraryRepository
    private var clock = 1_000L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BooksDatabase::class.java,
        ).allowMainThreadQueries().build()

        val recalculator = CharacterRecalculator(
            characterDao = database.characterDao(),
            appearanceDao = database.characterAppearanceDao(),
        )
        repository = RoomLibraryRepository(
            database = database,
            parser = ChapterJsonParser(),
            importer = ChapterImporter(database, recalculator) { clock },
            recalculator = recalculator,
            now = { clock },
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun importStoresChapterCharactersAndEvents() = runTest {
        import(chapterJson(number = 1, title = "O farol apagado"))

        val books = repository.observeLibrary().first()
        assertEquals(1, books.size)
        assertEquals("A Cidade das Marés", books.single().title)
        assertEquals(1, books.single().chapterCount)
        assertEquals(2, books.single().characterCount)

        val chapter = repository.observeChapters(books.single().id).first().single()
        assertEquals(1, chapter.number)
        assertEquals("O farol apagado", chapter.title)
        assertEquals(2, chapter.characterCount)
        assertEquals(2, chapter.eventCount)

        val events = repository.observeChapterEvents(chapter.id).first()
        assertEquals(listOf("Dona Lua conta a historia.", "Benicio chega ferido."), events.map { it.text })
    }

    @Test
    fun reimportReplacesChapterContentAndDropsOrphanCharacters() = runTest {
        import(chapterJson(number = 1))
        clock += 1_000

        val outcome = import(
            chapterJson(
                number = 1,
                characters = listOf(Character("Iara", "revisado", "revisado")),
                events = listOf("Somente um acontecimento."),
            ),
        )

        assertTrue((outcome as ImportOutcome.Stored).replacedExisting)
        val book = repository.observeLibrary().first().single()
        assertEquals(1, book.chapterCount)
        assertEquals(1, book.characterCount)

        val chapter = repository.observeChapters(book.id).first().single()
        assertEquals(1, repository.observeChapterEvents(chapter.id).first().size)
        assertEquals("revisado", repository.observeBookCharacters(book.id).first().single().currentSummary)
    }

    @Test
    fun currentSummaryComesFromTheHighestChapterRegardlessOfImportOrder() = runTest {
        import(
            chapterJson(
                number = 3,
                characters = listOf(Character("Iara", "no cap 3", "visao do cap 3")),
            ),
        )
        clock += 1_000
        import(
            chapterJson(
                number = 1,
                characters = listOf(Character("Iara", "no cap 1", "visao do cap 1")),
            ),
        )

        val book = repository.observeLibrary().first().single()
        val character = repository.observeBookCharacters(book.id).first().single()
        assertEquals("visao do cap 3", character.currentSummary)
        assertEquals(3, character.currentChapterNumber)
        assertEquals(2, character.appearanceCount)
        assertTrue(character.changedInLatestChapter)

        val history = repository.observeCharacterHistory(character.id).first()
        assertEquals(listOf(1, 3), history.map { it.chapterNumber })
        assertEquals("no cap 1", history.first().chapterDescription)
    }

    @Test
    fun deletingAChapterRecalculatesTheCurrentView() = runTest {
        import(
            chapterJson(
                number = 1,
                characters = listOf(Character("Iara", "no cap 1", "visao do cap 1")),
            ),
        )
        clock += 1_000
        import(
            chapterJson(
                number = 2,
                characters = listOf(Character("Iara", "no cap 2", "visao do cap 2")),
            ),
        )

        val book = repository.observeLibrary().first().single()
        val secondChapter = repository.observeChapters(book.id).first().last()
        repository.deleteChapter(secondChapter.id)

        val character = repository.observeBookCharacters(book.id).first().single()
        assertEquals("visao do cap 1", character.currentSummary)
        assertEquals(1, character.currentChapterNumber)
        assertEquals(1, character.appearanceCount)
        assertFalse(character.changedInLatestChapter)
    }

    @Test
    fun deletingAChapterRemovesCharactersThatOnlyAppearedThere() = runTest {
        import(chapterJson(number = 1))

        val book = repository.observeLibrary().first().single()
        val chapter = repository.observeChapters(book.id).first().single()
        repository.deleteChapter(chapter.id)

        assertEquals(emptyList<Long>(), repository.observeBookCharacters(book.id).first().map { it.id })
    }

    @Test
    fun mergingCharactersMovesHistoryAndRegistersTheAlias() = runTest {
        import(
            chapterJson(
                number = 1,
                characters = listOf(Character("Jake (Jacob Walker)", "amigo de infancia", "amigo")),
            ),
        )
        clock += 1_000
        import(
            chapterJson(
                number = 2,
                characters = listOf(Character("Jake", "segue no grupo", "amigo do grupo")),
            ),
        )

        val book = repository.observeLibrary().first().single()
        val before = repository.observeBookCharacters(book.id).first()
        assertEquals(2, before.size)

        val target = before.single { it.displayName == "Jake (Jacob Walker)" }
        val source = before.single { it.displayName == "Jake" }
        repository.mergeCharacters(targetId = target.id, sourceId = source.id)

        val merged = repository.observeBookCharacters(book.id).first().single()
        assertEquals("Jake (Jacob Walker)", merged.displayName)
        assertEquals(2, merged.appearanceCount)
        assertEquals("amigo do grupo", merged.currentSummary)

        clock += 1_000
        import(
            chapterJson(
                number = 3,
                characters = listOf(Character("Jake", "ainda no grupo", "amigo veterano")),
            ),
        )

        val afterAliasedImport = repository.observeBookCharacters(book.id).first().single()
        assertEquals(3, afterAliasedImport.appearanceCount)
        assertEquals("amigo veterano", afterAliasedImport.currentSummary)
    }

    @Test
    fun malformedFilesAreRejectedWithoutTouchingTheLibrary() = runTest {
        val outcome = repository.importFiles(
            listOf(ImportRequest(fileName = "quebrado.json", content = "{ nao e json")),
        ).single().outcome

        assertEquals(
            ImportOutcome.Rejected(ImportRejection.MalformedJson),
            outcome,
        )
        assertEquals(emptyList<Long>(), repository.observeLibrary().first().map { it.id })
    }

    @Test
    fun unreadableFilesAreReported() = runTest {
        val outcome = repository.importFiles(
            listOf(ImportRequest(fileName = "sumido.json", content = null)),
        ).single().outcome

        assertEquals(ImportOutcome.Rejected(ImportRejection.Unreadable), outcome)
    }

    private suspend fun import(json: String): ImportOutcome =
        repository.importFiles(listOf(ImportRequest(fileName = "capitulo.json", content = json)))
            .single()
            .outcome

    private data class Character(
        val name: String,
        val chapterDescription: String,
        val currentSummary: String,
    )

    private fun chapterJson(
        number: Int,
        book: String = "A Cidade das Marés",
        title: String = "Capitulo $number",
        characters: List<Character> = listOf(
            Character("Iara", "faroleira da ilha", "faroleira reservada"),
            Character("Tome", "aprendiz de Iara", "aprendiz dedicado"),
        ),
        events: List<String> = listOf("Dona Lua conta a historia.", "Benicio chega ferido."),
    ): String {
        val charactersJson = characters.joinToString(",") { character ->
            """
            {
              "name": "${character.name}",
              "chapter_description": "${character.chapterDescription}",
              "current_summary": "${character.currentSummary}"
            }
            """.trimIndent()
        }
        val eventsJson = events.joinToString(",") { "\"$it\"" }
        return """
        {
          "book": "$book",
          "chapter": $number,
          "chapter_title": "$title",
          "characters": [$charactersJson],
          "events": [$eventsJson]
        }
        """.trimIndent()
    }
}
