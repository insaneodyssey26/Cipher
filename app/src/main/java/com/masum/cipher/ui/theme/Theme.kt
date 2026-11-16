package com.masum.cipher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun CipherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = ElectricIndigo,
    fontScale: Float = 1.0f,
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
            surfaceContainerLowest = Color(0xFF07070A),
            surfaceContainerLow = Color(0xFF0D0D13),
            surfaceContainer = VaultSurface,
            surfaceContainerHigh = VaultElevated,
            surfaceContainerHighest = Color(0xFF22222E),
            outline = Slate600,
            outlineVariant = Color(0x1FFFFFFF)
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
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE8EDF5),
            surfaceContainerHighest = Color(0xFFDEE5F0),
            outline = Slate400,
            outlineVariant = LightBorder
        )
    }


    val currentDensity = LocalDensity.current
    val effectiveFontScale = (currentDensity.fontScale * fontScale).coerceIn(0.7f, 2.0f)
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = effectiveFontScale
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            content = content
        )
    }
}
