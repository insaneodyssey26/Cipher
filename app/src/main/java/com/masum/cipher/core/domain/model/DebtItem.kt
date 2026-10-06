package com.masum.cipher.core.domain.model

import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity

data class DebtItem(
    val debt: DebtEntity,
    val repayments: List<DebtRepaymentEntity> = emptyList(),
    val accountName: String? = null
) {
    val id: Long get() = debt.id
    val personName: String get() = debt.personName
    val totalAmount: Double get() = debt.amount
    val remainingAmount: Double get() = debt.remainingAmount
    val repaidAmount: Double get() = (debt.amount - debt.remainingAmount).coerceAtLeast(0.0)
    val type: DebtType get() = DebtType.fromKey(debt.type)
    val isSettled: Boolean get() = debt.isSettled || remainingAmount <= 0.001
    val dueDate: Long? get() = debt.dueDate
    val createdAt: Long get() = debt.createdAt
    val note: String? get() = debt.note
    val accountId: Long? get() = debt.accountId
    val interestRate: Double get() = debt.interestRate
    val progress: Float get() = if (debt.amount > 0) (repaidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 1f
}
