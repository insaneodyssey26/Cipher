package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserSettingsProvider
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.notifications.TransactionNotifier
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessIncomingTransactionUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val categorizerEngine: CategorizerEngine,
    private val transactionNotifier: TransactionNotifier,
    private val userSettingsProvider: UserSettingsProvider,
    private val widgetSyncer: WidgetSyncer
) {
    suspend operator fun invoke(transaction: TransactionEntity): TransactionEntity? {
        val timeWindow = 60_000L
        val startTime = transaction.timestamp - timeWindow
        val endTime = transaction.timestamp + timeWindow

        val duplicate = transactionDao.findDuplicate(transaction.amount, startTime, endTime)
        if (duplicate != null) {
            return null
        }

        val rawMerchant = transaction.merchant.uppercase().trim()
        val alias = merchantAliasDao.getAliasForRawName(rawMerchant)
        val finalMerchant: String
        val finalCategory: String

        if (alias != null) {
            finalMerchant = alias.cleanName
            val savedCategory = categoryRuleDao.getCategoryForMerchant(finalMerchant)
            finalCategory = transaction.category.ifBlank {
                savedCategory ?: categorizerEngine.categorize(finalMerchant).name
            }
        } else {
            val cleanName = categorizerEngine.cleanMerchantName(transaction.merchant)
            val savedCategory = categoryRuleDao.getCategoryForMerchant(cleanName)
            val autoCategory = categorizerEngine.categorize(cleanName)

            if (cleanName != transaction.merchant) {
                merchantAliasDao.insertAlias(MerchantAliasEntity(rawMerchant, cleanName))
            }
            finalMerchant = cleanName
            finalCategory = transaction.category.ifBlank {
                savedCategory ?: autoCategory.name
            }
        }

        val start = monthStart()
        val previousSpent = transactionDao.sumExpensesSince(start)

        val newTx = transaction.copy(
            merchant = finalMerchant,
            category = finalCategory
        )
        val insertedId = transactionDao.insertTransaction(newTx)
        val savedTx = newTx.copy(id = insertedId)

        widgetSyncer.syncWidget()
        val settings = userSettingsProvider.settingsFlow.first()
        if (settings.notifyAllTransactions) {
            transactionNotifier.showNewTransactionNotification(savedTx)
        }
        checkBudgetAlert(previousSpent)

        if (finalCategory == TransactionCategory.OTHERS.name) {
            val count = transactionDao.getUncategorizedCount()
            if (count > 0) {
                transactionNotifier.showUncategorizedReminderNotification(count)
            }
        }

        return savedTx
    }

    private suspend fun checkBudgetAlert(previousSpent: Double) {
        val settings = userSettingsProvider.settingsFlow.first()
        val baseBudget = settings.monthlyBudget
        if (baseBudget <= 0) return

        val start = monthStart()
        val totalIncome = if (settings.isDynamicBudgetEnabled) transactionDao.sumIncomeSince(start) else 0.0
        val budget = baseBudget + totalIncome
        val newSpent = transactionDao.sumExpensesSince(start)

        if (budget in previousSpent..<newSpent) {
            transactionNotifier.showBudgetAlertNotification(isExceeded = true, amount = newSpent - budget, threshold = 100)
        } else if ((budget * 0.9) in previousSpent..<newSpent) {
            transactionNotifier.showBudgetAlertNotification(isExceeded = false, amount = budget - newSpent, threshold = 90)
        } else if ((budget * 0.5) in previousSpent..<newSpent) {
            transactionNotifier.showBudgetAlertNotification(isExceeded = false, amount = budget - newSpent, threshold = 50)
        }
    }

    private fun monthStart(): Long = com.masum.cipher.core.util.DateTimeUtils.currentMonthStart()
}
