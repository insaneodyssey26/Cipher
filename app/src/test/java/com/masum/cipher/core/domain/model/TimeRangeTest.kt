package com.masum.cipher.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class TimeRangeTest {

    private val zoneId = ZoneId.of("UTC")
    private val locale = Locale.US

    @Test
    fun `this month range spans from first of month to start of next month minus one`() {
        val range = TimeRange.from(TimePeriod.THIS_MONTH, zoneId = zoneId, locale = locale)
        val today = LocalDate.now(zoneId)
        val expectedStart = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val expectedEnd = today.withDayOfMonth(1).plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        assertEquals(expectedStart, range.startTime)
        assertEquals(expectedEnd, range.endTime)
    }

    @Test
    fun `last month range spans previous full month`() {
        val range = TimeRange.from(TimePeriod.LAST_MONTH, zoneId = zoneId, locale = locale)
        val today = LocalDate.now(zoneId)
        val prevMonth = today.minusMonths(1).withDayOfMonth(1)
        val expectedStart = prevMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val expectedEnd = prevMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        assertEquals(expectedStart, range.startTime)
        assertEquals(expectedEnd, range.endTime)
    }

    @Test
    fun `this year range spans from start of year to start of next year minus one`() {
        val range = TimeRange.from(TimePeriod.THIS_YEAR, zoneId = zoneId, locale = locale)
        val today = LocalDate.now(zoneId)
        val expectedStart = today.withDayOfYear(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val expectedEnd = today.withDayOfYear(1).plusYears(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        assertEquals(expectedStart, range.startTime)
        assertEquals(expectedEnd, range.endTime)
    }

    @Test
    fun `all time range spans 0 to Long MAX_VALUE`() {
        val range = TimeRange.from(TimePeriod.ALL_TIME, zoneId = zoneId, locale = locale)
        assertEquals(0L, range.startTime)
        assertEquals(Long.MAX_VALUE, range.endTime)
    }

    @Test
    fun `custom range preserves custom start and end`() {
        val range = TimeRange.from(TimePeriod.CUSTOM, customStart = 1000L, customEnd = 5000L, zoneId = zoneId, locale = locale)
        assertEquals(1000L, range.startTime)
        assertEquals(5000L, range.endTime)
        assertEquals("Custom", range.label)
    }

    @Test
    fun `previousEquivalentRange for this month returns equivalent past period`() {
        val prevRange = TimeRange.previousEquivalentRange(TimePeriod.THIS_MONTH, zoneId = zoneId, locale = locale)
        assertNotNull(prevRange)
        assertEquals("vs last month", prevRange?.label)
        assertTrue(prevRange!!.startTime < prevRange.endTime)
    }

    @Test
    fun `previousEquivalentRange for all time and custom returns null`() {
        assertNull(TimeRange.previousEquivalentRange(TimePeriod.ALL_TIME, zoneId = zoneId, locale = locale))
        assertNull(TimeRange.previousEquivalentRange(TimePeriod.CUSTOM, zoneId = zoneId, locale = locale))
    }
}
