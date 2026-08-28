package com.masum.cipher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CipherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = ElectricIndigo,
    content: @Composable () -> Unit
) {
    val accentSubtle = accentColor.copy(alpha = 0.08f)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Slate50,
            primaryContainer = accentSubtle,
            onPrimaryContainer = accentColor,
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
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Slate50,
            primaryContainer = accentSubtle,
            onPrimaryContainer = accentColor,
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
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
