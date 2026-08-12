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
import com.masum.cipher.MainActivity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "cipher_app_alerts"
        const val NOTIFICATION_ID_NEW_APP = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cipher Alerts"
            val descriptionText = "Notifications for new apps and tracking suggestions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewAppDetectedNotification(appName: String, packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor("#4F46E5"))
            .setContentTitle("New Payment App Detected")
            .setContentText("Would you like Cipher to track transactions from $appName?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_NEW_APP, builder.build())
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
            1, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor("#4F46E5"))
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = if (isExceeded) "Budget Exceeded" else "Budget Alert ($threshold%)"
        val text = if (isExceeded) "You have exceeded your monthly budget by ₹${amount.toInt()}." 
                   else if (threshold == 90) "You've used 90% of your budget! Only ₹${amount.toInt()} remaining."
                   else "You've used 50% of your budget. ₹${amount.toInt()} remaining."
                   
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor(if (isExceeded) "#F43F5E" else "#F59E0B"))
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) { notify(1003, builder.build()) }
    }

    fun showDailySummaryNotification(spent: Double, count: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor("#10B981"))
            .setContentTitle("Daily Summary")
            .setContentText("You spent ₹${spent.toInt()} today across $count transactions. Tap to review.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) { notify(1004, builder.build()) }
    }

    fun showMonthlyWrappedNotification(monthName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor("#4F46E5"))
            .setContentTitle("Your $monthName Wrap-up")
            .setContentText("Your spending report for $monthName is ready! See how you did.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) { notify(1005, builder.build()) }
    }

    fun showUncategorizedReminderNotification(count: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor("#F59E0B"))
            .setContentTitle("Action Needed")
            .setContentText("You have $count new transactions waiting to be categorized.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) { notify(1006, builder.build()) }
    }

    fun showNewTransactionNotification(transaction: TransactionEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notificationId = transaction.id.toInt()

        // Categorize action (Deep link)
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

        // Add Note Action (Inline Reply)
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

        val amountStr = "₹${String.format(java.util.Locale.getDefault(), "%.0f", transaction.amount)}"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
            .setColor(android.graphics.Color.parseColor(if (transaction.isIncome) "#10B981" else "#F43F5E"))
            .setContentTitle("New Transaction")
            .setContentText("You spent $amountStr at ${transaction.merchant}.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(categorizePendingIntent) // Tapping it opens the details
            .addAction(addNoteAction)
            .addAction(categorizeAction)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) { notify(notificationId, builder.build()) }
    }
}
