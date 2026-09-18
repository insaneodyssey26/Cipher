package com.masum.cipher.core.domain.model

import androidx.annotation.StringRes
import com.masum.cipher.R

enum class AccountType(
    val key: String,
    val displayName: String,
    val defaultIcon: String,
    @StringRes val labelRes: Int
) {
    BANK("BANK", "Bank Account", "Landmark", R.string.account_type_bank),
    CASH("CASH", "Cash / Physical", "Wallet", R.string.account_type_cash),
    CREDIT_CARD("CREDIT_CARD", "Credit Card", "CreditCard", R.string.account_type_credit_card),
    SAVINGS("SAVINGS", "Savings Account", "PiggyBank", R.string.account_type_savings),
    INVESTMENT("INVESTMENT", "Investment", "Briefcase", R.string.account_type_investment),
    WALLET("WALLET", "Digital Wallet", "Smartphone", R.string.account_type_wallet),
    OTHER("OTHER", "Other Account", "Layers", R.string.account_type_other);

    companion object {
        fun fromKey(key: String): AccountType {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: BANK
        }
    }
}
