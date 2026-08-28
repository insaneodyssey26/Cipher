package com.masum.cipher.ui.dashboard

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

class DashboardContract {

    sealed class Intent : UiIntent {
        object LoadDashboard : Intent()
        data class DeleteTransaction(val transaction: TransactionEntity) : Intent()
        data class UpdateTransaction(val transaction: TransactionEntity) : Intent()
        data class RestoreTransaction(val transaction: TransactionEntity) : Intent()
        data class AddTransaction(val transaction: TransactionEntity) : Intent()
        data class SearchTransactions(val query: String) : Intent()
        data class FilterTransactions(val filter: FilterType) : Intent()
        data class SetDashboardFilter(val filter: DashboardFilter) : Intent()
        object ResetDashboardFilter : Intent()
        data class SetTimePeriod(val period: com.masum.cipher.core.domain.model.TimePeriod, val customStart: Long? = null, val customEnd: Long? = null) : Intent()
        data class UpdateDraftTransaction(val transaction: TransactionEntity?) : Intent()
        data class SaveCategoryRule(val merchantName: String, val category: String) : Intent()
        object DismissCategoryRulePrompt : Intent()
        data class ApproveSubscription(val subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) : Intent()
        data class SkipSubscription(val subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) : Intent()
        data class UpdateMonthlyBudget(val budget: Double) : Intent()
    }

    enum class FilterType { ALL, INCOME, EXPENSE }

    data class State(
        val isLoading: Boolean = true,
        val transactions: List<TransactionEntity> = emptyList(),
        val pendingSubscriptions: List<com.masum.cipher.core.data.local.entity.SubscriptionEntity> = emptyList(),
        val hasAnyTransactions: Boolean = false,
        val searchQuery: String = "",
        val activeFilter: FilterType = FilterType.ALL,
        val filter: DashboardFilter = DashboardFilter(),
        val selectedTimePeriod: com.masum.cipher.core.domain.model.TimePeriod = com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH,
        val selectedTimeRange: com.masum.cipher.core.domain.model.TimeRange = com.masum.cipher.core.domain.model.TimeRange.from(com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH),
        val totalBalance: Double = 0.0,
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val thisMonthExpenses: Double = 0.0,
        val monthlyBudget: Double = 0.0,
        val expenseComparisonPercent: Double? = null,
        val expenseComparisonLabel: String? = null,
        val previousPeriodExpenses: Double? = null,
        val velocity: VelocityData = VelocityData(),
        val categories: List<CategoryData> = emptyList(),
        val draftTransaction: TransactionEntity? = null,
        val promptCategoryRuleFor: TransactionEntity? = null
    ) : UiState

    data class VelocityData(
        val currentWeekAvg: Double = 0.0,
        val lastWeekAvg: Double = 0.0,
        val trendPercentage: Double = 0.0
    )

    data class Point(val x: Float, val y: Float, val timestamp: Long)

    @androidx.compose.runtime.Immutable
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
