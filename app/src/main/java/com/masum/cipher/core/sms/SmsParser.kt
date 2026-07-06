package com.masum.cipher.core.sms

import com.masum.cipher.core.domain.model.ParsedTransaction
import com.masum.cipher.core.sms.config.SmsPatterns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    fun parse(message: String): ParsedTransaction? {
        val cleanMessage = message.replace("\\s+".toRegex(), " ")

        if (hasExclusionKeywords(cleanMessage)) return null
        if (!hasTransactionIntent(cleanMessage)) return null

        val amount = extractAmount(cleanMessage) ?: return null

        var merchant = findBrandInText(cleanMessage)
        if (merchant == null) merchant = extractMerchantStructural(cleanMessage)

        val isDebit = SmsPatterns.DEBIT_KEYWORDS.any { cleanMessage.contains(it, ignoreCase = true) }
        val isCredit = SmsPatterns.CREDIT_KEYWORDS.any { cleanMessage.contains(it, ignoreCase = true) }
        val isIncome = isCredit && !isDebit

        return ParsedTransaction(
            amount = amount,
            merchant = sanitizeMerchant(merchant ?: "Miscellaneous"),
            currency = "INR",
            isIncome = isIncome
        )
    }

    private fun hasExclusionKeywords(message: String): Boolean {
        val lower = message.lowercase()
        return SmsPatterns.EXCLUSION_KEYWORDS.any { lower.contains(it) }
    }

    private fun hasTransactionIntent(message: String): Boolean {
        val lower = message.lowercase()
        return SmsPatterns.INTENT_KEYWORDS.any { lower.contains(it) }
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in SmsPatterns.AMOUNT_PATTERNS) {
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
        val matcher = SmsPatterns.ACCOUNT_EXCLUSION_PATTERN.matcher(message)
        while (matcher.find()) {
            if (matchStart >= matcher.start() && matchStart < matcher.end()) return true
        }
        return false
    }

    private fun findBrandInText(message: String): String? {
        val upper = message.uppercase()
        return SmsPatterns.BRAND_DICTIONARY.find { brand ->
            upper.contains(Regex("\\b${Regex.escape(brand)}\\b"))
        }
    }

    private fun extractMerchantStructural(message: String): String? {
        for (pattern in SmsPatterns.STRUCTURAL_MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(message)
            while (matcher.find()) {
                val raw = matcher.group(1)?.trim() ?: continue
                if (raw.isBlank()) continue

                val lower = raw.lowercase()
                if (SmsPatterns.MERCHANT_FALSE_POSITIVE_PREFIXES.any { lower.startsWith(it) }) continue

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
            .replace(Regex("(?i)\\busing\\b.*|\\bvia\\b.*|\\bon\\b.*|\\bref\\b.*|\\bVPA\\b.*|\\bUPI\\b.*"), "")
            .trim()
            .split(" ")
            .take(2)
            .joinToString(" ")
            .uppercase()
    }
}
