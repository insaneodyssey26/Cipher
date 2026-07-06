package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.config.CategorizerConfig
import com.masum.cipher.core.domain.model.TransactionCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorizerEngine @Inject constructor() {

    fun cleanMerchantName(raw: String): String {
        return raw.split("*", "-", "  ")
            .filter { it.isNotBlank() && it.length > 2 }
            .firstOrNull { it.any { char -> char.isLetter() } }
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
            ?: raw
    }

    fun categorize(merchantName: String): TransactionCategory {
        val normalized = merchantName.uppercase().trim()

        CategorizerConfig.BRAND_MAPPINGS[normalized]?.let { return it }

        for ((keywords, category) in CategorizerConfig.KEYWORD_ANCHORS) {
            if (keywords.any { normalized.contains(it) }) return category
        }

        return TransactionCategory.OTHERS
    }
}
