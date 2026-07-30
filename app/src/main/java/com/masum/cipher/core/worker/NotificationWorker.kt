package com.masum.cipher.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.notifications.LocalNotificationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun transactionDao(): TransactionDao
        fun notificationManager(): LocalNotificationManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val transactionDao = entryPoint.transactionDao()
        val notificationManager = entryPoint.notificationManager()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        if (hour in 18..23) {
            val startOfDay = getStartOfDay()
            val spentToday = transactionDao.sumExpensesSince(startOfDay)
            if (spentToday > 0) {
                val expenseCount = transactionDao.getExpensesCountSince(startOfDay)
                if (expenseCount > 0) {
                    notificationManager.showDailySummaryNotification(spentToday, expenseCount)
                }
            }
        }

        if (dayOfMonth == 1) {
            val lastMonth = getPreviousMonthName()
            notificationManager.showMonthlyWrappedNotification(lastMonth)
        }

        return Result.success()
    }

    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getPreviousMonthName(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: "Month"
    }
}
