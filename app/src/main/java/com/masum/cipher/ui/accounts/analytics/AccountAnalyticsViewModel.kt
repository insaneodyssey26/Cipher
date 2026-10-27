package com.masum.cipher.ui.accounts.analytics

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.groupByMerchant
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
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

                val rootAccountId = accountItems.minByOrNull { it.entity.createdAt }?.id ?: 1L
                val isTargetRoot = (targetEntity != null && targetEntity.id == rootAccountId)
                val accTxs = if (targetEntity != null) {
                    allTxs.filter { tx ->
                        tx.accountId == targetEntity.id || (tx.accountId == null && isTargetRoot)
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

        val zoneId = ZoneId.systemDefault()
        val nowLocalDate = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()

        val startDate = when (period) {
            AccountAnalyticsPeriod.DAYS_7 -> nowLocalDate.minusDays(6)
            AccountAnalyticsPeriod.DAYS_30 -> nowLocalDate.minusDays(29)
            AccountAnalyticsPeriod.DAYS_90 -> nowLocalDate.minusDays(89)
            AccountAnalyticsPeriod.YEAR_1 -> nowLocalDate.minusDays(364)
            AccountAnalyticsPeriod.ALL -> {
                val minTs = transactions.minOfOrNull { it.timestamp } ?: now
                Instant.ofEpochMilli(minTs).atZone(zoneId).toLocalDate()
            }
        }

        val dailyExpenses = mutableMapOf<LocalDate, Double>()
        val dailyIncomes = mutableMapOf<LocalDate, Double>()

        transactions.forEach { tx ->
            if (tx.category.equals("TRANSFER", ignoreCase = true)) return@forEach
            val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(zoneId).toLocalDate()
            if (tx.isIncome) {
                dailyIncomes[txDate] = (dailyIncomes[txDate] ?: 0.0) + tx.amount
            } else {
                dailyExpenses[txDate] = (dailyExpenses[txDate] ?: 0.0) + tx.amount
            }
        }

        val expensePoints = mutableListOf<DashboardContract.Point>()
        val incomePoints = mutableListOf<DashboardContract.Point>()
        val netPoints = mutableListOf<DashboardContract.Point>()

        var index = 0f
        var runningExpense = 0.0
        var runningIncome = 0.0

        var cursorDate = startDate
        while (!cursorDate.isAfter(nowLocalDate)) {
            val exp = dailyExpenses[cursorDate] ?: 0.0
            val inc = dailyIncomes[cursorDate] ?: 0.0
            runningExpense += exp
            runningIncome += inc
            val runningNet = runningIncome - runningExpense

            val cursorMillis = cursorDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            expensePoints.add(DashboardContract.Point(index, runningExpense.toFloat(), cursorMillis))
            incomePoints.add(DashboardContract.Point(index, runningIncome.toFloat(), cursorMillis))
            netPoints.add(DashboardContract.Point(index, runningNet.toFloat(), cursorMillis))

            index += 1f
            cursorDate = cursorDate.plusDays(1)
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
        return transactions
            .filter { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
            .groupByMerchant(blankLabel = "Unknown") { it.merchant }
            .map { group ->
                InsightsContract.MerchantData(
                    merchant = group.name,
                    amount = group.items.sumOf { it.amount },
                    count = group.items.size
                )
            }
            .sortedByDescending { it.amount }
            .take(5)
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
        val zoneId = ZoneId.systemDefault()
        val locale = Locale.getDefault()

        val weekdayTotals = mutableMapOf<DayOfWeek, Double>()
        for (day in DayOfWeek.entries) {
            weekdayTotals[day] = 0.0
        }

        expenses.forEach { tx ->
            val dayOfWeek = Instant.ofEpochMilli(tx.timestamp).atZone(zoneId).dayOfWeek
            weekdayTotals[dayOfWeek] = (weekdayTotals[dayOfWeek] ?: 0.0) + tx.amount
        }

        val maxAmount = weekdayTotals.values.maxOrNull() ?: 0.0

        val orderedDays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )

        return orderedDays.map { day ->
            val amt = weekdayTotals[day] ?: 0.0
            val name = day.getDisplayName(TextStyle.SHORT, locale)
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
