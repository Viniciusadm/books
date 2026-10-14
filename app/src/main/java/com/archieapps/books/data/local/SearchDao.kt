package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Query
import com.archieapps.books.domain.ChapterHit
import com.archieapps.books.domain.CharacterHit
import com.archieapps.books.domain.EventHit

@Dao
interface SearchDao {

    @Query(
        """
        SELECT ch.id AS characterId, ch.bookId AS bookId, b.title AS bookTitle,
            ch.displayName AS displayName, ch.currentSummary AS currentSummary,
            ch.currentChapterNumber AS currentChapterNumber
        FROM characters ch
        JOIN books b ON b.id = ch.bookId
        WHERE ch.nameKey LIKE '%' || :term || '%' OR ch.searchBlob LIKE '%' || :term || '%'
        ORDER BY CASE WHEN ch.nameKey LIKE '%' || :term || '%' THEN 0 ELSE 1 END, ch.nameKey ASC
        LIMIT :limit
        """,
    )
    suspend fun searchCharacters(term: String, limit: Int): List<CharacterHit>

    @Query(
        """
        SELECT c.id AS chapterId, c.bookId AS bookId, b.title AS bookTitle,
            c.number AS number, c.title AS title
        FROM chapters c
        JOIN books b ON b.id = c.bookId
        WHERE c.searchBlob LIKE '%' || :term || '%'
        ORDER BY b.titleKey ASC, c.number ASC
        LIMIT :limit
        """,
    )
    suspend fun searchChapters(term: String, limit: Int): List<ChapterHit>

    @Query(
        """
        SELECT e.chapterId AS chapterId, c.bookId AS bookId, b.title AS bookTitle,
            c.number AS chapterNumber, c.title AS chapterTitle, e.text AS text
        FROM events e
        JOIN chapters c ON c.id = e.chapterId
        JOIN books b ON b.id = c.bookId
        WHERE e.searchBlob LIKE '%' || :term || '%'
        ORDER BY b.titleKey ASC, c.number ASC, e.orderIndex ASC
        LIMIT :limit
        """,
    )
    suspend fun searchEvents(term: String, limit: Int): List<EventHit>
}
