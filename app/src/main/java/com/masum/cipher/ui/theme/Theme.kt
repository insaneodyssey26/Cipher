package com.masum.cipher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Vault Dark Color Scheme
 * 
 * Optimized for OLED displays with #0A0A0F base to maintain depth.
 */
private val VaultDarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = Slate50,
    primaryContainer = ElectricIndigoSubtle,
    onPrimaryContainer = ElectricIndigo,

    secondary = Slate400,
    onSecondary = MidnightDeep,
    secondaryContainer = VaultSurface,
    onSecondaryContainer = Slate50,

    tertiary = EmeraldIncome,
    onTertiary = MidnightDeep,
    tertiaryContainer = EmeraldIncomeSubtle,
    onTertiaryContainer = EmeraldIncome,

    error = RoseExpense,
    onError = Slate50,
    errorContainer = RoseExpenseSubtle,
    onErrorContainer = RoseExpense,

    background = MidnightDeep,
    onBackground = Slate50,

    surface = VaultSurface,
    onSurface = Slate50,
    surfaceVariant = VaultElevated,
    onSurfaceVariant = Slate400,

    outline = Slate600,
    outlineVariant = White10
)

/**
 * Vault Light Color Scheme
 * 
 * Reverses the depth system using Slate-based backgrounds.
 */
private val VaultLightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Slate50,
    primaryContainer = ElectricIndigoSubtle,
    onPrimaryContainer = ElectricIndigo,

    secondary = Slate600,
    onSecondary = Slate50,
    secondaryContainer = LightSurface,
    onSecondaryContainer = Slate900,

    tertiary = EmeraldIncome,
    onTertiary = Slate50,
    tertiaryContainer = EmeraldIncomeSubtle,
    onTertiaryContainer = EmeraldIncome,

    error = RoseExpense,
    onError = Slate50,
    errorContainer = RoseExpenseSubtle,
    onErrorContainer = RoseExpense,

    background = LightBase,
    onBackground = Slate900,

    surface = LightSurface,
    onSurface = Slate900,
    surfaceVariant = LightElevated,
    onSurfaceVariant = Slate600,

    outline = Slate400,
    outlineVariant = LightBorder
)

@Composable
fun CipherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VaultDarkColorScheme else VaultLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
