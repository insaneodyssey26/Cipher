package com.masum.cipher.core.domain.model

import com.masum.cipher.core.data.local.entity.AccountEntity

data class AccountItem(
    val entity: AccountEntity,
    val currentBalance: Double,
    val transactionCount: Int = 0
) {
    val id: Long get() = entity.id
    val name: String get() = entity.name
    val type: AccountType get() = AccountType.fromKey(entity.type)
    val colorHex: Long get() = entity.colorHex
    val iconName: String get() = entity.iconName
    val isDefault: Boolean get() = entity.isDefault
    val accountNumberLast4: String? get() = entity.accountNumberLast4
}
