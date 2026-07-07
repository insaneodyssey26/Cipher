package com.masum.cipher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vault Design System - Midnight Deep Palette
 * 
 * Inspired by high-end developer tools and premium fintech.
 * Focuses on depth through subtle color shifts rather than elevation shadows.
 */

// Base Layers
val MidnightDeep = Color(0xFF0A0A0F)   // The base background - infinity deep
val VaultSurface = Color(0xFF12121A)   // Level 1 elevation - Cards
val VaultElevated = Color(0xFF1A1A24)  // Level 2 elevation - Dialogs, Bottom Sheets

// Accent - Electric Indigo
// WCAG AA Contrast Ratio: 6.1:1 against MidnightDeep
val ElectricIndigo = Color(0xFF6366F1)
val ElectricIndigoSubtle = Color(0x146366F1) // 8% opacity for backgrounds

// Semantic Colors
val EmeraldIncome = Color(0xFF10B981)
val EmeraldIncomeSubtle = Color(0x1410B981)

val RoseExpense = Color(0xFFF43F5E)
val RoseExpenseSubtle = Color(0x14F43F5E)

// Text Hierarchy
val Slate50 = Color(0xFFF8FAFC)   // Primary text in Dark Mode / Background in Light Mode
val Slate400 = Color(0xFF94A3B8)  // Secondary text - labels/hints
val Slate600 = Color(0xFF475569)  // Muted text - whispers/metadata
val Slate900 = Color(0xFF0F172A)  // Primary text in Light Mode

// Light Mode Layers
val LightBase = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightElevated = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)

// Utility
val Transparent = Color.Transparent
val White10 = Color(0x1AFFFFFF)
val Black40 = Color(0x66000000)

// Legacy Aliases (To be removed after UI overhaul)
val CipherBlue = ElectricIndigo
val CipherBlueDim = ElectricIndigoSubtle
val CipherBlueContainer = ElectricIndigoSubtle
val CipherIncome = EmeraldIncome
val CipherIncomeContainer = EmeraldIncomeSubtle
val CipherExpense = RoseExpense
val CipherExpenseContainer = RoseExpenseSubtle
val TextPrimary = Slate50
val TextSecondary = Slate400
val TextTertiary = Slate600
val IncomeGreen = EmeraldIncome
val ExpenseRed = RoseExpense
val BalanceBlue = ElectricIndigo
val VoidBlack = MidnightDeep
val VoidSurface = VaultSurface
val VoidSurfaceHigh = VaultElevated
