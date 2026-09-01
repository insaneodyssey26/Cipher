package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object UaeParserRules : RegionParserRules {
    override val regionCode: String = "AE"
    override val defaultCurrency: String = "AED"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|charge of|payment of|transfer(?:red)?|txn|transaction)\\s*(?:by|with|of|for|to)?\\s*(?:AED|Dhs|AED\\.)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?:AED|Dhs|AED\\.)\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|authorized|approved)"),
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of)\\s*(?:by|with|of|for|to)?\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:AED|Dhs)"),
        Pattern.compile("(?i)(?:AED|aed|Dhs|dhs)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )\\b([\\d,]+\\.\\d{2})\\s*(?:AED|Dhs)\\b")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time password", "otp", "temporary password", "toll-free", "helpline",
        "reward points", "touchpoints", "credit limit", "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:purchase|authorized|approved|declined|sent|received|paid|withdrawn|charge|deposited|refunded)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)purchased at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)(?:AED|Dhs)\\s*[\\d,.]+")
    )

    override val intentKeywords: List<String> = listOf(
        "aed", "dhs", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "authorized", "approved", "payment"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged|authorized)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+AED|\\s+Dhs|\\s+for|\\s+on|\\.|$)"),
        Pattern.compile("(?i)from\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+sent|\\s+paid|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "LULU", "LULU HYPERMARKET", "CARREFOUR UAE", "SPINNEYS", "CHOITHRAMS", "WAITROSE", "UNION COOP", "NOON", "AMAZON UAE",
        "SHARAF DG", "VIRGIN MEGSTORE", "IKEA UAE", "ZARA", "H&M", "CAREEM", "TALABAT", "DELIVEROO UAE", "UBER UAE",
        "DU", "ETISALAT", "E&", "DEWA", "SEWA", "ADDC", "SALIK", "ADNOC", "ENOC", "EPPCO", "EMARAT", "CINEMARK", "VOX CINEMAS"
    )
}
