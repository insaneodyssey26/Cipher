package com.masum.cipher.core.domain.model

import androidx.annotation.StringRes
import com.masum.cipher.R
import java.util.Calendar

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
        fun from(period: TimePeriod, customStart: Long? = null, customEnd: Long? = null): TimeRange {
            if (period == TimePeriod.CUSTOM && customStart != null && customEnd != null) {
                return TimeRange(period, customStart, customEnd, "Custom")
            }
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return when (period) {
                TimePeriod.THIS_WEEK -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.LAST_WEEK -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.THIS_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.LAST_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.MONTH, -1)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.THIS_YEAR -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.YEAR, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
                TimePeriod.ALL_TIME -> {
                    TimeRange(period, 0L, Long.MAX_VALUE)
                }
                TimePeriod.CUSTOM -> {
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    val end = calendar.timeInMillis - 1
                    TimeRange(period, start, end)
                }
            }
        }

        fun previousEquivalentRange(period: TimePeriod): TimeRange? {
            val now = Calendar.getInstance()
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            return when (period) {
                TimePeriod.THIS_MONTH -> {
                    val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.add(Calendar.MONTH, -1)
                    val start = cal.timeInMillis
                    val maxDayInPrevMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDayInPrevMonth))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    TimeRange(TimePeriod.LAST_MONTH, start, end, "vs last month")
                }
                TimePeriod.THIS_WEEK -> {
                    val currentWeekRange = from(TimePeriod.THIS_WEEK)
                    val elapsedMs = (now.timeInMillis - currentWeekRange.startTime).coerceAtLeast(0L)
                    val lastWeekStart = currentWeekRange.startTime - (7L * 24L * 60L * 60L * 1000L)
                    val lastWeekEnd = lastWeekStart + elapsedMs
                    TimeRange(TimePeriod.LAST_WEEK, lastWeekStart, lastWeekEnd, "vs last week")
                }
                TimePeriod.LAST_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.add(Calendar.MONTH, -2)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    val end = cal.timeInMillis - 1
                    TimeRange(TimePeriod.LAST_MONTH, start, end, "vs prior month")
                }
                TimePeriod.LAST_WEEK -> {
                    val lastWeekRange = from(TimePeriod.LAST_WEEK)
                    val priorWeekStart = lastWeekRange.startTime - (7L * 24L * 60L * 60L * 1000L)
                    val priorWeekEnd = lastWeekRange.endTime - (7L * 24L * 60L * 60L * 1000L)
                    TimeRange(TimePeriod.LAST_WEEK, priorWeekStart, priorWeekEnd, "vs prior week")
                }
                TimePeriod.THIS_YEAR -> {
                    val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.add(Calendar.YEAR, -1)
                    val start = cal.timeInMillis
                    val maxDayInPrevYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                    cal.set(Calendar.DAY_OF_YEAR, dayOfYear.coerceAtMost(maxDayInPrevYear))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    TimeRange(TimePeriod.THIS_YEAR, start, end, "vs last year")
                }
                TimePeriod.ALL_TIME, TimePeriod.CUSTOM -> null
            }
        }
    }
}
