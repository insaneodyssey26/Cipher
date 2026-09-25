package com.masum.cipher.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "goals")
@Serializable
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val colorHex: Long,
    val iconName: String,
    val createdAt: Long = System.currentTimeMillis()
)
