package com.example.cipherspend.core.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsParser: SmsParser

    @Inject
    lateinit var repository: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.displayMessageBody
                val parsed = smsParser.parse(body)

                if (parsed != null) {
                    scope.launch {
                        repository.insertTransaction(
                            TransactionEntity(
                                amount = parsed.amount,
                                merchant = parsed.merchant,
                                currency = parsed.currency,
                                timestamp = System.currentTimeMillis(),
                                category = "Uncategorized",
                                rawSms = body,
                                isIncome = parsed.isIncome
                            )
                        )
                    }
                }
            }
        }
    }
}