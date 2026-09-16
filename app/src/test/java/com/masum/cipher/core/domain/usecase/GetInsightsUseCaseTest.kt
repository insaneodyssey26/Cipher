package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AccentColor
import com.masum.cipher.core.data.local.pref.AppTheme
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.concurrent.TimeUnit

class GetInsightsUseCaseTest {

    private lateinit var repository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var subscriptionDao: SubscriptionDao
    private lateinit var userPreferences: UserPreferences
    private lateinit var useCase: GetInsightsUseCase

    private fun settings() = UserSettings(
        theme = AppTheme.SYSTEM,
        isBiometricEnabled = false,
        isPrivacyModeEnabled = false,
        isHapticsEnabled = true,
        currency = "INR",
        autoLockTimeout = 0L,
        lastStopTime = 0L,
        monthlyBudget = 0.0,
        accentColor = AccentColor.INDIGO
    )

    private fun daysAgo(days: Int): Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())

    private fun timestampAtHour(hour: Int): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun expense(amount: Double, daysAgo: Int = 0, merchant: String = "MERCHANT", category: String = "OTHERS", timestamp: Long? = null) = TransactionEntity(
        amount = amount,
        merchant = merchant,
        currency = "INR",
        category = category,
        timestamp = timestamp ?: daysAgo(daysAgo),
        rawSms = null,
        isIncome = false
    )

    private fun income(amount: Double, daysAgo: Int = 0, merchant: String = "SALARY") = TransactionEntity(
        amount = amount,
        merchant = merchant,
        currency = "INR",
        category = "INCOME",
        timestamp = daysAgo(daysAgo),
        rawSms = null,
        isIncome = true
    )

    private fun stub(
        rangeTransactions: List<TransactionEntity>,
        recentExpenses: List<TransactionEntity> = emptyList(),
        allTransactions: List<TransactionEntity> = rangeTransactions,
        subscriptions: List<SubscriptionEntity> = emptyList(),
        settings: UserSettings = settings()
    ) {
        whenever(repository.getTransactionsBetween(any(), any())).thenReturn(flowOf(rangeTransactions))
        whenever(repository.getExpensesSince(any())).thenReturn(flowOf(recentExpenses))
        whenever(repository.getAllTransactions()).thenReturn(flowOf(allTransactions))
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(subscriptions))
        whenever(categoryRepository.getAllCustomCategoriesFlow()).thenReturn(flowOf(emptyList()))
        whenever(userPreferences.settingsFlow).thenReturn(flowOf(settings))
    }

    @Before
    fun setup() {
        repository = mock()
        categoryRepository = mock()
        subscriptionDao = mock()
        userPreferences = mock()
        useCase = GetInsightsUseCase(repository, categoryRepository, SubscriptionDetector(), subscriptionDao, userPreferences)
    }

    private fun invoke(transactions: List<TransactionEntity>) = runBlocking {
        stub(rangeTransactions = transactions)
        useCase(TimeRange.from(TimePeriod.THIS_MONTH)).first()
    }

    // ─── category breakdown ──────────────────────────────────────────────

    @Test
    fun `category breakdown sums amounts per category and computes percentages`() {
        val state = invoke(listOf(
            expense(300.0, category = "FOOD"),
            expense(100.0, category = "FOOD"),
            expense(200.0, category = "SHOPPING")
        ))

        val food = state.categoryBreakdown.first { it.amount == 400.0 }
        val shopping = state.categoryBreakdown.first { it.amount == 200.0 }
        assertEquals(400.0 / 600.0, food.percentage.toDouble(), 0.001)
        assertEquals(200.0 / 600.0, shopping.percentage.toDouble(), 0.001)
    }

    @Test
    fun `category breakdown excludes income transactions`() {
        val state = invoke(listOf(expense(100.0, category = "FOOD"), income(5000.0)))

        assertEquals(1, state.categoryBreakdown.size)
        assertEquals(100.0, state.categoryBreakdown[0].amount, 0.001)
    }

    @Test
    fun `category breakdown is sorted by amount descending`() {
        val state = invoke(listOf(
            expense(50.0, category = "SHOPPING"),
            expense(500.0, category = "FOOD"),
            expense(200.0, category = "TRANSPORT")
        ))

        val amounts = state.categoryBreakdown.map { it.amount }
        assertEquals(listOf(500.0, 200.0, 50.0), amounts)
    }

    @Test
    fun `no expenses yields empty category breakdown`() {
        val state = invoke(listOf(income(1000.0)))
        assertTrue(state.categoryBreakdown.isEmpty())
    }

    // ─── top merchants ───────────────────────────────────────────────────

    @Test
    fun `top merchants aggregates amount and count per merchant`() {
        val state = invoke(listOf(
            expense(100.0, merchant = "ZOMATO"),
            expense(150.0, merchant = "ZOMATO"),
            expense(50.0, merchant = "UBER")
        ))

        val zomato = state.topMerchants.first { it.merchant == "ZOMATO" }
        assertEquals(250.0, zomato.amount, 0.001)
        assertEquals(2, zomato.count)
    }

    @Test
    fun `top merchants is capped at 5 entries sorted by amount`() {
        val state = invoke((1..6).map { expense(it * 10.0, merchant = "MERCHANT_$it") })

        assertEquals(5, state.topMerchants.size)
        assertEquals("MERCHANT_6", state.topMerchants.first().merchant)
    }

    @Test
    fun `top merchants excludes income`() {
        val state = invoke(listOf(income(500.0, merchant = "EMPLOYER")))
        assertTrue(state.topMerchants.none { it.merchant == "EMPLOYER" })
    }

    // ─── monthly summary ─────────────────────────────────────────────────

    @Test
    fun `monthly summary only counts transactions from the current month`() {
        val state = invoke(listOf(
            expense(100.0, daysAgo = 0),
            expense(500.0, daysAgo = 90) // almost certainly a different month
        ))

        assertEquals(100.0, state.monthlySummary.expense, 0.001)
    }

    @Test
    fun `monthly summary savings rate is computed from income and expense`() {
        val state = invoke(listOf(income(1000.0, daysAgo = 0), expense(400.0, daysAgo = 0)))

        assertEquals(1000.0, state.monthlySummary.income, 0.001)
        assertEquals(400.0, state.monthlySummary.expense, 0.001)
        assertEquals(0.6f, state.monthlySummary.savingsRate, 0.001f)
    }

    @Test
    fun `monthly summary savings rate is zero when there is no income`() {
        val state = invoke(listOf(expense(400.0, daysAgo = 0)))
        assertEquals(0f, state.monthlySummary.savingsRate, 0.001f)
    }

    // ─── weekday breakdown ───────────────────────────────────────────────

    @Test
    fun `weekday breakdown exactly one day is flagged as max and totals match`() {
        val state = invoke(listOf(expense(300.0, daysAgo = 0), expense(100.0, daysAgo = 1)))

        assertEquals(1, state.weekdayBreakdown.count { it.isMax })
        assertEquals(400.0, state.weekdayBreakdown.sumOf { it.amount }, 0.001)
        assertTrue(state.weekdayBreakdown.first { it.isMax }.amount >= 100.0)
    }

    // ─── peak hours ──────────────────────────────────────────────────────

    @Test
    fun `peak hours buckets a morning transaction correctly`() {
        val state = invoke(listOf(expense(200.0, timestamp = timestampAtHour(8))))
        val morning = state.peakHours.first { it.label == "Morning" }
        assertEquals(200.0, morning.amount, 0.001)
        assertEquals(1f, morning.percentage, 0.001f)
    }

    @Test
    fun `peak hours buckets a night transaction correctly`() {
        val state = invoke(listOf(expense(150.0, timestamp = timestampAtHour(23))))
        val night = state.peakHours.first { it.label == "Night" }
        assertEquals(150.0, night.amount, 0.001)
    }

    @Test
    fun `peak hours has all four buckets even when empty`() {
        val state = invoke(emptyList())
        assertEquals(setOf("Morning", "Afternoon", "Evening", "Night"), state.peakHours.map { it.label }.toSet())
    }

    // ─── no-spend streak ─────────────────────────────────────────────────

    @Test
    fun `no spend streak is zero when there was an expense today`() {
        val state = invoke(listOf(expense(50.0, daysAgo = 0)))
        assertEquals(0, state.noSpendStreak)
    }

    @Test
    fun `no spend streak counts days since the last expense`() {
        val state = invoke(listOf(expense(50.0, daysAgo = 3)))
        assertEquals(3, state.noSpendStreak)
    }

    // ─── average transaction size ────────────────────────────────────────

    @Test
    fun `average transaction size divides total expense by expense count`() {
        val state = invoke(listOf(expense(100.0), expense(200.0), expense(300.0)))
        assertEquals(200.0, state.avgTransactionSize, 0.001)
    }

    @Test
    fun `average transaction size excludes income from both sum and count`() {
        val state = invoke(listOf(expense(100.0), income(9000.0)))
        assertEquals(100.0, state.avgTransactionSize, 0.001)
    }

    // ─── calendar heatmap ────────────────────────────────────────────────

    @Test
    fun `calendar heatmap sums amounts landing on the same day`() {
        val today = daysAgo(0)
        val state = invoke(listOf(expense(100.0, daysAgo = 0), expense(50.0, daysAgo = 0)))
        assertEquals(150.0, state.calendarHeatmap.values.sum(), 0.001)
        assertEquals(1, state.calendarHeatmap.size)
    }

    // ─── financial flow history ──────────────────────────────────────────

    @Test
    fun `financial flow history final point equals total running expense`() {
        val state = invoke(listOf(expense(100.0, daysAgo = 2), expense(50.0, daysAgo = 0)))
        assertEquals(150.0, state.expenseTrendHistory.last().y.toDouble(), 0.001)
    }

    @Test
    fun `financial flow history is empty for no transactions`() {
        val state = invoke(emptyList())
        assertTrue(state.expenseTrendHistory.isEmpty())
        assertTrue(state.incomeTrendHistory.isEmpty())
        assertTrue(state.netFlowTrendHistory.isEmpty())
    }

    // ─── subscriptions ───────────────────────────────────────────────────

    @Test
    fun `manual subscriptions are surfaced with full confidence`() = runBlocking {
        stub(
            rangeTransactions = emptyList(),
            subscriptions = listOf(
                SubscriptionEntity(merchant = "NETFLIX", amount = 649.0, category = "ENTERTAINMENT", frequencyDays = 30, nextExpectedDate = daysAgo(-5))
            )
        )
        val state = useCase(TimeRange.from(TimePeriod.THIS_MONTH)).first()

        val manual = state.detectedSubscriptions.first { it.merchant == "NETFLIX" }
        assertEquals(1.0f, manual.confidence, 0.001f)
    }

    @Test
    fun `ignored subscriptions are filtered out of auto detected results`() = runBlocking {
        val subscriptionTxs = listOf(
            expense(100.0, daysAgo = 90, merchant = "GYM"),
            expense(100.0, daysAgo = 60, merchant = "GYM"),
            expense(100.0, daysAgo = 30, merchant = "GYM")
        )
        stub(
            rangeTransactions = subscriptionTxs,
            allTransactions = subscriptionTxs,
            settings = settings().copy(ignoredSubscriptions = setOf("GYM"))
        )
        val state = useCase(TimeRange.from(TimePeriod.THIS_MONTH)).first()

        assertTrue(state.detectedSubscriptions.none { it.merchant.equals("GYM", ignoreCase = true) })
    }

    // ─── spending velocity ───────────────────────────────────────────────

    @Test
    fun `spending velocity current week average is total spend over 7 days`() = runBlocking {
        stub(rangeTransactions = emptyList(), recentExpenses = listOf(expense(70.0, daysAgo = 0)))
        val state = useCase(TimeRange.from(TimePeriod.THIS_MONTH)).first()

        assertEquals(70.0 / 7.0, state.spendingVelocity.currentWeekAvg, 0.001)
    }
}
