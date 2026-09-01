package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.LucideIcons
import compose.icons.lucideicons.BriefcaseMedical
import compose.icons.lucideicons.CarFront
import compose.icons.lucideicons.Clapperboard
import compose.icons.lucideicons.HandCoins
import compose.icons.lucideicons.ReceiptText
import compose.icons.lucideicons.Shapes
import compose.icons.lucideicons.ShoppingBag
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.UtensilsCrossed

enum class TransactionCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    @androidx.annotation.StringRes val titleRes: Int
) {
    FOOD("Food & Dining", LucideIcons.UtensilsCrossed, Color(0xFFFF7043), com.masum.cipher.R.string.cat_food_dining),
    SHOPPING("Shopping", LucideIcons.ShoppingBag, Color(0xFFAB47BC), com.masum.cipher.R.string.cat_shopping),
    TRANSPORT("Transport", LucideIcons.CarFront, Color(0xFF26A69A), com.masum.cipher.R.string.cat_transport),
    ENTERTAINMENT("Entertainment", LucideIcons.Clapperboard, Color(0xFF42A5F5), com.masum.cipher.R.string.cat_entertainment),
    HEALTH("Health", LucideIcons.BriefcaseMedical, Color(0xFFEF5350), com.masum.cipher.R.string.cat_health),
    BILLS("Bills & Utilities", LucideIcons.ReceiptText, Color(0xFFFFCA28), com.masum.cipher.R.string.cat_bills_utilities),
    INVESTMENT("Investment", LucideIcons.TrendingUp, Color(0xFF5C6BC0), com.masum.cipher.R.string.cat_investment),
    INCOME("Income", LucideIcons.HandCoins, Color(0xFF66BB6A), com.masum.cipher.R.string.cat_income),
    OTHERS("General", LucideIcons.Shapes, Color(0xFF78909C), com.masum.cipher.R.string.cat_general);

    companion object {
        fun fromString(name: String?): TransactionCategory {
            if (name.isNullOrBlank()) return OTHERS
            return entries.find { 
                it.name.equals(name, ignoreCase = true) || 
                it.displayName.equals(name, ignoreCase = true)
            } ?: OTHERS
        }
    }
}
