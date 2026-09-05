package com.masum.cipher.core.sms.config

import java.util.regex.Pattern

object TransactionPatterns {
    val ACCOUNT_EXCLUSION_PATTERN: Pattern = Pattern.compile(
        "(?i)(?:a/c|acc|account|ending|no|id|ref)\\s*(?:no\\.?)?\\s*[:#-]?\\s*\\d+"
    )

    val DEBIT_KEYWORDS: List<String> = listOf("debited", "spent", "withdrawn", "charged", "deducted")
    val CREDIT_KEYWORDS: List<String> = listOf("credited", "deposited", "refunded", "incoming", "cashback", "salary", "received")
    val MERCHANT_FALSE_POSITIVE_PREFIXES: List<String> = listOf("your", "a/c", "account", "bank", "the ", "my ")
}
