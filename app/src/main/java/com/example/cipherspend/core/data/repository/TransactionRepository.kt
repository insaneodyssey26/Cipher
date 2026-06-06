package com.masum.cipher.core.data.repository

import android.content.Context
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.pref.WidgetDataStore
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.ui.widget.BudgetWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categorizerEngine: CategorizerEngine,
    @ApplicationContext private val context: Context
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
        syncWidget()
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        val existing = transactionDao.getTransactionById(transaction.id)
        if (existing != null && existing.merchant != transaction.merchant) {
            val rawKey = existing.merchant.uppercase().trim()
            if (rawKey != "MISCELLANEOUS") {
                merchantAliasDao.insertAlias(
                    MerchantAliasEntity(
                        rawName = rawKey,
                        cleanName = transaction.merchant,
                        isUserDefined = true
                    )
                )
            }
        }
        transactionDao.insertTransaction(transaction)
        syncWidget()
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        syncWidget()
    }

    private fun autoCleanMerchantName(raw: String): String {
        return raw.split("*", "-", "  ")
            .filter { it.isNotBlank() && it.length > 2 }
            .firstOrNull { it.any { char -> char.isLetter() } }
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
            ?: raw
    }

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> = transactionDao.getExpensesSince(startTime)

    private suspend fun syncWidget() {
        val spent = transactionDao.sumExpensesSince(monthStart())
        WidgetDataStore.update(context, spent)
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(BudgetWidget::class.java)
        ids.forEach { BudgetWidget().update(context, it) }
    }

    private fun monthStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
