package com.example.cipherspend.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["timestamp"])]
)
@Serializable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val currency: String,
    val timestamp: Long,
    val category: String,
    val rawSms: String?,
    val isIncome: Boolean
)
