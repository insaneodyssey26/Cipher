package com.masum.cipher.core.sms

import com.masum.cipher.core.domain.model.ParsedTransaction
import com.masum.cipher.core.sms.config.TransactionPatterns
import com.masum.cipher.core.sms.region.RegionParserRules
import com.masum.cipher.core.sms.region.RegionRuleProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionParser @Inject constructor() {

    fun parse(message: String, preferredCurrency: String? = null): ParsedTransaction? {
        val cleanMessage = message.replace("\\s+".toRegex(), " ")

        val ruleChain = RegionRuleProvider.getAllRules(preferredCurrency ?: "INR")

        for (rules in ruleChain) {
            val parsed = tryParseWithRules(cleanMessage, rules)
            if (parsed != null) return parsed
        }

        return null
    }

    private fun tryParseWithRules(message: String, rules: RegionParserRules): ParsedTransaction? {
        if (hasExclusionKeywords(message, rules)) return null
        if (!hasTransactionIntent(message, rules)) return null
        if (!hasTransactionEvidence(message, rules)) return null

        val amount = extractAmount(message, rules) ?: return null

        var merchant = findBrandInText(message, rules)
        if (merchant == null) merchant = extractMerchantStructural(message, rules)

        val isDebit = TransactionPatterns.DEBIT_KEYWORDS.any { message.contains(it, ignoreCase = true) }
        val isCredit = TransactionPatterns.CREDIT_KEYWORDS.any { message.contains(it, ignoreCase = true) }
        val isIncome = isCredit && !isDebit

        return ParsedTransaction(
            amount = amount,
            merchant = sanitizeMerchant(merchant ?: "Miscellaneous"),
            currency = rules.defaultCurrency,
            isIncome = isIncome
        )
    }

    private fun hasExclusionKeywords(message: String, rules: RegionParserRules): Boolean {
        val lower = message.lowercase()
        return rules.exclusionKeywords.any { lower.contains(it) }
    }

    private fun hasTransactionIntent(message: String, rules: RegionParserRules): Boolean {
        val lower = message.lowercase()
        return rules.intentKeywords.any { lower.contains(it) }
    }

    private fun hasTransactionEvidence(message: String, rules: RegionParserRules): Boolean {
        return rules.evidencePatterns.any { pattern ->
            pattern.matcher(message).find()
        }
    }

    private fun extractAmount(message: String, rules: RegionParserRules): Double? {
        for (pattern in rules.amountPatterns) {
            val matcher = pattern.matcher(message)
            while (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group(0)
                if (isPartOfAccountNumber(message, matcher.start())) continue

                val numeric = match.replace(",", "").replace(Regex("[^\\d.]"), "")
                val value = numeric.toDoubleOrNull() ?: continue

                if (value <= 0) continue
                if (value > 1_000_000 && !match.contains(".")) continue

                return value
            }
        }
        return null
    }

    private fun isPartOfAccountNumber(message: String, matchStart: Int): Boolean {
        val matcher = TransactionPatterns.ACCOUNT_EXCLUSION_PATTERN.matcher(message)
        while (matcher.find()) {
            if (matchStart >= matcher.start() && matchStart < matcher.end()) return true
        }
        return false
    }

    private fun findBrandInText(message: String, rules: RegionParserRules): String? {
        val upper = message.uppercase()
        return rules.brandDictionary
            .sortedByDescending { it.length }
            .find { brand ->
                upper.contains(Regex("\\b${Regex.escape(brand)}\\b"))
            }
    }

    private fun extractMerchantStructural(message: String, rules: RegionParserRules): String? {
        for (pattern in rules.structuralMerchantPatterns) {
            val matcher = pattern.matcher(message)
            while (matcher.find()) {
                val raw = matcher.group(1)?.trim() ?: continue
                if (raw.isBlank()) continue

                val lower = raw.lowercase()
                if (TransactionPatterns.MERCHANT_FALSE_POSITIVE_PREFIXES.any { lower.startsWith(it) }) continue

                val cleaned = raw.replace(
                    Regex("^(?:to|from|payment\\s+to|transfer\\s+to)\\s+", RegexOption.IGNORE_CASE), ""
                ).trim()

                if (cleaned.isNotBlank()) return cleaned
            }
        }
        return null
    }

    private fun sanitizeMerchant(merchant: String): String {
        return merchant
            .replace(Regex("(?i)\\busing\\b.*|\\bvia\\b.*|\\bon\\b.*|\\bref\\b.*|\\bVPA\\b.*|\\bUPI\\b.*|\\bcard\\b.*|\\bwith\\b.*"), "")
            .trim()
            .split(" ")
            .take(2)
            .joinToString(" ")
            .uppercase()
    }
}
