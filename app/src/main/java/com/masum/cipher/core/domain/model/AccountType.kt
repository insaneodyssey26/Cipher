package com.masum.cipher.core.domain.model

enum class AccountType(
    val key: String,
    val displayName: String,
    val defaultIcon: String
) {
    BANK("BANK", "Bank Account", "Landmark"),
    CASH("CASH", "Cash / Physical", "Wallet"),
    CREDIT_CARD("CREDIT_CARD", "Credit Card", "CreditCard"),
    SAVINGS("SAVINGS", "Savings Account", "PiggyBank"),
    INVESTMENT("INVESTMENT", "Investment", "TrendingUp"),
    WALLET("WALLET", "Digital Wallet", "Coins"),
    OTHER("OTHER", "Other Account", "Layers");

    companion object {
        fun fromKey(key: String): AccountType {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: BANK
        }
    }
}
