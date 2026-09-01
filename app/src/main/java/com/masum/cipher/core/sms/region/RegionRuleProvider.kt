package com.masum.cipher.core.sms.region

object RegionRuleProvider {

    fun getRulesForCurrency(currencyCode: String): RegionParserRules {
        return when (currencyCode.uppercase()) {
            "INR" -> IndiaParserRules
            "USD" -> UsParserRules
            "EUR" -> EuroParserRules
            "GBP" -> UkParserRules
            "CAD" -> CanadaParserRules
            "AUD" -> AustraliaParserRules
            "AED" -> UaeParserRules
            "SGD" -> SingaporeParserRules
            else -> GlobalFallbackRules
        }
    }

    fun getAllRules(activeCurrencyCode: String): List<RegionParserRules> {
        val primary = getRulesForCurrency(activeCurrencyCode)
        return if (primary == GlobalFallbackRules) {
            listOf(
                GlobalFallbackRules,
                UsParserRules,
                EuroParserRules,
                UkParserRules,
                IndiaParserRules,
                CanadaParserRules,
                AustraliaParserRules,
                UaeParserRules,
                SingaporeParserRules
            )
        } else {
            listOf(primary, GlobalFallbackRules)
        }
    }
}
