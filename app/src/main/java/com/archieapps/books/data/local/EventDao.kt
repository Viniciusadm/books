package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.archieapps.books.domain.ChapterEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert
    suspend fun insertAll(events: List<EventEntity>)

    @Query(
        """
        SELECT id AS id, text AS text
        FROM events
        WHERE chapterId = :chapterId
        ORDER BY orderIndex ASC
        """,
    )
    fun observeEvents(chapterId: Long): Flow<List<ChapterEvent>>
}
