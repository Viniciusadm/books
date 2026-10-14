package com.archieapps.books.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.domain.CharacterHistoryEntry
import com.archieapps.books.domain.CharacterProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CharacterViewModel(
    repository: LibraryRepository,
    characterId: Long,
) : ViewModel() {

    private val sharing = SharingStarted.WhileSubscribed(5_000)

    val profile: StateFlow<CharacterProfile?> = repository.observeCharacterProfile(characterId)
        .stateIn(viewModelScope, sharing, null)

    val history: StateFlow<List<CharacterHistoryEntry>?> =
        repository.observeCharacterHistory(characterId)
            .stateIn(viewModelScope, sharing, null)
}
