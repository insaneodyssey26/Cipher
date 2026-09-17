package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.domain.model.AccountItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    fun getAllAccountsFlow(): Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()

    fun getAllAccountsWithBalancesFlow(): Flow<List<AccountItem>> {
        return combine(accountDao.getAllAccountsFlow(), transactionDao.getAllTransactions()) { accounts, transactions ->
            if (accounts.isEmpty()) {
                val defaultAcc = AccountEntity(
                    id = 1,
                    name = "Main Account",
                    type = "BANK",
                    initialBalance = 0.0,
                    colorHex = 0xFF4F46E5,
                    iconName = "Landmark",
                    isDefault = true
                )
                val defaultTxs = transactions.filter { it.accountId == null || it.accountId == 1L }
                val income = defaultTxs.filter { it.isIncome }.sumOf { it.amount }
                val expense = defaultTxs.filter { !it.isIncome }.sumOf { it.amount }
                listOf(AccountItem(defaultAcc, income - expense, defaultTxs.size))
            } else {
                val defaultAccountId = accounts.find { it.isDefault }?.id ?: accounts.first().id
                accounts.map { account ->
                    val accTxs = transactions.filter {
                        it.accountId == account.id || (it.accountId == null && account.id == defaultAccountId)
                    }
                    val income = accTxs.filter { it.isIncome }.sumOf { it.amount }
                    val expense = accTxs.filter { !it.isIncome }.sumOf { it.amount }
                    val currentBal = account.initialBalance + (income - expense)
                    AccountItem(account, currentBal, accTxs.size)
                }
            }
        }
    }

    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    fun getAccountByIdFlow(id: Long): Flow<AccountEntity?> = accountDao.getAccountByIdFlow(id)

    suspend fun getDefaultAccount(): AccountEntity? = accountDao.getDefaultAccount()

    suspend fun createAccount(account: AccountEntity): Long {
        val id = accountDao.insertAccount(account)
        if (account.isDefault) {
            accountDao.clearOtherDefaults(id)
        }
        return id
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
        if (account.isDefault) {
            accountDao.clearOtherDefaults(account.id)
        }
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
        val remaining = accountDao.getAllAccounts()
        if (remaining.isNotEmpty() && remaining.none { it.isDefault }) {
            accountDao.setAccountAsDefault(remaining.first().id)
        }
    }

    suspend fun setDefaultAccount(id: Long) {
        accountDao.setAccountAsDefault(id)
        accountDao.clearOtherDefaults(id)
    }

    suspend fun getAccountCount(): Int = accountDao.getAccountCount()
}
