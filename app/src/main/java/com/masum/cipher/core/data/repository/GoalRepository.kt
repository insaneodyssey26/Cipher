package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.GoalDao
import com.masum.cipher.core.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun getGoalById(id: Long): GoalEntity? = goalDao.getGoalById(id)

    suspend fun createGoal(
        name: String,
        targetAmount: Double,
        savedAmount: Double,
        colorHex: Long,
        iconName: String
    ): Long {
        val entity = GoalEntity(
            name = name,
            targetAmount = targetAmount,
            savedAmount = savedAmount,
            colorHex = colorHex,
            iconName = iconName
        )
        return goalDao.insertGoal(entity)
    }

    suspend fun updateGoal(
        id: Long,
        name: String,
        targetAmount: Double,
        savedAmount: Double,
        colorHex: Long,
        iconName: String
    ) {
        val existing = goalDao.getGoalById(id) ?: return
        val updated = existing.copy(
            name = name,
            targetAmount = targetAmount,
            savedAmount = savedAmount,
            colorHex = colorHex,
            iconName = iconName
        )
        goalDao.updateGoal(updated)
    }

    suspend fun adjustSavedAmount(id: Long, delta: Double) {
        val existing = goalDao.getGoalById(id) ?: return
        val newAmount = (existing.savedAmount + delta).coerceAtLeast(0.0)
        goalDao.updateSavedAmount(id, newAmount)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    suspend fun restoreGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }
}
