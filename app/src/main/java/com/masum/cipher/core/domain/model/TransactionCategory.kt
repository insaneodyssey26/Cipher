package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.LucideIcons
import compose.icons.lucideicons.UtensilsCrossed
import compose.icons.lucideicons.ShoppingBag
import compose.icons.lucideicons.CarFront
import compose.icons.lucideicons.Clapperboard
import compose.icons.lucideicons.BriefcaseMedical
import compose.icons.lucideicons.ReceiptText
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.HandCoins
import compose.icons.lucideicons.Shapes

enum class TransactionCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food & Dining", LucideIcons.UtensilsCrossed, Color(0xFFFF7043)),
    SHOPPING("Shopping", LucideIcons.ShoppingBag, Color(0xFFAB47BC)),
    TRANSPORT("Transport", LucideIcons.CarFront, Color(0xFF26A69A)),
    ENTERTAINMENT("Entertainment", LucideIcons.Clapperboard, Color(0xFF42A5F5)),
    HEALTH("Health", LucideIcons.BriefcaseMedical, Color(0xFFEF5350)),
    BILLS("Bills & Utilities", LucideIcons.ReceiptText, Color(0xFFFFCA28)),
    INVESTMENT("Investment", LucideIcons.TrendingUp, Color(0xFF5C6BC0)),
    INCOME("Income", LucideIcons.HandCoins, Color(0xFF66BB6A)),
    OTHERS("General", LucideIcons.Shapes, Color(0xFF78909C));

    companion object {
        fun fromString(name: String?): TransactionCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHERS
        }
    }
}
