package com.example.cipherspend.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.repository.TransactionRepository
import com.example.cipherspend.ui.dashboard.DashboardContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsContract.State())
    val state: StateFlow<InsightsContract.State> = _state.asStateFlow()

    init {
        loadInsights()
    }

    fun handleIntent(intent: InsightsContract.Intent) {
        when (intent) {
            is InsightsContract.Intent.LoadInsights -> loadInsights()
            is InsightsContract.Intent.SelectDay -> {
                _state.update { it.copy(selectedDayTimestamp = intent.timestamp) }
            }
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            val startOfCurrentWeek = getStartOfCurrentWeek()
            val startOfLastWeek = startOfCurrentWeek - TimeUnit.DAYS.toMillis(7)

            repository.getAllTransactions()
                .combine(repository.getExpensesSince(startOfLastWeek)) { transactions, recentExpenses ->
                    // Perform all heavy calculations on a background thread
                    val currentWeekExpenses = recentExpenses.filter { it.timestamp >= startOfCurrentWeek }
                    val lastWeekExpenses = recentExpenses.filter { it.timestamp in startOfLastWeek until startOfCurrentWeek }

                    val currentWeekAvg = if (currentWeekExpenses.isNotEmpty()) currentWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
                    val lastWeekAvg = if (lastWeekExpenses.isNotEmpty()) lastWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
                    val trend = if (lastWeekAvg > 0.0) ((currentWeekAvg - lastWeekAvg) / lastWeekAvg) * 100.0 else 0.0

                    InsightsContract.State(
                        isLoading = false,
                        spendingVelocity = DashboardContract.VelocityData(
                            currentWeekAvg = currentWeekAvg,
                            lastWeekAvg = lastWeekAvg,
                            trendPercentage = trend
                        ),
                        netWorthHistory = calculateNetWorthHistory(transactions),
                        calendarHeatmap = calculateHeatmap(transactions),
                        categoryBreakdown = calculateCategories(transactions),
                        allTransactions = transactions
                    )
                }
                .flowOn(Dispatchers.Default) // Crucial: moves the calculations off the Main thread
                .collect { newState ->
                    _state.value = newState.copy(selectedDayTimestamp = _state.value.selectedDayTimestamp)
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
}
