package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.ui.dashboard.DashboardContract
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
        filter: DashboardContract.FilterType, 
        timeRange: com.masum.cipher.core.domain.model.TimeRange
    ): Flow<DashboardContract.State> {
        val thisMonthRange = com.masum.cipher.core.domain.model.TimeRange.from(com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH)
        return combine(
            combine(
                repository.getTotalIncomeBetween(timeRange.startTime, timeRange.endTime),
                repository.getTotalExpensesBetween(timeRange.startTime, timeRange.endTime),
                repository.getTotalExpensesBetween(thisMonthRange.startTime, thisMonthRange.endTime)
            ) { inc, exp, monthExp -> Triple(inc, exp, monthExp) },
            repository.getTotalIncome(),
            repository.getTotalExpenses(),
            userPreferences.settingsFlow
        ) { rangeStats, incomeAllTime, expensesAllTime, settings ->
            val (incomeRange, expensesRange, thisMonthExp) = rangeStats
            val rangeInc = incomeRange ?: 0.0
            val rangeExp = expensesRange ?: 0.0
            val monthExp = thisMonthExp ?: 0.0
            val allTimeInc = incomeAllTime ?: 0.0
            val allTimeExp = expensesAllTime ?: 0.0
            
            Triple(Triple(rangeInc, rangeExp, monthExp), allTimeInc - allTimeExp, settings.monthlyBudget)
        }.flatMapLatest { (rangeStats, totalBalance, budget) ->
            val (income, expenses, thisMonthExpenses) = rangeStats
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

            transactionsFlow.map { transactions ->
                val filteredList = when (filter) {
                    DashboardContract.FilterType.ALL -> transactions
                    DashboardContract.FilterType.INCOME -> transactions.filter { it.isIncome }
                    DashboardContract.FilterType.EXPENSE -> transactions.filter { !it.isIncome }
                }
                
                DashboardContract.State(
                    isLoading = false,
                    transactions = filteredList,
                    searchQuery = query,
                    activeFilter = filter,
                    selectedTimePeriod = timeRange.period,
                    totalIncome = income,
                    totalExpenses = expenses,
                    thisMonthExpenses = thisMonthExpenses,
                    totalBalance = totalBalance,
                    monthlyBudget = budget
                )
            }
        }
    }
}
