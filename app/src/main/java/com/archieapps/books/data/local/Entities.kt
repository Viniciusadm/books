package com.archieapps.books.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [Index(value = ["titleKey"], unique = true)],
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleKey: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bookId", "number"], unique = true)],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val number: Int,
    val title: String,
    val sourceName: String,
    val importedAt: Long,
    val searchBlob: String,
)

@Entity(
    tableName = "characters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["bookId", "nameKey"], unique = true)],
)
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val displayName: String,
    val nameKey: String,
    val currentSummary: String,
    val currentChapterNumber: Int,
    val changedInLatestChapter: Boolean,
    val appearanceCount: Int,
    val updatedAt: Long,
    val searchBlob: String,
)

@Entity(
    tableName = "character_aliases",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["bookId", "nameKey"], unique = true),
        Index(value = ["characterId"]),
    ],
)
data class CharacterAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val characterId: Long,
    val nameKey: String,
)

@Entity(
    tableName = "character_appearances",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["chapterId", "characterId"], unique = true),
        Index(value = ["characterId"]),
    ],
)
data class CharacterAppearanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val characterId: Long,
    val nameAsWritten: String,
    val chapterDescription: String,
    val incomingSummary: String,
    val orderIndex: Int,
    val searchBlob: String,
)

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["chapterId"])],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val orderIndex: Int,
    val text: String,
    val searchBlob: String,
)
