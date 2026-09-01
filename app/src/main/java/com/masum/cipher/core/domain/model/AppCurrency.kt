package com.masum.cipher.core.domain.model

import java.util.Currency
import java.util.Locale

data class AppCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val countryCode: String
) {
    companion object {
        val DEFAULT = AppCurrency(
            code = "INR",
            symbol = "₹",
            name = "Indian Rupee",
            countryCode = "IN"
        )

        val SUPPORTED_CURRENCIES = listOf(
            AppCurrency("USD", "$", "US Dollar", "US"),
            AppCurrency("EUR", "€", "Euro", "EU"),
            AppCurrency("GBP", "£", "British Pound", "GB"),
            AppCurrency("INR", "₹", "Indian Rupee", "IN"),
            AppCurrency("JPY", "¥", "Japanese Yen", "JP"),
            AppCurrency("BDT", "৳", "Bangladeshi Taka", "BD"),
            AppCurrency("CAD", "$", "Canadian Dollar", "CA"),
            AppCurrency("AUD", "$", "Australian Dollar", "AU"),
            AppCurrency("AED", "AED", "UAE Dirham", "AE"),
            AppCurrency("SGD", "S$", "Singapore Dollar", "SG")
        )

        fun fromCode(code: String, customSymbol: String? = null): AppCurrency {
            val matched = SUPPORTED_CURRENCIES.firstOrNull { it.code.equals(code, ignoreCase = true) }
            if (matched != null) {
                return if (customSymbol != null) matched.copy(symbol = customSymbol) else matched
            }
            return try {
                val jCurrency = Currency.getInstance(code.uppercase())
                AppCurrency(
                    code = jCurrency.currencyCode,
                    symbol = customSymbol ?: jCurrency.symbol,
                    name = jCurrency.displayName,
                    countryCode = "GLOBAL"
                )
            } catch (_: Exception) {
                AppCurrency(
                    code = code.uppercase(),
                    symbol = customSymbol ?: code.uppercase(),
                    name = code.uppercase(),
                    countryCode = "GLOBAL"
                )
            }
        }

        fun detectDefault(locale: Locale = Locale.getDefault()): AppCurrency {
            return try {
                val jCurrency = Currency.getInstance(locale)
                fromCode(jCurrency.currencyCode, jCurrency.symbol)
            } catch (_: Exception) {
                DEFAULT
            }
        }
    }
}
