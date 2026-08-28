package com.masum.cipher.core.sms.config

import java.util.regex.Pattern

object TransactionPatterns {
    val AMOUNT_PATTERNS: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|transfer(?:red)?|txn|transaction)\\s*(?:by|with|of|for|to)?\\s*(?:₹|rs\\.?|inr)?\\s*(?:/-\\s*)?([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)([\\d,]+\\.?\\d{0,2})\\s*(?:rs\\.?|inr|₹)?\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded)"),
        Pattern.compile("(?i)(?:₹|rs\\.?|inr|amt|amount)\\s*(?:/-\\s*)?([\\d,]+\\.?\\d{0,2})"),
        Pattern.compile("(?i)(?<!a/c |acc |account |ending |ref |no |id )([\\d,]+\\.\\d{2})(?!\\d)")
    )

    val EXCLUSION_KEYWORDS: List<String> = listOf(
        "otp", "verification code", "secret code", "tollfree", "helpline", "dial", "win", "won", "offered", "validity",
        "plan", "recharge", "expires", "pack", "unlimited", "data", "exclusive", "discount", "reward", "points",
        "eligible", "pre-approved", "credit limit", "claim", "offer", "limited period", "active"
    )

    val TRANSACTION_EVIDENCE_PATTERNS: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:a/c|acc|account|ending|card|vpa|acct)\\s*(?:no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:txn|ref|rrn|id|vpa)\\b"),
        Pattern.compile("(?i)[a-z0-9.]+@[a-z]{3,}"),
        Pattern.compile("(?i)linked to"),
        Pattern.compile("(?i)avl bal"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)debited from"),
        Pattern.compile("(?i)credited to"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)paid Rs"),
        Pattern.compile("(?i)transferred to"),
        Pattern.compile("(?i)(?:sent|paid|spent|received)\\s*(?:rs\\.?|inr|₹)?\\s*\\d+"),
        Pattern.compile("(?i)\\d+\\s*(?:rs\\.?|inr|₹)?\\s*(?:sent|paid|spent|received)")
    )

    val INTENT_KEYWORDS: List<String> = listOf(
        "rs", "rs.", "inr", "debited", "spent", "paid", "credited", "received", "txn", "transaction", "amount", "amt", "sent"
    )

    val ACCOUNT_EXCLUSION_PATTERN: Pattern = Pattern.compile(
        "(?i)(?:a/c|acc|account|ending|no|id|ref)\\s*(?:no\\.?)?\\s*[:#-]?\\s*\\d+"
    )

    val STRUCTURAL_MERCHANT_PATTERNS: List<Pattern> = listOf(
        Pattern.compile("(?i)\\b([A-Za-z][A-Za-z0-9.]{2,})@(?:okaxis|okicici|okhdfcbank|ybl|ibl|axl|paytm|upi|waicici|wahdfc|indus|fbl|aubank|kotak|hsbc|sbi|icici|hdfc|axis|airtel|jio|oksbi)\\b"),
        Pattern.compile("(?i)/\\d{5,}/([^/\\d\\s][^/]+)(?:/|$)"),
        Pattern.compile("(?i)\\bfrom\\s+([A-Za-z][A-Za-z0-9\\s&.]{2,}?)(?=\\s+on|\\s+using|\\s+via|\\s+ref|\\s+to|\\.|$)"),
        Pattern.compile("(?i)(?:at|to|towards|info|vpa|into|merchant|payee)\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\s+at|\\s+via|\\s+ref|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\.|$)"),
        Pattern.compile("(?i)used\\s+at\\s+([^\\d\\s][^;.]+?)(?=\\s+on|\\s+using|\\.|$)")
    )

    val BRAND_DICTIONARY: List<String> = listOf(
        "AMAZON", "FLIPKART", "MYNTRA", "AJIO", "MEESHO", "NYKAA", "RELIANCE", "CROMA",
        "BLINKIT", "BIGBASKET", "ZEPTO", "INSTAMART", "JIOMART", "ZOMATO", "SWIGGY",
        "EATFIT", "DOMINOS", "KFC", "PIZZA HUT", "STARBUCKS", "MCDONALDS", "BURGER KING",
        "UBER", "OLA", "RAPIDO", "INDIGO", "AIR INDIA", "SPICEJET", "IRCTC", "REDBUS",
        "MAKEMYTRIP", "GOIBIBO", "BOOKMYSHOW", "NETFLIX", "SPOTIFY", "HOTSTAR", "PRIME VIDEO",
        "PVR", "INOX", "STEAM", "APOLLO", "TATA 1MG", "PHARMEASY", "NETMEDS", "PRACTO",
        "AIRTEL", "JIO", "VODAFONE", "VI", "TATA PLAY", "GOOGLE", "PAYTM", "PHONEPE",
        "CRED", "GROWW", "ZERODHA", "UPSTOX", "NAVI", "SLICE", "DUNZO", "FASTTAG",
        "CLEARTRIP", "LENSKART", "FIRSTCRY", "URBAN COMPANY", "MAMAEARTH", "BOAT", 
        "INDMONEY", "KUVERA", "FI MONEY", "JUPITER", "DECATHLON", "PAYZAPP", "MOBIKWIK", "FREECHARGE"
    )

    val DEBIT_KEYWORDS: List<String> = listOf("debited", "spent", "withdrawn", "charged", "deducted")
    val CREDIT_KEYWORDS: List<String> = listOf("credited", "deposited", "refunded", "incoming", "cashback", "salary", "received")
    val MERCHANT_FALSE_POSITIVE_PREFIXES: List<String> = listOf("your", "a/c", "account", "bank", "the ", "my ")
}
