package com.archieapps.books.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterAliasDao {

    @Query("SELECT characterId FROM character_aliases WHERE bookId = :bookId AND nameKey = :nameKey LIMIT 1")
    suspend fun findCharacterId(bookId: Long, nameKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alias: CharacterAliasEntity)

    @Query("UPDATE character_aliases SET characterId = :targetId WHERE characterId = :sourceId")
    suspend fun reassign(sourceId: Long, targetId: Long)
}
