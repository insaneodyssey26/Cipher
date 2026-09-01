package com.masum.cipher.core.domain.usecase

import androidx.compose.ui.graphics.toArgb
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.domain.model.TimeRange
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetInsightsUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val subscriptionDetector: SubscriptionDetector,
    private val subscriptionDao: SubscriptionDao,
    private val userPreferences: UserPreferences
) {
    operator fun invoke(timeRange: TimeRange): Flow<InsightsContract.State> {
        val startOfCurrentWeek = getStartOfCurrentWeek()
        val startOfLastWeek = startOfCurrentWeek - TimeUnit.DAYS.toMillis(7)

        return combine(
            repository.getTransactionsBetween(timeRange.startTime, timeRange.endTime),
            repository.getExpensesSince(startOfLastWeek),
            repository.getAllTransactions(),
            subscriptionDao.getAllSubscriptions(),
            userPreferences.settingsFlow
        ) { transactions, recentExpenses, allTx, manualSubscriptions, settings ->
            val currentWeekExpenses = recentExpenses.filter { it.timestamp >= startOfCurrentWeek }
            val lastWeekExpenses = recentExpenses.filter { it.timestamp in startOfLastWeek until startOfCurrentWeek }

            val currentWeekAvg = if (currentWeekExpenses.isNotEmpty()) currentWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
            val lastWeekAvg = if (lastWeekExpenses.isNotEmpty()) lastWeekExpenses.sumOf { it.amount } / 7.0 else 0.0
            val trend = if (lastWeekAvg > 0.0) ((currentWeekAvg - lastWeekAvg) / lastWeekAvg) * 100.0 else 0.0

            // Merge detected and manual subscriptions
            val autoDetected = subscriptionDetector.detect(transactions)
            val manualDomainSubscriptions = manualSubscriptions.map { entity ->
                SubscriptionDetector.Subscription(
                    merchant = entity.merchant,
                    amount = entity.amount,
                    category = TransactionCategory.fromString(entity.category),
                    frequencyDays = entity.frequencyDays,
                    lastDate = 0L, // manual might not have a last date
                    nextExpectedDate = entity.nextExpectedDate,
                    confidence = 1.0f // 100% confidence for manual
                )
            }
            
            val manualMerchants = manualDomainSubscriptions.map { it.merchant.uppercase() }.toSet()
            val ignoredUpper = settings.ignoredSubscriptions.map { it.uppercase() }.toSet()
            val filteredAutoDetected = autoDetected.filter { 
                !manualMerchants.contains(it.merchant.uppercase()) && !ignoredUpper.contains(it.merchant.uppercase())
            }
            
            val allSubscriptions = (manualDomainSubscriptions + filteredAutoDetected).sortedBy { it.nextExpectedDate }

            val (expenseFlow, incomeFlow, netFlow) = calculateFinancialFlowHistory(transactions)

            InsightsContract.State(
                isLoading = false,
                currencyCode = settings.currencyCode,
                currencySymbol = settings.currencySymbol,
                selectedTimePeriod = timeRange.period,
                selectedTimeRange = timeRange,
                spendingVelocity = DashboardContract.VelocityData(
                    currentWeekAvg = currentWeekAvg,
                    lastWeekAvg = lastWeekAvg,
                    trendPercentage = trend
                ),
                netWorthHistory = expenseFlow,
                expenseTrendHistory = expenseFlow,
                incomeTrendHistory = incomeFlow,
                netFlowTrendHistory = netFlow,
                calendarHeatmap = calculateHeatmap(allTx),
                categoryBreakdown = calculateCategories(transactions),
                detectedSubscriptions = allSubscriptions,
                allTransactions = allTx,
                topMerchants = calculateTopMerchants(transactions),
                monthlySummary = calculateMonthlySummary(transactions),
                weekdayBreakdown = calculateWeekdayBreakdown(transactions),
                peakHours = calculatePeakHours(transactions),
                noSpendStreak = calculateNoSpendStreak(transactions),
                avgTransactionSize = calculateAvgTransactionSize(transactions)
            )
        }.flowOn(Dispatchers.Default)
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

    private fun calculateFinancialFlowHistory(transactions: List<TransactionEntity>): Triple<List<DashboardContract.Point>, List<DashboardContract.Point>, List<DashboardContract.Point>> {
        if (transactions.isEmpty()) return Triple(emptyList(), emptyList(), emptyList())

        val minTime = transactions.minOf { it.timestamp }
        val maxTime = transactions.maxOf { it.timestamp }
        
        val cal = Calendar.getInstance().apply { 
            timeInMillis = minTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply { 
            timeInMillis = maxTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dailyExpenses = mutableMapOf<Long, Double>()
        val dailyIncomes = mutableMapOf<Long, Double>()

        transactions.forEach { tx ->
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

        while (cal.timeInMillis <= endCal.timeInMillis) {
            val exp = dailyExpenses[cal.timeInMillis] ?: 0.0
            val inc = dailyIncomes[cal.timeInMillis] ?: 0.0
            runningExpense += exp
            runningIncome += inc
            val runningNet = runningIncome - runningExpense

            expensePoints.add(DashboardContract.Point(index, runningExpense.toFloat(), cal.timeInMillis))
            incomePoints.add(DashboardContract.Point(index, runningIncome.toFloat(), cal.timeInMillis))
            netPoints.add(DashboardContract.Point(index, runningNet.toFloat(), cal.timeInMillis))

            index += 1f
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return Triple(expensePoints, incomePoints, netPoints)
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
                val categoryModel = TransactionCategory.fromString(entry.key)
                DashboardContract.CategoryData(
                    category = categoryModel.displayName,
                    amount = amount,
                    percentage = (amount / total).toFloat(),
                    color = categoryModel.color.toArgb().toLong()
                )
            }.sortedByDescending { it.amount }
    }

    private fun calculateTopMerchants(transactions: List<TransactionEntity>): List<InsightsContract.MerchantData> {
        return transactions.asSequence().filter { !it.isIncome }
            .groupBy { it.merchant.trim() }
            .map { (merchant, txs) ->
                InsightsContract.MerchantData(
                    merchant = merchant,
                    amount = txs.sumOf { it.amount },
                    count = txs.size
                )
            }
            .sortedByDescending { it.amount }
            .take(5).toList()
    }

    private fun calculateMonthlySummary(transactions: List<TransactionEntity>): InsightsContract.MonthlySummary {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthTxs = transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val income = monthTxs.filter { it.isIncome }.sumOf { it.amount }
        val expense = monthTxs.filter { !it.isIncome }.sumOf { it.amount }
        val savingsRate = if (income > 0) ((income - expense) / income).toFloat().coerceIn(-1f, 1f) else 0f

        return InsightsContract.MonthlySummary(income, expense, savingsRate)
    }

    private fun calculateWeekdayBreakdown(transactions: List<TransactionEntity>): List<InsightsContract.DayOfWeekData> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val amounts = DoubleArray(7)

        transactions.filter { !it.isIncome }.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val dayIndex = (txCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
            amounts[dayIndex] += tx.amount
        }

        val maxAmount = amounts.maxOrNull() ?: 0.0

        return dayNames.mapIndexed { index, name ->
            InsightsContract.DayOfWeekData(
                dayName = name,
                amount = amounts[index],
                isMax = maxAmount > 0 && amounts[index] == maxAmount
            )
        }
    }

    private fun calculatePeakHours(transactions: List<TransactionEntity>): List<InsightsContract.PeakHourData> {
        val buckets = mutableMapOf("Morning" to 0.0, "Afternoon" to 0.0, "Evening" to 0.0, "Night" to 0.0)

        transactions.filter { !it.isIncome }.forEach { tx ->
            val hour = Calendar.getInstance().apply { timeInMillis = tx.timestamp }.get(Calendar.HOUR_OF_DAY)
            val label = when (hour) {
                in 6..11 -> "Morning"
                in 12..16 -> "Afternoon"
                in 17..21 -> "Evening"
                else -> "Night"
            }
            buckets[label] = (buckets[label] ?: 0.0) + tx.amount
        }

        val total = buckets.values.sum().coerceAtLeast(1.0)

        return listOf("Morning", "Afternoon", "Evening", "Night").map { label ->
            val amount = buckets[label] ?: 0.0
            InsightsContract.PeakHourData(
                label = label,
                amount = amount,
                percentage = (amount / total).toFloat()
            )
        }
    }

    private fun calculateNoSpendStreak(transactions: List<TransactionEntity>): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val spendDays = transactions.asSequence()
            .filter { !it.isIncome }
            .map { tx ->
                Calendar.getInstance().apply {
                    timeInMillis = tx.timestamp
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.toSet()

        var streak = 0
        var day = today
        val oneDayMs = TimeUnit.DAYS.toMillis(1)

        while (!spendDays.contains(day) && streak <= 365) {
            streak++
            day -= oneDayMs
        }

        return streak
    }

    private fun calculateAvgTransactionSize(transactions: List<TransactionEntity>): Double {
        val expenses = transactions.filter { !it.isIncome }
        return if (expenses.isNotEmpty()) expenses.sumOf { it.amount } / expenses.size else 0.0
    }
}
