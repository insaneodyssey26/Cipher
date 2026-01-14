package com.example.cipherspend.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardContract.State())
    val state: StateFlow<DashboardContract.State> = _state.asStateFlow()

    init {
        handleIntent(DashboardContract.Intent.LoadDashboard)
    }

    fun handleIntent(intent: DashboardContract.Intent) {
        when (intent) {
            is DashboardContract.Intent.LoadDashboard -> observeDashboardData()
            is DashboardContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                repository.getTotalIncome(),
                repository.getTotalExpenses()
            ) { transactions: List<TransactionEntity>, 
                income: Double?, 
                expenses: Double? ->
                
                val totalIncome = income ?: 0.0
                val totalExpenses = expenses ?: 0.0

                DashboardContract.State(
                    isLoading = false,
                    transactions = transactions.take(10), // Only show recent 10 on home
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalBalance = totalIncome - totalExpenses
                )
            }.collect { newState: DashboardContract.State ->
                _state.value = newState
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}