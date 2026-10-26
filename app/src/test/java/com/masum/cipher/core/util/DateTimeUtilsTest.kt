package com.masum.cipher.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class DateTimeUtilsTest {

    @Test
    fun currentMonthStartMatchesStartOfDayFirstOfMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expected = cal.timeInMillis
        val actual = DateTimeUtils.currentMonthStart()

        assertEquals(expected, actual)
    }

    @Test
    fun todayStartMatchesMidnightToday() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expected = cal.timeInMillis
        val actual = DateTimeUtils.todayStart()

        assertEquals(expected, actual)
    }

    @Test
    fun remainingDaysInMonthCalculatesCorrectCount() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val expected = today.lengthOfMonth() - today.dayOfMonth + 1
        val actual = DateTimeUtils.remainingDaysInMonth(zone)

        assertEquals(expected, actual)
        assertTrue(actual >= 1)
    }
}
