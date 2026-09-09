package com.masum.cipher.core.domain.model

import com.masum.cipher.core.data.local.entity.CustomCategoryEntity

object CategoryHelper {

    fun resolveCategory(
        categoryName: String?,
        customCategories: List<CustomCategoryEntity> = emptyList()
    ): CategoryItem {
        if (!categoryName.isNullOrBlank()) {
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
