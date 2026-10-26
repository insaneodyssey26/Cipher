package com.masum.cipher.core.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object DateTimeUtils {
    fun currentMonthStart(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId)
            .with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun todayStart(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun remainingDaysInMonth(zoneId: ZoneId = ZoneId.systemDefault()): Int {
        val today = LocalDate.now(zoneId)
        val lengthOfMonth = today.lengthOfMonth()
        return (lengthOfMonth - today.dayOfMonth + 1).coerceAtLeast(1)
    }
}
