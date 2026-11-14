package com.masum.cipher.core.worker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationScheduleRulesTest {

    @Test
    fun dailySummaryOnlySendsInTheEveningWindow() {
        assertFalse(shouldSendDailySummary(hourOfDay = 17, today = 100L, lastSentDay = 99L))
        assertTrue(shouldSendDailySummary(hourOfDay = 18, today = 100L, lastSentDay = 99L))
        assertTrue(shouldSendDailySummary(hourOfDay = 23, today = 100L, lastSentDay = 99L))
        assertFalse(shouldSendDailySummary(hourOfDay = 0, today = 100L, lastSentDay = 99L))
    }

    @Test
    fun dailySummaryIsSentOncePerDay() {
        assertFalse(shouldSendDailySummary(hourOfDay = 20, today = 100L, lastSentDay = 100L))
        assertTrue(shouldSendDailySummary(hourOfDay = 20, today = 101L, lastSentDay = 100L))
    }

    @Test
    fun monthlyWrappedOnlySendsOnTheFirstAndOncePerMonth() {
        assertFalse(shouldSendMonthlyWrapped(dayOfMonth = 2, monthKey = 202611, lastSentKey = 202610))
        assertTrue(shouldSendMonthlyWrapped(dayOfMonth = 1, monthKey = 202611, lastSentKey = 202610))
        assertFalse(shouldSendMonthlyWrapped(dayOfMonth = 1, monthKey = 202611, lastSentKey = 202611))
        assertTrue(shouldSendMonthlyWrapped(dayOfMonth = 1, monthKey = 202701, lastSentKey = 202612))
    }
}
