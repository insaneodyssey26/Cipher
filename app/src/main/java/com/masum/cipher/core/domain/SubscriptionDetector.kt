package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SubscriptionDetector @Inject constructor() {

    data class Subscription(
        val merchant: String,
        val amount: Double,
        val category: String,
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
            val intervalsMs = mutableListOf<Long>()

            for (i in 1 until sortedTxs.size) {
                intervalsMs.add(sortedTxs[i].timestamp - sortedTxs[i - 1].timestamp)
            }

            val avgIntervalMs = intervalsMs.average()
            val avgDays = TimeUnit.MILLISECONDS.toDays(avgIntervalMs.toLong()).toInt()

            val bucketMatches = avgDays in 25..35 || avgDays in 12..16 || avgDays in 6..8 || avgDays in 85..95
            if (!bucketMatches) return@forEach

            val amounts = sortedTxs.map { it.amount }
            val amountCv = coefficientOfVariation(amounts)
            if (amountCv > AMOUNT_VARIATION_TOLERANCE) return@forEach

            val intervalDays = intervalsMs.map { TimeUnit.MILLISECONDS.toDays(it).toDouble() }
            val intervalCv = coefficientOfVariation(intervalDays)
            val confidence = confidenceFrom(intervalCv, amountCv)

            val lastTx = sortedTxs.last()
            val nextDate = lastTx.timestamp + avgIntervalMs.toLong()

            if (nextDate > System.currentTimeMillis()) {
                subscriptions.add(
                    Subscription(
                        merchant = lastTx.merchant,
                        amount = lastTx.amount,
                        category = lastTx.category,
                        frequencyDays = avgDays,
                        lastDate = lastTx.timestamp,
                        nextExpectedDate = nextDate,
                        confidence = confidence
                    )
                )
            }
        }

        return subscriptions.sortedBy { it.nextExpectedDate }
    }

    private fun coefficientOfVariation(values: List<Double>): Double {
        val mean = values.average()
        if (mean <= 0.0) return 1.0
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        return sqrt(variance) / mean
    }

    private fun confidenceFrom(intervalCv: Double, amountCv: Double): Float {
        val intervalScore = (1.0 - intervalCv).coerceIn(0.0, 1.0)
        val amountScore = (1.0 - amountCv).coerceIn(0.0, 1.0)
        val combined = intervalScore * 0.5 + amountScore * 0.5
        return combined.coerceIn(MIN_CONFIDENCE, MAX_CONFIDENCE).toFloat()
    }

    companion object {
        private const val AMOUNT_VARIATION_TOLERANCE = 0.15
        private const val MIN_CONFIDENCE = 0.3
        private const val MAX_CONFIDENCE = 0.95
    }
}
