package com.masum.cipher.core.data.local.dao

data class AccountFlowTotals(
    val accountId: Long?,
    val income: Double,
    val expense: Double,
    val transactionCount: Int
)
