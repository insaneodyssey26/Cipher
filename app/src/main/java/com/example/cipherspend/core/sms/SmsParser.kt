package com.masum.cipher.core.sms

import com.masum.cipher.core.domain.model.ParsedTransaction
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private val amountPatterns = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|transfer(?:red)?|txn|transaction)\\s*(?:by|with|of|for|to)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)([\\d,]+\\.?\\d{0,2})\\s*(?:rs\\.?|inr)?\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded)"),
        Pattern.compile("(?i)(?:rs\\.?|inr|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!a/c |acc |account |ending |ref |no |id )([\\d,]+\\.\\d{2})(?!\\d)")
    )

    private val exclusionKeywords = listOf(
        "otp", "verification code", "secret code", "tollfree", "helpline", "dial", "win", "won", "offered", "validity"
    )

    private val transactionIntentKeywords = listOf(
        "rs.", "inr", "debited", "spent", "paid", "credited", "received", "txn", "transaction", "amount", "amt"
    )

    private val accountExclusionPattern = Pattern.compile(
        "(?i)(?:a/c|acc|account|ending|no|id|ref)\\s*(?:no\\.?)?\\s*[:#-]?\\s*\\d+"
    )

    private val structuralMerchantPatterns = listOf(
        Pattern.compile("(?i)\\b([A-Za-z][A-Za-z0-9.]{2,})@(?:okaxis|okicici|okhdfcbank|oksbi|ybl|ibl|axl|paytm|upi|waicici|wahdfc|indus|fbl|aubank|kotak|hsbc|sbi|icici|hdfc|axis|airtel|jio|oksbi)\\b"),
        Pattern.compile("(?i)/\\d{5,}/([^/\\d\\s][^/]{1,})(?:/|$)"),
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
        "AIRTEL", "JIO", "VODAFONE", "VI", "TATA PLAY", "GOOGLE", "PAYTM", "PHONEPE",
        "CRED", "GROWW", "ZERODHA", "UPSTOX", "NAVI", "SLICE", "DUNZO", "FASTTAG"
    )

    private val debitKeywords = listOf("debited", "spent", "withdrawn", "charged", "deducted")
    private val creditKeywords = listOf("credited", "deposited", "refunded", "incoming", "cashback", "salary", "received")

    private val merchantFalsePositivePrefixes = listOf("your", "a/c", "account", "bank", "the ", "my ")

    fun parse(message: String): ParsedTransaction? {
        val cleanMessage = message.replace("\\s+".toRegex(), " ")

        if (hasExclusionKeywords(cleanMessage)) return null
        if (!hasTransactionIntent(cleanMessage)) return null

        val amount = extractAmount(cleanMessage) ?: return null

        var merchant = findBrandInText(cleanMessage)
        if (merchant == null) merchant = extractMerchantStructural(cleanMessage)

        val isDebit = debitKeywords.any { cleanMessage.contains(it, ignoreCase = true) }
        val isCredit = creditKeywords.any { cleanMessage.contains(it, ignoreCase = true) }
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
        return exclusionKeywords.any { lower.contains(it) }
    }

    private fun hasTransactionIntent(message: String): Boolean {
        val lower = message.lowercase()
        return transactionIntentKeywords.any { lower.contains(it) }
    }

    private fun extractAmount(message: String): Double? {
        for (pattern in amountPatterns) {
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
        val matcher = accountExclusionPattern.matcher(message)
        while (matcher.find()) {
            if (matchStart >= matcher.start() && matchStart < matcher.end()) return true
        }
        return false
    }

    private fun findBrandInText(message: String): String? {
        val upper = message.uppercase()
        return brandDictionary.find { brand ->
            upper.contains(Regex("\\b${Regex.escape(brand)}\\b"))
        }
    }

    private fun extractMerchantStructural(message: String): String? {
        for (pattern in structuralMerchantPatterns) {
            val matcher = pattern.matcher(message)
            if (!matcher.find()) continue

            val raw = matcher.group(1)?.trim() ?: continue
            if (raw.isBlank()) continue

            val lower = raw.lowercase()
            if (merchantFalsePositivePrefixes.any { lower.startsWith(it) }) continue

            val cleaned = raw.replace(
                Regex("^(?:to|from|payment\\s+to|transfer\\s+to)\\s+", RegexOption.IGNORE_CASE), ""
            ).trim()

            if (cleaned.isNotBlank()) return cleaned
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
