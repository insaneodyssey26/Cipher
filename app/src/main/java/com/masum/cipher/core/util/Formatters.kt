package com.masum.cipher.core.util

import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppFormatters {

    fun getDay(locale: Locale = Locale.getDefault()): SimpleDateFormat =
        SimpleDateFormat("MMM dd", locale)

    fun getFullDate(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat("MMMM dd, yyyy", locale)

    fun getPeriodLabel(
        period: com.masum.cipher.core.domain.model.TimePeriod,
        transactions: List<TransactionEntity> = emptyList()
    ): String {
        if (period == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME && transactions.isNotEmpty()) {
            val minTime = transactions.minOfOrNull { it.timestamp }
            val maxTime = transactions.maxOfOrNull { it.timestamp }
            if (minTime != null && maxTime != null) {
                val format = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val startStr = format.format(Date(minTime))
                val endStr = format.format(Date(maxTime))
                return if (startStr == endStr) startStr else "$startStr - $endStr"
            }
        }
        val calendar = Calendar.getInstance()
        return when (period) {
            com.masum.cipher.core.domain.model.TimePeriod.THIS_WEEK -> {
                "This Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK -> {
                "Last Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH -> {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_YEAR -> {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME -> {
                "All Time"
            }
            com.masum.cipher.core.domain.model.TimePeriod.CUSTOM -> {
                if (transactions.isNotEmpty()) {
                    val minTime = transactions.minOfOrNull { it.timestamp }
                    val maxTime = transactions.maxOfOrNull { it.timestamp }
                    if (minTime != null && maxTime != null) {
                        val format = SimpleDateFormat("MMM d", Locale.getDefault())
                        "${format.format(Date(minTime))} – ${format.format(Date(maxTime))}"
                    } else "Custom Range"
                } else "Custom Range"
            }
        }
    }

    fun formatCompactCurrency(value: Double, prefix: String = "₹"): String {
        val absVal = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""
        return when {
            absVal >= 1_000_000_000_000.0 -> {
                val formatted = String.format(Locale.US, "%.1f", absVal / 1_000_000_000_000.0).removeSuffix(".0")
                "$sign$prefix${formatted}T"
            }
            absVal >= 1_000_000_000.0 -> {
                val formatted = String.format(Locale.US, "%.1f", absVal / 1_000_000_000.0).removeSuffix(".0")
                "$sign$prefix${formatted}B"
            }
            absVal >= 1_000_000.0 -> {
                val formatted = String.format(Locale.US, "%.1f", absVal / 1_000_000.0).removeSuffix(".0")
                "$sign$prefix${formatted}M"
            }
            absVal >= 1_000.0 -> {
                val formatted = String.format(Locale.US, "%.1f", absVal / 1_000.0).removeSuffix(".0")
                "$sign$prefix${formatted}k"
            }
            else -> {
                val formatted = String.format(Locale.getDefault(), "%,.0f", absVal)
                "$sign$prefix$formatted"
            }
        }
    }
}
