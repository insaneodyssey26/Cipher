package com.masum.cipher.core.sms

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionTimestampTest {

    private val now = 1_800_000_000_000L

    @Test
    fun delayedSmsKeepsItsOwnTimestamp() {
        val sentThreeHoursAgo = now - 3 * 60 * 60 * 1000L

        assertEquals(sentThreeHoursAgo, resolveTransactionTimestamp(sentThreeHoursAgo, now))
    }

    @Test
    fun smsSentRightNowKeepsItsTimestamp() {
        assertEquals(now, resolveTransactionTimestamp(now, now))
    }

    @Test
    fun missingTimestampFallsBackToNow() {
        assertEquals(now, resolveTransactionTimestamp(0L, now))
    }

    @Test
    fun negativeTimestampFallsBackToNow() {
        assertEquals(now, resolveTransactionTimestamp(-5L, now))
    }

    @Test
    fun timestampAheadOfDeviceClockFallsBackToNow() {
        assertEquals(now, resolveTransactionTimestamp(now + 60_000L, now))
    }
}
