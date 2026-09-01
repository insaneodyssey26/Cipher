package com.masum.cipher.core.sms.region

object RegionRuleProvider {

    fun getRulesForCurrency(currencyCode: String): RegionParserRules {
        return when (currencyCode.uppercase()) {
            "INR" -> IndiaParserRules
            "USD" -> UsParserRules
            "GBP" -> UkParserRules
            else -> GlobalFallbackRules
        }
    }

    fun getAllRules(activeCurrencyCode: String): List<RegionParserRules> {
        val primary = getRulesForCurrency(activeCurrencyCode)
        return if (primary == GlobalFallbackRules) {
            listOf(GlobalFallbackRules, IndiaParserRules, UsParserRules, UkParserRules)
        } else {
            listOf(primary, GlobalFallbackRules)
        }
    }
}
