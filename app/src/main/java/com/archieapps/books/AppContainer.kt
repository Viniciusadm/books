package com.archieapps.books

import android.content.Context
import androidx.room.Room
import com.archieapps.books.data.importer.ChapterImporter
import com.archieapps.books.data.importer.ChapterJsonParser
import com.archieapps.books.data.importer.CharacterRecalculator
import com.archieapps.books.data.importer.DocumentContentReader
import com.archieapps.books.data.local.BooksDatabase
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.data.repository.RoomLibraryRepository

class AppContainer(context: Context) {

    private val clock: () -> Long = System::currentTimeMillis

    private val database = Room
        .databaseBuilder(context, BooksDatabase::class.java, "books.db")
        .build()

    private val recalculator = CharacterRecalculator(
        characterDao = database.characterDao(),
        appearanceDao = database.characterAppearanceDao(),
    )

    val repository: LibraryRepository = RoomLibraryRepository(
        database = database,
        parser = ChapterJsonParser(),
        importer = ChapterImporter(database, recalculator, clock),
        recalculator = recalculator,
        now = clock,
    )

    val contentReader = DocumentContentReader(context.contentResolver)
}
