package com.example.cipherspend.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DashboardContract.FilterType.ALL)

    private val _state = MutableStateFlow(DashboardContract.State())
    val state: StateFlow<DashboardContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardContract.Effect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<DashboardContract.Effect> = _effect.asSharedFlow()

    init {
        observeDashboardData()
    }

    fun handleIntent(intent: DashboardContract.Intent) {
        when (intent) {
            is DashboardContract.Intent.LoadDashboard -> { /* Data is already observed in init */ }
            is DashboardContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
            is DashboardContract.Intent.UpdateTransaction -> updateTransaction(intent.transaction)
            is DashboardContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction)
            is DashboardContract.Intent.AddTransaction -> addTransaction(intent.transaction)
            is DashboardContract.Intent.SearchTransactions -> _searchQuery.value = intent.query
            is DashboardContract.Intent.FilterTransactions -> _activeFilter.value = intent.filter
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _activeFilter,
                repository.getTotalIncome(),
                repository.getTotalExpenses()
            ) { query, filter, income, expenses ->
                Triple(query, filter, Pair(income ?: 0.0, expenses ?: 0.0))
            }.flatMapLatest { (query, filter, money) ->
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
                        totalIncome = money.first,
                        totalExpenses = money.second,
                        totalBalance = money.first - money.second
                    )
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _effect.emit(DashboardContract.Effect.ShowUndoDelete(transaction))
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    private fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    private fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
}
