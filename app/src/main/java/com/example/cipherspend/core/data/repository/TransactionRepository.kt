package com.example.cipherspend.core.data.repository

import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity
import com.example.cipherspend.core.domain.CategorizerEngine
import com.example.cipherspend.core.domain.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categorizerEngine: CategorizerEngine
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions().map { transactions ->
            transactions.map { it.withCleanData() }
        }
    }

    private suspend fun TransactionEntity.withCleanData(): TransactionEntity {
        val alias = merchantAliasDao.getAliasForRawName(this.merchant)
        return if (alias != null) {
            this.copy(
                merchant = alias.cleanName,
                category = this.category.ifBlank { categorizerEngine.categorize(alias.cleanName).name }
            )
        } else {
            val cleanName = autoCleanMerchantName(this.merchant)
            val category = categorizerEngine.categorize(cleanName)
            
            if (cleanName != this.merchant) {
                merchantAliasDao.insertAlias(MerchantAliasEntity(this.merchant, cleanName))
                this.copy(merchant = cleanName, category = category.name)
            } else {
                this.copy(category = category.name)
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

    suspend fun insertTransaction(transaction: TransactionEntity) {
        val category = if (transaction.category.isBlank()) {
            categorizerEngine.categorize(transaction.merchant).name
        } else transaction.category
        
        transactionDao.insertTransaction(transaction.copy(category = category))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getExpensesSince(startTime).map { transactions ->
            transactions.map { it.withCleanData() }
        }
    }
}
