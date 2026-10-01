package com.masum.cipher.core.domain

import java.util.Locale

data class MerchantGroup<T>(val name: String, val items: List<T>)

private val WHITESPACE = Regex("\\s+")

private fun normalizeSpacing(merchant: String): String = merchant.trim().replace(WHITESPACE, " ")

private fun merchantKey(merchant: String): String = normalizeSpacing(merchant).lowercase(Locale.ROOT)

fun <T> Iterable<T>.groupByMerchant(
    blankLabel: String = "",
    merchantOf: (T) -> String
): List<MerchantGroup<T>> =
    groupBy { merchantKey(merchantOf(it)) }.values.map { items ->
        val spellings = items.map { normalizeSpacing(merchantOf(it)) }
        val mostCommon = spellings.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key.orEmpty()
        MerchantGroup(name = mostCommon.ifEmpty { blankLabel }, items = items)
    }
