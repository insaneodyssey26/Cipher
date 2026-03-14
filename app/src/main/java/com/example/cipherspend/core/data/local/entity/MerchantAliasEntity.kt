package com.example.cipherspend.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_aliases")
data class MerchantAliasEntity(
    @PrimaryKey
    val rawName: String,
    val cleanName: String,
    val isUserDefined: Boolean = false
)
