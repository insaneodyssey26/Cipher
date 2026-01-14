package com.example.cipherspend.core.sms

import com.example.cipherspend.core.domain.model.ParsedTransaction
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private val amountPatterns = listOf(
        // Standard: Rs. 500.00, INR 500, Rs500
        Pattern.compile("(?i)(?:rs|inr|amt|amount)\\.?\\s*([\\d,]+\\.?\\d{0,2})"),
        // Transactional: Debited by 500, Spent 500
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn)\\s*(?:by|with|of)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d{0,2})"),
        // Bank specific: "a/c... debited for 500"
        Pattern.compile("(?i)debited\\s+for\\s+([\\d,]+\\.?\\d{0,2})")
    )

    private val merchantPatterns = listOf(
        // UPI/VPA: "at ZOMATO", "to amazon@paytm", "info: SWIGGY"
        Pattern.compile("(?i)(?:at|to|towards|info|vpa|into)\\s+([^\\d\\s][^;.]+?)(?:\\s+on|\\s+using|\\s+at|\\s+via|\\.)"),
        // Specific: "Sent to [Merchant]"
        Pattern.compile("(?i)sent\\s+to\\s+([^\\d\\s][^;.]+?)(?:\\s+on|\\.)"),
        // Card: "used at [Merchant]"
        Pattern.compile("(?i)used\\s+at\\s+([^\\d\\s][^;.]+?)(?:\\s+on|\\.)")
    )

    private val incomeKeywords = listOf(
        "credited", "received", "deposited", "added", "refunded", "incoming", "cashback"
    )

    private val currencyMap = mapOf(
        "$" to "USD",
        "INR" to "INR",
        "Rs" to "INR",
        "€" to "EUR",
        "£" to "GBP"
    )

    fun parse(message: String): ParsedTransaction? {
        val cleanMessage = message.replace("\\s+".toRegex(), " ")
        val amount = extractAmount(cleanMessage) ?: return null
        val merchant = extractMerchant(cleanMessage) ?: "Miscellaneous"
        val isIncome = incomeKeywords.any { cleanMessage.contains(it, ignoreCase = true) }
        val currency = extractCurrency(cleanMessage)

        return ParsedTransaction(
            amount = amount,
            merchant = sanitizeMerchant(merchant),
            currency = currency,
            isIncome = isIncome
        )
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group(0)
                // Filter out currency symbols if the whole group was matched
                val numericOnly = match?.replace(Regex("[^\\d.]"), "")
                return numericOnly?.toDoubleOrNull()
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

    private fun sanitizeMerchant(merchant: String): String {
        return merchant
            .replace(Regex("(?i)using.*|via.*|on.*"), "") // Remove tails
            .trim()
            .split(" ")
            .take(3) // Take first 3 words max
            .joinToString(" ")
            .uppercase()
    }

    private fun extractCurrency(message: String): String {
        for ((symbol, code) in currencyMap) {
            if (message.contains(symbol, ignoreCase = true)) return code
        }
        return "INR"
    }
}