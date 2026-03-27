package com.example.cipherspend.core.sms

import com.example.cipherspend.core.domain.model.ParsedTransaction
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private val amountPatterns = listOf(
        Pattern.compile("(?i)(?:rs\\.?|inr|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|transfer(?:red)?|txn|transaction)\\s*(?:by|with|of|for|to)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)([\\d,]+\\.?\\d{0,2})\\s*(?:rs\\.?|inr)?\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded)"),
        Pattern.compile("(?i)(?<!a/c|acc|account|ending|ref|id|no|#)\\s*([\\d,]+\\.\\d{2})(?!\\d)")
    )

    private val exclusionKeywords = listOf(
        "otp", "verification code", "secret code", "tollfree", "helpline", "call", "report", "dial", "win", "offered", "validity", "service", "government"
    )

    private val transactionIntentKeywords = listOf(
        "rs.", "inr", "debited", "spent", "paid", "credited", "received", "txn", "transaction", "spent", "amount", "amt"
    )

    private val accountExclusionPattern = Pattern.compile("(?i)(?:a/c|acc|account|ending|no|id|ref)\\s*(?:no\\.?)?\\s*[:#-]?\\s*\\d+")

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
        
        if (hasExclusionKeywords(cleanMessage)) return null
        if (!hasTransactionIntent(cleanMessage)) return null
        
        val amount = extractAmount(cleanMessage) ?: return null
        
        var merchant = findBrandInText(cleanMessage)
        
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

    private fun hasExclusionKeywords(message: String): Boolean {
        val lowerMessage = message.lowercase()
        return exclusionKeywords.any { lowerMessage.contains(it) }
    }

    private fun hasTransactionIntent(message: String): Boolean {
        val lowerMessage = message.lowercase()
        return transactionIntentKeywords.any { lowerMessage.contains(it) }
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(message)
            while (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group(0)
                
                if (isPartOfAccountNumber(message, matcher.start())) {
                    continue
                }

                val numericOnly = match.replace(",", "").replace(Regex("[^\\d.]"), "")
                val value = numericOnly.toDoubleOrNull()
                
                if (value != null && value > 0) {
                    if (value > 1000000 && !match.contains(".")) {
                        continue
                    }
                    
                    return value
                }
            }
        }
        return null
    }

    private fun isPartOfAccountNumber(message: String, matchStart: Int): Boolean {
        val accountMatcher = accountExclusionPattern.matcher(message)
        while (accountMatcher.find()) {
            if (matchStart >= accountMatcher.start() && matchStart < accountMatcher.end()) {
                return true
            }
        }
        return false
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
