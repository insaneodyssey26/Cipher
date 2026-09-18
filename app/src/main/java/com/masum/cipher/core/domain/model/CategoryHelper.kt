package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.Color
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeftRight

object CategoryHelper {

    fun resolveCategory(
        categoryName: String?,
        customCategories: List<CustomCategoryEntity> = emptyList()
    ): CategoryItem {
        if (!categoryName.isNullOrBlank()) {
            if (categoryName.equals("TRANSFER", ignoreCase = true)) {
                return CategoryItem(
                    id = null,
                    name = "TRANSFER",
                    displayName = "Transfer",
                    icon = compose.icons.LucideIcons.ArrowLeftRight,
                    iconName = "ArrowLeftRight",
                    color = androidx.compose.ui.graphics.Color(0xFF06B6D4),
                    colorHex = 0xFF06B6D4,
                    isCustom = false,
                    titleRes = com.masum.cipher.R.string.account_details_filter_transfer
                )
            }
            val customMatch = customCategories.find { it.name.equals(categoryName, ignoreCase = true) }
            if (customMatch != null) {
                return CategoryItem.fromCustom(customMatch)
            }
        }
        val defaultCategory = TransactionCategory.fromString(categoryName)
        return CategoryItem.fromDefault(defaultCategory)
    }

    fun getAllCategories(
        customCategories: List<CustomCategoryEntity> = emptyList(),
        includeIncome: Boolean = true
    ): List<CategoryItem> {
        val defaultList = if (includeIncome) {
            TransactionCategory.entries.map { CategoryItem.fromDefault(it) }
        } else {
            TransactionCategory.entries.filter { it != TransactionCategory.INCOME }.map { CategoryItem.fromDefault(it) }
        }
        val customList = customCategories.map { CategoryItem.fromCustom(it) }
        return defaultList + customList
    }
}
