package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["type"]),
        Index(value = ["isSettled"]),
        Index(value = ["accountId"]),
        Index(value = ["dueDate"])
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val personName: String,
    val amount: Double,
    val remainingAmount: Double,
    val type: String,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String? = null,
    val accountId: Long? = null,
    val isSettled: Boolean = false,
    val interestRate: Double = 0.0,
    val transactionId: Long? = null
)
