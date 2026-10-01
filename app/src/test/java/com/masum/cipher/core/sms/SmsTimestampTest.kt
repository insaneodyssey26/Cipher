package com.masum.cipher.core.sms

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsTimestampTest {

    private val now = 1_800_000_000_000L

    @Test
    fun delayedSmsKeepsItsOwnTimestamp() {
        val sentThreeHoursAgo = now - 3 * 60 * 60 * 1000L

        assertEquals(sentThreeHoursAgo, resolveSmsTimestamp(sentThreeHoursAgo, now))
    }

    @Test
    fun smsSentRightNowKeepsItsTimestamp() {
        assertEquals(now, resolveSmsTimestamp(now, now))
    }

    @Test
    fun missingTimestampFallsBackToNow() {
        assertEquals(now, resolveSmsTimestamp(0L, now))
    }

    @Test
    fun negativeTimestampFallsBackToNow() {
        assertEquals(now, resolveSmsTimestamp(-5L, now))
    }

    @Test
    fun timestampAheadOfDeviceClockFallsBackToNow() {
        assertEquals(now, resolveSmsTimestamp(now + 60_000L, now))
    }
}
