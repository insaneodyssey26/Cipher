package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.AutoBackupFrequency
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.worker.AutoBackupScheduler
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdateTransactionUseCaseTest {

    private lateinit var repository: TransactionRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var autoBackupScheduler: AutoBackupScheduler
    private lateinit var useCase: UpdateTransactionUseCase

    private fun settings(
        autoBackupEnabled: Boolean = false,
        autoBackupFrequency: AutoBackupFrequency = AutoBackupFrequency.NEVER
    ) = UserSettings(
        theme = AppTheme.SYSTEM,
        isBiometricEnabled = false,
        isPrivacyModeEnabled = false,
        isHapticsEnabled = true,
        currency = "INR",
        autoLockTimeout = 0L,
        lastStopTime = 0L,
        monthlyBudget = 0.0,
        autoBackupEnabled = autoBackupEnabled,
        autoBackupFrequency = autoBackupFrequency
    )

    private fun transaction(id: Long = 1L, merchant: String, category: String = "OTHERS") = TransactionEntity(
        id = id,
        amount = 100.0,
        merchant = merchant,
        currency = "INR",
        category = category,
        timestamp = 1_000L,
        rawSms = null,
        isIncome = false
    )

    @Before
    fun setup() {
        repository = mock()
        userPreferences = mock()
        autoBackupScheduler = mock()
        whenever(userPreferences.settingsFlow).thenReturn(flowOf(settings()))
        useCase = UpdateTransactionUseCase(repository, userPreferences, autoBackupScheduler)
    }

    @Test
    fun merchantRenameIsDetectedAndPromptHasOldAndNewNames() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "SWIGGY"))
        val updated = transaction(merchant = "Swiggy Ltd")

        val result = useCase(updated)

        assertTrue(result is TransactionUpdateResult.MerchantRenamed)
        val prompt = (result as TransactionUpdateResult.MerchantRenamed).prompt
        assertEquals("SWIGGY", prompt.rawMerchant)
        assertEquals("Swiggy Ltd", prompt.newMerchant)
    }

    @Test
    fun categoryChangeOnSameMerchantIsDetected() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO", category = "FOOD"))
        val updated = transaction(merchant = "ZOMATO", category = "SHOPPING")

        val result = useCase(updated)

        assertTrue(result is TransactionUpdateResult.CategoryChanged)
        assertEquals("SHOPPING", (result as TransactionUpdateResult.CategoryChanged).transaction.category)
    }

    @Test
    fun noChangeYieldsNoRulePrompt() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO", category = "FOOD"))
        val updated = transaction(merchant = "ZOMATO", category = "FOOD")

        val result = useCase(updated)

        assertEquals(TransactionUpdateResult.NoRulePrompt, result)
    }

    @Test
    fun merchantAndCategoryBothChangedOnlyReportsMerchantRename() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "SWIGGY", category = "FOOD"))
        val updated = transaction(merchant = "ZOMATO", category = "SHOPPING")

        val result = useCase(updated)

        assertTrue(result is TransactionUpdateResult.MerchantRenamed)
    }

    @Test
    fun caseOnlyMerchantChangeIsNotTreatedAsRename() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "swiggy"))
        val updated = transaction(merchant = "SWIGGY")

        val result = useCase(updated)

        assertEquals(TransactionUpdateResult.NoRulePrompt, result)
    }

    @Test
    fun missingExistingTransactionYieldsNoRulePrompt() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(null)
        val updated = transaction(merchant = "NEW MERCHANT", category = "SHOPPING")

        val result = useCase(updated)

        assertEquals(TransactionUpdateResult.NoRulePrompt, result)
    }

    @Test
    fun blankOldMerchantIsNotTreatedAsRename() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = ""))
        val updated = transaction(merchant = "ZOMATO")

        val result = useCase(updated)

        assertEquals(TransactionUpdateResult.NoRulePrompt, result)
    }

    @Test
    fun blankNewMerchantIsNotTreatedAsRename() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO"))
        val updated = transaction(merchant = "")

        val result = useCase(updated)

        assertEquals(TransactionUpdateResult.NoRulePrompt, result)
    }

    @Test
    fun repositoryUpdateIsAlwaysCalled() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO"))
        val updated = transaction(merchant = "ZOMATO")

        useCase(updated)

        verify(repository).updateTransaction(updated)
    }

    @Test
    fun autoBackupTriggersOnlyWhenEnabledForEveryChange() = runBlocking {
        whenever(userPreferences.settingsFlow).thenReturn(
            flowOf(settings(autoBackupEnabled = true, autoBackupFrequency = AutoBackupFrequency.EVERY_CHANGE))
        )
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO"))

        useCase(transaction(merchant = "ZOMATO"))

        verify(autoBackupScheduler).triggerImmediateBackup()
    }

    @Test
    fun autoBackupDoesNotTriggerWhenDisabled() = runBlocking {
        whenever(repository.getTransactionById(1L)).thenReturn(transaction(merchant = "ZOMATO"))

        useCase(transaction(merchant = "ZOMATO"))

        verify(autoBackupScheduler, never()).triggerImmediateBackup()
    }
}
