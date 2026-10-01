package com.masum.cipher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {

    private val lightText = Color(0xFFF8FAFC)
    private val darkText = Color(0xFF0D0D1A)
    private val darkCard = Color(0xFF14151F)
    private val rose = Color(0xFFF43F5E)

    @Test
    fun blackOnWhiteHasTheMaximumRatio() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
    }

    @Test
    fun ratioIsSymmetric() {
        assertEquals(contrastRatio(rose, darkCard), contrastRatio(darkCard, rose), 0.0001f)
    }

    @Test
    fun aFaintCellOnADarkCardGetsLightText() {
        val faintCell = rose.copy(alpha = 0.35f).compositeOver(darkCard)

        assertEquals(lightText, bestContrastingColor(faintCell, lightText, darkText))
    }

    @Test
    fun aMidOpacityCellOnADarkCardGetsLightText() {
        val midCell = rose.copy(alpha = 0.6f).compositeOver(darkCard)

        assertEquals(lightText, bestContrastingColor(midCell, lightText, darkText))
    }

    @Test
    fun aFullyBrightCellGetsTheTextWithTheBetterContrast() {
        val brightCell = Color(0xFFFFEB3B)

        assertEquals(darkText, bestContrastingColor(brightCell, lightText, darkText))
    }

    @Test
    fun theChosenColourIsNeverTheWorseOfTheTwo() {
        listOf(0.35f, 0.5f, 0.65f, 0.8f, 1f).forEach { alpha ->
            val cell = rose.copy(alpha = alpha).compositeOver(darkCard)
            val chosen = bestContrastingColor(cell, lightText, darkText)
            val other = if (chosen == lightText) darkText else lightText

            assertTrue(contrastRatio(cell, chosen) >= contrastRatio(cell, other))
        }
    }
}
