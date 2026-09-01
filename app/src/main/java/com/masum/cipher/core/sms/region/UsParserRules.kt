package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object UsParserRules : RegionParserRules {
    override val regionCode: String = "US"
    override val defaultCurrency: String = "USD"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|charge of|payment of|transfer(?:red)?|txn|transaction)\\s*(?:by|with|of|for|to)?\\s*\\$\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)\\$\\s*([\\d,]+\\.?\\d{0,2})\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|authorized|approved)"),
        Pattern.compile("(?i)(?:\\$|usd|amt|amount)\\s*([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )\\$([\\d,]+\\.\\d{2})")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time passcode", "temporary password", "toll-free", "helpline",
        "reward points", "credit limit", "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:purchase|authorized|approved|declined|sent|received|paid|withdrawn|charge|deposited|refunded)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)purchased at"),
        Pattern.compile("(?i)authorized at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)\\$\\d+\\.\\d{2}")
    )

    override val intentKeywords: List<String> = listOf(
        "$", "usd", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "authorized", "approved", "payment"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged|authorized)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+\\$|\\s+for|\\s+on|\\.|$)"),
        Pattern.compile("(?i)from\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+sent|\\s+paid|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "WALMART", "TARGET", "COSTCO", "TRADER JOE'S", "WHOLE FOODS", "KROGER", "SAFEWAY", "ALDI", "HEB",
        "AMAZON", "BEST BUY", "HOME DEPOT", "LOWE'S", "APPLE", "STARBUCKS", "DUNKIN", "MCDONALDS",
        "CHIPOTLE", "CHICK-FIL-A", "TACO BELL", "SUBWAY", "WENDYS", "BURGER KING", "DOMINOS", "PANERA",
        "UBER", "LYFT", "DOORDASH", "UBER EATS", "GRUBHUB", "INSTACART", "CHEVRON", "SHELL", "EXXON",
        "BP", "MOBIL", "7-ELEVEN", "CIRCLE K", "WAWA", "CVS", "WALGREENS", "RITE AID", "NETFLIX",
        "SPOTIFY", "HULU", "DISNEY+", "HBO MAX", "APPLE TV", "YOUTUBE", "STEAM", "PLAYSTATION", "XBOX",
        "CHASE", "BANK OF AMERICA", "WELLS FARGO", "CAPITAL ONE", "CITI", "DISCOVER", "AMEX", "AMERICAN EXPRESS",
        "VENMO", "CASH APP", "PAYPAL", "ZELLE", "ROBINHOOD", "FIDELITY", "VANGUARD", "SCHWAB", "COINBASE"
    )
}
