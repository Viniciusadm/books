package com.archieapps.books.ui.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.domain.ChapterCharacter
import com.archieapps.books.domain.ChapterEvent
import com.archieapps.books.domain.ChapterHeader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChapterViewModel(
    repository: LibraryRepository,
    chapterId: Long,
) : ViewModel() {

    private val sharing = SharingStarted.WhileSubscribed(5_000)

    val header: StateFlow<ChapterHeader?> = repository.observeChapterHeader(chapterId)
        .stateIn(viewModelScope, sharing, null)

    val characters: StateFlow<List<ChapterCharacter>?> =
        repository.observeChapterCharacters(chapterId)
            .stateIn(viewModelScope, sharing, null)

    val events: StateFlow<List<ChapterEvent>?> = repository.observeChapterEvents(chapterId)
        .stateIn(viewModelScope, sharing, null)
}
