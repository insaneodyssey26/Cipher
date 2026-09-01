package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object GlobalFallbackRules : RegionParserRules {
    override val regionCode: String = "GLOBAL"
    override val defaultCurrency: String = "USD"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|payment of|charge of)\\s*(?:by|with|of|for|to)?\\s*(?:[A-Z]{3}|[$€£¥₹₩₱₫฿R])?\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?:[$€£¥₹₩₱₫฿R]|[A-Z]{3})\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded)"),
        Pattern.compile("(?i)(?:[$€£¥₹₩₱₫฿R]|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )([\\d,]+\\.\\d{2})(?!\\d)")
    )

    override val exclusionKeywords: List<String> = listOf(
        "otp", "verification code", "security code", "passcode", "helpline", "toll-free", "promo", "discount",
        "reward points", "pre-approved", "credit limit", "claim", "free trial", "expire", "package"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:spent|paid|charged|transferred|withdrawn|deposited|refunded|purchase|sent|received)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)received from")
    )

    override val intentKeywords: List<String> = listOf(
        "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "transferred", "payment", "amount"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+[$€£₹]|\\s+for|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "AMAZON", "APPLE", "GOOGLE", "MICROSOFT", "NETFLIX", "SPOTIFY", "YOUTUBE", "STEAM",
        "PLAYSTATION", "XBOX", "NINTENDO", "DISNEY+", "UBER", "AIRBNB", "BOOKING.COM",
        "STARBUCKS", "MCDONALDS", "KFC", "BURGER KING", "SUBWAY", "DOMINOS", "PIZZA HUT",
        "ZARA", "H&M", "NIKE", "ADIDAS", "UNIQLO", "IKEA", "SHELL", "BP", "TOTAL", "EXXON"
    )
}
