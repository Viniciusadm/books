package com.archieapps.books.ui.character

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.archieapps.books.domain.CharacterHistoryEntry
import com.archieapps.books.domain.CharacterProfile
import com.archieapps.books.ui.booksViewModel
import com.archieapps.books.ui.components.BooksCard
import com.archieapps.books.ui.components.BooksTopBar
import com.archieapps.books.ui.components.ChapterBadge
import com.archieapps.books.ui.components.chapterShortLabel
import com.archieapps.books.ui.components.CharacterAvatar
import com.archieapps.books.ui.components.LevelChip
import com.archieapps.books.ui.components.LoadingState
import com.archieapps.books.ui.components.SectionHeader
import com.archieapps.books.ui.components.UpdatedPill
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun CharacterScreen(
    characterId: Long,
    onBack: () -> Unit,
) {
    val viewModel = booksViewModel("character-$characterId") { container ->
        CharacterViewModel(container.repository, characterId)
    }
    val profile by viewModel.profile.collectAsState()
    val history by viewModel.history.collectAsState()
    var expandedChapterId by remember(history) {
        mutableStateOf(history?.lastOrNull()?.chapterId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BooksTopBar(
                title = profile?.displayName.orEmpty(),
                subtitle = profile?.bookTitle,
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
            val current = profile
            if (current == null) {
                item { LoadingState() }
            } else {
                item { CharacterIdentity(profile = current) }
                item { CurrentViewBlock(profile = current) }
                item {
                    SectionHeader(
                        title = stringResource(R.string.character_history_section),
                        meta = history?.let {
                            pluralStringResource(R.plurals.chapter_count, it.size, it.size)
                        },
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            items(history.orEmpty(), key = { it.chapterId }) { entry ->
                HistoryCard(
                    entry = entry,
                    canonicalName = current?.displayName.orEmpty(),
                    expanded = expandedChapterId == entry.chapterId,
                    onToggle = {
                        expandedChapterId = entry.chapterId.takeIf { it != expandedChapterId }
                    },
                )
            }
        }
    }
}

@Composable
private fun CharacterIdentity(profile: CharacterProfile) {
    val palette = LocalBooksPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        CharacterAvatar(name = profile.displayName, size = 60)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.appearance_count,
                    profile.appearanceCount,
                    profile.appearanceCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun CurrentViewBlock(profile: CharacterProfile) {
    val palette = LocalBooksPalette.current
    Surface(
        color = palette.currentWash,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            Row(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(palette.currentAccent),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 22.dp, top = 18.dp, end = 18.dp, bottom = 18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelChip(
                        text = stringResource(R.string.level_current),
                        wash = palette.currentAccent,
                        ink = MaterialTheme.colorScheme.surface,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (profile.changedInLatestChapter) {
                        UpdatedPill()
                    }
                }
                Text(
                    text = profile.currentSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.character_current_from_chapter,
                        chapterShortLabel(profile.currentChapterNumber),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: CharacterHistoryEntry,
    canonicalName: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val palette = LocalBooksPalette.current
    BooksCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
            ) {
                ChapterBadge(number = entry.chapterNumber)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = entry.chapterTitle.ifBlank {
                            stringResource(R.string.chapter_untitled)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (entry.nameAsWritten != canonicalName) {
                        Text(
                            text = stringResource(
                                R.string.character_appears_as,
                                entry.nameAsWritten,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = palette.textSecondary,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LevelChip(
                        text = stringResource(R.string.level_in_chapter),
                        wash = palette.chapterWash,
                        ink = palette.chapterInk,
                    )
                    Text(
                        text = entry.chapterDescription.ifBlank {
                            stringResource(R.string.character_no_description)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
