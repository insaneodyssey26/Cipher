package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class TransferFundsUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var useCase: TransferFundsUseCase

    private val sourceAccount = AccountEntity(
        id = 1L,
        name = "HDFC Bank",
        type = "BANK",
        initialBalance = 5000.0,
        colorHex = 0xFF2563EB,
        iconName = "Landmark",
        accountNumberLast4 = "1234"
    )

    private val targetAccount = AccountEntity(
        id = 2L,
        name = "ICICI Bank",
        type = "BANK",
        initialBalance = 1000.0,
        colorHex = 0xFFDC2626,
        iconName = "Landmark",
        accountNumberLast4 = "5678"
    )

    @Before
    fun setup() {
        transactionRepository = mock()
        accountRepository = mock()
        useCase = TransferFundsUseCase(
            transactionRepository = transactionRepository,
            accountRepository = accountRepository
        )
    }

    @Test
    fun transferCreatesTwoMatchedTransactionsWithCorrectMetadata() = runBlocking {
        val (outflow, inflow) = useCase(
            fromAccount = sourceAccount,
            toAccount = targetAccount,
            amount = 1500.0,
            currency = "INR",
            timestamp = 10_000L,
            note = "Rent split",
            outflowMerchantText = "Transfer to ICICI Bank",
            inflowMerchantText = "Transfer from HDFC Bank"
        )

        val txCaptor = argumentCaptor<TransactionEntity>()
        verify(transactionRepository, times(2)).insertDirectTransaction(txCaptor.capture())

        val transactions = txCaptor.allValues
        assertEquals(2, transactions.size)

        assertEquals(1500.0, outflow.amount, 0.001)
        assertEquals("Transfer to ICICI Bank", outflow.merchant)
        assertEquals("TRANSFER", outflow.category)
        assertEquals("INR", outflow.currency)
        assertEquals(1L, outflow.accountId)
        assertEquals(10_000L, outflow.timestamp)
        assertFalse(outflow.isIncome)
        assertEquals("Rent split", outflow.note)

        assertEquals(1500.0, inflow.amount, 0.001)
        assertEquals("Transfer from HDFC Bank", inflow.merchant)
        assertEquals("TRANSFER", inflow.category)
        assertEquals("INR", inflow.currency)
        assertEquals(2L, inflow.accountId)
        assertEquals(10_001L, inflow.timestamp)
        assertTrue(inflow.isIncome)
        assertEquals("Rent split", inflow.note)

        assertEquals(outflow, transactions[0])
        assertEquals(inflow, transactions[1])
    }
}
