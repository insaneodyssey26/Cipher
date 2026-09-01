package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object UkParserRules : RegionParserRules {
    override val regionCode: String = "GB"
    override val defaultCurrency: String = "GBP"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|payment of|transfer(?:red)?)\\s*(?:by|with|of|for|to)?\\s*£\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)£\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded)"),
        Pattern.compile("(?i)(?:£|gbp)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!card |ending |ref |no |id )£([\\d,]+\\.\\d{2})")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time passcode", "temporary password", "helpline", "reward points",
        "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|sort code)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:spent|paid|charged|transferred|withdrawn|deposited|refunded|purchase)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)£\\d+\\.\\d{2}")
    )

    override val intentKeywords: List<String> = listOf(
        "£", "gbp", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "transferred", "payment"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+£|\\s+for|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "TESCO", "SAINSBURY'S", "ASDA", "MORRISONS", "ALDI", "LIDL", "MARKS & SPENCER", "M&S", "WAITROSE",
        "CO-OP", "ICELAND", "BOOTS", "SUPERDRUG", "GREGGS", "COSTA COFFEE", "PRET A MANGER", "STARBUCKS",
        "CAFFE NERO", "NANDOS", "WAGAMAMA", "DELIVEROO", "JUST EAT", "UBER EATS", "UBER", "BOLT",
        "TRANSPORT FOR LONDON", "TFL", "NATIONAL RAIL", "TRAINLINE", "AMAZON", "ARGOS", "CURRYS", "JOHN LEWIS",
        "ASOS", "PRIMARK", "ZARA", "H&M", "NEXT", "SHELL", "BP", "ESSO", "TEXACO", "NETFLIX", "SPOTIFY",
        "MONZO", "REVOLUT", "STARLING", "BARCLAYS", "HSBC", "LLOYDS", "NATWEST", "SANTANDER", "HALIFAX", "NATIONWIDE"
    )
}
