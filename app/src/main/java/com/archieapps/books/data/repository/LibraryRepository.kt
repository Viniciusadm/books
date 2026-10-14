package com.archieapps.books.data.repository

import com.archieapps.books.domain.BookCharacter
import com.archieapps.books.domain.BookSummary
import com.archieapps.books.domain.ChapterCharacter
import com.archieapps.books.domain.ChapterEvent
import com.archieapps.books.domain.ChapterHeader
import com.archieapps.books.domain.ChapterSummary
import com.archieapps.books.domain.CharacterHistoryEntry
import com.archieapps.books.domain.CharacterProfile
import com.archieapps.books.domain.ImportRequest
import com.archieapps.books.domain.ImportedFile
import com.archieapps.books.domain.SearchResults
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    fun observeLibrary(): Flow<List<BookSummary>>

    fun observeBookTitle(bookId: Long): Flow<String?>

    fun observeChapters(bookId: Long): Flow<List<ChapterSummary>>

    fun observeBookCharacters(bookId: Long): Flow<List<BookCharacter>>

    fun observeChapterHeader(chapterId: Long): Flow<ChapterHeader?>

    fun observeChapterCharacters(chapterId: Long): Flow<List<ChapterCharacter>>

    fun observeChapterEvents(chapterId: Long): Flow<List<ChapterEvent>>

    fun observeCharacterProfile(characterId: Long): Flow<CharacterProfile?>

    fun observeCharacterHistory(characterId: Long): Flow<List<CharacterHistoryEntry>>

    suspend fun search(query: String): SearchResults

    suspend fun importFiles(requests: List<ImportRequest>): List<ImportedFile>

    suspend fun deleteBook(bookId: Long)

    suspend fun deleteChapter(chapterId: Long)

    suspend fun mergeCharacters(targetId: Long, sourceId: Long)
}
