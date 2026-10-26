package com.masum.cipher.core.util

import android.content.Context
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object AppFormatters {

    private val dayFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val shortDateFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val fullDateFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val monthYearFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val monthYearShortFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val yearFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()
    private val dateTimeFormatters = ConcurrentHashMap<Locale, DateTimeFormatter>()

    fun getDateTimeFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        dateTimeFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "dMMM, HH:mm"),
                locale
            )
        }

    fun getDayFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        dayFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd"),
                locale
            )
        }

    fun getShortDateFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        shortDateFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMd"),
                locale
            )
        }

    fun getFullDateFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        fullDateFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMMd"),
                locale
            )
        }

    fun getMonthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        monthYearFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMM"),
                locale
            )
        }

    fun getMonthYearShortFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        monthYearShortFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMM"),
                locale
            )
        }

    fun getYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        yearFormatters.computeIfAbsent(locale) {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyy"),
                locale
            )
        }

    fun formatDay(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        getDayFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

    fun formatShortDate(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        getShortDateFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

    fun formatFullDate(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        getFullDateFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

    fun formatMonthYear(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        getMonthYearFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

    fun formatDateTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        getDateTimeFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

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
        locale: Locale = Locale.getDefault(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (period == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME && transactions.isNotEmpty()) {
            val minTime = transactions.minOfOrNull { it.timestamp }
            val maxTime = transactions.maxOfOrNull { it.timestamp }
            if (minTime != null && maxTime != null) {
                val startStr = formatMonthYearShort(minTime, zoneId, locale)
                val endStr = formatMonthYearShort(maxTime, zoneId, locale)
                return if (startStr == endStr) startStr else "$startStr - $endStr"
            }
        }
        val today = LocalDate.now(zoneId)
        return when (period) {
            com.masum.cipher.core.domain.model.TimePeriod.THIS_WEEK -> {
                context?.getString(R.string.period_this_week) ?: "This Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK -> {
                context?.getString(R.string.period_last_week) ?: "Last Week"
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH -> {
                getMonthYearFormatter(locale).format(today)
            }
            com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH -> {
                getMonthYearFormatter(locale).format(today.minusMonths(1))
            }
            com.masum.cipher.core.domain.model.TimePeriod.THIS_YEAR -> {
                getYearFormatter(locale).format(today)
            }
            com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME -> {
                context?.getString(R.string.period_all_time) ?: "All Time"
            }
            com.masum.cipher.core.domain.model.TimePeriod.CUSTOM -> {
                if (transactions.isNotEmpty()) {
                    val minTime = transactions.minOfOrNull { it.timestamp }
                    val maxTime = transactions.maxOfOrNull { it.timestamp }
                    if (minTime != null && maxTime != null) {
                        "${formatDay(minTime, zoneId, locale)} – ${formatDay(maxTime, zoneId, locale)}"
                    } else context?.getString(R.string.period_custom_range) ?: "Custom Range"
                } else context?.getString(R.string.period_custom_range) ?: "Custom Range"
            }
        }
    }

    private fun formatMonthYearShort(timestamp: Long, zoneId: ZoneId, locale: Locale): String =
        getMonthYearShortFormatter(locale).format(Instant.ofEpochMilli(timestamp).atZone(zoneId))

    @Volatile
    var activeIsSuffix: Boolean = false
    @Volatile
    var activeHasSpace: Boolean = false

    fun setActiveCurrencyFormatting(isSuffix: Boolean, hasSpace: Boolean) {
        activeIsSuffix = isSuffix
        activeHasSpace = hasSpace
    }

    fun isSuffixCurrency(currencySymbol: String, locale: Locale = Locale.getDefault()): Boolean {
        if (activeIsSuffix) return true
        val cleanSym = currencySymbol.trim()
        val lang = locale.language.lowercase()
        if (cleanSym == "€") {
            return lang != "en"
        }
        val suffixSymbols = setOf("AED", "kr", "zł", "Kč", "₫", "CHF", "Ft", "lei", "kn", "din")
        return suffixSymbols.contains(cleanSym)
    }

    fun formatAmountWithSymbol(
        amountStr: String,
        currencySymbol: String,
        locale: Locale = Locale.getDefault(),
        sign: String = "",
        isSuffix: Boolean? = null,
        hasSpace: Boolean? = null
    ): String {
        val suffix = isSuffix ?: (if (activeIsSuffix) true else isSuffixCurrency(currencySymbol, locale))
        val space = if (hasSpace ?: activeHasSpace) " " else if (suffix && !activeIsSuffix) " " else ""
        return if (suffix) {
            "$sign$amountStr$space$currencySymbol".trim()
        } else {
            "$sign$currencySymbol$space$amountStr"
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
