package com.archieapps.books.data.importer

import com.archieapps.books.data.local.CharacterAppearanceDao
import com.archieapps.books.data.local.CharacterDao
import com.archieapps.books.domain.CharacterConsolidator
import com.archieapps.books.domain.TextNormalizer

class CharacterRecalculator(
    private val characterDao: CharacterDao,
    private val appearanceDao: CharacterAppearanceDao,
) {

    suspend fun recalculate(characterId: Long, timestamp: Long) {
        val consolidated = CharacterConsolidator.consolidate(appearanceDao.snapshots(characterId))
        if (consolidated == null) {
            characterDao.delete(characterId)
            return
        }
        characterDao.updateConsolidation(
            characterId = characterId,
            currentSummary = consolidated.currentSummary,
            currentChapterNumber = consolidated.currentChapterNumber,
            changedInLatestChapter = consolidated.changedInLatestChapter,
            appearanceCount = consolidated.appearanceCount,
            searchBlob = TextNormalizer.normalize(consolidated.currentSummary),
            timestamp = timestamp,
        )
    }
}
