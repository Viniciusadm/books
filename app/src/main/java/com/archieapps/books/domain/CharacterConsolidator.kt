package com.archieapps.books.domain

data class AppearanceSnapshot(
    val id: Long,
    val chapterNumber: Int,
    val chapterImportedAt: Long,
    val incomingSummary: String,
)

data class ConsolidatedCharacter(
    val currentSummary: String,
    val currentChapterNumber: Int,
    val changedInLatestChapter: Boolean,
    val appearanceCount: Int,
)

object CharacterConsolidator {

    private val authorityOrder = compareBy<AppearanceSnapshot>(
        { it.chapterNumber },
        { it.chapterImportedAt },
        { it.id },
    )

    fun consolidate(appearances: List<AppearanceSnapshot>): ConsolidatedCharacter? {
        if (appearances.isEmpty()) return null

        val ordered = appearances.sortedWith(authorityOrder)
        val authoritative = ordered.last()
        val previous = ordered.getOrNull(ordered.lastIndex - 1)

        return ConsolidatedCharacter(
            currentSummary = authoritative.incomingSummary,
            currentChapterNumber = authoritative.chapterNumber,
            changedInLatestChapter = previous != null &&
                previous.incomingSummary != authoritative.incomingSummary,
            appearanceCount = appearances.size,
        )
    }
}
