package com.archieapps.books.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archieapps.books.data.importer.DocumentContentReader
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.domain.BookSummary
import com.archieapps.books.domain.ImportedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ImportState {
    data object Idle : ImportState
    data object Running : ImportState
    data class Finished(val files: List<ImportedFile>) : ImportState
}

class LibraryViewModel(
    private val repository: LibraryRepository,
    private val contentReader: DocumentContentReader,
) : ViewModel() {

    val books: StateFlow<List<BookSummary>?> = repository.observeLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val mutableImportState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = mutableImportState.asStateFlow()

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            mutableImportState.value = ImportState.Running
            val requests = contentReader.read(uris)
            mutableImportState.value = ImportState.Finished(repository.importFiles(requests))
        }
    }

    fun dismissImportReport() {
        mutableImportState.value = ImportState.Idle
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch { repository.deleteBook(bookId) }
    }
}
