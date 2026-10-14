package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.archieapps.books.domain.BookCharacter
import com.archieapps.books.domain.CharacterProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters WHERE bookId = :bookId AND nameKey = :nameKey LIMIT 1")
    suspend fun findByNameKey(bookId: Long, nameKey: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun findById(characterId: Long): CharacterEntity?

    @Insert
    suspend fun insert(character: CharacterEntity): Long

    @Query(
        """
        UPDATE characters
        SET currentSummary = :currentSummary,
            currentChapterNumber = :currentChapterNumber,
            changedInLatestChapter = :changedInLatestChapter,
            appearanceCount = :appearanceCount,
            searchBlob = :searchBlob,
            updatedAt = :timestamp
        WHERE id = :characterId
        """,
    )
    suspend fun updateConsolidation(
        characterId: Long,
        currentSummary: String,
        currentChapterNumber: Int,
        changedInLatestChapter: Boolean,
        appearanceCount: Int,
        searchBlob: String,
        timestamp: Long,
    )

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun delete(characterId: Long)

    @Query(
        """
        SELECT id AS id, displayName AS displayName, currentSummary AS currentSummary,
            currentChapterNumber AS currentChapterNumber,
            changedInLatestChapter AS changedInLatestChapter,
            appearanceCount AS appearanceCount
        FROM characters
        WHERE bookId = :bookId
        ORDER BY nameKey ASC
        """,
    )
    fun observeBookCharacters(bookId: Long): Flow<List<BookCharacter>>

    @Query(
        """
        SELECT ch.id AS id, ch.bookId AS bookId, b.title AS bookTitle,
            ch.displayName AS displayName, ch.currentSummary AS currentSummary,
            ch.currentChapterNumber AS currentChapterNumber,
            ch.changedInLatestChapter AS changedInLatestChapter,
            ch.appearanceCount AS appearanceCount
        FROM characters ch
        JOIN books b ON b.id = ch.bookId
        WHERE ch.id = :characterId
        """,
    )
    fun observeProfile(characterId: Long): Flow<CharacterProfile?>
}
