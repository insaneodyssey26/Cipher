package com.masum.cipher.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.graphics.toColorInt
import com.masum.cipher.R
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var accountRepository: AccountRepository

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
                        val builder = NotificationCompat.Builder(context, LocalNotificationManager.CHANNEL_TRANSACTIONS)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setColor("#4F46E5".toColorInt())
                            .setContentTitle(context.getString(R.string.tx_details_title))
                            .setContentText(context.getString(R.string.action_done) + ": " + noteText)
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
        } else if (intent.action == ACTION_SWITCH_ACCOUNT) {
            val transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1L)
            if (transactionId == -1L) return

            val directAccountId = intent.getLongExtra(EXTRA_SELECTED_ACCOUNT_ID, -1L)
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            val selectedChoice = remoteInput?.getCharSequence(KEY_ACCOUNT_CHOICE)?.toString()

            scope.launch {
                val accountsList = try {
                    accountRepository.getAllAccountsFlow().first()
                } catch (e: Exception) {
                    emptyList()
                }

                val targetAccount = if (directAccountId > 0) {
                    accountsList.find { it.id == directAccountId }
                } else if (!selectedChoice.isNullOrBlank()) {
                    accountsList.find { acc ->
                        val label = buildAccountLabel(acc)
                        label == selectedChoice || acc.name.equals(selectedChoice, ignoreCase = true)
                    }
                } else {
                    null
                }

                if (targetAccount != null) {
                    val transaction = transactionRepository.getTransactionById(transactionId)
                    if (transaction != null) {
                        transactionRepository.updateTransaction(transaction.copy(accountId = targetAccount.id))

                        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, transactionId.toInt())
                        val builder = NotificationCompat.Builder(context, LocalNotificationManager.CHANNEL_TRANSACTIONS)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setColor("#4F46E5".toColorInt())
                            .setContentTitle(context.getString(R.string.notify_account_updated_title))
                            .setContentText(context.getString(R.string.notify_account_updated_desc, targetAccount.name))
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

    private fun buildAccountLabel(account: com.masum.cipher.core.data.local.entity.AccountEntity): String {
        return if (!account.accountNumberLast4.isNullOrBlank()) {
            "${account.name} (••${account.accountNumberLast4})"
        } else {
            account.name
        }
    }

    companion object {
        const val ACTION_ADD_NOTE = "com.masum.cipher.ACTION_ADD_NOTE"
        const val ACTION_SWITCH_ACCOUNT = "com.masum.cipher.ACTION_SWITCH_ACCOUNT"
        const val ACTION_APPROVE_SUBSCRIPTION = "com.masum.cipher.ACTION_APPROVE_SUBSCRIPTION"
        const val ACTION_SKIP_SUBSCRIPTION = "com.masum.cipher.ACTION_SKIP_SUBSCRIPTION"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_ACCOUNT_CHOICE = "key_account_choice"
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_SELECTED_ACCOUNT_ID = "extra_selected_account_id"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
