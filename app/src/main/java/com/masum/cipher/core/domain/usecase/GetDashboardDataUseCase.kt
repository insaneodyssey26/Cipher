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
    operator fun invoke(query: String, filter: DashboardContract.FilterType): Flow<DashboardContract.State> {
        return combine(
            repository.getTotalIncome(),
            repository.getTotalExpenses(),
            userPreferences.settingsFlow
        ) { income, expenses, settings ->
            Triple(income ?: 0.0, expenses ?: 0.0, settings.monthlyBudget)
        }.flatMapLatest { (income, expenses, budget) ->
            val transactionsFlow = if (query.isBlank()) {
                repository.getRecentTransactions(20)
            } else {
                repository.getAllTransactions().map { list ->
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
                    totalIncome = income,
                    totalExpenses = expenses,
                    totalBalance = income - expenses,
                    monthlyBudget = budget
                )
            }
        }
    }
}
