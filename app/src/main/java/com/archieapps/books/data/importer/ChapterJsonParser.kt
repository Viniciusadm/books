package com.archieapps.books.data.importer

import com.archieapps.books.domain.TextNormalizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ParsedCharacter(
    val name: String,
    val chapterDescription: String,
    val currentSummary: String,
)

data class ParsedChapter(
    val bookTitle: String,
    val number: Int,
    val title: String,
    val characters: List<ParsedCharacter>,
    val events: List<String>,
)

sealed interface ChapterParseResult {
    data class Success(val chapter: ParsedChapter) : ChapterParseResult
    data object MalformedJson : ChapterParseResult
    data class MissingField(val field: String) : ChapterParseResult
    data class InvalidChapterNumber(val value: Int) : ChapterParseResult
}

class ChapterJsonParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(raw: String): ChapterParseResult {
        val dto = runCatching { json.decodeFromString<ChapterDto>(raw) }.getOrNull()
            ?: return ChapterParseResult.MalformedJson

        val bookTitle = dto.book?.trim().orEmpty()
        if (bookTitle.isEmpty()) return ChapterParseResult.MissingField("book")

        val number = dto.chapter ?: return ChapterParseResult.MissingField("chapter")
        if (number < 0) return ChapterParseResult.InvalidChapterNumber(number)

        val characters = mutableMapOf<String, ParsedCharacter>()
        dto.characters.forEachIndexed { index, character ->
            val name = character.name?.trim().orEmpty()
            if (name.isEmpty()) return ChapterParseResult.MissingField("characters[$index].name")

            val chapterDescription =
                (character.chapterDescription ?: character.description)?.trim().orEmpty()
            val key = TextNormalizer.normalize(name)
            characters[key] = ParsedCharacter(
                name = characters[key]?.name ?: name,
                chapterDescription = chapterDescription,
                currentSummary = character.currentSummary?.trim()?.ifEmpty { null }
                    ?: chapterDescription,
            )
        }

        return ChapterParseResult.Success(
            ParsedChapter(
                bookTitle = bookTitle,
                number = number,
                title = dto.chapterTitle?.trim().orEmpty(),
                characters = characters.values.toList(),
                events = dto.events.map { it.trim() }.filter { it.isNotEmpty() },
            ),
        )
    }
}

@Serializable
private data class ChapterDto(
    val book: String? = null,
    val chapter: Int? = null,
    @SerialName("chapter_title") val chapterTitle: String? = null,
    val characters: List<CharacterDto> = emptyList(),
    val events: List<String> = emptyList(),
)

@Serializable
private data class CharacterDto(
    val name: String? = null,
    @SerialName("chapter_description") val chapterDescription: String? = null,
    @SerialName("current_summary") val currentSummary: String? = null,
    val description: String? = null,
)
