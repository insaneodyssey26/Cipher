package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"])
    ]
)
@Serializable
data class TransactionSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val name: String,
    val amount: Double,
    val isPaid: Boolean = false,
    val isCurrentUser: Boolean = false
)
