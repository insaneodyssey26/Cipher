package com.masum.cipher.ui.goals

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.GoalRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    private lateinit var goalRepository: GoalRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: GoalsViewModel

    private val testGoal = GoalEntity(
        id = 1L,
        name = "New Car",
        targetAmount = 10000.0,
        savedAmount = 2500.0,
        colorHex = 0xFF4F46E5,
        iconName = "Car"
    )

    private val testAccount = AccountEntity(
        id = 42L,
        name = "Checking Account",
        type = "BANK",
        initialBalance = 5000.0,
        isDefault = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        goalRepository = mock()
        accountRepository = mock()
        transactionRepository = mock()
        userPreferences = mock()

        val mockSettings = mock<UserSettings>()
        whenever(mockSettings.currencySymbol).thenReturn("₹")
        whenever(mockSettings.isHapticsEnabled).thenReturn(true)
        whenever(mockSettings.isPro).thenReturn(true)

        whenever(goalRepository.allGoals).thenReturn(flowOf(listOf(testGoal)))
        whenever(accountRepository.getAllAccountsFlow()).thenReturn(flowOf(listOf(testAccount)))
        whenever(userPreferences.settingsFlow).thenReturn(flowOf(mockSettings))
        whenever(userPreferences.getCachedCurrencyCode()).thenReturn("INR")

        viewModel = GoalsViewModel(
            goalRepository = goalRepository,
            accountRepository = accountRepository,
            transactionRepository = transactionRepository,
            userPreferences = userPreferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateCombinesGoalsAccountsAndSettings() {
        val state = viewModel.state.value
        assertEquals(1, state.goals.size)
        assertEquals(1, state.accounts.size)
        assertEquals(2500.0, state.totalSaved, 0.001)
        assertEquals(10000.0, state.totalTarget, 0.001)
        assertEquals("₹", state.currencySymbol)
        assertTrue(state.isPro)
        assertFalse(state.isLoading)
    }

    @Test
    fun adjustSavedAmountWithoutAccountDoesNotCreateTransaction() {
        runBlocking {
            whenever(goalRepository.getGoalById(1L)).thenReturn(testGoal)

            viewModel.handleIntent(GoalsContract.Intent.AdjustSavedAmount(id = 1L, delta = 500.0, accountId = null))

            verify(goalRepository).adjustSavedAmount(1L, 500.0)
            verify(transactionRepository, never()).insertTransaction(any())
        }
    }

    @Test
    fun adjustSavedAmountDepositWithAccountCreatesExpenseTransaction() {
        runBlocking {
            whenever(goalRepository.getGoalById(1L)).thenReturn(testGoal)

            viewModel.handleIntent(GoalsContract.Intent.AdjustSavedAmount(id = 1L, delta = 500.0, accountId = 42L))

            verify(goalRepository).adjustSavedAmount(1L, 500.0)

            val captor = argumentCaptor<TransactionEntity>()
            verify(transactionRepository).insertTransaction(captor.capture())

            val tx = captor.firstValue
            assertEquals(500.0, tx.amount, 0.001)
            assertEquals(42L, tx.accountId)
            assertFalse(tx.isIncome) // Deposit into goal = expense from account
            assertEquals("Goal: New Car", tx.merchant)
            assertEquals("INVESTMENT", tx.category)
        }
    }

    @Test
    fun adjustSavedAmountWithdrawalWithAccountCreatesIncomeTransaction() {
        runBlocking {
            whenever(goalRepository.getGoalById(1L)).thenReturn(testGoal)

            viewModel.handleIntent(GoalsContract.Intent.AdjustSavedAmount(id = 1L, delta = -300.0, accountId = 42L))

            verify(goalRepository).adjustSavedAmount(1L, -300.0)

            val captor = argumentCaptor<TransactionEntity>()
            verify(transactionRepository).insertTransaction(captor.capture())

            val tx = captor.firstValue
            assertEquals(300.0, tx.amount, 0.001)
            assertEquals(42L, tx.accountId)
            assertTrue(tx.isIncome) // Withdrawal from goal = income back into account
            assertEquals("Goal Withdrawal: New Car", tx.merchant)
            assertEquals("INVESTMENT", tx.category)
        }
    }
}
