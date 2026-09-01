package com.masum.cipher.core.sms.region

import java.util.regex.Pattern

object EuroParserRules : RegionParserRules {
    override val regionCode: String = "EU"
    override val defaultCurrency: String = "EUR"

    override val amountPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|purchase of|charge of|payment of|transfer(?:red)?|txn|transaction|virement|paiement|prélèvement)\\s*(?:by|with|of|for|to|de)?\\s*€\\s*([\\d,.]+)"),
        Pattern.compile("(?i)€\\s*([\\d,.]+)\\s*(?:debited|spent|charged|paid|withdrawn|sent|credited|received|deposited|added|refunded|authorized|approved|payé|viré)"),
        Pattern.compile("(?i)(?:€|eur|amt|amount)\\s*([\\d,.]+)"),
        Pattern.compile("(?i)(?<!ending |card |ref |no |id )€([\\d,.]+)")
    )

    override val exclusionKeywords: List<String> = listOf(
        "security code", "verification code", "one-time passcode", "code de sécurité", "sicherheitscode", "temporary password", "toll-free", "helpline",
        "reward points", "credit limit", "pre-approved", "special offer", "promotional", "claim your", "free trial"
    )

    override val evidencePatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:card|account|ending|acct|carte|konto|compte|iban)\\s*(?:in|no\\.?)?\\s*[:#-]?\\s*[x*]*\\d{2,4}"),
        Pattern.compile("(?i)\\b(?:purchase|authorized|approved|declined|sent|received|paid|withdrawn|charge|deposited|refunded|virement|paiement|sepa|ideal)\\b"),
        Pattern.compile("(?i)paid to"),
        Pattern.compile("(?i)spent at"),
        Pattern.compile("(?i)charged at"),
        Pattern.compile("(?i)purchased at"),
        Pattern.compile("(?i)sent to"),
        Pattern.compile("(?i)received from"),
        Pattern.compile("(?i)payment to"),
        Pattern.compile("(?i)available balance"),
        Pattern.compile("(?i)solde disponible"),
        Pattern.compile("(?i)€\\s*[\\d,.]+")
    )

    override val intentKeywords: List<String> = listOf(
        "€", "eur", "spent", "paid", "charged", "purchase", "debited", "credited", "received", "sent", "authorized", "approved", "payment", "sepa", "virement"
    )

    override val structuralMerchantPatterns: List<Pattern> = listOf(
        Pattern.compile("(?i)(?:at|to|towards|into|chez|bei|a|merchant|payee)\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\s+for|\\.|$)"),
        Pattern.compile("(?i)(?:purchased|spent|charged|authorized|payé)\\s+at\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+with|\\s+on|\\s+using|\\s+via|\\s+card|\\.|$)"),
        Pattern.compile("(?i)sent\\s+to\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+on|\\s+using|\\s+for|\\.|$)"),
        Pattern.compile("(?i)paid\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+€|\\s+for|\\s+on|\\.|$)"),
        Pattern.compile("(?i)from\\s+([A-Za-z0-9&'., -]{2,30}?)(?=\\s+sent|\\s+paid|\\s+on|\\.|$)")
    )

    override val brandDictionary: List<String> = listOf(
        "CARREFOUR", "LIDL", "ALDI", "AUCHAN", "E.LECLERC", "INTERMARCHE", "EDEKA", "REWE", "MERCADONA", "ALBERT HEIJN",
        "AMAZON", "IKEA", "DECATHLON", "ZARA", "H&M", "SEPHORA", "UBER", "BOLT", "DELIVEROO", "JUST EAT", "UBER EATS",
        "TOTALENERGIES", "SHELL", "BP", "ENI", "REPSOL", "NETFLIX", "SPOTIFY", "DISNEY+", "STEAM", "KLARNA", "PAYPAL"
    )
}
