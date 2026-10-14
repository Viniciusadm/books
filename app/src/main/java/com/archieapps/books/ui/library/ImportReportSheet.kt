package com.archieapps.books.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.domain.ImportOutcome
import com.archieapps.books.domain.ImportRejection
import com.archieapps.books.domain.ImportedFile
import com.archieapps.books.ui.components.BooksBottomSheet
import com.archieapps.books.ui.components.chapterLabel
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun ImportReportSheet(
    files: List<ImportedFile>,
    onDismiss: () -> Unit,
) {
    val palette = LocalBooksPalette.current
    BooksBottomSheet(onDismiss = onDismiss) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.import_report_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            files.forEach { file ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val rejected = file.outcome is ImportOutcome.Rejected
                    Icon(
                        imageVector = if (rejected) {
                            Icons.Rounded.Warning
                        } else {
                            Icons.Rounded.CheckCircle
                        },
                        contentDescription = null,
                        tint = if (rejected) MaterialTheme.colorScheme.error else palette.updated,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Top),
                    )
                    Column {
                        Text(
                            text = outcomeLabel(file.outcome),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = file.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun outcomeLabel(outcome: ImportOutcome): String = when (outcome) {
    is ImportOutcome.Stored -> stringResource(
        if (outcome.replacedExisting) R.string.import_replaced else R.string.import_stored,
        chapterLabel(outcome.chapterNumber),
        outcome.bookTitle,
    )

    is ImportOutcome.Rejected -> when (val rejection = outcome.rejection) {
        ImportRejection.Unreadable -> stringResource(R.string.import_failed_unreadable)
        ImportRejection.MalformedJson -> stringResource(R.string.import_failed_malformed)
        is ImportRejection.MissingField ->
            stringResource(R.string.import_failed_missing_field, rejection.field)
        is ImportRejection.InvalidChapterNumber ->
            stringResource(R.string.import_failed_chapter_number, rejection.value)
    }
}
