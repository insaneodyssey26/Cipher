package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity

data class CategoryItem(
    val id: Long? = null,
    val name: String,
    val displayName: String,
    val icon: ImageVector,
    val iconName: String,
    val color: Color,
    val colorHex: Long,
    val isCustom: Boolean = false,
    val titleRes: Int? = null
) {
    companion object {
        fun fromDefault(category: TransactionCategory): CategoryItem {
            return CategoryItem(
                id = null,
                name = category.name,
                displayName = category.displayName,
                icon = category.icon,
                iconName = category.name,
                color = category.color,
                colorHex = category.color.value.toLong(),
                isCustom = false,
                titleRes = category.titleRes
            )
        }

        fun fromCustom(entity: CustomCategoryEntity): CategoryItem {
            return CategoryItem(
                id = entity.id,
                name = entity.name,
                displayName = entity.name,
                icon = CategoryIconRegistry.getIcon(entity.iconName),
                iconName = entity.iconName,
                color = Color(entity.colorHex.toInt()),
                colorHex = entity.colorHex,
                isCustom = true,
                titleRes = null
            )
        }
    }
}
