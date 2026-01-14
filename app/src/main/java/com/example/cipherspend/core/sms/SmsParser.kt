package com.example.cipherspend.core.sms

import com.example.cipherspend.core.domain.model.ParsedTransaction
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private val amountPatterns = listOf(
        Pattern.compile("(?i)(?:rs|inr|amt|amount)\\.?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)debited\\s*(?:by|with)?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)spent\\s*([\\d,]+\\.?\\d{0,2})")
    )

    private val merchantPatterns = listOf(
        Pattern.compile("(?i)(?:at|to|info|vpa)\\s+([^\\d\\s][^;.]+?)(?:\\s+on|\\s+using|\\s+at|\\.)"),
        Pattern.compile("(?i)towards\\s+([^\\d\\s][^;.]+?)(?:\\s+on|\\s+using|\\s+at|\\.)")
    )

    private val incomeKeywords = listOf("credited", "received", "deposited", "added")

    fun parse(message: String): ParsedTransaction? {
        val amount = extractAmount(message) ?: return null
        val merchant = extractMerchant(message) ?: "Unknown Merchant"
        val isIncome = incomeKeywords.any { message.contains(it, ignoreCase = true) }
        val currency = extractCurrency(message)

        return ParsedTransaction(
            amount = amount,
            merchant = merchant.trim(),
            currency = currency,
            isIncome = isIncome
        )
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractMerchant(message: String): String? {
        for (pattern in merchantPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }

    private fun extractCurrency(message: String): String {
        return when {
            message.contains("INR", ignoreCase = true) || message.contains("Rs", ignoreCase = true) -> "INR"
            message.contains("$") -> "USD"
            else -> "INR"
        }
    }
}