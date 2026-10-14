package com.archieapps.books.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archieapps.books.data.repository.LibraryRepository
import com.archieapps.books.domain.SearchResults
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEBOUNCE_MILLIS = 220L

class SearchViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val mutableQuery = MutableStateFlow("")
    val query: StateFlow<String> = mutableQuery.asStateFlow()

    private val mutableResults = MutableStateFlow(SearchResults())
    val results: StateFlow<SearchResults> = mutableResults.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        mutableQuery.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            mutableResults.value = repository.search(value)
        }
    }

    fun clear() {
        searchJob?.cancel()
        mutableQuery.value = ""
        mutableResults.value = SearchResults()
    }
}
