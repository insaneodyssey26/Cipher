package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.AccountFlowTotals
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

    fun getAllAccountsWithBalancesFlow(): Flow<List<AccountItem>> =
        combine(accountDao.getAllAccountsFlow(), transactionDao.getAccountFlowTotals(), ::buildAccountItems)

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

private const val FALLBACK_ACCOUNT_ID = 1L

internal fun buildAccountItems(accounts: List<AccountEntity>, totals: List<AccountFlowTotals>): List<AccountItem> {
    val totalsByAccount = totals.associateBy { it.accountId }
    val unassigned = totalsByAccount[null]

    if (accounts.isEmpty()) {
        val fallbackAccount = AccountEntity(
            id = FALLBACK_ACCOUNT_ID,
            name = "Main Account",
            type = "BANK",
            initialBalance = 0.0,
            colorHex = 0xFF4F46E5,
            iconName = "Landmark",
            isDefault = true
        )
        return listOf(accountItem(fallbackAccount, listOfNotNull(unassigned, totalsByAccount[FALLBACK_ACCOUNT_ID])))
    }

    val rootAccountId = accounts.minByOrNull { it.createdAt }?.id ?: accounts.first().id
    return accounts.map { account ->
        val owned = listOfNotNull(
            totalsByAccount[account.id],
            unassigned.takeIf { account.id == rootAccountId }
        )
        accountItem(account, owned)
    }
}

private fun accountItem(account: AccountEntity, owned: List<AccountFlowTotals>): AccountItem =
    AccountItem(
        entity = account,
        currentBalance = account.initialBalance + owned.sumOf { it.income } - owned.sumOf { it.expense },
        transactionCount = owned.sumOf { it.transactionCount }
    )
