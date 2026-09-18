package com.masum.cipher.ui.accounts.analytics

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.mvi.BaseViewModel
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class AccountAnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences
) : BaseViewModel<AccountAnalyticsContract.State, AccountAnalyticsContract.Intent, AccountAnalyticsContract.Effect>(
    initialState = AccountAnalyticsContract.State(
        currencySymbol = userPreferences.getCachedCurrencySymbol(),
        isPro = userPreferences.isCachedPro()
    )
) {
    private var currentAccountId: Long = savedStateHandle.get<Long>("accountId") ?: 0L
    private var allAccountTransactions: List<TransactionEntity> = emptyList()
    private var customCategoriesList: List<CustomCategoryEntity> = emptyList()

    init {
        if (currentAccountId != 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                val initialAccount = accountRepository.getAccountById(currentAccountId)
                if (initialAccount != null) {
                    updateState {
                        copy(
                            account = initialAccount,
                            isLoading = false
                        )
                    }
                }
            }
            observeAccountAnalytics(currentAccountId)
        }
    }

    private fun observeAccountAnalytics(accountId: Long) {
        currentAccountId = accountId
        viewModelScope.launch {
            combine(
                accountRepository.getAllAccountsWithBalancesFlow(),
                transactionRepository.getAllTransactions(),
                categoryRepository.getAllCustomCategoriesFlow(),
                userPreferences.settingsFlow
            ) { accountItems, allTxs, categories, settings ->
                val targetItem = accountItems.find { it.id == accountId } ?: accountItems.firstOrNull()
                val targetEntity = targetItem?.entity ?: accountRepository.getAccountById(accountId)

                val isTargetDefault = targetEntity?.isDefault ?: false
                val accTxs = if (targetEntity != null) {
                    allTxs.filter { tx ->
                        tx.accountId == targetEntity.id || (tx.accountId == null && isTargetDefault)
                    }
                } else {
                    emptyList()
                }

                allAccountTransactions = accTxs
                customCategoriesList = categories

                val computed = computeAnalytics(
                    transactions = accTxs,
                    period = currentState.analyticsPeriod,
                    customCategories = categories
                )

                currentState.copy(
                    account = targetEntity,
                    accountItem = targetItem,
                    currencySymbol = settings.currencySymbol,
                    isHapticsEnabled = settings.isHapticsEnabled,
                    isPro = settings.isPro,
                    isLoading = false,
                    periodInflow = computed.periodInflow,
                    periodOutflow = computed.periodOutflow,
                    periodNetFlow = computed.periodNetFlow,
                    periodTxCount = computed.periodTxCount,
                    expenseTrendPoints = computed.expenseTrendPoints,
                    incomeTrendPoints = computed.incomeTrendPoints,
                    netFlowTrendPoints = computed.netFlowTrendPoints,
                    categoryBreakdown = computed.categoryBreakdown,
                    topMerchants = computed.topMerchants,
                    spendingVelocity = computed.spendingVelocity,
                    weekdayBreakdown = computed.weekdayBreakdown,
                    customCategories = categories.toPersistentList()
                )
            }
            .flowOn(Dispatchers.Default)
            .collect { newState ->
                updateState { newState }
            }
        }
    }

    override fun handleIntent(intent: AccountAnalyticsContract.Intent) {
        when (intent) {
            is AccountAnalyticsContract.Intent.LoadAccount -> {
                observeAccountAnalytics(intent.accountId)
            }
            is AccountAnalyticsContract.Intent.SelectPeriod -> {
                val computed = computeAnalytics(
                    transactions = allAccountTransactions,
                    period = intent.period,
                    customCategories = customCategoriesList
                )
                updateState {
                    copy(
                        analyticsPeriod = intent.period,
                        periodInflow = computed.periodInflow,
                        periodOutflow = computed.periodOutflow,
                        periodNetFlow = computed.periodNetFlow,
                        periodTxCount = computed.periodTxCount,
                        expenseTrendPoints = computed.expenseTrendPoints,
                        incomeTrendPoints = computed.incomeTrendPoints,
                        netFlowTrendPoints = computed.netFlowTrendPoints,
                        categoryBreakdown = computed.categoryBreakdown,
                        topMerchants = computed.topMerchants,
                        spendingVelocity = computed.spendingVelocity,
                        weekdayBreakdown = computed.weekdayBreakdown
                    )
                }
            }
        }
    }

    private data class ComputedAnalytics(
        val periodInflow: Double,
        val periodOutflow: Double,
        val periodNetFlow: Double,
        val periodTxCount: Int,
        val expenseTrendPoints: ImmutableList<DashboardContract.Point>,
        val incomeTrendPoints: ImmutableList<DashboardContract.Point>,
        val netFlowTrendPoints: ImmutableList<DashboardContract.Point>,
        val categoryBreakdown: ImmutableList<DashboardContract.CategoryData>,
        val topMerchants: ImmutableList<InsightsContract.MerchantData>,
        val spendingVelocity: SpendingVelocityData,
        val weekdayBreakdown: ImmutableList<WeekdaySpendData>
    )

    private fun computeAnalytics(
        transactions: List<TransactionEntity>,
        period: AccountAnalyticsPeriod,
        customCategories: List<CustomCategoryEntity>
    ): ComputedAnalytics {
        val now = System.currentTimeMillis()
        val periodDurationMs = when (period) {
            AccountAnalyticsPeriod.DAYS_7 -> 7L * 86400000L
            AccountAnalyticsPeriod.DAYS_30 -> 30L * 86400000L
            AccountAnalyticsPeriod.DAYS_90 -> 90L * 86400000L
            AccountAnalyticsPeriod.YEAR_1 -> 365L * 86400000L
            AccountAnalyticsPeriod.ALL -> Long.MAX_VALUE
        }

        val periodStartTs = if (period == AccountAnalyticsPeriod.ALL) 0L else now - periodDurationMs
        val periodTxs = transactions.filter { it.timestamp >= periodStartTs }

        val inflow = periodTxs.filter { it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }.sumOf { it.amount }
        val outflow = periodTxs.filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }.sumOf { it.amount }
        val net = inflow - outflow

        val (expensePts, incomePts, netPts) = calculateTrendPoints(periodTxs, period, now)
        val catBreakdown = calculateCategories(periodTxs, customCategories)
        val merchants = calculateTopMerchants(periodTxs)
        val velocity = calculateVelocity(transactions, periodTxs, period, outflow, now, periodDurationMs)
        val weekdays = calculateWeekdayBreakdown(periodTxs)

        return ComputedAnalytics(
            periodInflow = inflow,
            periodOutflow = outflow,
            periodNetFlow = net,
            periodTxCount = periodTxs.size,
            expenseTrendPoints = expensePts.toPersistentList(),
            incomeTrendPoints = incomePts.toPersistentList(),
            netFlowTrendPoints = netPts.toPersistentList(),
            categoryBreakdown = catBreakdown.toPersistentList(),
            topMerchants = merchants.toPersistentList(),
            spendingVelocity = velocity,
            weekdayBreakdown = weekdays.toPersistentList()
        )
    }

    private fun calculateTrendPoints(
        transactions: List<TransactionEntity>,
        period: AccountAnalyticsPeriod,
        now: Long
    ): Triple<List<DashboardContract.Point>, List<DashboardContract.Point>, List<DashboardContract.Point>> {
        if (transactions.isEmpty()) return Triple(emptyList(), emptyList(), emptyList())

        val startCal = Calendar.getInstance().apply {
            when (period) {
                AccountAnalyticsPeriod.DAYS_7 -> timeInMillis = now - 6L * 86400000L
                AccountAnalyticsPeriod.DAYS_30 -> timeInMillis = now - 29L * 86400000L
                AccountAnalyticsPeriod.DAYS_90 -> timeInMillis = now - 89L * 86400000L
                AccountAnalyticsPeriod.YEAR_1 -> timeInMillis = now - 364L * 86400000L
                AccountAnalyticsPeriod.ALL -> {
                    val minTs = transactions.minOfOrNull { it.timestamp } ?: now
                    timeInMillis = minTs
                }
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dailyExpenses = mutableMapOf<Long, Double>()
        val dailyIncomes = mutableMapOf<Long, Double>()

        transactions.forEach { tx ->
            if (tx.category.equals("TRANSFER", ignoreCase = true)) return@forEach
            val txCal = Calendar.getInstance().apply {
                timeInMillis = tx.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val key = txCal.timeInMillis
            if (tx.isIncome) {
                dailyIncomes[key] = (dailyIncomes[key] ?: 0.0) + tx.amount
            } else {
                dailyExpenses[key] = (dailyExpenses[key] ?: 0.0) + tx.amount
            }
        }

        val expensePoints = mutableListOf<DashboardContract.Point>()
        val incomePoints = mutableListOf<DashboardContract.Point>()
        val netPoints = mutableListOf<DashboardContract.Point>()

        var index = 0f
        var runningExpense = 0.0
        var runningIncome = 0.0

        val cursorCal = Calendar.getInstance().apply { timeInMillis = startCal.timeInMillis }
        while (cursorCal.timeInMillis <= endCal.timeInMillis) {
            val exp = dailyExpenses[cursorCal.timeInMillis] ?: 0.0
            val inc = dailyIncomes[cursorCal.timeInMillis] ?: 0.0
            runningExpense += exp
            runningIncome += inc
            val runningNet = runningIncome - runningExpense

            expensePoints.add(DashboardContract.Point(index, runningExpense.toFloat(), cursorCal.timeInMillis))
            incomePoints.add(DashboardContract.Point(index, runningIncome.toFloat(), cursorCal.timeInMillis))
            netPoints.add(DashboardContract.Point(index, runningNet.toFloat(), cursorCal.timeInMillis))

            index += 1f
            cursorCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return Triple(expensePoints, incomePoints, netPoints)
    }

    private fun calculateCategories(
        transactions: List<TransactionEntity>,
        customCategories: List<CustomCategoryEntity>
    ): List<DashboardContract.CategoryData> {
        val expenses = transactions.filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        return expenses.groupBy { it.category }
            .map { entry ->
                val amount = entry.value.sumOf { it.amount }
                val resolvedCategory = CategoryHelper.resolveCategory(entry.key, customCategories)
                DashboardContract.CategoryData(
                    category = resolvedCategory.displayName,
                    amount = amount,
                    percentage = (amount / total).toFloat(),
                    color = resolvedCategory.color.toArgb().toLong()
                )
            }.sortedByDescending { it.amount }
    }

    private fun calculateTopMerchants(transactions: List<TransactionEntity>): List<InsightsContract.MerchantData> {
        return transactions.asSequence()
            .filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
            .groupBy { it.merchant.trim().ifEmpty { "Unknown" } }
            .map { (merchant, txs) ->
                InsightsContract.MerchantData(
                    merchant = merchant,
                    amount = txs.sumOf { it.amount },
                    count = txs.size
                )
            }
            .sortedByDescending { it.amount }
            .take(5)
            .toList()
    }

    private fun calculateVelocity(
        allTransactions: List<TransactionEntity>,
        periodTxs: List<TransactionEntity>,
        period: AccountAnalyticsPeriod,
        totalOutflow: Double,
        now: Long,
        periodDurationMs: Long
    ): SpendingVelocityData {
        val expenses = periodTxs.filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
        val largestOutflow = expenses.maxOfOrNull { it.amount } ?: 0.0

        val daysCount = when (period) {
            AccountAnalyticsPeriod.DAYS_7 -> 7
            AccountAnalyticsPeriod.DAYS_30 -> 30
            AccountAnalyticsPeriod.DAYS_90 -> 90
            AccountAnalyticsPeriod.YEAR_1 -> 365
            AccountAnalyticsPeriod.ALL -> {
                val minTs = allTransactions.minOfOrNull { it.timestamp } ?: now
                max(1, ((now - minTs) / 86400000L).toInt())
            }
        }

        val avgDaily = totalOutflow / daysCount
        val avgWeekly = avgDaily * 7.0

        val trendPercentage = if (period != AccountAnalyticsPeriod.ALL && periodDurationMs < Long.MAX_VALUE) {
            val prevStart = now - (periodDurationMs * 2)
            val prevEnd = now - periodDurationMs
            val prevExpenses = allTransactions.filter {
                !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) && it.timestamp in prevStart until prevEnd
            }.sumOf { it.amount }
            val prevDaily = prevExpenses / daysCount
            if (prevDaily > 0.0) {
                ((avgDaily - prevDaily) / prevDaily) * 100.0
            } else {
                0.0
            }
        } else {
            0.0
        }

        return SpendingVelocityData(
            avgDailySpend = avgDaily,
            avgWeeklySpend = avgWeekly,
            velocityTrendPercentage = trendPercentage,
            largestOutflow = largestOutflow
        )
    }

    private fun calculateWeekdayBreakdown(transactions: List<TransactionEntity>): List<WeekdaySpendData> {
        val expenses = transactions.filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
        val total = expenses.sumOf { it.amount }
        val cal = Calendar.getInstance()
        val symbols = DateFormatSymbols(Locale.getDefault()).shortWeekdays

        val weekdayTotals = mutableMapOf<Int, Double>()
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            weekdayTotals[day] = 0.0
        }

        expenses.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            weekdayTotals[dayOfWeek] = (weekdayTotals[dayOfWeek] ?: 0.0) + tx.amount
        }

        val maxAmount = weekdayTotals.values.maxOrNull() ?: 0.0

        val orderedDays = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )

        return orderedDays.map { dayKey ->
            val amt = weekdayTotals[dayKey] ?: 0.0
            val name = symbols.getOrNull(dayKey) ?: ""
            val pct = if (total > 0.0) (amt / total).toFloat() else 0f
            WeekdaySpendData(
                dayName = name,
                amount = amt,
                percentage = pct,
                isMax = maxAmount > 0.0 && amt == maxAmount
            )
        }
    }
}
