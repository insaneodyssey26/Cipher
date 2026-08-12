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
                            
                        // Show success notification, replacing the old one
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
        }
    }

    companion object {
        const val ACTION_ADD_NOTE = "com.masum.cipher.ACTION_ADD_NOTE"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
