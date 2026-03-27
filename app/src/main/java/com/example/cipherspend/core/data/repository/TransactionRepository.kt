package com.example.cipherspend.core.data.repository

import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity
import com.example.cipherspend.core.domain.CategorizerEngine
import com.example.cipherspend.core.domain.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categorizerEngine: CategorizerEngine
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(limit)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        val rawMerchant = transaction.merchant.uppercase().trim()
        
        val alias = merchantAliasDao.getAliasForRawName(rawMerchant)
        val finalMerchant: String
        val finalCategory: String

        if (alias != null) {
            finalMerchant = alias.cleanName
            finalCategory = if (transaction.category.isBlank()) {
                categorizerEngine.categorize(alias.cleanName).name
            } else {
                transaction.category
            }
        } else {
            val cleanName = autoCleanMerchantName(transaction.merchant)
            val autoCategory = categorizerEngine.categorize(cleanName)
            
            if (cleanName != transaction.merchant) {
                merchantAliasDao.insertAlias(MerchantAliasEntity(rawMerchant, cleanName))
            }
            finalMerchant = cleanName
            finalCategory = if (transaction.category.isBlank()) autoCategory.name else transaction.category
        }

        transactionDao.insertTransaction(
            transaction.copy(
                merchant = finalMerchant,
                category = finalCategory
            )
        )
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    private fun autoCleanMerchantName(raw: String): String {
        return raw.split("*", "-", "  ")
            .filter { it.isNotBlank() && it.length > 2 }
            .firstOrNull { it.any { char -> char.isLetter() } }
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
            ?: raw
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> = transactionDao.getExpensesSince(startTime)
}
