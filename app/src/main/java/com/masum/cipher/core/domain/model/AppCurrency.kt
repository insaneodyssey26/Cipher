package com.masum.cipher.core.domain.model

import java.util.Currency
import java.util.Locale

data class AppCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val countryCode: String,
    val isSuffix: Boolean = false,
    val hasSpace: Boolean = false
) {
    fun formatSample(amountStr: String = "1,450.00", sign: String = ""): String {
        val sym = symbol.ifBlank { code }
        val space = if (hasSpace) " " else ""
        return if (isSuffix) {
            "$sign$amountStr$space$sym".trim()
        } else {
            "$sign$sym$space$amountStr"
        }
    }

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
            AppCurrency("SGD", "S$", "Singapore Dollar", "SG"),
            AppCurrency("MXN", "$", "Mexican Peso", "MX"),
            AppCurrency("BRL", "R$", "Brazilian Real", "BR"),
            AppCurrency("CHF", "CHF", "Swiss Franc", "CH"),
            AppCurrency("CNY", "¥", "Chinese Yuan", "CN"),
            AppCurrency("HKD", "HK$", "Hong Kong Dollar", "HK"),
            AppCurrency("NZD", "NZ$", "New Zealand Dollar", "NZ"),
            AppCurrency("KRW", "₩", "South Korean Won", "KR"),
            AppCurrency("SAR", "SR", "Saudi Riyal", "SA"),
            AppCurrency("TRY", "₺", "Turkish Lira", "TR"),
            AppCurrency("RUB", "₽", "Russian Ruble", "RU"),
            AppCurrency("IDR", "Rp", "Indonesian Rupiah", "ID"),
            AppCurrency("MYR", "RM", "Malaysian Ringgit", "MY"),
            AppCurrency("PHP", "₱", "Philippine Peso", "PH"),
            AppCurrency("THB", "฿", "Thai Baht", "TH"),
            AppCurrency("VND", "₫", "Vietnamese Dong", "VN"),
            AppCurrency("PLN", "zł", "Polish Zloty", "PL"),
            AppCurrency("SEK", "kr", "Swedish Krona", "SE"),
            AppCurrency("NOK", "kr", "Norwegian Krone", "NO"),
            AppCurrency("DKK", "kr", "Danish Krone", "DK"),
            AppCurrency("ZAR", "R", "South African Rand", "ZA"),
            AppCurrency("PKR", "Rs", "Pakistani Rupee", "PK"),
            AppCurrency("EGP", "E£", "Egyptian Pound", "EG"),
            AppCurrency("NGN", "₦", "Nigerian Naira", "NG"),
            AppCurrency("ILS", "₪", "Israeli New Shekel", "IL")
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
                if (!locale.country.isNullOrBlank()) {
                    val jCurrency = Currency.getInstance(locale)
                    if (jCurrency != null) {
                        return fromCode(jCurrency.currencyCode, jCurrency.symbol)
                    }
                }
                when (locale.language.lowercase(Locale.ROOT)) {
                    "hi" -> fromCode("INR")
                    "bn" -> fromCode("BDT")
                    "ja" -> fromCode("JPY")
                    "pl" -> fromCode("PLN")
                    "pt" -> fromCode("BRL")
                    "de", "fr", "es" -> fromCode("EUR")
                    else -> {
                        val sys = Locale.getDefault()
                        if (!sys.country.isNullOrBlank()) {
                            val sysCur = Currency.getInstance(sys)
                            if (sysCur != null) fromCode(sysCur.currencyCode, sysCur.symbol) else DEFAULT
                        } else {
                            DEFAULT
                        }
                    }
                }
            } catch (_: Exception) {
                DEFAULT
            }
        }
    }
}
