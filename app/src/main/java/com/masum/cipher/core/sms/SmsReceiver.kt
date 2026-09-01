package com.masum.cipher.core.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsParser: TransactionParser

    @Inject
    lateinit var repository: TransactionRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            scope.launch {
                try {
                    val settings = userPreferences.settingsFlow.first()
                    for (sms in messages) {
                        val body = sms.displayMessageBody
                        val parsed = smsParser.parse(body, settings.currencyCode)

                        if (parsed != null) {
                            repository.insertTransaction(
                                TransactionEntity(
                                    amount = parsed.amount,
                                    merchant = parsed.merchant,
                                    currency = parsed.currency,
                                    timestamp = System.currentTimeMillis(),
                                    category = "",
                                    rawSms = body,
                                    isIncome = parsed.isIncome
                                )
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
