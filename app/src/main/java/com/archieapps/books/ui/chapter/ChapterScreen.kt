package com.archieapps.books.ui.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.ChapterCharacter
import com.archieapps.books.domain.ChapterEvent
import com.archieapps.books.domain.ChapterHeader
import com.archieapps.books.ui.booksViewModel
import com.archieapps.books.ui.components.BooksCard
import com.archieapps.books.ui.components.BooksTopBar
import com.archieapps.books.ui.components.ChapterBadge
import com.archieapps.books.ui.components.chapterLabel
import com.archieapps.books.ui.components.CharacterAvatar
import com.archieapps.books.ui.components.LevelChip
import com.archieapps.books.ui.components.LoadingState
import com.archieapps.books.ui.components.SectionHeader
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun ChapterScreen(
    chapterId: Long,
    onBack: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
) {
    val viewModel = booksViewModel("chapter-$chapterId") { container ->
        ChapterViewModel(container.repository, chapterId)
    }
    val header by viewModel.header.collectAsState()
    val chapterCharacters by viewModel.characters.collectAsState()
    val chapterEvents by viewModel.events.collectAsState()
    val characters = chapterCharacters
    val events = chapterEvents
    val palette = LocalBooksPalette.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BooksTopBar(
                title = header?.let { chapterLabel(it.number) }.orEmpty(),
                subtitle = header?.bookTitle,
                compact = true,
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            header?.let { item { ChapterHeaderBlock(header = it) } }

            item {
                SectionHeader(
                    title = stringResource(R.string.chapter_characters_section),
                    meta = characters?.let {
                        pluralStringResource(R.plurals.character_count, it.size, it.size)
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            if (characters == null) {
                item { LoadingState() }
            } else if (characters.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.chapter_characters_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }

            itemsIndexed(
                characters.orEmpty(),
                key = { _, item -> "character-${item.characterId}" },
            ) { _, character ->
                ChapterCharacterCard(
                    character = character,
                    onOpen = { onOpenCharacter(character.characterId) },
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.chapter_events_section),
                    meta = events?.let {
                        pluralStringResource(R.plurals.event_count, it.size, it.size)
                    },
                    modifier = Modifier.padding(top = 20.dp),
                )
            }

            if (events == null) {
                item { LoadingState() }
            } else if (events.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.chapter_events_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }

            itemsIndexed(
                events.orEmpty(),
                key = { _, item -> "event-${item.id}" },
            ) { index, event ->
                EventTimelineRow(event = event, isLast = index == events.orEmpty().lastIndex)
            }
        }
    }
}

@Composable
private fun ChapterHeaderBlock(header: ChapterHeader) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ChapterBadge(number = header.number, large = true)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = header.title.ifBlank { stringResource(R.string.chapter_untitled) },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ChapterCharacterCard(character: ChapterCharacter, onOpen: () -> Unit) {
    val palette = LocalBooksPalette.current
    BooksCard(onClick = onOpen) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CharacterAvatar(name = character.displayName, size = 38)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = character.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (character.nameAsWritten != character.displayName) {
                        Text(
                            text = stringResource(
                                R.string.character_appears_as,
                                character.nameAsWritten,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.chapter_open_character),
                    tint = palette.textSecondary,
                )
            }
            LevelChip(
                text = stringResource(R.string.level_in_chapter),
                wash = palette.chapterWash,
                ink = palette.chapterInk,
            )
            Text(
                text = character.chapterDescription.ifBlank {
                    stringResource(R.string.character_no_description)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EventTimelineRow(event: ChapterEvent, isLast: Boolean) {
    val palette = LocalBooksPalette.current
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.width(22.dp).fillMaxHeight(),
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(palette.hairline),
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(palette.chapterAccent),
            )
        }
        Text(
            text = event.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp, bottom = 16.dp),
        )
    }
}
