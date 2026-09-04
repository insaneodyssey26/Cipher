package com.masum.cipher.core.util

import android.content.Context
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppFormatters {

    fun getDay(locale: Locale = Locale.getDefault()): SimpleDateFormat =
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd"), locale)

    fun getShortDate(locale: Locale = Locale.getDefault()): SimpleDateFormat =
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMd"), locale)

    fun getFullDate(locale: Locale = Locale.getDefault()): SimpleDateFormat = 
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMMd"), locale)

    fun getMonthYearFormat(locale: Locale = Locale.getDefault()): SimpleDateFormat =
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMM"), locale)

    fun getMonthYearShortFormat(locale: Locale = Locale.getDefault()): SimpleDateFormat =
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMM"), locale)

    fun getPeriodLabel(
        period: com.masum.cipher.core.domain.model.TimePeriod,
        transactions: List<TransactionEntity> = emptyList(),
        context: Context? = null,
        locale: Locale = Locale.getDefault()
    ): String {
        if (period == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME && transactions.isNotEmpty()) {
            val minTime = transactions.minOfOrNull { it.timestamp }
            val maxTime = transactions.maxOfOrNull { it.timestamp }
            if (minTime != null && maxTime != null) {
                val format = getMonthYearShortFormat(locale)
                val startStr = format.format(Date(minTime))
                val endStr = format.format(Date(maxTime))
                return if (startStr == endStr) startStr else "$startStr - $endStr"
            }
        }
        val calendar = Calendar.getInstance()
        return when (period) {
            com.masum.cipher.core.domain.model.TimePeriod.THIS_WEEK -> {
                context?.getString(R.string.period_this_week) ?: "This Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK -> {
                context?.getString(R.string.period_last_week) ?: "Last Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH -> {
                getMonthYearFormat(locale).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                getMonthYearFormat(locale).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_YEAR -> {
                SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyy"), locale).format(calendar.time)
            }
            com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME -> {
                context?.getString(R.string.period_all_time) ?: "All Time"
            }
            com.masum.cipher.core.domain.model.TimePeriod.CUSTOM -> {
                if (transactions.isNotEmpty()) {
                    val minTime = transactions.minOfOrNull { it.timestamp }
                    val maxTime = transactions.maxOfOrNull { it.timestamp }
                    if (minTime != null && maxTime != null) {
                        val format = getDay(locale)
                        "${format.format(Date(minTime))} – ${format.format(Date(maxTime))}"
                    } else context?.getString(R.string.period_custom_range) ?: "Custom Range"
                } else context?.getString(R.string.period_custom_range) ?: "Custom Range"
            }
        }
    }

    fun isSuffixCurrency(currencySymbol: String, locale: Locale = Locale.getDefault()): Boolean {
        val cleanSym = currencySymbol.trim()
        val lang = locale.language.lowercase()
        if (cleanSym == "€") {
            return lang != "en"
        }
        val suffixSymbols = setOf("AED", "kr", "zł", "Kč", "₫", "CHF", "Ft", "lei", "kn", "din", "R$")
        return suffixSymbols.contains(cleanSym)
    }

    fun formatAmountWithSymbol(
        amountStr: String,
        currencySymbol: String,
        locale: Locale = Locale.getDefault(),
        sign: String = ""
    ): String {
        return if (isSuffixCurrency(currencySymbol, locale)) {
            "$sign$amountStr $currencySymbol".trim()
        } else {
            "$sign$currencySymbol$amountStr"
        }
    }

    fun formatCurrency(
        value: Double,
        currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
        locale: Locale = Locale.getDefault(),
        decimals: Int = 0
    ): String {
        val absVal = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""
        val pattern = if (decimals > 0) "%,.${decimals}f" else "%,.0f"
        val formattedNumber = String.format(Locale.US, pattern, absVal)
        return formatAmountWithSymbol(formattedNumber, currencySymbol, locale, sign)
    }

    fun formatCompactCurrency(
        value: Double,
        currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
        locale: Locale = Locale.getDefault()
    ): String {
        val absVal = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""
        val lang = locale.language.lowercase()

        val formattedNumber: String = if (lang == "ja") {
            when {
                absVal >= 100_000_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 100_000_000.0).removeSuffix(".0").removeSuffix(",0") + "億"
                }
                absVal >= 10_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 10_000.0).removeSuffix(".0").removeSuffix(",0") + "万"
                }
                else -> {
                    String.format(Locale.US, "%,.0f", absVal)
                }
            }
        } else {
            when {
                absVal >= 1_000_000_000_000_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 1_000_000_000_000_000.0).removeSuffix(".0").removeSuffix(",0") + "Q"
                }
                absVal >= 1_000_000_000_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 1_000_000_000_000.0).removeSuffix(".0").removeSuffix(",0") + "T"
                }
                absVal >= 1_000_000_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 1_000_000_000.0).removeSuffix(".0").removeSuffix(",0") + "B"
                }
                absVal >= 1_000_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 1_000_000.0).removeSuffix(".0").removeSuffix(",0") + "M"
                }
                absVal >= 100_000.0 -> {
                    String.format(Locale.US, "%.1f", absVal / 1000.0).removeSuffix(".0").removeSuffix(",0") + "k"
                }
                else -> {
                    String.format(Locale.US, "%,.0f", absVal)
                }
            }
        }

        return formatAmountWithSymbol(formattedNumber, currencySymbol, locale, sign)
    }
}
