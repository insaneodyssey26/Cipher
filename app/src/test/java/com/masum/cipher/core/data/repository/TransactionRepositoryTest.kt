package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.ProcessIncomingTransactionUseCase
import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class TransactionRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var widgetSyncManager: WidgetSyncManager
    private lateinit var repository: TransactionRepository

    private val outflow = TransactionEntity(
        amount = 1500.0,
        merchant = "Transfer to ICICI Bank",
        currency = "INR",
        timestamp = 10_000L,
        category = "TRANSFER",
        rawSms = null,
        isIncome = false,
        accountId = 1L
    )

    private val inflow = outflow.copy(
        merchant = "Transfer from HDFC Bank",
        timestamp = 10_001L,
        isIncome = true,
        accountId = 2L
    )

    @Before
    fun setup() {
        transactionDao = mock()
        widgetSyncManager = mock()
        repository = TransactionRepository(
            transactionDao = transactionDao,
            processIncomingTransactionUseCase = mock<ProcessIncomingTransactionUseCase>(),
            widgetSyncManager = widgetSyncManager
        )
    }

    @Test
    fun transferPairIsWrittenInOneDaoCallWithOutflowBeforeInflow() {
        runBlocking {
            repository.insertTransferPair(outflow, inflow)

            val captor = argumentCaptor<List<TransactionEntity>>()
            verify(transactionDao, times(1)).insertTransactions(captor.capture())
            assertEquals(listOf(outflow, inflow), captor.firstValue)
            verify(transactionDao, never()).insertTransaction(any())
        }
    }

    @Test
    fun transferPairSyncsWidgetsExactlyOnce() {
        runBlocking {
            repository.insertTransferPair(outflow, inflow)

            verify(widgetSyncManager, times(1)).syncWidget()
        }
    }
}
