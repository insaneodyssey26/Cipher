package com.masum.cipher.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.sms.TransactionParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationService : NotificationListenerService() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var transactionParser: TransactionParser

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(android.app.Notification.EXTRA_TEXT) ?: ""
        val bigText = extras.getString(android.app.Notification.EXTRA_BIG_TEXT) ?: ""
        
        val fullMessage = "$title $text $bigText".trim()
        if (fullMessage.isBlank()) return

        serviceScope.launch {
            val trackedApps = userPreferences.settingsFlow.first().trackedApps
            if (!trackedApps.contains(packageName)) return@launch

            val parsedTx = transactionParser.parse(fullMessage)
            if (parsedTx != null) {
                val transactionEntity = TransactionEntity(
                    merchant = parsedTx.merchant,
                    amount = parsedTx.amount,
                    currency = parsedTx.currency,
                    category = "", // Categorization engine will handle this in repo
                    isIncome = parsedTx.isIncome,
                    rawSms = fullMessage, // Save notification text here
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transactionEntity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
