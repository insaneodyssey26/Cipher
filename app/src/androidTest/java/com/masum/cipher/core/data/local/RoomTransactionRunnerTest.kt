package com.masum.cipher.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTransactionRunnerTest {

    private lateinit var database: AppDatabase
    private lateinit var runner: TransactionRunner

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runner = RoomTransactionRunner(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun transaction() = TransactionEntity(
        amount = 100.0,
        merchant = "Merchant",
        currency = "INR",
        timestamp = 1_000L,
        category = "OTHERS",
        rawSms = null,
        isIncome = false
    )

    private fun account(isDefault: Boolean) = AccountEntity(
        name = "Account",
        type = "BANK",
        initialBalance = 0.0,
        colorHex = 0xFF2563EB,
        iconName = "Landmark",
        isDefault = isDefault
    )

    @Test
    fun aFailureInsideTheBlockRollsBackEveryWrite() = runBlocking {
        val result = runCatching {
            runner.run {
                database.transactionDao().insertTransaction(transaction())
                database.accountDao().insertAccount(account(isDefault = true))
                error("simulated failure")
            }
        }

        assertTrue(result.isFailure)
        assertEquals(0, database.transactionDao().getAllTransactionsList().size)
        assertEquals(0, database.accountDao().getAccountCount())
    }

    @Test
    fun aSuccessfulBlockCommitsEveryWriteAndReturnsItsResult() = runBlocking {
        val id = runner.run {
            database.accountDao().insertAccount(account(isDefault = true))
            database.transactionDao().insertTransaction(transaction())
        }

        assertTrue(id > 0)
        assertEquals(1, database.transactionDao().getAllTransactionsList().size)
        assertEquals(1, database.accountDao().getAccountCount())
    }
}
