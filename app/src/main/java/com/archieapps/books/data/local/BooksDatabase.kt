package com.archieapps.books.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        CharacterEntity::class,
        CharacterAliasEntity::class,
        CharacterAppearanceEntity::class,
        EventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun eventDao(): EventDao
    abstract fun characterDao(): CharacterDao
    abstract fun characterAliasDao(): CharacterAliasDao
    abstract fun characterAppearanceDao(): CharacterAppearanceDao
    abstract fun searchDao(): SearchDao
}
