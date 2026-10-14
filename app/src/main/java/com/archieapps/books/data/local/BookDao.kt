package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.archieapps.books.domain.BookSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query(
        """
        SELECT b.id AS id, b.title AS title,
            (SELECT COUNT(*) FROM chapters c WHERE c.bookId = b.id) AS chapterCount,
            (SELECT COUNT(*) FROM characters ch WHERE ch.bookId = b.id) AS characterCount,
            (SELECT MAX(c.number) FROM chapters c WHERE c.bookId = b.id) AS lastChapterNumber
        FROM books b
        ORDER BY b.titleKey ASC
        """,
    )
    fun observeLibrary(): Flow<List<BookSummary>>

    @Query("SELECT * FROM books WHERE titleKey = :titleKey LIMIT 1")
    suspend fun findByTitleKey(titleKey: String): BookEntity?

    @Insert
    suspend fun insert(book: BookEntity): Long

    @Query("UPDATE books SET updatedAt = :timestamp WHERE id = :bookId")
    suspend fun touch(bookId: Long, timestamp: Long)

    @Query("SELECT title FROM books WHERE id = :bookId")
    fun observeTitle(bookId: Long): Flow<String?>

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun delete(bookId: Long)
}
