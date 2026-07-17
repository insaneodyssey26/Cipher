package com.masum.cipher.core.domain.model

import java.util.Calendar

enum class TimePeriod(val label: String) {
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

data class TimeRange(
    val period: TimePeriod,
    val startTime: Long,
    val endTime: Long,
    val label: String = period.label
) {
    companion object {
        fun from(period: TimePeriod): TimeRange {
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
            }
        }
    }
}
