package com.archieapps.books.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class BooksPalette(
    val textSecondary: Color,
    val hairline: Color,
    val chapterAccent: Color,
    val chapterWash: Color,
    val chapterInk: Color,
    val currentAccent: Color,
    val currentWash: Color,
    val updated: Color,
    val updatedWash: Color,
    val updatedInk: Color,
    val elevated: Color,
)

private val LightPalette = BooksPalette(
    textSecondary = InkMutedLight,
    hairline = HairlineLight,
    chapterAccent = BrandAmber,
    chapterWash = AmberWashLight,
    chapterInk = AmberInkLight,
    currentAccent = BrandBlue,
    currentWash = BlueWashLight,
    updated = SproutGreen,
    updatedWash = GreenWashLight,
    updatedInk = GreenInkLight,
    elevated = ElevatedLight,
)

private val DarkPalette = BooksPalette(
    textSecondary = InkMutedDark,
    hairline = HairlineDark,
    chapterAccent = BrandAmberOnDark,
    chapterWash = AmberWashDark,
    chapterInk = BrandAmberOnDark,
    currentAccent = BrandBlueOnDark,
    currentWash = BlueWashDark,
    updated = SproutGreenOnDark,
    updatedWash = GreenWashDark,
    updatedInk = SproutGreenOnDark,
    elevated = ElevatedDark,
)

val LocalBooksPalette: ProvidableCompositionLocal<BooksPalette> =
    staticCompositionLocalOf { LightPalette }

private val LightScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BlueWashLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandAmber,
    onSecondary = InkLight,
    secondaryContainer = AmberWashLight,
    onSecondaryContainer = AmberInkLight,
    tertiary = BrandBlueDark,
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = ElevatedLight,
    onSurfaceVariant = InkMutedLight,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = SurfaceLight,
    surfaceContainer = ElevatedLight,
    surfaceContainerHigh = ElevatedLight,
    surfaceContainerHighest = HairlineLight,
    outline = HairlineLight,
    outlineVariant = HairlineLight,
    error = DangerLight,
    onError = Color.White,
    errorContainer = Color(0xFFFBE4E6),
    onErrorContainer = Color(0xFF8A1F27),
    inverseSurface = InkLight,
    inverseOnSurface = CanvasLight,
)

private val DarkScheme = darkColorScheme(
    primary = BrandBlueOnDark,
    onPrimary = Color(0xFF06253C),
    primaryContainer = BlueWashDark,
    onPrimaryContainer = BrandBlueOnDark,
    secondary = BrandAmberOnDark,
    onSecondary = Color(0xFF2C1D05),
    secondaryContainer = AmberWashDark,
    onSecondaryContainer = BrandAmberOnDark,
    tertiary = BrandBlue,
    onTertiary = Color(0xFF06253C),
    background = CanvasDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = ElevatedDark,
    onSurfaceVariant = InkMutedDark,
    surfaceContainerLowest = CanvasDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = ElevatedDark,
    surfaceContainerHigh = ElevatedDark,
    surfaceContainerHighest = HairlineDark,
    outline = HairlineDark,
    outlineVariant = HairlineDark,
    error = DangerDark,
    onError = Color(0xFF3B0A0E),
    errorContainer = Color(0xFF4A171C),
    onErrorContainer = DangerDark,
    inverseSurface = InkDark,
    inverseOnSurface = CanvasDark,
)

@Composable
fun BooksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalBooksPalette provides if (darkTheme) DarkPalette else LightPalette,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = BooksTypography,
            shapes = BooksShapes,
            content = content,
        )
    }
}
