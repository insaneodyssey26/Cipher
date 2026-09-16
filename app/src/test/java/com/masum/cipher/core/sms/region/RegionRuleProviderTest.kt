package com.masum.cipher.core.sms.region

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionRuleProviderTest {

    @Test
    fun `INR resolves to India rules`() {
        assertEquals(IndiaParserRules, RegionRuleProvider.getRulesForCurrency("INR"))
    }

    @Test
    fun `USD resolves to US rules`() {
        assertEquals(UsParserRules, RegionRuleProvider.getRulesForCurrency("USD"))
    }

    @Test
    fun `EUR resolves to Euro rules`() {
        assertEquals(EuroParserRules, RegionRuleProvider.getRulesForCurrency("EUR"))
    }

    @Test
    fun `GBP resolves to UK rules`() {
        assertEquals(UkParserRules, RegionRuleProvider.getRulesForCurrency("GBP"))
    }

    @Test
    fun `CAD resolves to Canada rules`() {
        assertEquals(CanadaParserRules, RegionRuleProvider.getRulesForCurrency("CAD"))
    }

    @Test
    fun `AUD resolves to Australia rules`() {
        assertEquals(AustraliaParserRules, RegionRuleProvider.getRulesForCurrency("AUD"))
    }

    @Test
    fun `AED resolves to UAE rules`() {
        assertEquals(UaeParserRules, RegionRuleProvider.getRulesForCurrency("AED"))
    }

    @Test
    fun `SGD resolves to Singapore rules`() {
        assertEquals(SingaporeParserRules, RegionRuleProvider.getRulesForCurrency("SGD"))
    }

    @Test
    fun `unmapped currency resolves to global fallback rules`() {
        assertEquals(GlobalFallbackRules, RegionRuleProvider.getRulesForCurrency("JPY"))
    }

    @Test
    fun `currency code lookup is case insensitive`() {
        assertEquals(IndiaParserRules, RegionRuleProvider.getRulesForCurrency("inr"))
    }

    @Test
    fun `getAllRules for a mapped currency returns primary rules then global fallback only`() {
        val chain = RegionRuleProvider.getAllRules("USD")
        assertEquals(listOf(UsParserRules, GlobalFallbackRules), chain)
    }

    @Test
    fun `getAllRules for an unmapped currency returns the full region chain starting with global fallback`() {
        val chain = RegionRuleProvider.getAllRules("JPY")
        assertEquals(GlobalFallbackRules, chain.first())
        assertTrue(chain.containsAll(listOf(UsParserRules, EuroParserRules, UkParserRules, IndiaParserRules, CanadaParserRules, AustraliaParserRules, UaeParserRules, SingaporeParserRules)))
        assertEquals(9, chain.size)
    }

    @Test
    fun `getAllRules for India does not repeat India in the fallback chain`() {
        val chain = RegionRuleProvider.getAllRules("INR")
        assertEquals(2, chain.size)
        assertEquals(IndiaParserRules, chain[0])
        assertEquals(GlobalFallbackRules, chain[1])
    }
}
