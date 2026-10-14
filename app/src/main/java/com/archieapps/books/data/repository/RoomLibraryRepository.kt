package com.archieapps.books.data.repository

import androidx.room.withTransaction
import com.archieapps.books.data.importer.ChapterImporter
import com.archieapps.books.data.importer.ChapterJsonParser
import com.archieapps.books.data.importer.ChapterParseResult
import com.archieapps.books.data.importer.CharacterRecalculator
import com.archieapps.books.data.local.BooksDatabase
import com.archieapps.books.data.local.CharacterAliasEntity
import com.archieapps.books.domain.ImportOutcome
import com.archieapps.books.domain.ImportRejection
import com.archieapps.books.domain.ImportRequest
import com.archieapps.books.domain.ImportedFile
import com.archieapps.books.domain.SearchResults
import com.archieapps.books.domain.TextNormalizer

private const val SEARCH_LIMIT = 40
private const val MIN_SEARCH_TERM_LENGTH = 2

class RoomLibraryRepository(
    private val database: BooksDatabase,
    private val parser: ChapterJsonParser,
    private val importer: ChapterImporter,
    private val recalculator: CharacterRecalculator,
    private val now: () -> Long,
) : LibraryRepository {

    override fun observeLibrary() = database.bookDao().observeLibrary()

    override fun observeBookTitle(bookId: Long) = database.bookDao().observeTitle(bookId)

    override fun observeChapters(bookId: Long) = database.chapterDao().observeChapters(bookId)

    override fun observeBookCharacters(bookId: Long) =
        database.characterDao().observeBookCharacters(bookId)

    override fun observeChapterHeader(chapterId: Long) =
        database.chapterDao().observeHeader(chapterId)

    override fun observeChapterCharacters(chapterId: Long) =
        database.characterAppearanceDao().observeChapterCharacters(chapterId)

    override fun observeChapterEvents(chapterId: Long) =
        database.eventDao().observeEvents(chapterId)

    override fun observeCharacterProfile(characterId: Long) =
        database.characterDao().observeProfile(characterId)

    override fun observeCharacterHistory(characterId: Long) =
        database.characterAppearanceDao().observeHistory(characterId)

    override suspend fun search(query: String): SearchResults {
        val term = TextNormalizer.normalize(query).filterNot { it == '%' || it == '_' }
        if (term.length < MIN_SEARCH_TERM_LENGTH) return SearchResults()
        val dao = database.searchDao()
        return SearchResults(
            characters = dao.searchCharacters(term, SEARCH_LIMIT),
            chapters = dao.searchChapters(term, SEARCH_LIMIT),
            events = dao.searchEvents(term, SEARCH_LIMIT),
        )
    }

    override suspend fun importFiles(requests: List<ImportRequest>): List<ImportedFile> =
        requests.map { request ->
            val content = request.content
                ?: return@map ImportedFile(
                    fileName = request.fileName,
                    outcome = ImportOutcome.Rejected(ImportRejection.Unreadable),
                )

            val outcome = when (val parsed = parser.parse(content)) {
                is ChapterParseResult.Success -> importer.store(parsed.chapter, request.fileName)
                ChapterParseResult.MalformedJson ->
                    ImportOutcome.Rejected(ImportRejection.MalformedJson)
                is ChapterParseResult.MissingField ->
                    ImportOutcome.Rejected(ImportRejection.MissingField(parsed.field))
                is ChapterParseResult.InvalidChapterNumber ->
                    ImportOutcome.Rejected(ImportRejection.InvalidChapterNumber(parsed.value))
            }
            ImportedFile(fileName = request.fileName, outcome = outcome)
        }

    override suspend fun deleteBook(bookId: Long) = database.bookDao().delete(bookId)

    override suspend fun deleteChapter(chapterId: Long) {
        database.withTransaction {
            val affected = database.characterAppearanceDao().characterIdsIn(chapterId)
            database.chapterDao().delete(chapterId)
            val timestamp = now()
            affected.forEach { recalculator.recalculate(it, timestamp) }
        }
    }

    override suspend fun mergeCharacters(targetId: Long, sourceId: Long) {
        if (targetId == sourceId) return
        database.withTransaction {
            val characterDao = database.characterDao()
            val appearanceDao = database.characterAppearanceDao()
            val target = characterDao.findById(targetId) ?: return@withTransaction
            val source = characterDao.findById(sourceId) ?: return@withTransaction
            if (target.bookId != source.bookId) return@withTransaction

            val targetChapterIds = appearanceDao.chapterIdsOf(targetId)
            if (targetChapterIds.isNotEmpty()) {
                appearanceDao.deleteFor(sourceId, targetChapterIds)
            }
            appearanceDao.reassign(sourceId = sourceId, targetId = targetId)

            val aliasDao = database.characterAliasDao()
            aliasDao.reassign(sourceId = sourceId, targetId = targetId)
            aliasDao.insert(
                CharacterAliasEntity(
                    bookId = target.bookId,
                    characterId = targetId,
                    nameKey = source.nameKey,
                ),
            )

            characterDao.delete(sourceId)
            recalculator.recalculate(targetId, now())
        }
    }
}
