package com.masum.cipher.core.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.notifications.LocalNotificationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SubscriptionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun subscriptionDao(): SubscriptionDao
        fun transactionDao(): TransactionDao
        fun notificationManager(): LocalNotificationManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val subscriptionDao = entryPoint.subscriptionDao()
        val notificationManager = entryPoint.notificationManager()
        
        val currentTime = System.currentTimeMillis()
        val subscriptions = subscriptionDao.getAllSubscriptions().first()

        for (subscription in subscriptions) {
            if (subscription.nextExpectedDate <= currentTime) {
                notificationManager.showSubscriptionPendingNotification(subscription)
            }
        }

        return Result.success()
    }
}
