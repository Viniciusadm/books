package com.archieapps.books.domain

import java.text.Normalizer

object TextNormalizer {

    private val diacritics = "\\p{Mn}+".toRegex()
    private val whitespace = "\\s+".toRegex()

    fun normalize(raw: String): String {
        val decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD)
        return diacritics.replace(decomposed, "")
            .lowercase()
            .replace(whitespace, " ")
            .trim()
    }
}
