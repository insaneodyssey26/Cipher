package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object SingaporeParserRules : RegionParserRules {
    override val regionCode: String = "SG"
    override val defaultCurrency: String = "SGD"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|charge of|payment of|transfer(?:red)?|paynow)\\s*(?:by|with|of|for|to)?\\s*(?:S\\$|SGD|\\$)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?:S\\$|SGD)\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|authorized|approved|via paynow)"),
        Pattern.compile("(?i)(?:S\\$|sgd|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )S\\$([\\d,]+\\.\\d{2})")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time password", "otp", "temporary password", "toll-free", "helpline",
        "reward points", "credit limit", "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:purchase|authorized|approved|declined|sent|received|paid|withdrawn|charge|deposited|refunded|paynow)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)purchased at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)S\\$\\s*[\\d,.]+")
    )

    override val intentKeywords: List<String> = listOf(
        "s$", "sgd", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "authorized", "approved", "payment", "paynow"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged|authorized)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)(?:sent to|paynow to)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+S\\$|\\s+for|\\s+on|\\.|$)"),
        Pattern.compile("(?i)from\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+sent|\\s+paid|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "GRAB", "GRABPAY", "FAIRPRICE", "NTUC FAIRPRICE", "SHENG SIONG", "COLD STORAGE",
        "GIANT", "DON DON DONKI", "LAZADA", "SHOPEE", "AMAZON SG", "FOODPANDA", "DELIVEROO SG", "SINGTEL", "STARHUB",
        "M1", "SIMBA", "COMFORTDELGRO", "GOJEK", "EZ-LINK", "NETS", "WATSONS SG", "GUARDIAN SG", "UNIQLO SG", "NETFLIX", "SPOTIFY"
    )
}
