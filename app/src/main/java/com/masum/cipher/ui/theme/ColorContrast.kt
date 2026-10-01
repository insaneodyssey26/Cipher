package com.masum.cipher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private const val LUMINANCE_OFFSET = 0.05f

fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + LUMINANCE_OFFSET) / (darker + LUMINANCE_OFFSET)
}

fun bestContrastingColor(background: Color, light: Color, dark: Color): Color =
    if (contrastRatio(background, light) >= contrastRatio(background, dark)) light else dark
