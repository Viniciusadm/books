package com.archieapps.books.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.ChapterHit
import com.archieapps.books.domain.CharacterHit
import com.archieapps.books.domain.EventHit
import com.archieapps.books.ui.booksViewModel
import com.archieapps.books.ui.components.BooksCard
import com.archieapps.books.ui.components.BooksTopBar
import com.archieapps.books.ui.components.CharacterAvatar
import com.archieapps.books.ui.components.ChapterBadge
import com.archieapps.books.ui.components.chapterShortLabel
import com.archieapps.books.ui.components.SectionHeader
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenChapter: (Long) -> Unit,
) {
    val viewModel = booksViewModel("search") { container -> SearchViewModel(container.repository) }
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val palette = LocalBooksPalette.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BooksTopBar(
                title = stringResource(R.string.action_search),
                compact = true,
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            SearchField(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClear = viewModel::clear,
                focusRequester = focusRequester,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )

            when {
                query.length < 2 -> Text(
                    text = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                results.isEmpty -> Text(
                    text = stringResource(R.string.search_no_results, query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (results.characters.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.search_section_characters))
                        }
                        items(results.characters, key = { "char-${it.characterId}" }) { hit ->
                            CharacterHitCard(hit = hit, onOpen = { onOpenCharacter(hit.characterId) })
                        }
                    }
                    if (results.chapters.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.search_section_chapters),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        items(results.chapters, key = { "chap-${it.chapterId}" }) { hit ->
                            ChapterHitCard(hit = hit, onOpen = { onOpenChapter(hit.chapterId) })
                        }
                    }
                    if (results.events.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.search_section_events),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        itemsIndexed(results.events) { index, hit ->
                            EventHitCard(hit = hit, onOpen = { onOpenChapter(hit.chapterId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBooksPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, palette.hairline, MaterialTheme.shapes.medium)
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(20.dp),
        )
        Box(modifier = Modifier.weight(1f).padding(start = 10.dp, top = 12.dp, bottom = 12.dp)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textSecondary,
                )
            }
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.action_clear),
                    tint = palette.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun CharacterHitCard(hit: CharacterHit, onOpen: () -> Unit) {
    val palette = LocalBooksPalette.current
    BooksCard(onClick = onOpen) {
        Row(modifier = Modifier.padding(14.dp)) {
            CharacterAvatar(name = hit.displayName, size = 38)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = hit.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.search_breadcrumb,
                        hit.bookTitle,
                        chapterShortLabel(hit.currentChapterNumber),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Text(
                    text = hit.currentSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ChapterHitCard(hit: ChapterHit, onOpen: () -> Unit) {
    val palette = LocalBooksPalette.current
    BooksCard(onClick = onOpen) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            ChapterBadge(number = hit.number)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = hit.title.ifBlank { stringResource(R.string.chapter_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = hit.bookTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun EventHitCard(hit: EventHit, onOpen: () -> Unit) {
    val palette = LocalBooksPalette.current
    BooksCard(onClick = onOpen) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(
                    R.string.search_breadcrumb,
                    hit.bookTitle,
                    chapterShortLabel(hit.chapterNumber),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = palette.chapterInk,
            )
            Text(
                text = hit.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
