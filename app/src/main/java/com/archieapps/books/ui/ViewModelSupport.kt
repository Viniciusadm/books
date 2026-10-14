package com.archieapps.books.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.archieapps.books.AppContainer
import com.archieapps.books.BooksApplication

@Composable
inline fun <reified VM : ViewModel> booksViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as BooksApplication).container
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(container) } },
    )
}
