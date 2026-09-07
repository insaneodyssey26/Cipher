package com.masum.cipher.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.sms.TransactionParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationService : NotificationListenerService() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var transactionParser: TransactionParser

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var packageInstallReceiver: PackageInstallReceiver

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        registerReceiver(packageInstallReceiver, filter)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val titleBig = extras.getCharSequence(android.app.Notification.EXTRA_TITLE_BIG)?.toString().orEmpty()
        val subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val textLines = extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES)?.joinToString(" ").orEmpty()
        val summaryText = extras.getCharSequence(android.app.Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        val infoText = extras.getCharSequence(android.app.Notification.EXTRA_INFO_TEXT)?.toString().orEmpty()

        val fullMessage = listOf(title, titleBig, text, bigText, subText, textLines, summaryText, infoText)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" ")
            .trim()
        if (fullMessage.isBlank()) return

        serviceScope.launch {
            val settings = userPreferences.settingsFlow.first()
            val trackedApps = settings.trackedApps
            if (!trackedApps.contains(packageName)) return@launch

            val parsedTx = transactionParser.parse(fullMessage, settings.currencyCode)
            if (parsedTx != null) {
                val transactionEntity = TransactionEntity(
                    merchant = parsedTx.merchant,
                    amount = parsedTx.amount,
                    currency = parsedTx.currency,
                    category = "",
                    isIncome = parsedTx.isIncome,
                    rawSms = fullMessage,
                    timestamp = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transactionEntity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageInstallReceiver)
        serviceJob.cancel()
    }
}
