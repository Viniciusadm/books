package com.archieapps.books.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archieapps.books.R
import com.archieapps.books.ui.theme.LocalBooksPalette

@Composable
fun BooksTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    compact: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    val palette = LocalBooksPalette.current
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .padding(start = if (onBack == null) 20.dp else 4.dp, end = 8.dp)
                    .padding(vertical = if (compact) 8.dp else 14.dp),
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = if (compact) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.headlineLarge
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                actions()
            }
            Hairline()
        }
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalBooksPalette.current.hairline),
    )
}

@Composable
fun LevelChip(
    text: String,
    wash: Color,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = ink,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(wash)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun ChapterBadge(
    number: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val palette = LocalBooksPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(if (large) 52.dp else 40.dp)
            .clip(RoundedCornerShape(if (large) 16.dp else 12.dp))
            .background(palette.chapterWash),
    ) {
        Text(
            text = chapterBadgeLabel(number),
            style = if (large) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = palette.chapterInk,
        )
    }
}

@Composable
fun CharacterAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 44,
) {
    val palette = LocalBooksPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(palette.chapterWash),
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = palette.chapterInk,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (meta != null) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalBooksPalette.current.textSecondary,
                )
            }
        }
        trailing()
    }
}

@Composable
fun BooksCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,
    content: @Composable () -> Unit,
) {
    val palette = LocalBooksPalette.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, palette.hairline, MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Box {
            if (accent != null) {
                Row(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent, accent.copy(alpha = 0.45f)),
                                ),
                            ),
                    )
                }
            }
            Box(modifier = Modifier.padding(start = if (accent != null) 4.dp else 0.dp)) {
                content()
            }
        }
    }
}

@Composable
fun UpdatedPill(modifier: Modifier = Modifier) {
    val palette = LocalBooksPalette.current
    LevelChip(
        text = stringResource(R.string.character_updated),
        wash = palette.updatedWash,
        ink = palette.updatedInk,
        modifier = modifier,
    )
}

@Composable
fun UpdatedDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(LocalBooksPalette.current.updated),
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBooksPalette.current.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAction) {
                Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    ) {
        CircularProgressIndicator(
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}
