package com.masum.cipher.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.masum.cipher.core.data.local.pref.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationService : NotificationListenerService() {

    @Inject
    lateinit var userPreferences: UserPreferences

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
            // Only process if the app is tracked
            val trackedApps = userPreferences.settingsFlow.first().trackedApps
            if (!trackedApps.contains(packageName)) return@launch

            // TODO: In Phase 3, we will pass `fullMessage` to the Universal Parser
            // val parsedTx = transactionParser.parse(fullMessage)
            // if (parsedTx != null) { ... }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
