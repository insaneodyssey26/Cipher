package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.model.TimePeriod
import com.masum.cipher.core.domain.model.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.THIS_MONTH)
    val selectedTimePeriod = _selectedTimePeriod.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.from(TimePeriod.THIS_MONTH))
    val selectedTimeRange = _selectedTimeRange.asStateFlow()

    fun setTimePeriod(period: TimePeriod, customStart: Long? = null, customEnd: Long? = null) {
        _selectedTimePeriod.value = period
        _selectedTimeRange.value = TimeRange.from(period, customStart, customEnd)
    }
}
