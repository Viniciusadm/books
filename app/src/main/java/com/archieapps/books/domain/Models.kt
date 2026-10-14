package com.archieapps.books.domain

data class BookSummary(
    val id: Long,
    val title: String,
    val chapterCount: Int,
    val characterCount: Int,
    val lastChapterNumber: Int?,
)

data class ChapterSummary(
    val id: Long,
    val number: Int,
    val title: String,
    val characterCount: Int,
    val eventCount: Int,
)

data class ChapterHeader(
    val id: Long,
    val bookId: Long,
    val bookTitle: String,
    val number: Int,
    val title: String,
)

data class ChapterCharacter(
    val characterId: Long,
    val displayName: String,
    val nameAsWritten: String,
    val chapterDescription: String,
    val changedInLatestChapter: Boolean,
    val currentChapterNumber: Int,
)

data class ChapterEvent(
    val id: Long,
    val text: String,
)

data class BookCharacter(
    val id: Long,
    val displayName: String,
    val currentSummary: String,
    val currentChapterNumber: Int,
    val changedInLatestChapter: Boolean,
    val appearanceCount: Int,
)

data class CharacterProfile(
    val id: Long,
    val bookId: Long,
    val bookTitle: String,
    val displayName: String,
    val currentSummary: String,
    val currentChapterNumber: Int,
    val changedInLatestChapter: Boolean,
    val appearanceCount: Int,
)

data class CharacterHistoryEntry(
    val chapterId: Long,
    val chapterNumber: Int,
    val chapterTitle: String,
    val nameAsWritten: String,
    val chapterDescription: String,
    val incomingSummary: String,
)

data class CharacterHit(
    val characterId: Long,
    val bookId: Long,
    val bookTitle: String,
    val displayName: String,
    val currentSummary: String,
    val currentChapterNumber: Int,
)

data class ChapterHit(
    val chapterId: Long,
    val bookId: Long,
    val bookTitle: String,
    val number: Int,
    val title: String,
)

data class EventHit(
    val chapterId: Long,
    val bookId: Long,
    val bookTitle: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val text: String,
)

data class SearchResults(
    val characters: List<CharacterHit> = emptyList(),
    val chapters: List<ChapterHit> = emptyList(),
    val events: List<EventHit> = emptyList(),
) {
    val isEmpty: Boolean get() = characters.isEmpty() && chapters.isEmpty() && events.isEmpty()
}

sealed interface ImportRejection {
    data object Unreadable : ImportRejection
    data object MalformedJson : ImportRejection
    data class MissingField(val field: String) : ImportRejection
    data class InvalidChapterNumber(val value: Int) : ImportRejection
}

sealed interface ImportOutcome {
    data class Stored(
        val bookTitle: String,
        val chapterNumber: Int,
        val chapterTitle: String,
        val replacedExisting: Boolean,
    ) : ImportOutcome

    data class Rejected(val rejection: ImportRejection) : ImportOutcome
}

data class ImportedFile(
    val fileName: String,
    val outcome: ImportOutcome,
)

data class ImportRequest(
    val fileName: String,
    val content: String?,
)
