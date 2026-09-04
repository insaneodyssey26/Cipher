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
        data class SetTimePeriod(val period: com.masum.cipher.core.domain.model.TimePeriod, val customStart: Long? = null, val customEnd: Long? = null) : Intent()
        data class UpdateDraftTransaction(val transaction: TransactionEntity?) : Intent()
        data class SaveCategoryRule(val merchantName: String, val category: String) : Intent()
        object DismissCategoryRulePrompt : Intent()
        data class SaveSubscription(val merchant: String, val amount: Double, val category: String, val frequencyDays: Int, val nextExpectedDate: Long) : Intent()
        data class DeleteSubscription(val merchant: String) : Intent()
        data class IgnoreSubscription(val merchant: String) : Intent()
        data class RestoreSubscription(val subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) : Intent()
        data class SetCategoryBudget(val category: String, val limit: Double) : Intent()
        data class SetDynamicBudget(val enabled: Boolean) : Intent()
        data class SaveTransactionSplits(val transactionId: Long, val splits: List<com.masum.cipher.core.domain.model.SplitParticipant>) : Intent()
    }

    data class MerchantData(val merchant: String, val amount: Double, val count: Int)
    data class MonthlySummary(val income: Double, val expense: Double, val savingsRate: Float)
    data class DayOfWeekData(val dayName: String, val amount: Double, val isMax: Boolean)
    data class PeakHourData(val label: String, val amount: Double, val percentage: Float)

    data class State(
        val isLoading: Boolean = true,
        val currencyCode: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().code,
        val currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
        val selectedTimePeriod: com.masum.cipher.core.domain.model.TimePeriod = com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH,
        val selectedTimeRange: com.masum.cipher.core.domain.model.TimeRange = com.masum.cipher.core.domain.model.TimeRange.from(com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH),
        val spendingVelocity: DashboardContract.VelocityData = DashboardContract.VelocityData(),
        val netWorthHistory: List<DashboardContract.Point> = emptyList(),
        val expenseTrendHistory: List<DashboardContract.Point> = emptyList(),
        val incomeTrendHistory: List<DashboardContract.Point> = emptyList(),
        val netFlowTrendHistory: List<DashboardContract.Point> = emptyList(),
        val calendarHeatmap: Map<Long, Double> = emptyMap(),
        val categoryBreakdown: List<DashboardContract.CategoryData> = emptyList(),
        val detectedSubscriptions: List<SubscriptionDetector.Subscription> = emptyList(),
        val allTransactions: List<TransactionEntity> = emptyList(),
        val selectedDayTimestamp: Long? = null,
        val topMerchants: List<MerchantData> = emptyList(),
        val monthlySummary: MonthlySummary = MonthlySummary(0.0, 0.0, 0f),
        val weekdayBreakdown: List<DayOfWeekData> = emptyList(),
        val peakHours: List<PeakHourData> = emptyList(),
        val noSpendStreak: Int = 0,
        val avgTransactionSize: Double = 0.0,
        val draftTransaction: TransactionEntity? = null,
        val promptCategoryRuleFor: TransactionEntity? = null
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowUndoDelete(val transaction: TransactionEntity) : Effect()
        data class ShowUndoSubscriptionDelete(val subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) : Effect()
    }
}
