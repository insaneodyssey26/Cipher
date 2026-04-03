package com.example.cipherspend.core.domain

import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.domain.model.TransactionCategory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionDetector @Inject constructor() {

    data class Subscription(
        val merchant: String,
        val amount: Double,
        val category: TransactionCategory,
        val frequencyDays: Int,
        val lastDate: Long,
        val nextExpectedDate: Long,
        val confidence: Float
    )

    fun detect(transactions: List<TransactionEntity>): List<Subscription> {
        val expenses = transactions.filter { !it.isIncome }
        val groups = expenses.groupBy { it.merchant.uppercase() }
        val subscriptions = mutableListOf<Subscription>()

        groups.forEach { (_, txs) ->
            if (txs.size < 3) return@forEach
            
            val sortedTxs = txs.sortedBy { it.timestamp }
            val intervals = mutableListOf<Long>()
            
            for (i in 1 until sortedTxs.size) {
                intervals.add(sortedTxs[i].timestamp - sortedTxs[i - 1].timestamp)
            }

            val avgInterval = intervals.average()
            val avgDays = TimeUnit.MILLISECONDS.toDays(avgInterval.toLong()).toInt()
            
            if (avgDays in 25..35 || avgDays in 12..16 || avgDays in 6..8 || avgDays in 85..95) {
                val lastTx = sortedTxs.last()
                val nextDate = lastTx.timestamp + avgInterval.toLong()
                
                if (nextDate > System.currentTimeMillis()) {
                    subscriptions.add(
                        Subscription(
                            merchant = lastTx.merchant,
                            amount = lastTx.amount,
                            category = TransactionCategory.fromString(lastTx.category),
                            frequencyDays = avgDays,
                            lastDate = lastTx.timestamp,
                            nextExpectedDate = nextDate,
                            confidence = 0.8f
                        )
                    )
                }
            }
        }

        return subscriptions.sortedBy { it.nextExpectedDate }
    }
}
