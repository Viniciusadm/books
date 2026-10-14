# Books

Books is an Android app for keeping chapter summaries of the books you are reading. Each chapter is imported from a JSON file with its characters and events, and the app builds the book's cast from them: who each character is now, in which chapters they appeared and how they were described in each one. This repository holds the whole app, written in Kotlin with Jetpack Compose and Room; the interface is in Portuguese.

## What it does

- **Import.** Pick one or more `.json` files; each one is a chapter. Importing a chapter that already exists replaces its content, and characters that no longer appear anywhere are dropped. Malformed or unreadable files are reported without touching the library.
- **Library and book.** Books with their chapter and character counts, the chapters in order (chapter `0` is accepted as a prologue) and the cast of the book.
- **Characters.** The current summary of a character comes from the highest chapter they appear in, regardless of import order, with the full history per chapter. Names are matched ignoring case, accents and extra spaces, and two characters can be merged; the merged name is kept as an alias for later imports.
- **Search.** Searches characters, chapters and events.

## Chapter format

[`docs/example-chapter.json`](docs/example-chapter.json) is a complete example from an invented book:

```json
{
  "book": "A Cidade das Marés",
  "chapter": 1,
  "chapter_title": "O farol apagado",
  "characters": [
    {
      "name": "Iara",
      "chapter_description": "Faroleira da ilha, desconfiada dos barcos que chegam à noite.",
      "current_summary": "Faroleira da ilha que guarda o farol desde a morte do pai."
    }
  ],
  "events": ["O farol se apaga sem motivo durante a tempestade."]
}
```

`book` and `chapter` are required. `chapter_title` may be missing, `current_summary` falls back to `chapter_description`, an older `description` field is still read, and unknown fields are ignored.

## Requirements

- Android **9+** (minSdk 28), targetSdk 37
- JDK **21** (Gradle daemon toolchain)
- Gradle **9.5.0** (wrapper), Android Gradle Plugin **9.3.1**, Kotlin **2.2.10**
- Android SDK with platform 37, set through `local.properties` or `ANDROID_HOME`

## Building

Build a debug APK, or install it on a connected device:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Tests

```bash
./gradlew test
```

`./gradlew test` runs the JVM unit tests: the JSON parser, name normalization, character consolidation and the Room repository under Robolectric (import, re-import, current summary by chapter, chapter deletion, merging characters and rejected files).
