package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.archieapps.books.domain.ChapterHeader
import com.archieapps.books.domain.ChapterSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    @Query(
        """
        SELECT c.id AS id, c.number AS number, c.title AS title,
            (SELECT COUNT(*) FROM character_appearances a WHERE a.chapterId = c.id) AS characterCount,
            (SELECT COUNT(*) FROM events e WHERE e.chapterId = c.id) AS eventCount
        FROM chapters c
        WHERE c.bookId = :bookId
        ORDER BY c.number ASC
        """,
    )
    fun observeChapters(bookId: Long): Flow<List<ChapterSummary>>

    @Query(
        """
        SELECT c.id AS id, c.bookId AS bookId, b.title AS bookTitle,
            c.number AS number, c.title AS title
        FROM chapters c
        JOIN books b ON b.id = c.bookId
        WHERE c.id = :chapterId
        """,
    )
    fun observeHeader(chapterId: Long): Flow<ChapterHeader?>

    @Query("SELECT id FROM chapters WHERE bookId = :bookId AND number = :number LIMIT 1")
    suspend fun findId(bookId: Long, number: Int): Long?

    @Query("SELECT bookId FROM chapters WHERE id = :chapterId")
    suspend fun bookIdOf(chapterId: Long): Long?

    @Insert
    suspend fun insert(chapter: ChapterEntity): Long

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun delete(chapterId: Long)
}
