package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "subscriptions")
@Serializable
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val category: String,
    val frequencyDays: Int,
    val nextExpectedDate: Long
)
