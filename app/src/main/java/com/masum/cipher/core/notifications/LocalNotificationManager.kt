package com.masum.cipher.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.graphics.toColorInt
import com.masum.cipher.MainActivity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {

    companion object {
        const val CHANNEL_ID = "cipher_app_alerts"
        const val CHANNEL_TRANSACTIONS = "cipher_transactions"
        const val CHANNEL_BUDGET = "cipher_budget"
        const val CHANNEL_SUMMARIES = "cipher_summaries"
        const val CHANNEL_SUBSCRIPTIONS = "cipher_subscriptions"
        const val CHANNEL_REMINDERS = "cipher_reminders"
        const val CHANNEL_SYSTEM = "cipher_system"
        const val NOTIFICATION_ID_NEW_APP = 1001
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(CHANNEL_TRANSACTIONS, "Transactions", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts for newly detected transactions"
                },
                NotificationChannel(CHANNEL_BUDGET, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Warnings when approaching or exceeding monthly limits"
                },
                NotificationChannel(CHANNEL_SUMMARIES, "Spending Summaries", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Daily spending summaries and monthly wrap-ups"
                },
                NotificationChannel(CHANNEL_SUBSCRIPTIONS, "Subscriptions", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Upcoming subscription dues and auto-log alerts"
                },
                NotificationChannel(CHANNEL_REMINDERS, "Action Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Nudges to review uncategorized transactions"
                },
                NotificationChannel(CHANNEL_SYSTEM, "App & System Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "New app suggestions and statement exports"
                },
                NotificationChannel(CHANNEL_ID, "Cipher Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General notifications"
                }
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    fun showNewAppDetectedNotification(appName: String, packageName: String) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyNewAppDetected) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "manage_apps")
                putExtra("suggested_package", packageName)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor("#4F46E5".toColorInt())
                .setContentTitle("New Payment App Detected")
                .setContentText("Would you like Cipher to track transactions from $appName?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_NEW_APP, builder.build())
            }
        }
    }

    fun showPdfGeneratedNotification(uri: android.net.Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor("#4F46E5".toColorInt())
            .setContentTitle("Statement Generated")
            .setContentText("Your PDF statement is ready. Tap to view.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1002, builder.build())
        }
    }

    fun showBudgetAlertNotification(isExceeded: Boolean, amount: Double, threshold: Int) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyBudgetAlerts) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val currencySymbol = settings.currencySymbol
            val formattedAmount = com.masum.cipher.core.util.AppFormatters.formatCurrency(amount, currencySymbol, decimals = 0)
            val title = if (isExceeded) "Budget Exceeded" else "Budget Alert ($threshold%)"
            val text = if (isExceeded) "You have exceeded your monthly budget by $formattedAmount." 
                       else if (threshold == 90) "You've used 90% of your budget! Only $formattedAmount remaining."
                       else "You've used 50% of your budget. $formattedAmount remaining."
                        
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor(if (isExceeded) "#F43F5E".toColorInt() else "#F59E0B".toColorInt())
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(1003, builder.build()) }
        }
    }

    fun showDailySummaryNotification(spent: Double, count: Int) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyDailySummary) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val currencySymbol = settings.currencySymbol
            val formattedSpent = com.masum.cipher.core.util.AppFormatters.formatCurrency(spent, currencySymbol, decimals = 0)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARIES)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor("#10B981".toColorInt())
                .setContentTitle("Daily Summary")
                .setContentText("You spent $formattedSpent today across $count transactions. Tap to review.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(1004, builder.build()) }
        }
    }

    fun showMonthlyWrappedNotification(monthName: String) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyMonthlyWrapped) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARIES)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor("#4F46E5".toColorInt())
                .setContentTitle("Your $monthName Wrap-up")
                .setContentText("Your spending report for $monthName is ready! See how you did.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(1005, builder.build()) }
        }
    }

    fun showUncategorizedReminderNotification(count: Int) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyUncategorizedReminder) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor("#F59E0B".toColorInt())
                .setContentTitle("Action Needed")
                .setContentText("You have $count new transactions waiting to be categorized.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(1006, builder.build()) }
        }
    }

    fun showNewTransactionNotification(transaction: TransactionEntity) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifyAllTransactions) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val notificationId = transaction.id.toInt()

            val categorizeIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "transaction_details")
                putExtra("transaction_id", transaction.id)
            }
            val categorizePendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1000,
                categorizeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_TEXT_REPLY)
                .setLabel("Add a note...")
                .build()

            val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_ADD_NOTE
                putExtra(NotificationActionReceiver.EXTRA_TRANSACTION_ID, transaction.id)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }

            val replyPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 2000,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val addNoteAction = NotificationCompat.Action.Builder(
                com.masum.cipher.R.drawable.ic_notification,
                "Add Note",
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            val categorizeAction = NotificationCompat.Action.Builder(
                com.masum.cipher.R.drawable.ic_notification,
                "Categorize",
                categorizePendingIntent
            ).build()

            val currencySymbol = settings.currencySymbol
            val amountStr = com.masum.cipher.core.util.AppFormatters.formatCurrency(transaction.amount, currencySymbol, decimals = 0)
            val builder = NotificationCompat.Builder(context, CHANNEL_TRANSACTIONS)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor(if (transaction.isIncome) "#10B981".toColorInt() else "#F43F5E".toColorInt())
                .setContentTitle(if (transaction.isIncome) "Money Received" else "New Expense")
                .setContentText(
                    if (transaction.isIncome) "You received $amountStr from ${transaction.merchant}."
                    else "You spent $amountStr at ${transaction.merchant}."
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(categorizePendingIntent)
                .addAction(addNoteAction)
                .addAction(categorizeAction)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(notificationId, builder.build()) }
        }
    }

    fun showSubscriptionPendingNotification(subscription: SubscriptionEntity) {
        scope.launch {
            val settings = userPreferences.settingsFlow.first()
            if (!settings.notifySubscriptions) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            val notificationId = (subscription.id + 5000).toInt()

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val approveIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_APPROVE_SUBSCRIPTION
                putExtra(NotificationActionReceiver.EXTRA_SUBSCRIPTION_ID, subscription.id)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val approvePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 100,
                approveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val approveAction = NotificationCompat.Action.Builder(
                com.masum.cipher.R.drawable.ic_notification,
                "Log it",
                approvePendingIntent
            ).build()

            val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SKIP_SUBSCRIPTION
                putExtra(NotificationActionReceiver.EXTRA_SUBSCRIPTION_ID, subscription.id)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val skipPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 200,
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val skipAction = NotificationCompat.Action.Builder(
                com.masum.cipher.R.drawable.ic_notification,
                "Skip",
                skipPendingIntent
            ).build()

            val currencySymbol = settings.currencySymbol
            val amountStr = com.masum.cipher.core.util.AppFormatters.formatCurrency(subscription.amount, currencySymbol, decimals = 0)
            val builder = NotificationCompat.Builder(context, CHANNEL_SUBSCRIPTIONS)
                .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                .setColor("#F59E0B".toColorInt())
                .setContentTitle("Subscription Due")
                .setContentText("$amountStr for ${subscription.merchant} is due today.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(approveAction)
                .addAction(skipAction)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) { notify(notificationId, builder.build()) }
        }
    }
}
