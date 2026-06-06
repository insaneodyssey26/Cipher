package com.masum.cipher.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Void Design System ───────────────────────────────────────────────────────
// Near-black with the faintest indigo warmth. Not pure black — that's too harsh.
// Not grey — that's too generic. Cipher has its own darkness.

val VoidBlack       = Color(0xFF000000)  // Pure AMOLED black
val VoidSurface     = Color(0xFF0A0A0F)  // Card surfaces
val VoidSurfaceHigh = Color(0xFF141420)  // Elevated — modals, bottom sheets
val VoidBorder      = Color(0x14FFFFFF)  // 8% white — barely-there card edges
val VoidBorderBright= Color(0x1FFFFFFF)  // 12% white — interactive borders

// Primary — Cipher Blue
val CipherBlue          = Color(0xFF4E6CF7)
val CipherBlueContainer = Color(0xFF18224E)
val CipherBlueDim       = Color(0x334E6CF7)  // 20% blue for tinted backgrounds

// Semantic colors
val CipherExpense          = Color(0xFFE8453C)
val CipherExpenseContainer = Color(0xFF2E1210)
val CipherIncome           = Color(0xFF1AC47D)
val CipherIncomeContainer  = Color(0xFF0B2E1F)

// Text scale
val TextPrimary   = Color(0xFFEEEEF5)  // High-emphasis
val TextSecondary = Color(0xFF8585A0)  // Mid — labels, subtitles
val TextTertiary  = Color(0xFF3A3A50)  // Low — disabled, hints, dividers

// Light theme — kept minimal, not the focus of this app
val LightBackground    = Color(0xFFF6F6FA)
val LightSurface       = Color(0xFFFFFFFF)
val LightSurfaceHigh   = Color(0xFFF0F0F6)
val LightPrimary       = Color(0xFF3A57E8)
val LightOnPrimary     = Color(0xFFFFFFFF)
val LightTextPrimary   = Color(0xFF12121A)
val LightTextSecondary = Color(0xFF6B6B85)
val LightBorder        = Color(0x14000000)

// Aliases — used in component files for direct import
val IncomeGreen = CipherIncome
val ExpenseRed  = CipherExpense
val BalanceBlue = CipherBlue
