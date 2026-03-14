package com.example.cipherspend.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "merchant_aliases")
@Serializable
data class MerchantAliasEntity(
    @PrimaryKey
    val rawName: String,
    val cleanName: String,
    val isUserDefined: Boolean = false
)
