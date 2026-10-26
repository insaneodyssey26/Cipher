package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.domain.model.TimePeriod
import com.masum.cipher.core.domain.model.TimeRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

class InsightsPerformanceBenchmarkTest {

    private lateinit var repository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var subscriptionDao: SubscriptionDao
    private lateinit var userPreferences: UserPreferences
    private lateinit var useCase: GetInsightsUseCase

    private val categories = listOf("Food", "Shopping", "Transport", "Bills", "Entertainment", "Health", "Education", "Groceries")
    private val merchants = listOf("Uber", "Amazon", "Starbucks", "Netflix", "Walmart", "Apple", "Spotify", "Shell", "Target", "Costco")

    @Before
    fun setup() {
        repository = mock()
        categoryRepository = mock()
        subscriptionDao = mock()
        userPreferences = mock()

        whenever(categoryRepository.getAllCustomCategoriesFlow()).thenReturn(flowOf(emptyList()))
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(emptyList()))
        val mockSettings = UserSettings(
            theme = com.masum.cipher.core.data.local.pref.AppTheme.SYSTEM,
            isBiometricEnabled = false,
            isPrivacyModeEnabled = false,
            isHapticsEnabled = true,
            currency = "INR",
            autoLockTimeout = 0L,
            lastStopTime = 0L,
            monthlyBudget = 0.0,
            accentColor = com.masum.cipher.core.data.local.pref.AccentColor.INDIGO
        )
        whenever(userPreferences.settingsFlow).thenReturn(flowOf(mockSettings))

        useCase = GetInsightsUseCase(
            repository = repository,
            categoryRepository = categoryRepository,
            subscriptionDetector = SubscriptionDetector(),
            subscriptionDao = subscriptionDao,
            userPreferences = userPreferences
        )
    }

    private fun generateMockTransactions(count: Int): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)
        val list = ArrayList<TransactionEntity>(count)

        for (i in 0 until count) {
            val isIncome = i % 20 == 0
            val daysAgo = (i % 90).toLong()
            val txTime = now - (daysAgo * dayMs) - (i * 1000L % dayMs)
            val merchant = merchants[i % merchants.size]
            val category = if (isIncome) "INCOME" else categories[i % categories.size]
            val amount = if (isIncome) 2500.0 else 10.0 + (i % 150)

            list.add(
                TransactionEntity(
                    id = (i + 1).toLong(),
                    merchant = merchant,
                    amount = amount,
                    currency = "INR",
                    timestamp = txTime,
                    category = category,
                    rawSms = null,
                    isIncome = isIncome
                )
            )
        }
        return list
    }

    private fun runBenchmark(count: Int, iterations: Int = 10) {
        val txs = generateMockTransactions(count)
        val now = System.currentTimeMillis()
        val range = TimeRange(TimePeriod.THIS_MONTH, now - TimeUnit.DAYS.toMillis(30), now)

        whenever(repository.getTransactionsBetween(any(), any())).thenReturn(flowOf(txs))
        whenever(repository.getExpensesSince(any())).thenReturn(flowOf(txs.filter { !it.isIncome }))
        whenever(repository.getAllTransactions()).thenReturn(flowOf(txs))

        runBlocking {
            useCase(range).first()
        }

        val timesMs = ArrayList<Double>(iterations)
        for (i in 0 until iterations) {
            val nanos = measureNanoTime {
                runBlocking {
                    useCase(range).first()
                }
            }
            timesMs.add(nanos / 1_000_000.0)
        }

        val avg = timesMs.average()
        val min = timesMs.minOrNull() ?: 0.0
        val max = timesMs.maxOrNull() ?: 0.0
        val p90 = timesMs.sorted()[(iterations * 0.9).toInt().coerceAtMost(iterations - 1)]

        println(
            String.format(
                "[BENCHMARK] Count: %-6d | Avg: %6.2f ms | Min: %6.2f ms | Max: %6.2f ms | p90: %6.2f ms",
                count, avg, min, max, p90
            )
        )
    }

    @Test
    fun benchmarkSmallDataset() {
        runBenchmark(count = 100, iterations = 15)
    }

    @Test
    fun benchmarkMediumDataset() {
        runBenchmark(count = 1_500, iterations = 15)
    }

    @Test
    fun benchmarkLargeDataset() {
        runBenchmark(count = 10_000, iterations = 10)
    }
}
