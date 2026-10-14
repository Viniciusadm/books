package com.archieapps.books.data.importer

import androidx.room.withTransaction
import com.archieapps.books.data.local.BookEntity
import com.archieapps.books.data.local.BooksDatabase
import com.archieapps.books.data.local.ChapterEntity
import com.archieapps.books.data.local.CharacterAppearanceEntity
import com.archieapps.books.data.local.CharacterEntity
import com.archieapps.books.data.local.EventEntity
import com.archieapps.books.domain.ImportOutcome
import com.archieapps.books.domain.TextNormalizer

class ChapterImporter(
    private val database: BooksDatabase,
    private val recalculator: CharacterRecalculator,
    private val now: () -> Long,
) {

    suspend fun store(parsed: ParsedChapter, sourceName: String): ImportOutcome.Stored =
        database.withTransaction {
            val timestamp = now()
            val bookId = resolveBook(parsed.bookTitle, timestamp)
            val affectedCharacterIds = mutableSetOf<Long>()

            val previousChapterId = database.chapterDao().findId(bookId, parsed.number)
            if (previousChapterId != null) {
                affectedCharacterIds += database.characterAppearanceDao()
                    .characterIdsIn(previousChapterId)
                database.chapterDao().delete(previousChapterId)
            }

            val chapterId = database.chapterDao().insert(
                ChapterEntity(
                    bookId = bookId,
                    number = parsed.number,
                    title = parsed.title,
                    sourceName = sourceName,
                    importedAt = timestamp,
                    searchBlob = TextNormalizer.normalize(parsed.title),
                ),
            )

            database.eventDao().insertAll(
                parsed.events.mapIndexed { index, text ->
                    EventEntity(
                        chapterId = chapterId,
                        orderIndex = index,
                        text = text,
                        searchBlob = TextNormalizer.normalize(text),
                    )
                },
            )

            parsed.characters.forEachIndexed { index, character ->
                val characterId = resolveCharacter(bookId, character, parsed.number, timestamp)
                database.characterAppearanceDao().insert(
                    CharacterAppearanceEntity(
                        chapterId = chapterId,
                        characterId = characterId,
                        nameAsWritten = character.name,
                        chapterDescription = character.chapterDescription,
                        incomingSummary = character.currentSummary,
                        orderIndex = index,
                        searchBlob = TextNormalizer.normalize(character.chapterDescription),
                    ),
                )
                affectedCharacterIds += characterId
            }

            affectedCharacterIds.forEach { recalculator.recalculate(it, timestamp) }

            ImportOutcome.Stored(
                bookTitle = parsed.bookTitle,
                chapterNumber = parsed.number,
                chapterTitle = parsed.title,
                replacedExisting = previousChapterId != null,
            )
        }

    private suspend fun resolveBook(title: String, timestamp: Long): Long {
        val titleKey = TextNormalizer.normalize(title)
        val existing = database.bookDao().findByTitleKey(titleKey)
        if (existing != null) {
            database.bookDao().touch(existing.id, timestamp)
            return existing.id
        }
        return database.bookDao().insert(
            BookEntity(
                title = title,
                titleKey = titleKey,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
    }

    private suspend fun resolveCharacter(
        bookId: Long,
        character: ParsedCharacter,
        chapterNumber: Int,
        timestamp: Long,
    ): Long {
        val nameKey = TextNormalizer.normalize(character.name)
        val known = database.characterDao().findByNameKey(bookId, nameKey)?.id
            ?: database.characterAliasDao().findCharacterId(bookId, nameKey)
        if (known != null) return known

        return database.characterDao().insert(
            CharacterEntity(
                bookId = bookId,
                displayName = character.name,
                nameKey = nameKey,
                currentSummary = character.currentSummary,
                currentChapterNumber = chapterNumber,
                changedInLatestChapter = false,
                appearanceCount = 0,
                updatedAt = timestamp,
                searchBlob = TextNormalizer.normalize(character.currentSummary),
            ),
        )
    }
}
