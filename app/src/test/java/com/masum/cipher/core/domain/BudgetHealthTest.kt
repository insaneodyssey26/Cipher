package com.masum.cipher.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetHealthTest {

    @Test
    fun testBudgetUtilizationPercentage() {
        val budget = 2000.0
        val spent = 800.0

        val utilization = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        assertEquals(0.40f, utilization, 0.001f)

        val percentage = if (budget > 0) ((spent / budget) * 100).toInt() else 0
        assertEquals(40, percentage)
    }

    @Test
    fun testBudgetOverspendingDetection() {
        val budget = 1500.0
        val spent = 1850.0

        val isOverBudget = spent > budget && budget > 0
        assertTrue(isOverBudget)

        val clampedUtilization = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        assertEquals(1.0f, clampedUtilization, 0.0f)

        val overspendAmount = (spent - budget).coerceAtLeast(0.0)
        assertEquals(350.0, overspendAmount, 0.001)
    }

    @Test
    fun testDailyAllowanceCalculation() {
        val totalBudget = 3000.0
        val spentSoFar = 1200.0
        val remainingBudget = (totalBudget - spentSoFar).coerceAtLeast(0.0)
        val remainingDays = 15

        val dailyAllowance = if (remainingDays > 0) remainingBudget / remainingDays else remainingBudget
        assertEquals(120.0, dailyAllowance, 0.001)
    }

    @Test
    fun testDailyAllowanceZeroRemainingDays() {
        val remainingBudget = 500.0
        val remainingDays = 0

        val dailyAllowance = if (remainingDays > 0) remainingBudget / remainingDays else remainingBudget
        assertEquals(500.0, dailyAllowance, 0.001)
    }

    @Test
    fun testDailySpentAgainstAllowance() {
        val dailyAllowance = 150.0
        val dailySpent = 90.0

        val safeToSpendToday = (dailyAllowance - dailySpent).coerceAtLeast(0.0)
        assertEquals(60.0, safeToSpendToday, 0.001)

        val isDailyOver = dailySpent > dailyAllowance && dailyAllowance > 0
        assertFalse(isDailyOver)

        val excessiveSpent = 210.0
        val isExcessiveOver = excessiveSpent > dailyAllowance && dailyAllowance > 0
        assertTrue(isExcessiveOver)
    }

    @Test
    fun testDynamicBudgetCalculationFromIncome() {
        val totalIncome = 5000.0
        val targetSavingsRate = 0.20
        val calculatedBudget = totalIncome * (1.0 - targetSavingsRate)

        assertEquals(4000.0, calculatedBudget, 0.001)

        val spent = 2500.0
        val remaining = (calculatedBudget - spent).coerceAtLeast(0.0)
        assertEquals(1500.0, remaining, 0.001)
    }
}
