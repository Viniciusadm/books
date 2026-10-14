package com.archieapps.books.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.BookCharacter
import com.archieapps.books.domain.ChapterSummary
import com.archieapps.books.ui.booksViewModel
import com.archieapps.books.ui.components.BooksCard
import com.archieapps.books.ui.components.BooksTopBar
import com.archieapps.books.ui.components.ChapterBadge
import com.archieapps.books.ui.components.CharacterAvatar
import com.archieapps.books.ui.components.ConfirmDialog
import com.archieapps.books.ui.components.EmptyState
import com.archieapps.books.ui.components.LoadingState
import com.archieapps.books.ui.components.UpdatedDot
import com.archieapps.books.ui.theme.LocalBooksPalette

private const val TAB_CHAPTERS = 0
private const val TAB_CHARACTERS = 1

@Composable
fun BookScreen(
    bookId: Long,
    onBack: () -> Unit,
    onOpenChapter: (Long) -> Unit,
    onOpenCharacter: (Long) -> Unit,
) {
    val viewModel = booksViewModel("book-$bookId") { container ->
        BookViewModel(container.repository, bookId)
    }
    val title by viewModel.title.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val characters by viewModel.characters.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(TAB_CHAPTERS) }
    var pendingChapterDeletion by remember { mutableStateOf<ChapterSummary?>(null) }
    var mergeSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BooksTopBar(
                title = title.orEmpty(),
                subtitle = chapters?.let {
                    pluralStringResource(R.plurals.chapter_count, it.size, it.size)
                },
                compact = true,
                onBack = onBack,
                actions = {
                    if (selectedTab == TAB_CHARACTERS && (characters?.size ?: 0) >= 2) {
                        TextButton(onClick = { mergeSheetOpen = true }) {
                            Text(text = stringResource(R.string.book_merge_characters))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            SegmentedTabs(
                labels = listOf(
                    stringResource(R.string.book_tab_chapters),
                    stringResource(R.string.book_tab_characters),
                ),
                selected = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            when (selectedTab) {
                TAB_CHAPTERS -> ChapterList(
                    chapters = chapters,
                    onOpen = onOpenChapter,
                    onDelete = { pendingChapterDeletion = it },
                )

                else -> CharacterList(
                    characters = characters,
                    onOpen = onOpenCharacter,
                )
            }
        }
    }

    pendingChapterDeletion?.let { chapter ->
        ConfirmDialog(
            title = stringResource(R.string.chapter_delete_title),
            message = stringResource(R.string.chapter_delete_message, chapter.number),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteChapter(chapter.id)
                pendingChapterDeletion = null
            },
            onDismiss = { pendingChapterDeletion = null },
        )
    }

    if (mergeSheetOpen) {
        MergeCharactersSheet(
            characters = characters.orEmpty(),
            onConfirm = viewModel::mergeCharacters,
            onDismiss = { mergeSheetOpen = false },
        )
    }
}

@Composable
private fun SegmentedTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBooksPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(palette.elevated)
            .padding(4.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Surface(
                color = if (isSelected) palette.currentWash else palette.elevated,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelect(index) },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) palette.currentAccent else palette.textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ChapterList(
    chapters: List<ChapterSummary>?,
    onOpen: (Long) -> Unit,
    onDelete: (ChapterSummary) -> Unit,
) {
    if (chapters == null) {
        LoadingState()
        return
    }
    if (chapters.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.book_chapters_empty),
            message = stringResource(R.string.library_empty_message),
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(chapters, key = { it.id }) { chapter ->
            ChapterRow(
                chapter = chapter,
                onOpen = { onOpen(chapter.id) },
                onDelete = { onDelete(chapter) },
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: ChapterSummary,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalBooksPalette.current
    var menuOpen by remember { mutableStateOf(false) }

    BooksCard(onClick = onOpen) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        ) {
            ChapterBadge(number = chapter.number)
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 8.dp),
            ) {
                Text(
                    text = chapter.title.ifBlank { stringResource(R.string.chapter_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOf(
                        pluralStringResource(
                            R.plurals.character_count,
                            chapter.characterCount,
                            chapter.characterCount,
                        ),
                        pluralStringResource(
                            R.plurals.event_count,
                            chapter.eventCount,
                            chapter.eventCount,
                        ),
                    ).joinToString(" · "),
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
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                            )
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
private fun CharacterList(
    characters: List<BookCharacter>?,
    onOpen: (Long) -> Unit,
) {
    if (characters == null) {
        LoadingState()
        return
    }
    if (characters.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.book_characters_empty),
            message = stringResource(R.string.library_empty_message),
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(characters, key = { it.id }) { character ->
            CharacterRow(character = character, onOpen = { onOpen(character.id) })
        }
    }
}

@Composable
private fun CharacterRow(character: BookCharacter, onOpen: () -> Unit) {
    val palette = LocalBooksPalette.current
    BooksCard(onClick = onOpen) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(14.dp),
        ) {
            CharacterAvatar(name = character.displayName)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = character.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (character.changedInLatestChapter) {
                        UpdatedDot(modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Text(
                    text = character.currentSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.appearance_count,
                        character.appearanceCount,
                        character.appearanceCount,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.chapterInk,
                )
            }
        }
    }
}
