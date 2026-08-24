package com.masum.cipher.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.masum.cipher.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ADD_NOTE) {
            val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1L)
            if (transactionId == -1L) return

            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            val noteText = remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()

            if (!noteText.isNullOrBlank()) {
                scope.launch {
                    val transaction = transactionRepository.getTransactionById(transactionId)
                    if (transaction != null) {
                        transactionRepository.updateTransaction(transaction.copy(note = noteText))
                        
                        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, transactionId.toInt())
                        val builder = NotificationCompat.Builder(context, LocalNotificationManager.CHANNEL_ID)
                            .setSmallIcon(com.masum.cipher.R.drawable.ic_notification)
                            .setColor(android.graphics.Color.parseColor("#4F46E5"))
                            .setContentTitle("Note Added!")
                            .setContentText("Your note was saved successfully.")
                            .setAutoCancel(true)
                            
                        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            with(NotificationManagerCompat.from(context)) {
                                notify(notificationId, builder.build())
                            }
                        }
                    }
                }
            }
        } else if (intent.action == ACTION_APPROVE_SUBSCRIPTION || intent.action == ACTION_SKIP_SUBSCRIPTION) {
            val subId = intent.getLongExtra(EXTRA_SUBSCRIPTION_ID, -1L)
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (subId == -1L) return
            
            scope.launch {
                val subscriptionDao = dagger.hilt.android.EntryPointAccessors.fromApplication(context.applicationContext, com.masum.cipher.core.worker.SubscriptionWorker.WorkerEntryPoint::class.java).subscriptionDao()
                val transactionDao = dagger.hilt.android.EntryPointAccessors.fromApplication(context.applicationContext, com.masum.cipher.core.worker.SubscriptionWorker.WorkerEntryPoint::class.java).transactionDao()
                val subscription = subscriptionDao.getById(subId)
                if (subscription != null) {
                    if (intent.action == ACTION_APPROVE_SUBSCRIPTION) {
                        val newTransaction = com.masum.cipher.core.data.local.entity.TransactionEntity(
                            merchant = subscription.merchant,
                            amount = subscription.amount,
                            currency = "INR",
                            rawSms = null,
                            category = subscription.category,
                            timestamp = System.currentTimeMillis(),
                            isIncome = false,
                            note = "Approved subscription"
                        )
                        transactionDao.insertTransaction(newTransaction)
                    }
                    
                    val intervalMs = java.util.concurrent.TimeUnit.DAYS.toMillis(subscription.frequencyDays.toLong())
                    subscriptionDao.update(subscription.copy(nextExpectedDate = subscription.nextExpectedDate + intervalMs))
                    
                    if (notificationId != -1) {
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_ADD_NOTE = "com.masum.cipher.ACTION_ADD_NOTE"
        const val ACTION_APPROVE_SUBSCRIPTION = "com.masum.cipher.ACTION_APPROVE_SUBSCRIPTION"
        const val ACTION_SKIP_SUBSCRIPTION = "com.masum.cipher.ACTION_SKIP_SUBSCRIPTION"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
