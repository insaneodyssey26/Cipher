package com.masum.cipher.ui.insights

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState
import com.masum.cipher.ui.dashboard.DashboardContract

class InsightsContract {
    sealed class Intent : UiIntent {
        object LoadInsights : Intent()
        data class SelectDay(val timestamp: Long?) : Intent()
        data class DeleteTransaction(val transaction: TransactionEntity) : Intent()
        data class UpdateTransaction(val transaction: TransactionEntity) : Intent()
        data class RestoreTransaction(val transaction: TransactionEntity) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val spendingVelocity: DashboardContract.VelocityData = DashboardContract.VelocityData(),
        val netWorthHistory: List<DashboardContract.Point> = emptyList(),
        val calendarHeatmap: Map<Long, Double> = emptyMap(),
        val categoryBreakdown: List<DashboardContract.CategoryData> = emptyList(),
        val detectedSubscriptions: List<SubscriptionDetector.Subscription> = emptyList(),
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

    sealed class Effect : UiEffect {
        data class ShowUndoDelete(val transaction: TransactionEntity) : Effect()
    }
}
