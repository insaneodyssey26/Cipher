package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SubscriptionDetectorTest {

    private val detector = SubscriptionDetector()

    private fun tx(merchant: String, amount: Double, daysAgo: Long): TransactionEntity {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo)
        return TransactionEntity(
            amount = amount,
            merchant = merchant,
            currency = "INR",
            category = "BILLS",
            timestamp = timestamp,
            rawSms = "Paid $amount to $merchant",
            isIncome = false
        )
    }

    @Test
    fun consistentMonthlyChargeIsDetectedWithHighConfidence() {
        val transactions = listOf(
            tx("NETFLIX", 499.0, 85),
            tx("NETFLIX", 499.0, 55),
            tx("NETFLIX", 499.0, 25)
        )

        val result = detector.detect(transactions)

        assertEquals(1, result.size)
        assertEquals("NETFLIX", result[0].merchant)
        assertTrue("expected high confidence for a tight amount/interval match, got ${result[0].confidence}", result[0].confidence >= 0.7f)
    }

    @Test
    fun wildlyVaryingAmountsAtRegularIntervalsAreNotFlaggedAsSubscription() {
        val transactions = listOf(
            tx("LOCAL STORE", 200.0, 90),
            tx("LOCAL STORE", 900.0, 60),
            tx("LOCAL STORE", 450.0, 30)
        )

        val result = detector.detect(transactions)

        assertTrue("expected no subscription for inconsistent amounts, got $result", result.isEmpty())
    }

    @Test
    fun fewerThanThreeChargesAreNeverFlagged() {
        val transactions = listOf(
            tx("SPOTIFY", 119.0, 60),
            tx("SPOTIFY", 119.0, 30)
        )

        val result = detector.detect(transactions)

        assertTrue(result.isEmpty())
    }

    @Test
    fun irregularIntervalsAreNotFlaggedEvenWithConsistentAmounts() {
        val transactions = listOf(
            tx("SHOP", 300.0, 50),
            tx("SHOP", 300.0, 40),
            tx("SHOP", 300.0, 2)
        )

        val result = detector.detect(transactions)

        assertTrue("expected no subscription for irregular spacing, got $result", result.isEmpty())
    }
}
