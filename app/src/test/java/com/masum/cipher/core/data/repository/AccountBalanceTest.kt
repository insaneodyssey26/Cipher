package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.AccountFlowTotals
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.AccountItem
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class AccountBalanceTest {

    private fun account(id: Long, createdAt: Long, initialBalance: Double = 0.0) = AccountEntity(
        id = id,
        name = "Account $id",
        type = "BANK",
        initialBalance = initialBalance,
        colorHex = 0xFF2563EB,
        iconName = "Landmark",
        isDefault = id == 1L,
        createdAt = createdAt
    )

    private fun transaction(accountId: Long?, amount: Double, isIncome: Boolean) = TransactionEntity(
        amount = amount,
        merchant = "M",
        currency = "INR",
        timestamp = 1_000L,
        category = "OTHERS",
        rawSms = null,
        isIncome = isIncome,
        accountId = accountId
    )

    private fun totalsOf(transactions: List<TransactionEntity>): List<AccountFlowTotals> =
        transactions.groupBy { it.accountId }.map { (accountId, group) ->
            AccountFlowTotals(
                accountId = accountId,
                income = group.filter { it.isIncome }.sumOf { it.amount },
                expense = group.filter { !it.isIncome }.sumOf { it.amount },
                transactionCount = group.size
            )
        }

    private fun referenceBalances(accounts: List<AccountEntity>, transactions: List<TransactionEntity>): List<AccountItem> {
        if (accounts.isEmpty()) {
            val fallback = AccountEntity(
                id = 1, name = "Main Account", type = "BANK", initialBalance = 0.0,
                colorHex = 0xFF4F46E5, iconName = "Landmark", isDefault = true
            )
            val owned = transactions.filter { it.accountId == null || it.accountId == 1L }
            val balance = owned.filter { it.isIncome }.sumOf { it.amount } - owned.filter { !it.isIncome }.sumOf { it.amount }
            return listOf(AccountItem(fallback, balance, owned.size))
        }
        val rootAccountId = accounts.minByOrNull { it.createdAt }?.id ?: accounts.first().id
        return accounts.map { account ->
            val owned = transactions.filter {
                it.accountId == account.id || (it.accountId == null && account.id == rootAccountId)
            }
            val income = owned.filter { it.isIncome }.sumOf { it.amount }
            val expense = owned.filter { !it.isIncome }.sumOf { it.amount }
            AccountItem(account, account.initialBalance + (income - expense), owned.size)
        }
    }

    private fun assertSameAsReference(accounts: List<AccountEntity>, transactions: List<TransactionEntity>) {
        val expected = referenceBalances(accounts, transactions)
        val actual = buildAccountItems(accounts, totalsOf(transactions))

        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) ->
            assertEquals(e.entity.id, a.entity.id)
            assertEquals(e.currentBalance, a.currentBalance, 0.0001)
            assertEquals(e.transactionCount, a.transactionCount)
        }
    }

    @Test
    fun balanceIsInitialBalancePlusIncomeMinusExpense() {
        val accounts = listOf(account(1, createdAt = 10, initialBalance = 1000.0))
        val transactions = listOf(
            transaction(1, 500.0, isIncome = true),
            transaction(1, 200.0, isIncome = false)
        )

        val item = buildAccountItems(accounts, totalsOf(transactions)).single()

        assertEquals(1300.0, item.currentBalance, 0.0001)
        assertEquals(2, item.transactionCount)
    }

    @Test
    fun unassignedTransactionsBelongToTheOldestAccountOnly() {
        val accounts = listOf(account(5, createdAt = 200), account(2, createdAt = 100))
        val transactions = listOf(transaction(null, 300.0, isIncome = true))

        val items = buildAccountItems(accounts, totalsOf(transactions)).associateBy { it.entity.id }

        assertEquals(300.0, items.getValue(2).currentBalance, 0.0001)
        assertEquals(0.0, items.getValue(5).currentBalance, 0.0001)
    }

    @Test
    fun withNoAccountsAFallbackAccountOwnsUnassignedAndAccountOneTransactions() {
        val transactions = listOf(
            transaction(null, 100.0, isIncome = true),
            transaction(1, 40.0, isIncome = false),
            transaction(9, 999.0, isIncome = true)
        )

        val item = buildAccountItems(emptyList(), totalsOf(transactions)).single()

        assertEquals(1L, item.entity.id)
        assertEquals(60.0, item.currentBalance, 0.0001)
        assertEquals(2, item.transactionCount)
    }

    @Test
    fun accountWithoutTransactionsKeepsItsInitialBalance() {
        val accounts = listOf(account(1, createdAt = 10), account(2, createdAt = 20, initialBalance = 750.0))

        val items = buildAccountItems(accounts, emptyList()).associateBy { it.entity.id }

        assertEquals(750.0, items.getValue(2).currentBalance, 0.0001)
        assertEquals(0, items.getValue(2).transactionCount)
    }

    @Test
    fun matchesTheOriginalAlgorithmOnRandomisedData() {
        val random = Random(42)
        repeat(50) {
            val accountCount = random.nextInt(0, 5)
            val accounts = (1..accountCount).map { id ->
                account(id.toLong(), createdAt = random.nextLong(1, 1_000), initialBalance = random.nextDouble(0.0, 5_000.0))
            }
            val ids = listOf<Long?>(null, 1L, 2L, 3L, 99L)
            val transactions = (1..200).map {
                transaction(ids.random(random), random.nextDouble(1.0, 10_000.0), random.nextBoolean())
            }

            assertSameAsReference(accounts, transactions)
        }
    }
}
