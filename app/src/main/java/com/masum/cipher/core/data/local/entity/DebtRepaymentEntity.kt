package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "debt_repayments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["debtId"]),
        Index(value = ["timestamp"])
    ]
)
@Serializable
data class DebtRepaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val debtId: Long,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val accountId: Long? = null,
    val note: String? = null,
    val transactionId: Long? = null
)
