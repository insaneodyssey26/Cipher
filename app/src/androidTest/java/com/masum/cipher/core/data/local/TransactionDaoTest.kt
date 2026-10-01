package com.masum.cipher.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun transaction(accountId: Long?, amount: Double, isIncome: Boolean, timestamp: Long = 1_000L) =
        TransactionEntity(
            amount = amount,
            merchant = "Merchant",
            currency = "INR",
            timestamp = timestamp,
            category = "OTHERS",
            rawSms = null,
            isIncome = isIncome,
            accountId = accountId
        )

    @Test
    fun insertTransactionsPersistsEveryRowAndReturnsTheirIds() = runBlocking {
        val ids = dao.insertTransactions(
            listOf(transaction(1, 100.0, isIncome = false), transaction(2, 100.0, isIncome = true))
        )

        assertEquals(2, ids.size)
        ids.forEach { assertNotNull(dao.getTransactionById(it)) }
        assertEquals(2, dao.getAllTransactionsList().size)
    }

    @Test
    fun accountFlowTotalsGroupIncomeExpenseAndCountPerAccount() = runBlocking {
        dao.insertTransactions(
            listOf(
                transaction(1, 500.0, isIncome = true),
                transaction(1, 120.0, isIncome = false),
                transaction(1, 30.0, isIncome = false),
                transaction(2, 80.0, isIncome = true),
                transaction(null, 40.0, isIncome = false)
            )
        )

        val totals = dao.getAccountFlowTotals().first().associateBy { it.accountId }

        assertEquals(3, totals.size)
        assertEquals(500.0, totals.getValue(1L).income, 0.0001)
        assertEquals(150.0, totals.getValue(1L).expense, 0.0001)
        assertEquals(3, totals.getValue(1L).transactionCount)
        assertEquals(80.0, totals.getValue(2L).income, 0.0001)
        assertEquals(0.0, totals.getValue(2L).expense, 0.0001)
        assertEquals(40.0, totals.getValue(null).expense, 0.0001)
        assertEquals(1, totals.getValue(null).transactionCount)
    }

    @Test
    fun accountFlowTotalsIsEmptyWithoutTransactions() = runBlocking {
        assertEquals(emptyList<Any>(), dao.getAccountFlowTotals().first())
    }
}
