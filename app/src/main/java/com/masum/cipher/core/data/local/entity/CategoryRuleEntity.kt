package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "category_rules")
@Serializable
data class CategoryRuleEntity(
    @PrimaryKey
    val merchantName: String,
    val customCategory: String
)
