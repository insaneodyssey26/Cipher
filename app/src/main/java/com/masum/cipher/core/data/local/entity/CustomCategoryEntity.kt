package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "custom_categories",
    indices = [Index(value = ["name"], unique = true)]
)
@Serializable
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val createdAt: Long = System.currentTimeMillis()
)
