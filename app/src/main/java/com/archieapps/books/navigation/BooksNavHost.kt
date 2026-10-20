package com.archieapps.books.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.archieapps.books.ui.book.BookScreen
import com.archieapps.books.ui.chapter.ChapterScreen
import com.archieapps.books.ui.character.CharacterScreen
import com.archieapps.books.ui.library.LibraryScreen
import com.archieapps.books.ui.search.SearchScreen
import kotlinx.coroutines.flow.StateFlow

private const val ARG_BOOK_ID = "bookId"
private const val ARG_CHAPTER_ID = "chapterId"
private const val ARG_CHARACTER_ID = "characterId"

private object Routes {
    const val LIBRARY = "library"
    const val SEARCH = "search"
    const val BOOK = "book/{$ARG_BOOK_ID}"
    const val CHAPTER = "chapter/{$ARG_CHAPTER_ID}"
    const val CHARACTER = "character/{$ARG_CHARACTER_ID}"

    fun book(bookId: Long) = "book/$bookId"
    fun chapter(chapterId: Long) = "chapter/$chapterId"
    fun character(characterId: Long) = "character/$characterId"
}

@Composable
fun BooksNavHost(
    pendingImports: StateFlow<List<Uri>>,
    onImportsConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val imports by pendingImports.collectAsState()

    LaunchedEffect(imports) {
        if (imports.isNotEmpty()) {
            navController.popBackStack(Routes.LIBRARY, inclusive = false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LIBRARY,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                pendingImports = pendingImports,
                onImportsConsumed = onImportsConsumed,
                onOpenBook = { navController.navigate(Routes.book(it)) },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
            )
        }

        composable(
            route = Routes.BOOK,
            arguments = listOf(navArgument(ARG_BOOK_ID) { type = NavType.LongType }),
        ) { entry ->
            BookScreen(
                bookId = entry.arguments?.getLong(ARG_BOOK_ID) ?: return@composable,
                onBack = navController::popBackStack,
                onOpenChapter = { navController.navigate(Routes.chapter(it)) },
                onOpenCharacter = { navController.navigate(Routes.character(it)) },
            )
        }

        composable(
            route = Routes.CHAPTER,
            arguments = listOf(navArgument(ARG_CHAPTER_ID) { type = NavType.LongType }),
        ) { entry ->
            ChapterScreen(
                chapterId = entry.arguments?.getLong(ARG_CHAPTER_ID) ?: return@composable,
                onBack = navController::popBackStack,
                onOpenCharacter = { navController.navigate(Routes.character(it)) },
            )
        }

        composable(
            route = Routes.CHARACTER,
            arguments = listOf(navArgument(ARG_CHARACTER_ID) { type = NavType.LongType }),
        ) { entry ->
            CharacterScreen(
                characterId = entry.arguments?.getLong(ARG_CHARACTER_ID) ?: return@composable,
                onBack = navController::popBackStack,
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = navController::popBackStack,
                onOpenCharacter = { navController.navigate(Routes.character(it)) },
                onOpenChapter = { navController.navigate(Routes.chapter(it)) },
            )
        }
    }
}
