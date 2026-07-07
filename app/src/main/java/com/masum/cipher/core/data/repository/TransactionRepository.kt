package com.masum.cipher.core.data.repository

import android.content.Context
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.ui.widget.BudgetWidget
import com.masum.cipher.ui.widget.StatsWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
            val cleanName = categorizerEngine.cleanMerchantName(transaction.merchant)
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

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()
    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double?> =
        transactionDao.getTotalExpensesBetween(startTime, endTime)

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> =
        transactionDao.getTotalIncomeBetween(startTime, endTime)

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> = transactionDao.getExpensesSince(startTime)

    suspend fun refreshWidgets() = syncWidget()

    private suspend fun syncWidget() {
        val start = monthStart()
        val spent = transactionDao.sumExpensesSince(start)
        val income = transactionDao.sumIncomeSince(start)
        val manager = GlanceAppWidgetManager(context)

        manager.getGlanceIds(BudgetWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply { this[WidgetKeys.BUDGET_SPENT] = spent }
            }
            BudgetWidget().update(context, id)
        }

        manager.getGlanceIds(StatsWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.STATS_SPENT] = spent
                    this[WidgetKeys.STATS_INCOME] = income
                }
            }
            StatsWidget().update(context, id)
        }
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
