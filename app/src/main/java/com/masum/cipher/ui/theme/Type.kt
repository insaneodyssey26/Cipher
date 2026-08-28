package com.masum.cipher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.masum.cipher.R

@OptIn(ExperimentalTextApi::class)
val Manrope = FontFamily(
    Font(
        resId = R.font.manrope_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.manrope_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.manrope_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.manrope_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.manrope_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
        weight = FontWeight.ExtraBold
    )
)

@OptIn(ExperimentalTextApi::class)
val DMSans = FontFamily(
    Font(
        resId = R.font.dmsans_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.dmsans_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.dmsans_variable,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        weight = FontWeight.Bold
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = (-1).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)
