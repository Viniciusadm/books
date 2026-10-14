package com.archieapps.books.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.BookCharacter
import com.archieapps.books.ui.components.BooksBottomSheet
import com.archieapps.books.ui.components.CharacterAvatar
import com.archieapps.books.ui.components.ConfirmDialog
import com.archieapps.books.ui.components.Hairline
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun MergeCharactersSheet(
    characters: List<BookCharacter>,
    onConfirm: (targetId: Long, sourceId: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var target by remember { mutableStateOf<BookCharacter?>(null) }
    var source by remember { mutableStateOf<BookCharacter?>(null) }

    BooksBottomSheet(onDismiss = onDismiss) {
        val chosenTarget = target
        Text(
            text = if (chosenTarget == null) {
                stringResource(R.string.merge_step_target)
            } else {
                stringResource(R.string.merge_step_source, chosenTarget.displayName)
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        if (characters.size < 2) {
            Text(
                text = stringResource(R.string.merge_needs_two),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBooksPalette.current.textSecondary,
                modifier = Modifier.padding(24.dp),
            )
            return@BooksBottomSheet
        }

        val options = characters.filter { it.id != chosenTarget?.id }
        LazyColumn(modifier = Modifier.heightIn(max = 420.dp).padding(top = 12.dp)) {
            items(options, key = { it.id }) { character ->
                MergeOptionRow(
                    character = character,
                    onClick = {
                        if (chosenTarget == null) target = character else source = character
                    },
                )
                Hairline(modifier = Modifier.padding(start = 24.dp))
            }
        }
    }

    val chosenTarget = target
    val chosenSource = source
    if (chosenTarget != null && chosenSource != null) {
        ConfirmDialog(
            title = stringResource(R.string.merge_confirm_title),
            message = stringResource(
                R.string.merge_confirm_message,
                chosenSource.displayName,
                chosenTarget.displayName,
            ),
            confirmLabel = stringResource(R.string.action_merge),
            onConfirm = {
                onConfirm(chosenTarget.id, chosenSource.id)
                onDismiss()
            },
            onDismiss = { source = null },
        )
    }
}

@Composable
private fun MergeOptionRow(character: BookCharacter, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        CharacterAvatar(name = character.displayName, size = 36)
        Column {
            Text(
                text = character.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.appearance_count,
                    character.appearanceCount,
                    character.appearanceCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = LocalBooksPalette.current.textSecondary,
            )
        }
    }
}
