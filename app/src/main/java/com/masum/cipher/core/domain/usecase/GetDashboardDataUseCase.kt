package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.dashboard.DashboardFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardDataUseCase @Inject constructor(
    private val repository: TransactionRepository,
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

        return combine(
            repository.getTotalIncomeBetween(timeRange.startTime, timeRange.endTime),
            repository.getTotalExpensesBetween(timeRange.startTime, timeRange.endTime),
            repository.getTotalExpensesBetween(thisMonthRange.startTime, thisMonthRange.endTime),
            prevExpenseFlow,
            userPreferences.settingsFlow
        ) { incomeRange, expensesRange, thisMonthExp, prevExp, settings ->
            val rangeInc = incomeRange ?: 0.0
            val rangeExp = expensesRange ?: 0.0
            val monthExp = thisMonthExp ?: 0.0
            val rangeBalance = rangeInc - rangeExp
            val (deltaPercent, compLabel) = if (prevExp != null && prevExp > 0.0) {
                val delta = ((rangeExp - prevExp) / prevExp) * 100.0
                Pair(delta, previousRange?.label)
            } else {
                Pair(null, null)
            }
            StateTuple(rangeInc, rangeExp, monthExp, rangeBalance, settings.monthlyBudget, deltaPercent, compLabel, prevExp)
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

            transactionsFlow.combine(repository.getAllTransactions()) { transactions, allTxs ->
                val filteredList = transactions.filter { tx ->
                    val matchesType = when (filter.type) {
                        DashboardContract.FilterType.ALL -> true
                        DashboardContract.FilterType.INCOME -> tx.isIncome
                        DashboardContract.FilterType.EXPENSE -> !tx.isIncome
                    }
                    if (!matchesType) return@filter false

                    if (filter.selectedCategories.isNotEmpty()) {
                        val categoryEnum = TransactionCategory.fromString(tx.category)
                        val matchesCategory = filter.selectedCategories.contains(tx.category) ||
                                filter.selectedCategories.contains(categoryEnum.displayName) ||
                                filter.selectedCategories.contains(categoryEnum.name)
                        if (!matchesCategory) return@filter false
                    }

                    if (filter.minAmount != null && tx.amount < filter.minAmount) {
                        return@filter false
                    }
                    if (filter.maxAmount != null && tx.amount > filter.maxAmount) {
                        return@filter false
                    }

                    true
                }

                DashboardContract.State(
                    isLoading = false,
                    transactions = filteredList,
                    hasAnyTransactions = allTxs.isNotEmpty(),
                    searchQuery = query,
                    activeFilter = filter.type,
                    filter = filter,
                    selectedTimePeriod = timeRange.period,
                    selectedTimeRange = timeRange,
                    totalIncome = stats.income,
                    totalExpenses = stats.expenses,
                    thisMonthExpenses = stats.thisMonthExpenses,
                    totalBalance = stats.totalBalance,
                    monthlyBudget = stats.budget,
                    expenseComparisonPercent = stats.deltaPercent,
                    expenseComparisonLabel = stats.compLabel,
                    previousPeriodExpenses = stats.prevExp
                )
            }
        }
    }

    private data class StateTuple(
        val income: Double,
        val expenses: Double,
        val thisMonthExpenses: Double,
        val totalBalance: Double,
        val budget: Double,
        val deltaPercent: Double?,
        val compLabel: String?,
        val prevExp: Double?
    )
}
