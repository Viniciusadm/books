package com.archieapps.books.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.domain.BookCharacter
import com.archieapps.books.domain.ChapterSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(
    private val repository: LibraryRepository,
    private val bookId: Long,
) : ViewModel() {

    private val sharing = SharingStarted.WhileSubscribed(5_000)

    val title: StateFlow<String?> = repository.observeBookTitle(bookId)
        .stateIn(viewModelScope, sharing, null)

    val chapters: StateFlow<List<ChapterSummary>?> = repository.observeChapters(bookId)
        .stateIn(viewModelScope, sharing, null)

    val characters: StateFlow<List<BookCharacter>?> = repository.observeBookCharacters(bookId)
        .stateIn(viewModelScope, sharing, null)

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch { repository.deleteChapter(chapterId) }
    }

    fun mergeCharacters(targetId: Long, sourceId: Long) {
        viewModelScope.launch { repository.mergeCharacters(targetId, sourceId) }
    }
}
