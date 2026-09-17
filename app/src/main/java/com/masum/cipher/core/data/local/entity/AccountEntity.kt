package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "accounts")
@Serializable
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "BANK",
    val initialBalance: Double = 0.0,
    val colorHex: Long = 0xFF4F46E5,
    val iconName: String = "Landmark",
    val isDefault: Boolean = false,
    val accountNumberLast4: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
