package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.notifications.LocalNotificationManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessIncomingTransactionUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val accountDao: AccountDao?,
    private val categorizerEngine: CategorizerEngine,
    private val localNotificationManager: LocalNotificationManager?,
    private val userPreferences: UserPreferences?,
    private val widgetSyncManager: WidgetSyncManager?
) {
    internal var onSyncWidget: (suspend () -> Unit)? = null
    internal var onGetSettings: (suspend () -> com.masum.cipher.core.data.local.pref.UserSettings)? = null
    internal var onNotifyNewTransaction: ((TransactionEntity) -> Unit)? = null
    internal var onNotifyUncategorized: ((Int) -> Unit)? = null
    internal var onNotifyBudgetAlert: ((isExceeded: Boolean, amount: Double, threshold: Int) -> Unit)? = null
    suspend operator fun invoke(transaction: TransactionEntity): TransactionEntity? {
        if (transaction.rawSms != null) {
            val timeWindow = 60_000L
            val startTime = transaction.timestamp - timeWindow
            val endTime = transaction.timestamp + timeWindow

            val duplicate = transactionDao.findDuplicate(transaction.amount, transaction.isIncome, transaction.merchant.trim(), startTime, endTime)
            if (duplicate != null) {
                return null
            }
        }

        val rawMerchant = transaction.merchant.trim()
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

        val settings = onGetSettings?.invoke() ?: userPreferences?.settingsFlow?.first()
        val isPro = settings?.isPro == true

        val resolvedAccountId = transaction.accountId ?: run {
            if (accountDao != null) {
                val accounts = accountDao.getAllAccounts()
                val activeAccounts = if (isPro || accounts.size <= 2) {
                    accounts
                } else {
                    val defaultAcc = accounts.firstOrNull { it.isDefault } ?: accounts.first()
                    val secondAcc = accounts.firstOrNull { it.id != defaultAcc.id }
                    listOfNotNull(defaultAcc, secondAcc)
                }
                resolveAccount(transaction.rawSms.orEmpty(), activeAccounts)
            } else {
                null
            }
        }

        val newTx = transaction.copy(
            merchant = finalMerchant,
            category = finalCategory,
            accountId = resolvedAccountId
        )
        val insertedId = transactionDao.insertTransaction(newTx)
        val savedTx = newTx.copy(id = insertedId)

        onSyncWidget?.invoke() ?: widgetSyncManager?.syncWidget()
        if (settings?.notifyAllTransactions == true) {
            onNotifyNewTransaction?.invoke(savedTx) ?: localNotificationManager?.showNewTransactionNotification(savedTx)
        }
        checkBudgetAlert(previousSpent)

        if (finalCategory == TransactionCategory.OTHERS.name) {
            val count = transactionDao.getUncategorizedCount()
            if (count > 0) {
                onNotifyUncategorized?.invoke(count) ?: localNotificationManager?.showUncategorizedReminderNotification(count)
            }
        }

        return savedTx
    }

    private suspend fun checkBudgetAlert(previousSpent: Double) {
        val settings = onGetSettings?.invoke() ?: userPreferences?.settingsFlow?.first()
        val baseBudget = settings?.monthlyBudget ?: 0.0
        if (baseBudget <= 0) return

        val start = monthStart()
        val totalIncome = if (settings?.isDynamicBudgetEnabled == true) transactionDao.sumIncomeSince(start) else 0.0
        val budget = baseBudget + totalIncome
        val newSpent = transactionDao.sumExpensesSince(start)

        if (budget in previousSpent..<newSpent) {
            onNotifyBudgetAlert?.invoke(true, newSpent - budget, 100) ?: localNotificationManager?.showBudgetAlertNotification(isExceeded = true, amount = newSpent - budget, threshold = 100)
        } else if ((budget * 0.9) in previousSpent..<newSpent) {
            onNotifyBudgetAlert?.invoke(false, budget - newSpent, 90) ?: localNotificationManager?.showBudgetAlertNotification(isExceeded = false, amount = budget - newSpent, threshold = 90)
        } else if ((budget * 0.5) in previousSpent..<newSpent) {
            onNotifyBudgetAlert?.invoke(false, budget - newSpent, 50) ?: localNotificationManager?.showBudgetAlertNotification(isExceeded = false, amount = budget - newSpent, threshold = 50)
        }
    }

    private fun monthStart(): Long = com.masum.cipher.core.util.DateTimeUtils.currentMonthStart()

    private fun resolveAccount(
        rawMessage: String,
        accounts: List<AccountEntity>
    ): Long? {
        if (accounts.isEmpty()) return null

        val digitPatterns = listOf(
            java.util.regex.Pattern.compile("(?i)(?:a/c|acct|account|card|ending|ending with|ending in|no\\.?|num|xx|x{2,}|[*]{2,}|\\.{2,})\\s*[:#.-]?\\s*[*xX.]*(\\d{3,4})\\b"),
            java.util.regex.Pattern.compile("(?i)[*xX]{2,}(\\d{3,4})\\b"),
            java.util.regex.Pattern.compile("(?i)\\b(\\d{4})\\s*(?:is debited|was debited|is credited|was credited|used at|spent on)")
        )

        var extractedDigits: String? = null
        if (rawMessage.isNotBlank()) {
            for (pattern in digitPatterns) {
                val matcher = pattern.matcher(rawMessage)
                if (matcher.find()) {
                    val candidate = matcher.group(1)?.trim()
                    if (!candidate.isNullOrBlank()) {
                        extractedDigits = candidate
                        break
                    }
                }
            }
        }

        if (extractedDigits != null) {
            val matchedByDigits = accounts.firstOrNull { acc ->
                val accLast4 = acc.accountNumberLast4?.trim()
                if (accLast4.isNullOrBlank()) false
                else accLast4 == extractedDigits ||
                    (accLast4.length >= 3 && extractedDigits.endsWith(accLast4)) ||
                    (extractedDigits.length >= 3 && accLast4.endsWith(extractedDigits))
            }
            if (matchedByDigits != null) {
                return matchedByDigits.id
            }
        }

        if (rawMessage.isNotBlank()) {
            val isCreditCardText = rawMessage.contains("credit card", ignoreCase = true) ||
                rawMessage.contains("cc ", ignoreCase = true) ||
                rawMessage.contains("card ending", ignoreCase = true)
            val isWalletText = rawMessage.contains("wallet", ignoreCase = true) ||
                rawMessage.contains("paytm", ignoreCase = true) ||
                rawMessage.contains("upi", ignoreCase = true)

            val matchedByName = accounts.filter { acc ->
                val cleanAccName = acc.name.trim().lowercase()
                if (cleanAccName.length < 2) false
                else {
                    val words = cleanAccName.split(" ").filter { it.length >= 3 }
                    val matchesWhole = rawMessage.contains(cleanAccName, ignoreCase = true)
                    val matchesWord = words.any { word ->
                        rawMessage.contains(Regex("(?i)\\b${Regex.escape(word)}\\b"))
                    }
                    matchesWhole || matchesWord
                }
            }.maxByOrNull { acc ->
                var score = acc.name.length
                if (isCreditCardText && acc.type == "CREDIT_CARD") score += 10
                if (isWalletText && acc.type == "WALLET") score += 10
                if (acc.isDefault) score += 1
                score
            }

            if (matchedByName != null) {
                return matchedByName.id
            }
        }

        val defaultAcc = accounts.firstOrNull { it.isDefault } ?: accounts.firstOrNull()
        return defaultAcc?.id
    }
}
