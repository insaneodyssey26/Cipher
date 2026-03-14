package com.example.cipherspend.core.sms

import com.example.cipherspend.core.domain.model.ParsedTransaction
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private val amountPatterns = listOf(
        Pattern.compile("(?i)(?:rs|inr|amt|amount)\\.?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent)\\s*(?:by|with|of)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)debited\\s+for\\s+([\\d,]+\\.?\\d{0,2})"),
        // Catch-all for any number that looks like an amount in a short message
        Pattern.compile("(?<!\\d)([\\d,]+\\.\\d{2})(?!\\d)"),
        Pattern.compile("(?<!\\d)([\\d,]{3,})(?!\\d)")
    )

    private val structuralMerchantPatterns = listOf(
        Pattern.compile("(?i)(?:at|to|towards|info|vpa|into|merchant|payee)\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\s+at|\\s+via|\\s+ref|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\.|$)"),
        Pattern.compile("(?i)used\\s+at\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\.|$)")
    )

    private val brandDictionary = listOf(
        "AMAZON", "FLIPKART", "MYNTRA", "AJIO", "MEESHO", "NYKAA", "RELIANCE", "CROMA",
        "BLINKIT", "BIGBASKET", "ZEPTO", "INSTAMART", "JIOMART", "ZOMATO", "SWIGGY", 
        "EATFIT", "DOMINOS", "KFC", "PIZZA HUT", "STARBUCKS", "MCDONALDS", "BURGER KING",
        "UBER", "OLA", "RAPIDO", "INDIGO", "AIR INDIA", "SPICEJET", "IRCTC", "REDBUS", 
        "MAKEMYTRIP", "GOIBIBO", "BOOKMYSHOW", "NETFLIX", "SPOTIFY", "HOTSTAR", "PRIME VIDEO", 
        "PVR", "INOX", "STEAM", "APOLLO", "TATA 1MG", "PHARMEASY", "NETMEDS", "PRACTO", 
        "AIRTEL", "JIO", "VODAFONE", "VI", "TATA PLAY", "GOOGLE", "PAYTM", "PHONEPE"
    )

    private val incomeKeywords = listOf(
        "credited", "received", "deposited", "added", "refunded", "incoming", "cashback", "salary"
    )

    fun parse(message: String): ParsedTransaction? {
        val cleanMessage = message.replace("\\s+".toRegex(), " ")
        val amount = extractAmount(cleanMessage) ?: return null
        
        // Strategy 1: Look for known brands anywhere in the text (Highest Accuracy)
        var merchant = findBrandInText(cleanMessage)
        
        // Strategy 2: If no brand found, use structural regex patterns
        if (merchant == null) {
            merchant = extractMerchantStructural(cleanMessage)
        }

        val isIncome = incomeKeywords.any { cleanMessage.contains(it, ignoreCase = true) }
        
        return ParsedTransaction(
            amount = amount,
            merchant = sanitizeMerchant(merchant ?: "Miscellaneous"),
            currency = "INR",
            isIncome = isIncome
        )
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group(0)
                val numericOnly = match.replace(",", "").replace(Regex("[^\\d.]"), "")
                val value = numericOnly.toDoubleOrNull()
                if (value != null && value > 0) return value
            }
        }
        return null
    }

    private fun findBrandInText(message: String): String? {
        val upperMessage = message.uppercase()
        return brandDictionary.find { upperMessage.contains(it) }
    }

    private fun extractMerchantStructural(message: String): String? {
        for (pattern in structuralMerchantPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val result = matcher.group(1)?.trim()
                if (!result.isNullOrBlank()) return result
            }
        }
        return null
    }

    private fun sanitizeMerchant(merchant: String): String {
        return merchant
            .replace(Regex("(?i)using.*|via.*|on.*|ref.*|VPA.*|UPI.*"), "")
            .trim()
            .split(" ")
            .take(2)
            .joinToString(" ")
            .uppercase()
    }
}
