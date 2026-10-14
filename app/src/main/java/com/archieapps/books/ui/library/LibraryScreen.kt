package com.archieapps.books.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.BookSummary
import com.archieapps.books.ui.booksViewModel
import com.archieapps.books.ui.components.BooksCard
import com.archieapps.books.ui.components.BooksTopBar
import com.archieapps.books.ui.components.ConfirmDialog
import com.archieapps.books.ui.components.chapterShortLabel
import com.archieapps.books.ui.components.EmptyState
import com.archieapps.books.ui.components.LoadingState
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun LibraryScreen(
    onOpenBook: (Long) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val viewModel = booksViewModel("library") { container ->
        LibraryViewModel(container.repository, container.contentReader)
    }
    val books by viewModel.books.collectAsState()
    val importState by viewModel.importState.collectAsState()
    var pendingDeletion by remember { mutableStateOf<BookSummary?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = viewModel::importFiles,
    )
    val openPicker = { picker.launch(arrayOf("application/json", "text/plain", "*/*")) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BooksTopBar(
                title = stringResource(R.string.library_title),
                subtitle = librarySubtitle(books),
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.action_search),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openPicker() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                },
                text = { Text(text = stringResource(R.string.action_import)) },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (importState is ImportState.Running) {
                ImportProgressBanner()
            }
            val library = books
            if (library == null) {
                LoadingState()
            } else if (library.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                EmptyState(
                    title = stringResource(R.string.library_empty_title),
                    message = stringResource(R.string.library_empty_message),
                    actionLabel = stringResource(R.string.library_empty_action),
                    onAction = { openPicker() },
                )
                Spacer(modifier = Modifier.weight(2f))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(library, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onOpen = { onOpenBook(book.id) },
                            onDelete = { pendingDeletion = book },
                        )
                    }
                }
            }
        }
    }

    pendingDeletion?.let { book ->
        ConfirmDialog(
            title = stringResource(R.string.book_delete_title),
            message = stringResource(R.string.book_delete_message, book.title),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteBook(book.id)
                pendingDeletion = null
            },
            onDismiss = { pendingDeletion = null },
        )
    }

    (importState as? ImportState.Finished)?.let { finished ->
        ImportReportSheet(files = finished.files, onDismiss = viewModel::dismissImportReport)
    }
}

@Composable
private fun ImportProgressBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalBooksPalette.current.currentWash)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.import_in_progress),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun librarySubtitle(books: List<BookSummary>?): String {
    if (books == null) return ""
    if (books.isEmpty()) return stringResource(R.string.library_subtitle_empty)
    val chapters = books.sumOf { it.chapterCount }
    val characters = books.sumOf { it.characterCount }
    return listOf(
        pluralStringResource(R.plurals.chapter_count, chapters, chapters),
        pluralStringResource(R.plurals.character_count, characters, characters),
    ).joinToString(" · ")
}

@Composable
private fun BookCard(
    book: BookSummary,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalBooksPalette.current
    var menuOpen by remember { mutableStateOf(false) }

    BooksCard(onClick = onOpen, accent = palette.chapterAccent) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(start = 18.dp, end = 4.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = bookMeta(book),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                        tint = palette.textSecondary,
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.action_delete)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                        },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun bookMeta(book: BookSummary): String = buildList {
    add(pluralStringResource(R.plurals.chapter_count, book.chapterCount, book.chapterCount))
    add(pluralStringResource(R.plurals.character_count, book.characterCount, book.characterCount))
    book.lastChapterNumber?.let {
        add(stringResource(R.string.library_up_to_chapter, chapterShortLabel(it)))
    }
}.joinToString(" · ")
