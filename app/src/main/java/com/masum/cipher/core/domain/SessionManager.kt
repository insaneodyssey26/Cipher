package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.model.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.THIS_MONTH)
    val selectedTimePeriod = _selectedTimePeriod.asStateFlow()

    fun setTimePeriod(period: TimePeriod) {
        _selectedTimePeriod.value = period
    }
}
