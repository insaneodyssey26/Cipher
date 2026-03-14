package com.example.cipherspend.core.data.repository

import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions().map { transactions ->
            transactions.map { it.withCleanName() }
        }
    }

    private suspend fun TransactionEntity.withCleanName(): TransactionEntity {
        val alias = merchantAliasDao.getAliasForRawName(this.merchant)
        return if (alias != null) {
            this.copy(merchant = alias.cleanName)
        } else {
            // Check for common patterns and auto-generate if not exists
            val cleanName = autoCleanMerchantName(this.merchant)
            if (cleanName != this.merchant) {
                merchantAliasDao.insertAlias(MerchantAliasEntity(this.merchant, cleanName))
                this.copy(merchant = cleanName)
            } else {
                this
            }
        }
    }

    private fun autoCleanMerchantName(raw: String): String {
        return raw.split("*", "-", "  ")
            .filter { it.isNotBlank() && it.length > 2 }
            .firstOrNull { it.any { char -> char.isLetter() } }
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
            ?: raw
    }

    suspend fun insertTransaction(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getExpensesSince(startTime).map { transactions ->
            transactions.map { it.withCleanName() }
        }
    }
}
