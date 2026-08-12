package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.config.CategorizerConfig
import com.masum.cipher.core.domain.model.TransactionCategory
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorizerEngine @Inject constructor() {

    fun cleanMerchantName(raw: String): String {
        return raw.split("*", "-", "  ")
            .filter { it.isNotBlank() && it.length > 2 }
            .firstOrNull { it.any { char -> char.isLetter() } }
            ?.lowercase()
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: raw
    }

    fun categorize(merchantName: String): TransactionCategory {
        val normalized = merchantName.uppercase().trim()

        if (normalized.isBlank()) return TransactionCategory.OTHERS

        val mappedCategory = CategorizerConfig.BRAND_MAPPINGS.entries.firstOrNull { 
            normalized.contains(it.key) || normalized.startsWith(it.key + " ")
        }?.value
        
        if (mappedCategory != null) return mappedCategory

        for ((keywords, category) in CategorizerConfig.KEYWORD_ANCHORS) {
            if (keywords.any { normalized.contains(it) }) return category
        }

        return TransactionCategory.OTHERS
    }
}
