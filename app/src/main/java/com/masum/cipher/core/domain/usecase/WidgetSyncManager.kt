package com.masum.cipher.core.domain.usecase

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.ui.widget.BudgetWidget
import com.masum.cipher.ui.widget.StatsWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao
) {

    suspend fun syncWidget() {
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

    private fun monthStart(): Long = com.masum.cipher.core.util.DateTimeUtils.currentMonthStart()
}
