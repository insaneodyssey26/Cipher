package com.masum.cipher.core.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.LucideIcons
import compose.icons.lucideicons.BookOpen
import compose.icons.lucideicons.Briefcase
import compose.icons.lucideicons.BriefcaseMedical
import compose.icons.lucideicons.CarFront
import compose.icons.lucideicons.Clapperboard
import compose.icons.lucideicons.Coffee
import compose.icons.lucideicons.Dumbbell
import compose.icons.lucideicons.Fuel
import compose.icons.lucideicons.Gamepad2
import compose.icons.lucideicons.Gift
import compose.icons.lucideicons.GraduationCap
import compose.icons.lucideicons.HandCoins
import compose.icons.lucideicons.Heart
import compose.icons.lucideicons.Laptop
import compose.icons.lucideicons.Luggage
import compose.icons.lucideicons.Music
import compose.icons.lucideicons.PawPrint
import compose.icons.lucideicons.Plane
import compose.icons.lucideicons.ReceiptText
import compose.icons.lucideicons.Shapes
import compose.icons.lucideicons.Shirt
import compose.icons.lucideicons.ShoppingBag
import compose.icons.lucideicons.Smartphone
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Trophy
import compose.icons.lucideicons.Tv
import compose.icons.lucideicons.UtensilsCrossed
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.Wrench

object CategoryIconRegistry {
    val ICONS: List<Pair<String, ImageVector>> = listOf(
        "UtensilsCrossed" to LucideIcons.UtensilsCrossed,
        "Coffee" to LucideIcons.Coffee,
        "ShoppingBag" to LucideIcons.ShoppingBag,
        "Shirt" to LucideIcons.Shirt,
        "CarFront" to LucideIcons.CarFront,
        "Fuel" to LucideIcons.Fuel,
        "Plane" to LucideIcons.Plane,
        "Luggage" to LucideIcons.Luggage,
        "Clapperboard" to LucideIcons.Clapperboard,
        "Tv" to LucideIcons.Tv,
        "Music" to LucideIcons.Music,
        "Gamepad2" to LucideIcons.Gamepad2,
        "BriefcaseMedical" to LucideIcons.BriefcaseMedical,
        "Heart" to LucideIcons.Heart,
        "Dumbbell" to LucideIcons.Dumbbell,
        "ReceiptText" to LucideIcons.ReceiptText,
        "Wallet" to LucideIcons.Wallet,
        "Wrench" to LucideIcons.Wrench,
        "TrendingUp" to LucideIcons.TrendingUp,
        "HandCoins" to LucideIcons.HandCoins,
        "GraduationCap" to LucideIcons.GraduationCap,
        "BookOpen" to LucideIcons.BookOpen,
        "Gift" to LucideIcons.Gift,
        "PawPrint" to LucideIcons.PawPrint,
        "Sparkles" to LucideIcons.Sparkles,
        "Trophy" to LucideIcons.Trophy,
        "Laptop" to LucideIcons.Laptop,
        "Smartphone" to LucideIcons.Smartphone,
        "Briefcase" to LucideIcons.Briefcase,
        "Shapes" to LucideIcons.Shapes
    )

    private val ICON_MAP: Map<String, ImageVector> = ICONS.toMap()

    fun getIcon(iconName: String?): ImageVector {
        return ICON_MAP[iconName] ?: LucideIcons.Shapes
    }
}

object CategoryColorRegistry {
    val COLORS: List<Long> = listOf(
        0xFFFF7043L,
        0xFFAB47BCL,
        0xFF26A69AL,
        0xFF42A5F5L,
        0xFFEF5350L,
        0xFFFFCA28L,
        0xFF5C6BC0L,
        0xFF66BB6AL,
        0xFFEC407AL,
        0xFF26C6DAL,
        0xFF8D6E63L,
        0xFFFFA726L,
        0xFF7E57C2L,
        0xFF78909CL,
        0xFF2E7D32L,
        0xFFD81B60L
    )
}
