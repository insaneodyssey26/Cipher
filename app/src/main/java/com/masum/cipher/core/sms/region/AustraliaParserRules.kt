package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object AustraliaParserRules : RegionParserRules {
    override val regionCode: String = "AU"
    override val defaultCurrency: String = "AUD"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|charge of|payment of|transfer(?:red)?|payid|osko)\\s*(?:by|with|of|for|to)?\\s*\\$\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)\\$\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|authorized|approved|via payid|via osko)"),
        Pattern.compile("(?i)(?:\\$|aud|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )\\$([\\d,]+\\.\\d{2})")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time passcode", "temporary password", "toll-free", "helpline",
        "reward points", "credit limit", "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:purchase|authorized|approved|declined|sent|received|paid|withdrawn|charge|deposited|refunded|payid|osko|bpay)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)purchased at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)\\$\\d+\\.\\d{2}")
    )

    override val intentKeywords: List<String> = listOf(
        "$", "aud", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "authorized", "approved", "payment", "payid", "osko", "bpay"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged|authorized)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)(?:sent to|payid to|bpay to)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+\\$|\\s+for|\\s+on|\\.|$)"),
        Pattern.compile("(?i)from\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+sent|\\s+paid|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "AFTERPAY", "ZIP", "WOOLWORTHS", "COLES", "ALDI", "IGA", "BUNNINGS", "JB HI-FI", "KMART", "TARGET",
        "BIG W", "HARVEY NORMAN", "DAN MURPHY'S", "BWS", "CHEMIST WAREHOUSE", "TELSTRA", "OPTUS", "VODAFONE AU",
        "7-ELEVEN", "AMPOL", "BP", "SHELL", "UBER", "DOORDASH", "MENULOG", "NETFLIX", "SPOTIFY", "AMAZON"
    )
}
