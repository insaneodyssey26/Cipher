package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.dashboard.DashboardFilter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardDataUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences
) {
    operator fun invoke(
        query: String,
        filter: DashboardFilter,
        timeRange: com.masum.cipher.core.domain.model.TimeRange
    ): Flow<DashboardContract.State> {
        val thisMonthRange = com.masum.cipher.core.domain.model.TimeRange.from(com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH)
        val previousRange = com.masum.cipher.core.domain.model.TimeRange.previousEquivalentRange(timeRange.period)
        val prevExpenseFlow = if (previousRange != null) {
            repository.getTotalExpensesBetween(previousRange.startTime, previousRange.endTime)
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }

        val rangeStatsFlow = combine(
            repository.getTotalIncomeBetween(timeRange.startTime, timeRange.endTime),
            repository.getTotalExpensesBetween(timeRange.startTime, timeRange.endTime),
            prevExpenseFlow
        ) { inc, exp, prev ->
            Triple(inc ?: 0.0, exp ?: 0.0, prev)
        }

        val monthStatsFlow = userPreferences.settingsFlow.flatMapLatest { settings ->
            val accountId = settings.budgetAccountId
            val expFlow = if (accountId != null) {
                repository.getTotalExpensesBetweenForAccount(thisMonthRange.startTime, thisMonthRange.endTime, accountId)
            } else {
                repository.getTotalExpensesBetween(thisMonthRange.startTime, thisMonthRange.endTime)
            }
            val incFlow = if (accountId != null) {
                repository.getTotalIncomeBetweenForAccount(thisMonthRange.startTime, thisMonthRange.endTime, accountId)
            } else {
                repository.getTotalIncomeBetween(thisMonthRange.startTime, thisMonthRange.endTime)
            }
            combine(expFlow, incFlow) { monthExp, monthInc ->
                Triple(monthExp ?: 0.0, monthInc ?: 0.0, settings)
            }
        }

        return combine(rangeStatsFlow, monthStatsFlow) { (rangeInc, rangeExp, prevExp), (monthExp, monthInc, settings) ->
            val rangeBalance = rangeInc - rangeExp
            val (deltaPercent, compLabel) = if (prevExp != null && prevExp > 0.0) {
                val delta = ((rangeExp - prevExp) / prevExp) * 100.0
                Pair(delta, previousRange?.label)
            } else {
                Pair(null, null)
            }
            StateTuple(rangeInc, rangeExp, monthExp, monthInc, rangeBalance, settings.monthlyBudget, settings.isDynamicBudgetEnabled, deltaPercent, compLabel, prevExp, settings.currencyCode, settings.currencySymbol)
        }.flatMapLatest { stats ->
            val transactionsFlow = if (query.isBlank()) {
                repository.getTransactionsBetween(timeRange.startTime, timeRange.endTime)
            } else {
                repository.getTransactionsBetween(timeRange.startTime, timeRange.endTime).map { list ->
                    list.filter {
                        it.merchant.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                    }
                }
            }

            combine(transactionsFlow, repository.hasNonTransferTransactions(), categoryRepository.getAllCustomCategoriesFlow()) { transactions, hasTransactions, customCats ->
                val hasCategoryFilter = filter.selectedCategories.isNotEmpty()
                val minAmt = filter.minAmount
                val maxAmt = filter.maxAmount
                val filterType = filter.type

                val resolvedCategoryCache = if (hasCategoryFilter) {
                    val distinctCategories = transactions.map { it.category }.distinct()
                    val map = HashMap<String, Boolean>(distinctCategories.size)
                    for (cat in distinctCategories) {
                        val categoryItem = CategoryHelper.resolveCategory(cat, customCats)
                        val matches = filter.selectedCategories.contains(cat) ||
                                filter.selectedCategories.contains(categoryItem.name) ||
                                filter.selectedCategories.contains(categoryItem.displayName)
                        map[cat] = matches
                    }
                    map
                } else null

                val filteredList = transactions.filter { tx ->
                    if (tx.category.equals("TRANSFER", ignoreCase = true)) return@filter false

                    val matchesType = when (filterType) {
                        DashboardContract.FilterType.ALL -> true
                        DashboardContract.FilterType.INCOME -> tx.isIncome
                        DashboardContract.FilterType.EXPENSE -> !tx.isIncome
                    }
                    if (!matchesType) return@filter false

                    if (resolvedCategoryCache != null) {
                        if (resolvedCategoryCache[tx.category] != true) return@filter false
                    }

                    if (minAmt != null && tx.amount < minAmt) {
                        return@filter false
                    }
                    if (maxAmt != null && tx.amount > maxAmt) {
                        return@filter false
                    }

                    true
                }

                DashboardContract.State(
                    isLoading = false,
                    transactions = filteredList.toPersistentList(),
                    hasAnyTransactions = hasTransactions,
                    searchQuery = query,
                    activeFilter = filter.type,
                    filter = filter,
                    selectedTimePeriod = timeRange.period,
                    selectedTimeRange = timeRange,
                    totalIncome = stats.income,
                    totalExpenses = stats.expenses,
                    thisMonthExpenses = stats.thisMonthExpenses,
                    thisMonthIncome = stats.thisMonthIncome,
                    currencyCode = stats.currencyCode,
                    currencySymbol = stats.currencySymbol,
                    totalBalance = stats.totalBalance,
                    monthlyBudget = stats.budget,
                    isDynamicBudget = stats.isDynamicBudget,
                    expenseComparisonPercent = stats.deltaPercent,
                    expenseComparisonLabel = stats.compLabel,
                    previousPeriodExpenses = stats.prevExp,
                    customCategories = customCats.toPersistentList()
                )
            }
        }
    }

    private data class StateTuple(
        val income: Double,
        val expenses: Double,
        val thisMonthExpenses: Double,
        val thisMonthIncome: Double,
        val totalBalance: Double,
        val budget: Double,
        val isDynamicBudget: Boolean,
        val deltaPercent: Double?,
        val compLabel: String?,
        val prevExp: Double?,
        val currencyCode: String,
        val currencySymbol: String
    )
}
