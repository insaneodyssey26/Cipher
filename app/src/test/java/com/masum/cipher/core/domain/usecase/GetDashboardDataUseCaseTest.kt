package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.TimePeriod
import com.masum.cipher.core.domain.model.TimeRange
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.dashboard.DashboardFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetDashboardDataUseCaseTest {

    private lateinit var repository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var useCase: GetDashboardDataUseCase

    private val timeRange = TimeRange.from(TimePeriod.THIS_MONTH)
    private val previousRange = TimeRange.previousEquivalentRange(TimePeriod.THIS_MONTH)!!

    private fun settings() = UserSettings(
        theme = AppTheme.SYSTEM,
        isBiometricEnabled = false,
        isPrivacyModeEnabled = false,
        isHapticsEnabled = true,
        currency = "INR",
        currencyCode = "INR",
        currencySymbol = "₹",
        autoLockTimeout = 0L,
        lastStopTime = 0L,
        monthlyBudget = 0.0
    )

    private fun tx(amount: Double, merchant: String, category: String, isIncome: Boolean = false, id: Long = 0L) = TransactionEntity(
        id = id,
        amount = amount,
        merchant = merchant,
        currency = "INR",
        category = category,
        timestamp = timeRange.startTime + 1000L,
        rawSms = null,
        isIncome = isIncome
    )

    private fun stub(
        transactions: List<TransactionEntity>,
        allTransactions: List<TransactionEntity> = transactions,
        rangeIncome: Double = 0.0,
        rangeExpense: Double = 0.0,
        prevExpense: Double? = null
    ) {
        whenever(repository.getTransactionsBetween(timeRange.startTime, timeRange.endTime)).thenReturn(flowOf(transactions))
        whenever(repository.getAllTransactions()).thenReturn(flowOf(allTransactions))
        whenever(repository.getTotalIncomeBetween(timeRange.startTime, timeRange.endTime)).thenReturn(flowOf(rangeIncome))
        whenever(repository.getTotalExpensesBetween(timeRange.startTime, timeRange.endTime)).thenReturn(flowOf(rangeExpense))
        whenever(repository.getTotalExpensesBetween(previousRange.startTime, previousRange.endTime)).thenReturn(flowOf(prevExpense))
        whenever(categoryRepository.getAllCustomCategoriesFlow()).thenReturn(flowOf(emptyList()))
        whenever(userPreferences.settingsFlow).thenReturn(flowOf(settings()))
    }

    @Before
    fun setup() {
        repository = mock()
        categoryRepository = mock()
        userPreferences = mock()
        useCase = GetDashboardDataUseCase(repository, categoryRepository, userPreferences)
    }

    private fun invoke(query: String = "", filter: DashboardFilter = DashboardFilter()) = runBlocking {
        useCase(query, filter, timeRange).first()
    }

    // ─── filtering ───────────────────────────────────────────────────────

    @Test
    fun `filter type EXPENSE excludes income transactions`() {
        stub(listOf(tx(100.0, "A", "FOOD"), tx(500.0, "B", "INCOME", isIncome = true)))

        val state = invoke(filter = DashboardFilter(type = DashboardContract.FilterType.EXPENSE))

        assertEquals(1, state.transactions.size)
        assertFalse(state.transactions[0].isIncome)
    }

    @Test
    fun `filter type INCOME excludes expense transactions`() {
        stub(listOf(tx(100.0, "A", "FOOD"), tx(500.0, "B", "INCOME", isIncome = true)))

        val state = invoke(filter = DashboardFilter(type = DashboardContract.FilterType.INCOME))

        assertEquals(1, state.transactions.size)
        assertTrue(state.transactions[0].isIncome)
    }

    @Test
    fun `filter by selected categories only keeps matching transactions`() {
        stub(listOf(tx(100.0, "A", "FOOD"), tx(200.0, "B", "SHOPPING")))

        val state = invoke(filter = DashboardFilter(selectedCategories = setOf("FOOD")))

        assertEquals(1, state.transactions.size)
        assertEquals("FOOD", state.transactions[0].category)
    }

    @Test
    fun `filter by min amount excludes smaller transactions`() {
        stub(listOf(tx(50.0, "A", "FOOD"), tx(500.0, "B", "FOOD")))

        val state = invoke(filter = DashboardFilter(minAmount = 100.0))

        assertEquals(1, state.transactions.size)
        assertEquals(500.0, state.transactions[0].amount, 0.001)
    }

    @Test
    fun `filter by max amount excludes larger transactions`() {
        stub(listOf(tx(50.0, "A", "FOOD"), tx(500.0, "B", "FOOD")))

        val state = invoke(filter = DashboardFilter(maxAmount = 100.0))

        assertEquals(1, state.transactions.size)
        assertEquals(50.0, state.transactions[0].amount, 0.001)
    }

    @Test
    fun `min and max amount together define an inclusive range`() {
        stub(listOf(tx(50.0, "A", "FOOD"), tx(150.0, "B", "FOOD"), tx(500.0, "C", "FOOD")))

        val state = invoke(filter = DashboardFilter(minAmount = 100.0, maxAmount = 200.0))

        assertEquals(1, state.transactions.size)
        assertEquals(150.0, state.transactions[0].amount, 0.001)
    }

    @Test
    fun `no filter returns all transactions`() {
        stub(listOf(tx(50.0, "A", "FOOD"), tx(500.0, "B", "SHOPPING", isIncome = true)))

        val state = invoke()

        assertEquals(2, state.transactions.size)
    }

    // ─── search query ────────────────────────────────────────────────────

    @Test
    fun `search query matches merchant name case insensitively`() {
        // getTransactionsBetween is mocked to return the full list regardless of query;
        // the use case itself applies the merchant/category filter on top of that.
        stub(listOf(tx(100.0, "Zomato", "FOOD"), tx(200.0, "Amazon", "SHOPPING")))

        val state = invoke(query = "zomato")

        assertEquals(1, state.transactions.size)
        assertEquals("Zomato", state.transactions[0].merchant)
    }

    @Test
    fun `search query also matches category`() {
        stub(listOf(tx(100.0, "Zomato", "FOOD"), tx(200.0, "Amazon", "SHOPPING")))

        val state = invoke(query = "shopping")

        assertEquals(1, state.transactions.size)
        assertEquals("Amazon", state.transactions[0].merchant)
    }

    // ─── totals and state fields ─────────────────────────────────────────

    @Test
    fun `has any transactions is true when the full ledger is non empty even if range is filtered empty`() {
        stub(transactions = emptyList(), allTransactions = listOf(tx(10.0, "A", "FOOD")))

        val state = invoke()

        assertTrue(state.hasAnyTransactions)
        assertTrue(state.transactions.isEmpty())
    }

    @Test
    fun `has any transactions is false when the ledger is completely empty`() {
        stub(transactions = emptyList(), allTransactions = emptyList())

        val state = invoke()

        assertFalse(state.hasAnyTransactions)
    }

    @Test
    fun `total balance is income minus expenses`() {
        stub(emptyList(), rangeIncome = 1000.0, rangeExpense = 400.0)

        val state = invoke()

        assertEquals(600.0, state.totalBalance, 0.001)
    }

    @Test
    fun `currency code and symbol are passed through from settings`() {
        stub(emptyList())

        val state = invoke()

        assertEquals("INR", state.currencyCode)
        assertEquals("₹", state.currencySymbol)
    }

    // ─── previous-period comparison ──────────────────────────────────────

    @Test
    fun `expense comparison percent is computed against the previous period`() {
        stub(emptyList(), rangeExpense = 150.0, prevExpense = 100.0)

        val state = invoke()

        assertEquals(50.0, state.expenseComparisonPercent!!, 0.001)
    }

    @Test
    fun `expense comparison is null when there is no previous period data`() {
        stub(emptyList(), rangeExpense = 150.0, prevExpense = null)

        val state = invoke()

        assertNull(state.expenseComparisonPercent)
    }

    @Test
    fun `expense comparison is null when previous period expense was zero`() {
        stub(emptyList(), rangeExpense = 150.0, prevExpense = 0.0)

        val state = invoke()

        assertNull(state.expenseComparisonPercent)
    }

    @Test
    fun `spending less than previous period yields a negative comparison percent`() {
        stub(emptyList(), rangeExpense = 50.0, prevExpense = 100.0)

        val state = invoke()

        assertEquals(-50.0, state.expenseComparisonPercent!!, 0.001)
    }
}
