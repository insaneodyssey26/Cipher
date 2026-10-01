package com.masum.cipher.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantGroupingTest {

    private fun group(vararg merchants: String, blankLabel: String = "") =
        merchants.toList().groupByMerchant(blankLabel = blankLabel) { it }

    @Test
    fun spellingsDifferingOnlyByCaseShareOneGroup() {
        val groups = group("JIO", "Jio", "jio")

        assertEquals(1, groups.size)
        assertEquals(3, groups.single().items.size)
    }

    @Test
    fun surroundingAndRepeatedSpacesAreIgnored() {
        val groups = group("Big Basket", "  Big   Basket ", "Big Basket")

        assertEquals(1, groups.size)
        assertEquals("Big Basket", groups.single().name)
    }

    @Test
    fun theMostCommonSpellingIsUsedAsTheName() {
        val groups = group("JIO", "Jio", "Jio", "Jio")

        assertEquals("Jio", groups.single().name)
    }

    @Test
    fun differentMerchantsStaySeparate() {
        val groups = group("Jio", "Airtel", "Wifi")

        assertEquals(3, groups.size)
    }

    @Test
    fun blankMerchantsUseTheBlankLabel() {
        val groups = group("", "   ", blankLabel = "Unknown")

        assertEquals("Unknown", groups.single().name)
        assertEquals(2, groups.single().items.size)
    }

    @Test
    fun aRealMerchantNamedLikeTheBlankLabelIsNotMergedWithBlanks() {
        val groups = group("", "Unknown", blankLabel = "Unknown")

        assertEquals(2, groups.size)
    }

    @Test
    fun accentedAndUnaccentedNamesAreDifferentMerchants() {
        assertEquals(2, group("Café", "Cafe").size)
    }
}
