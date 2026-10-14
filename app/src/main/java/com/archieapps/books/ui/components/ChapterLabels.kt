package com.archieapps.books.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.archieapps.books.R

@Composable
fun chapterLabel(number: Int): String = if (number == PROLOGUE_NUMBER) {
    stringResource(R.string.chapter_label_prologue)
} else {
    stringResource(R.string.chapter_label, number)
}

@Composable
fun chapterShortLabel(number: Int): String = if (number == PROLOGUE_NUMBER) {
    stringResource(R.string.chapter_short_prologue)
} else {
    stringResource(R.string.chapter_short, number)
}

@Composable
fun chapterBadgeLabel(number: Int): String = if (number == PROLOGUE_NUMBER) {
    stringResource(R.string.chapter_badge_prologue)
} else {
    number.toString()
}

private const val PROLOGUE_NUMBER = 0
