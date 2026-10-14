package com.archieapps.books.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.archieapps.books.R

val LoraFamily = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun serif(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.SemiBold,
    letterSpacing: Double = -0.2,
) = TextStyle(
    fontFamily = LoraFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

private fun sans(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val BooksTypography = Typography(
    displayLarge = serif(size = 40, lineHeight = 48, weight = FontWeight.Bold, letterSpacing = -0.6),
    displayMedium = serif(size = 34, lineHeight = 42, weight = FontWeight.Bold, letterSpacing = -0.5),
    displaySmall = serif(size = 29, lineHeight = 38, weight = FontWeight.Bold, letterSpacing = -0.4),
    headlineLarge = serif(size = 27, lineHeight = 36, weight = FontWeight.Bold),
    headlineMedium = serif(size = 23, lineHeight = 31, weight = FontWeight.SemiBold),
    headlineSmall = serif(size = 20, lineHeight = 28, weight = FontWeight.SemiBold),
    titleLarge = serif(size = 19, lineHeight = 26, weight = FontWeight.SemiBold),
    titleMedium = serif(size = 17, lineHeight = 24, weight = FontWeight.SemiBold),
    titleSmall = sans(size = 14, lineHeight = 20, weight = FontWeight.SemiBold, letterSpacing = 0.1),
    bodyLarge = sans(size = 16, lineHeight = 25),
    bodyMedium = sans(size = 15, lineHeight = 23),
    bodySmall = sans(size = 13, lineHeight = 19),
    labelLarge = sans(size = 14, lineHeight = 18, weight = FontWeight.SemiBold, letterSpacing = 0.2),
    labelMedium = sans(size = 12, lineHeight = 16, weight = FontWeight.SemiBold, letterSpacing = 0.4),
    labelSmall = sans(size = 11, lineHeight = 14, weight = FontWeight.SemiBold, letterSpacing = 0.8),
)
