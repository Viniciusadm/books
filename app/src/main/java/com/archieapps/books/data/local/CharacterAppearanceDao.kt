package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.archieapps.books.domain.AppearanceSnapshot
import com.archieapps.books.domain.ChapterCharacter
import com.archieapps.books.domain.CharacterHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterAppearanceDao {

    @Insert
    suspend fun insert(appearance: CharacterAppearanceEntity)

    @Query("SELECT characterId FROM character_appearances WHERE chapterId = :chapterId")
    suspend fun characterIdsIn(chapterId: Long): List<Long>

    @Query("SELECT chapterId FROM character_appearances WHERE characterId = :characterId")
    suspend fun chapterIdsOf(characterId: Long): List<Long>

    @Query(
        """
        SELECT a.id AS id, c.number AS chapterNumber, c.importedAt AS chapterImportedAt,
            a.incomingSummary AS incomingSummary
        FROM character_appearances a
        JOIN chapters c ON c.id = a.chapterId
        WHERE a.characterId = :characterId
        """,
    )
    suspend fun snapshots(characterId: Long): List<AppearanceSnapshot>

    @Query(
        """
        SELECT a.characterId AS characterId, ch.displayName AS displayName,
            a.nameAsWritten AS nameAsWritten, a.chapterDescription AS chapterDescription,
            ch.changedInLatestChapter AS changedInLatestChapter,
            ch.currentChapterNumber AS currentChapterNumber
        FROM character_appearances a
        JOIN characters ch ON ch.id = a.characterId
        WHERE a.chapterId = :chapterId
        ORDER BY a.orderIndex ASC
        """,
    )
    fun observeChapterCharacters(chapterId: Long): Flow<List<ChapterCharacter>>

    @Query(
        """
        SELECT c.id AS chapterId, c.number AS chapterNumber, c.title AS chapterTitle,
            a.nameAsWritten AS nameAsWritten, a.chapterDescription AS chapterDescription,
            a.incomingSummary AS incomingSummary
        FROM character_appearances a
        JOIN chapters c ON c.id = a.chapterId
        WHERE a.characterId = :characterId
        ORDER BY c.number ASC
        """,
    )
    fun observeHistory(characterId: Long): Flow<List<CharacterHistoryEntry>>

    @Query("DELETE FROM character_appearances WHERE characterId = :characterId AND chapterId IN (:chapterIds)")
    suspend fun deleteFor(characterId: Long, chapterIds: List<Long>)

    @Query("UPDATE character_appearances SET characterId = :targetId WHERE characterId = :sourceId")
    suspend fun reassign(sourceId: Long, targetId: Long)
}
