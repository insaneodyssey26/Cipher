package com.masum.cipher.core.domain.usecase

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.util.DateTimeUtils
import com.masum.cipher.ui.widget.AccountsWidget
import com.masum.cipher.ui.widget.BudgetWidget
import com.masum.cipher.ui.widget.DailyAllowanceWidget
import com.masum.cipher.ui.widget.PassbookWidget
import com.masum.cipher.ui.widget.QuickLogWidget
import com.masum.cipher.ui.widget.StatsWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val userPreferences: UserPreferences
) {

    suspend fun syncWidget() {
        val settings = userPreferences.settingsFlow.first()
        val isPro = settings.isPro
        val proTier = settings.proTier
        val baseBudget = settings.monthlyBudget
        val isDynamic = settings.isDynamicBudgetEnabled
        val budgetAccountId = settings.budgetAccountId

        val start = DateTimeUtils.currentMonthStart()
        val today = DateTimeUtils.todayStart()
        val allSpent = transactionDao.sumExpensesSince(start)
        val allIncome = transactionDao.sumIncomeSince(start)

        val spent = if (budgetAccountId != null) transactionDao.sumExpensesSinceForAccount(start, budgetAccountId) else allSpent
        val income = if (budgetAccountId != null) transactionDao.sumIncomeSinceForAccount(start, budgetAccountId) else allIncome
        val dailySpent = if (budgetAccountId != null) transactionDao.sumExpensesSinceForAccount(today, budgetAccountId) else transactionDao.sumExpensesSince(today)

        val effectiveBudget = if (isDynamic && baseBudget > 0) baseBudget + income else baseBudget
        val remainingDays = DateTimeUtils.remainingDaysInMonth()
        val remainingBudget = (effectiveBudget - spent).coerceAtLeast(0.0)
        val dailyAllowance = if (remainingDays > 0) remainingBudget / remainingDays else remainingBudget

        val accounts = accountDao.getAllAccounts()
        val allTransactions = transactionDao.getAllTransactionsList()
        val rootAccountId = accounts.minByOrNull { it.createdAt }?.id ?: accounts.firstOrNull()?.id ?: 1L

        val budgetAccountsJsonArray = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("id", -1L)
                    put("name", "All Accounts")
                    put("spent", allSpent)
                    put("income", allIncome)
                }
            )
            accounts.forEach { acc ->
                val accSpent = transactionDao.sumExpensesSinceForAccount(start, acc.id)
                val accIncome = transactionDao.sumIncomeSinceForAccount(start, acc.id)
                put(
                    JSONObject().apply {
                        put("id", acc.id)
                        put("name", acc.name)
                        put("spent", accSpent)
                        put("income", accIncome)
                    }
                )
            }
        }

        val targetAccount = accounts.find { it.id == budgetAccountId }
        val activeAccountName = targetAccount?.name ?: "All Accounts"

        val accountsJsonArray = JSONArray()
        var calculatedNetWorth = 0.0

        if (accounts.isEmpty()) {
            val defaultTxs = allTransactions.filter { it.accountId == null || it.accountId == 1L }
            val inc = defaultTxs.filter { it.isIncome }.sumOf { it.amount }
            val exp = defaultTxs.filter { !it.isIncome }.sumOf { it.amount }
            val bal = inc - exp
            calculatedNetWorth = bal
            accountsJsonArray.put(
                JSONObject().apply {
                    put("name", "Main Account")
                    put("type", "BANK")
                    put("balance", bal)
                    put("colorHex", 0xFF4F46E5L)
                }
            )
        } else {
            accounts.forEach { acc ->
                val accTxs = allTransactions.filter {
                    it.accountId == acc.id || (it.accountId == null && acc.id == rootAccountId)
                }
                val inc = accTxs.filter { it.isIncome }.sumOf { it.amount }
                val exp = accTxs.filter { !it.isIncome }.sumOf { it.amount }
                val currentBal = acc.initialBalance + (inc - exp)
                calculatedNetWorth += currentBal

                accountsJsonArray.put(
                    JSONObject().apply {
                        put("name", acc.name)
                        put("type", acc.type)
                        put("balance", currentBal)
                        put("colorHex", acc.colorHex)
                    }
                )
            }
        }

        val recentTxs = transactionDao.getRecentTransactionsList(50)
        val recentTxJsonArray = JSONArray()
        recentTxs.forEach { tx ->
            val accName = accounts.find { it.id == tx.accountId }?.name ?: "Main Account"
            recentTxJsonArray.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("merchant", tx.merchant)
                    put("amount", tx.amount)
                    put("isIncome", tx.isIncome)
                    put("category", tx.category)
                    put("accountId", tx.accountId ?: -1L)
                    put("accountName", accName)
                }
            )
        }

        val manager = GlanceAppWidgetManager(context)

        manager.getGlanceIds(BudgetWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.BUDGET_SPENT] = spent
                    this[WidgetKeys.BUDGET_INCOME] = income
                    this[WidgetKeys.BUDGET_ACCOUNT_NAME] = activeAccountName
                    this[WidgetKeys.BUDGET_ACCOUNTS_JSON] = budgetAccountsJsonArray.toString()
                }
            }
            BudgetWidget().update(context, id)
        }

        manager.getGlanceIds(StatsWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                val currentStatsAcc = prefs[WidgetKeys.STATS_ACCOUNT_NAME] ?: "All Accounts"
                var targetSpent = allSpent
                var targetIncome = allIncome
                for (i in 0 until budgetAccountsJsonArray.length()) {
                    val obj = budgetAccountsJsonArray.getJSONObject(i)
                    if (obj.optString("name") == currentStatsAcc) {
                        targetSpent = obj.optDouble("spent", allSpent)
                        targetIncome = obj.optDouble("income", allIncome)
                        break
                    }
                }
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.STATS_SPENT] = targetSpent
                    this[WidgetKeys.STATS_INCOME] = targetIncome
                    this[WidgetKeys.STATS_ACCOUNT_NAME] = currentStatsAcc
                    this[WidgetKeys.STATS_ACCOUNTS_JSON] = budgetAccountsJsonArray.toString()
                }
            }
            StatsWidget().update(context, id)
        }

        manager.getGlanceIds(AccountsWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.IS_PRO] = isPro
                    this[WidgetKeys.PRO_TIER] = proTier
                    this[WidgetKeys.NET_WORTH] = calculatedNetWorth
                    this[WidgetKeys.ACCOUNTS_JSON] = accountsJsonArray.toString()
                }
            }
            AccountsWidget().update(context, id)
        }

        manager.getGlanceIds(QuickLogWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.IS_PRO] = isPro
                    this[WidgetKeys.PRO_TIER] = proTier
                }
            }
            QuickLogWidget().update(context, id)
        }

        manager.getGlanceIds(PassbookWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                val currentPassbookAcc = prefs[WidgetKeys.PASSBOOK_ACCOUNT_NAME] ?: "All Accounts"
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.IS_PRO] = isPro
                    this[WidgetKeys.PRO_TIER] = proTier
                    this[WidgetKeys.PASSBOOK_ACCOUNT_NAME] = currentPassbookAcc
                    this[WidgetKeys.PASSBOOK_ACCOUNTS_JSON] = budgetAccountsJsonArray.toString()
                    this[WidgetKeys.RECENT_TX_JSON] = recentTxJsonArray.toString()
                }
            }
            PassbookWidget().update(context, id)
        }

        manager.getGlanceIds(DailyAllowanceWidget::class.java).forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetKeys.IS_PRO] = isPro
                    this[WidgetKeys.PRO_TIER] = proTier
                    this[WidgetKeys.DAILY_SPENT] = dailySpent
                    this[WidgetKeys.DAILY_ALLOWANCE] = dailyAllowance
                }
            }
            DailyAllowanceWidget().update(context, id)
        }
    }
}
