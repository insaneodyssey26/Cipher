package com.example.cipherspend.core.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food & Dining", Icons.Rounded.Restaurant, Color(0xFFFF7043)),
    SHOPPING("Shopping", Icons.Rounded.ShoppingBag, Color(0xFFAB47BC)),
    TRANSPORT("Transport", Icons.Rounded.DirectionsCar, Color(0xFF26A69A)),
    ENTERTAINMENT("Entertainment", Icons.Rounded.Movie, Color(0xFF42A5F5)),
    HEALTH("Health", Icons.Rounded.MedicalServices, Color(0xFFEF5350)),
    BILLS("Bills & Utilities", Icons.AutoMirrored.Rounded.ReceiptLong, Color(0xFFFFCA28)),
    INCOME("Income", Icons.Rounded.AddCard, Color(0xFF66BB6A)),
    OTHERS("General", Icons.Rounded.Category, Color(0xFF78909C));

    companion object {
        fun fromString(name: String?): TransactionCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHERS
        }
    }
}
