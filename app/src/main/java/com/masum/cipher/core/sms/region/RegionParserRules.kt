package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

interface RegionParserRules {
    val regionCode: String
    val defaultCurrency: String
    val amountPatterns: List<Pattern>
    val evidencePatterns: List<Pattern>
    val intentKeywords: List<String>
    val exclusionKeywords: List<String>
    val structuralMerchantPatterns: List<Pattern>
    val brandDictionary: List<String>
}
