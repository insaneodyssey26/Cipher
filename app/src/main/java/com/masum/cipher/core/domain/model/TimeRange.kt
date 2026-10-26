package com.masum.cipher.core.domain.model

import androidx.annotation.StringRes
import com.masum.cipher.R
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

enum class TimePeriod(
    val label: String,
    @StringRes val labelRes: Int
) {
    THIS_WEEK("This Week", R.string.period_this_week),
    LAST_WEEK("Last Week", R.string.period_last_week),
    THIS_MONTH("This Month", R.string.period_this_month),
    LAST_MONTH("Last Month", R.string.period_last_month),
    THIS_YEAR("This Year", R.string.period_this_year),
    ALL_TIME("All Time", R.string.period_all_time),
    CUSTOM("Custom", R.string.period_custom)
}

data class TimeRange(
    val period: TimePeriod,
    val startTime: Long,
    val endTime: Long,
    val label: String = period.label
) {
    companion object {
        fun from(
            period: TimePeriod,
            customStart: Long? = null,
            customEnd: Long? = null,
            zoneId: ZoneId = ZoneId.systemDefault(),
            locale: Locale = Locale.getDefault()
        ): TimeRange {
            if (period == TimePeriod.CUSTOM && customStart != null && customEnd != null) {
                return TimeRange(period, customStart, customEnd, "Custom")
            }
            val today = LocalDate.now(zoneId)

            return when (period) {
                TimePeriod.THIS_WEEK -> {
                    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
                    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                    val start = startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfWeek.plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.LAST_WEEK -> {
                    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
                    val startOfLastWeek = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                    val start = startOfLastWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfLastWeek.plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.THIS_MONTH -> {
                    val startOfMonth = today.withDayOfMonth(1)
                    val start = startOfMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.LAST_MONTH -> {
                    val startOfLastMonth = today.minusMonths(1).withDayOfMonth(1)
                    val start = startOfLastMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfLastMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.THIS_YEAR -> {
                    val startOfYear = today.withDayOfYear(1)
                    val start = startOfYear.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfYear.plusYears(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.ALL_TIME -> {
                    TimeRange(period, 0L, Long.MAX_VALUE)
                }
                TimePeriod.CUSTOM -> {
                    val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(period, start, end)
                }
            }
        }

        fun previousEquivalentRange(
            period: TimePeriod,
            zoneId: ZoneId = ZoneId.systemDefault(),
            locale: Locale = Locale.getDefault()
        ): TimeRange? {
            val nowInstant = Instant.now()
            val nowZoned = nowInstant.atZone(zoneId)
            val today = nowZoned.toLocalDate()

            return when (period) {
                TimePeriod.THIS_MONTH -> {
                    val startOfPrevMonth = today.minusMonths(1).withDayOfMonth(1)
                    val start = startOfPrevMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val maxDayInPrevMonth = startOfPrevMonth.lengthOfMonth()
                    val targetDay = today.dayOfMonth.coerceAtMost(maxDayInPrevMonth)
                    val end = startOfPrevMonth.withDayOfMonth(targetDay).atTime(23, 59, 59, 999_000_000)
                        .atZone(zoneId).toInstant().toEpochMilli()
                    TimeRange(TimePeriod.LAST_MONTH, start, end, "vs last month")
                }
                TimePeriod.THIS_WEEK -> {
                    val currentWeekRange = from(TimePeriod.THIS_WEEK, zoneId = zoneId, locale = locale)
                    val elapsedMs = (nowInstant.toEpochMilli() - currentWeekRange.startTime).coerceAtLeast(0L)
                    val lastWeekStart = currentWeekRange.startTime - (7L * 24L * 60L * 60L * 1000L)
                    val lastWeekEnd = lastWeekStart + elapsedMs
                    TimeRange(TimePeriod.LAST_WEEK, lastWeekStart, lastWeekEnd, "vs last week")
                }
                TimePeriod.LAST_MONTH -> {
                    val startOfPriorMonth = today.minusMonths(2).withDayOfMonth(1)
                    val start = startOfPriorMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val end = startOfPriorMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    TimeRange(TimePeriod.LAST_MONTH, start, end, "vs prior month")
                }
                TimePeriod.LAST_WEEK -> {
                    val lastWeekRange = from(TimePeriod.LAST_WEEK, zoneId = zoneId, locale = locale)
                    val priorWeekStart = lastWeekRange.startTime - (7L * 24L * 60L * 60L * 1000L)
                    val priorWeekEnd = lastWeekRange.endTime - (7L * 24L * 60L * 60L * 1000L)
                    TimeRange(TimePeriod.LAST_WEEK, priorWeekStart, priorWeekEnd, "vs prior week")
                }
                TimePeriod.THIS_YEAR -> {
                    val startOfPrevYear = today.minusYears(1).withDayOfYear(1)
                    val start = startOfPrevYear.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val maxDayInPrevYear = startOfPrevYear.lengthOfYear()
                    val targetDay = today.dayOfYear.coerceAtMost(maxDayInPrevYear)
                    val end = startOfPrevYear.withDayOfYear(targetDay).atTime(23, 59, 59, 999_000_000)
                        .atZone(zoneId).toInstant().toEpochMilli()
                    TimeRange(TimePeriod.THIS_YEAR, start, end, "vs last year")
                }
                TimePeriod.ALL_TIME, TimePeriod.CUSTOM -> null
            }
        }
    }
}
