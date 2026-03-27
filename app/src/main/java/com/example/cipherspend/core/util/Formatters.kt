package com.example.cipherspend.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object AppFormatters {
    fun getCurrency(locale: Locale = Locale("en", "IN")): NumberFormat = 
        NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 2
        }

    fun getCurrencyNoDecimals(locale: Locale = Locale("en", "IN")): NumberFormat = 
        NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        }

    fun getTime(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat("HH:mm", locale)

    fun getDay(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat("MMM dd", locale)

    fun getFullDate(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat("MMMM dd, yyyy", locale)

    fun getDayName(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat("EEEE", locale)
}
