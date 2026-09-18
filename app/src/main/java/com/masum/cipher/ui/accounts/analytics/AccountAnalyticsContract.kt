package com.masum.cipher.ui.accounts.analytics

import androidx.annotation.StringRes
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.domain.model.AccountItem
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class AccountAnalyticsPeriod(@StringRes val labelRes: Int) {
    DAYS_7(R.string.account_analytics_period_7d),
    DAYS_30(R.string.account_analytics_period_30d),
    DAYS_90(R.string.account_analytics_period_90d),
    YEAR_1(R.string.account_analytics_period_1y),
    ALL(R.string.account_analytics_period_all)
}

data class SpendingVelocityData(
    val avgDailySpend: Double = 0.0,
    val avgWeeklySpend: Double = 0.0,
    val velocityTrendPercentage: Double = 0.0,
    val largestOutflow: Double = 0.0
)

data class WeekdaySpendData(
    val dayName: String,
    val amount: Double,
    val percentage: Float,
    val isMax: Boolean
)

object AccountAnalyticsContract {

    data class State(
        val account: AccountEntity? = null,
        val accountItem: AccountItem? = null,
        val analyticsPeriod: AccountAnalyticsPeriod = AccountAnalyticsPeriod.DAYS_30,
        val currencySymbol: String = "₹",
        val isHapticsEnabled: Boolean = true,
        val isLoading: Boolean = true,
        val isPro: Boolean = false,
        val periodInflow: Double = 0.0,
        val periodOutflow: Double = 0.0,
        val periodNetFlow: Double = 0.0,
        val periodTxCount: Int = 0,
        val expenseTrendPoints: ImmutableList<DashboardContract.Point> = persistentListOf(),
        val incomeTrendPoints: ImmutableList<DashboardContract.Point> = persistentListOf(),
        val netFlowTrendPoints: ImmutableList<DashboardContract.Point> = persistentListOf(),
        val categoryBreakdown: ImmutableList<DashboardContract.CategoryData> = persistentListOf(),
        val topMerchants: ImmutableList<InsightsContract.MerchantData> = persistentListOf(),
        val spendingVelocity: SpendingVelocityData = SpendingVelocityData(),
        val weekdayBreakdown: ImmutableList<WeekdaySpendData> = persistentListOf(),
        val customCategories: ImmutableList<CustomCategoryEntity> = persistentListOf()
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadAccount(val accountId: Long) : Intent
        data class SelectPeriod(val period: AccountAnalyticsPeriod) : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToPro : Effect
    }
}
