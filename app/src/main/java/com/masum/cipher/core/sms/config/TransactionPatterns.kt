package com.masum.cipher.core.sms.config

import java.util.regex.Pattern

object TransactionPatterns {
    val ACCOUNT_EXCLUSION_PATTERN: Pattern = Pattern.compile(
        "(?i)(?:a/c|acc|account|ending|no|id|ref)\\s*(?:no\\.?)?\\s*[:#-]?\\s*\\d+"
    )

    val PROMOTIONAL_AMOUNT_PREFIX_PATTERN: Pattern = Pattern.compile(
        "(?i)\\b(?:from|starting\\s+(?:at|from)|up\\s*to|upto|save\\s+(?:up\\s*to)?|win\\s+(?:up\\s*to)?|cashback\\s+(?:of\\s+up\\s*to|up\\s*to|of)?|discount\\s+(?:of)?|min(?:imum)?\\s+(?:order|purchase|spend)\\s+(?:of)?|voucher\\s+worth|worth)\\s*$"
    )

    val PROMOTIONAL_PATTERNS: List<Pattern> = listOf(
        Pattern.compile("(?i)\\b(?:tap|click)\\s+(?:here\\s+)?to\\s+(?:learn|apply|know|avail|claim|redeem|buy|get|explore|scratch|activate|order)\\b"),
        Pattern.compile("(?i)\\b(?:apply|avail)\\s+(?:now|today|online|here|instantly)\\b"),
        Pattern.compile("(?i)\\bapply\\s+for\\s+(?:a\\s+)?(?:loan|credit\\s*card|card|personal\\s*loan)\\b"),
        Pattern.compile("(?i)\\bget\\s+(?:a\\s+)?(?:personal\\s*loan|instant\\s*loan|credit\\s*card|pre-?approved)\\b"),
        Pattern.compile("(?i)\\btake\\s+a\\s+(?:personal\\s*loan|loan)\\b"),
        Pattern.compile("(?i)\\b(?:instant|personal|home|car|gold|education)\\s+loan\\s+(?:of\\s+)?(?:up\\s*to\\s+)?(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])\\b"),
        Pattern.compile("(?i)\\bloan\\s+(?:of\\s+)?up\\s*to\\b"),
        Pattern.compile("(?i)\\bpre-?approved\\s+(?:loan|limit|offer|credit|card)\\b"),
        Pattern.compile("(?i)\\blifetime\\s+free\\s+(?:credit\\s*card|card)\\b"),
        Pattern.compile("(?i)\\bzero\\s+(?:cost\\s+emi|joining\\s+fee)\\b"),
        Pattern.compile("(?i)\\b(?:low|easy|no\\s+cost)\\s+emis?\\b"),
        Pattern.compile("(?i)\\bemis?\\s+(?:from|starting\\s+(?:at|from))\\s*(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])?\\s*[\\d,]+"),
        Pattern.compile("(?i)\\bstarting\\s+(?:at|from)\\s*(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])\\s*[\\d,]+"),
        Pattern.compile("(?i)\\b(?:save|win|earn|cashback\\s+(?:of\\s+)?)\\s*up\\s*to\\s*(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])?\\s*[\\d,]+"),
        Pattern.compile("(?i)\\bup\\s*to\\s*(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])?\\s*[\\d,]+\\s*(?:lakh|lac|cr|k|off|cashback|discount|bonus|loan|limit|voucher|reward)\\b"),
        Pattern.compile("(?i)\\b(?:flat|get\\s+flat)\\s+(?:₹|rs\\.?|inr|[$€£¥₩₱₫฿R])?\\s*[\\d,]+%?\\s*(?:off|cashback|discount)\\b"),
        Pattern.compile("(?i)\\b(?:use|coupon|promo)\\s+code\\b"),
        Pattern.compile("(?i)\\bmin(?:imum)?\\s+(?:order|purchase|spend|txn|transaction)\\s+of\\b"),
        Pattern.compile("(?i)\\bfund\\s+your\\s+(?:big\\s+)?purchase\\b"),
        Pattern.compile("(?i)\\b(?:scratch\\s+to\\s+win|scratch\\s+card)\\b"),
        Pattern.compile("(?i)\\byou(?:'re|\\s+are)?\\s+eligible\\s+for\\b"),
        Pattern.compile("(?i)\\blimited\\s+period\\s+offer\\b"),
        Pattern.compile("(?i)\\binterest\\s+rate\\s+starting\\b"),
        Pattern.compile("(?i)\\b(?:flexible|easy)\\s+tenure\\b"),
        Pattern.compile("(?i)\\bupgrade\\s+your\\s+(?:credit\\s+)?card\\b"),
        Pattern.compile("(?i)\\benjoy\\s+low\\s+emis?\\b")
    )

    val DEBIT_KEYWORDS: List<String> = listOf("debited", "spent", "withdrawn", "charged", "deducted")
    val CREDIT_KEYWORDS: List<String> = listOf("credited", "deposited", "refunded", "incoming", "cashback", "salary", "received", "paid you", "sent you", "transferred you", "payment from", "received from")
    val MERCHANT_FALSE_POSITIVE_PREFIXES: List<String> = listOf("your", "a/c", "account", "bank", "the ", "my ")
}
