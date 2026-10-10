package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.GoalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsGoalTest {

    @Test
    fun testGoalProgressFractionNormal() {
        val goal = GoalEntity(
            id = 1,
            name = "New Phone",
            targetAmount = 1000.0,
            savedAmount = 450.0,
            colorHex = 0xFF3B82F6,
            iconName = "Smartphone"
        )
        val fraction = if (goal.targetAmount > 0) {
            (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        assertEquals(0.45f, fraction, 0.001f)
    }

    @Test
    fun testGoalProgressFractionZeroTarget() {
        val goal = GoalEntity(
            id = 2,
            name = "Empty Target",
            targetAmount = 0.0,
            savedAmount = 100.0,
            colorHex = 0xFF10B981,
            iconName = "Target"
        )
        val fraction = if (goal.targetAmount > 0) {
            (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        assertEquals(0f, fraction, 0.0f)
    }

    @Test
    fun testGoalProgressFractionExceededTargetClamped() {
        val goal = GoalEntity(
            id = 3,
            name = "Overachieved",
            targetAmount = 500.0,
            savedAmount = 750.0,
            colorHex = 0xFFF59E0B,
            iconName = "Award"
        )
        val fraction = if (goal.targetAmount > 0) {
            (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        assertEquals(1.0f, fraction, 0.0f)
    }

    @Test
    fun testGoalPercentageCalculation() {
        val goal = GoalEntity(
            id = 4,
            name = "Vacation",
            targetAmount = 2000.0,
            savedAmount = 1500.0,
            colorHex = 0xFF8B5CF6,
            iconName = "Plane"
        )
        val percentage = if (goal.targetAmount > 0) {
            ((goal.savedAmount / goal.targetAmount) * 100).toInt()
        } else 0

        assertEquals(75, percentage)
    }

    @Test
    fun testGoalRemainingAmount() {
        val goal = GoalEntity(
            id = 5,
            name = "Emergency Fund",
            targetAmount = 5000.0,
            savedAmount = 3200.0,
            colorHex = 0xFFEC4899,
            iconName = "Shield"
        )
        val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
        assertEquals(1800.0, remaining, 0.001)

        val completedGoal = goal.copy(savedAmount = 6000.0)
        val completedRemaining = (completedGoal.targetAmount - completedGoal.savedAmount).coerceAtLeast(0.0)
        assertEquals(0.0, completedRemaining, 0.0)
    }

    @Test
    fun testGoalDepositAndWithdrawalMath() {
        var goal = GoalEntity(
            id = 6,
            name = "Laptop",
            targetAmount = 1200.0,
            savedAmount = 400.0,
            colorHex = 0xFF6366F1,
            iconName = "Laptop"
        )

        goal = goal.copy(savedAmount = goal.savedAmount + 300.0)
        assertEquals(700.0, goal.savedAmount, 0.001)
        assertFalse(goal.savedAmount >= goal.targetAmount)

        goal = goal.copy(savedAmount = goal.savedAmount + 500.0)
        assertEquals(1200.0, goal.savedAmount, 0.001)
        assertTrue(goal.savedAmount >= goal.targetAmount)

        val withdrawAmount = 200.0
        goal = goal.copy(savedAmount = (goal.savedAmount - withdrawAmount).coerceAtLeast(0.0))
        assertEquals(1000.0, goal.savedAmount, 0.001)
    }
}
