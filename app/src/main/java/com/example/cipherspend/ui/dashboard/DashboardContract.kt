package com.example.cipherspend.ui.dashboard

import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.mvi.UiEffect
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState

class DashboardContract {

    sealed class Intent : UiIntent {
        object LoadDashboard : Intent()
        data class DeleteTransaction(val transaction: TransactionEntity) : Intent()
        data class UpdateTransaction(val transaction: TransactionEntity) : Intent()
        data class RestoreTransaction(val transaction: TransactionEntity) : Intent()
        data class AddTransaction(val transaction: TransactionEntity) : Intent()
        data class SearchTransactions(val query: String) : Intent()
        data class FilterTransactions(val filter: FilterType) : Intent()
    }

    enum class FilterType { ALL, INCOME, EXPENSE }

    data class State(
        val isLoading: Boolean = true,
        val transactions: List<TransactionEntity> = emptyList(),
        val searchQuery: String = "",
        val activeFilter: FilterType = FilterType.ALL,
        val totalBalance: Double = 0.0,
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val velocity: VelocityData = VelocityData(),
        val trendPoints: List<Point> = emptyList(),
        val categories: List<CategoryData> = emptyList()
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
        val color: Long
    )

    sealed class Effect : UiEffect {
        data class ShowError(val message: String) : Effect()
        data class ShowUndoDelete(val transaction: TransactionEntity) : Effect()
    }
}
