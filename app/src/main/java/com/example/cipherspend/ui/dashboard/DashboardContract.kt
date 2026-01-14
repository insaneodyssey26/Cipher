package com.example.cipherspend.ui.dashboard

import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.mvi.UiEffect
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState

class DashboardContract {

    sealed class Intent : UiIntent {
        object LoadDashboard : Intent()
        data class DeleteTransaction(val transaction: TransactionEntity) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val transactions: List<TransactionEntity> = emptyList(),
        val totalBalance: Double = 0.0,
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0
    ) : UiState

    data class VelocityData(
        val currentWeekAvg: Double = 0.0,
        val lastWeekAvg: Double = 0.0,
        val trendPercentage: Double = 0.0
    )

    data class Point(val x: Float, val y: Float, val timestamp: Long)

    data class CategoryData(
        val category: String,
        val amount: Double,
        val percentage: Float,
        val color: Long,
        val subCategories: List<CategoryData> = emptyList()
    )

    sealed class Effect : UiEffect {
        data class ShowError(val message: String) : Effect()
    }
}