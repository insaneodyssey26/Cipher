package com.example.cipherspend.ui.insights

import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.mvi.UiEffect
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState
import com.example.cipherspend.ui.dashboard.DashboardContract

class InsightsContract {
    sealed class Intent : UiIntent {
        object LoadInsights : Intent()
        data class SelectDay(val timestamp: Long?) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val spendingVelocity: DashboardContract.VelocityData = DashboardContract.VelocityData(),
        val netWorthHistory: List<DashboardContract.Point> = emptyList(),
        val calendarHeatmap: Map<Long, Double> = emptyMap(),
        val categoryBreakdown: List<DashboardContract.CategoryData> = emptyList(),
        val allTransactions: List<TransactionEntity> = emptyList(),
        val selectedDayTimestamp: Long? = null
    ) : UiState {
        val selectedDayTransactions: List<TransactionEntity>
            get() = selectedDayTimestamp?.let { timestamp ->
                allTransactions.filter { tx ->
                    val txCal = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    val targetCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
                    txCal.get(java.util.Calendar.YEAR) == targetCal.get(java.util.Calendar.YEAR) &&
                    txCal.get(java.util.Calendar.DAY_OF_YEAR) == targetCal.get(java.util.Calendar.DAY_OF_YEAR)
                }
            } ?: emptyList()
    }

    sealed class Effect : UiEffect
}
