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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
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
            val startOfCurrentWeek = getStartOfCurrentWeek()
            val startOfLastWeek = startOfCurrentWeek - TimeUnit.DAYS.toMillis(7)

            combine(
                repository.getAllTransactions(),
                repository.getTotalIncome(),
                repository.getTotalExpenses(),
                repository.getExpensesSince(startOfLastWeek)
            ) { transactions: List<TransactionEntity>, 
                income: Double?, 
                expenses: Double?, 
                recentExpenses: List<TransactionEntity> ->
                
                val currentWeekExpenses = recentExpenses.filter { tx -> tx.timestamp >= startOfCurrentWeek }
                val lastWeekExpenses = recentExpenses.filter { tx -> 
                    tx.timestamp >= startOfLastWeek && tx.timestamp < startOfCurrentWeek 
                }

                val currentWeekAvg = if (currentWeekExpenses.isNotEmpty()) currentWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
                val lastWeekAvg = if (lastWeekExpenses.isNotEmpty()) lastWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
                val trend = if (lastWeekAvg > 0.0) ((currentWeekAvg - lastWeekAvg) / lastWeekAvg) * 100.0 else 0.0

                val totalIncome = income ?: 0.0
                val totalExpenses = expenses ?: 0.0

                DashboardContract.State(
                    isLoading = false,
                    transactions = transactions,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    totalBalance = totalIncome - totalExpenses,
                    spendingVelocity = DashboardContract.VelocityData(
                        currentWeekAvg = currentWeekAvg,
                        lastWeekAvg = lastWeekAvg,
                        trendPercentage = trend
                    ),
                    netWorthHistory = calculateNetWorthHistory(transactions),
                    calendarHeatmap = calculateHeatmap(transactions),
                    categoryBreakdown = calculateCategories(transactions)
                )
            }.collect { newState: DashboardContract.State ->
                _state.value = newState
            }
        }
    }

    private fun getStartOfCurrentWeek(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun calculateNetWorthHistory(transactions: List<TransactionEntity>): List<DashboardContract.Point> {
        if (transactions.isEmpty()) return emptyList()
        val sorted = transactions.sortedBy { it.timestamp }
        var netWorth = 0.0
        return sorted.mapIndexed { index, tx ->
            netWorth += if (tx.isIncome) tx.amount else -tx.amount
            DashboardContract.Point(index.toFloat(), netWorth.toFloat(), tx.timestamp)
        }
    }

    private fun calculateHeatmap(transactions: List<TransactionEntity>): Map<Long, Double> {
        return transactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    private fun calculateCategories(transactions: List<TransactionEntity>): List<DashboardContract.CategoryData> {
        val expenses = transactions.filter { !it.isIncome }
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        return expenses.groupBy { it.category }
            .map { entry ->
                val amount = entry.value.sumOf { it.amount }
                DashboardContract.CategoryData(
                    category = entry.key,
                    amount = amount,
                    percentage = (amount / total).toFloat(),
                    color = getCategoryColor(entry.key)
                )
            }.sortedByDescending { it.amount }
    }

    private fun getCategoryColor(category: String): Long {
        return when(category.lowercase()) {
            "food" -> 0xFFFF7043
            "rent" -> 0xFF42A5F5
            "shopping" -> 0xFFAB47BC
            "transport" -> 0xFF26A69A
            else -> 0xFF78909C
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}