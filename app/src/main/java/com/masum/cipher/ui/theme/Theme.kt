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

private val DarkColorScheme = darkColorScheme(
    primary             = CipherBlue,
    onPrimary           = Color.White,
    primaryContainer    = CipherBlueContainer,
    onPrimaryContainer  = CipherBlue,

    secondary           = TextSecondary,
    onSecondary         = VoidBlack,
    secondaryContainer  = VoidSurfaceHigh,
    onSecondaryContainer= TextPrimary,

    tertiary            = CipherIncome,
    onTertiary          = VoidBlack,
    tertiaryContainer   = CipherIncomeContainer,
    onTertiaryContainer = CipherIncome,

    error               = CipherExpense,
    onError             = Color.White,
    errorContainer      = CipherExpenseContainer,
    onErrorContainer    = CipherExpense,

    background          = VoidBlack,
    onBackground        = TextPrimary,

    surface             = VoidSurface,
    onSurface           = TextPrimary,
    surfaceVariant      = VoidSurfaceHigh,
    onSurfaceVariant    = TextSecondary,

    outline             = TextTertiary,
    outlineVariant      = Color(0x1AFFFFFF),

    inverseSurface      = TextPrimary,
    inverseOnSurface    = VoidBlack,
    inversePrimary      = CipherBlue,
    scrim               = Color(0xCC000000)
)

private val LightColorScheme = lightColorScheme(
    primary             = LightPrimary,
    onPrimary           = LightOnPrimary,
    primaryContainer    = Color(0xFFDDE3FF),
    onPrimaryContainer  = Color(0xFF001258),

    secondary           = LightTextSecondary,
    onSecondary         = Color.White,
    secondaryContainer  = LightSurfaceHigh,
    onSecondaryContainer= LightTextPrimary,

    tertiary            = Color(0xFF0D9B62),
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFB8F5D8),
    onTertiaryContainer = Color(0xFF002116),

    error               = Color(0xFFBA1A1A),
    onError             = Color.White,
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Color(0xFF410002),

    background          = LightBackground,
    onBackground        = LightTextPrimary,

    surface             = LightSurface,
    onSurface           = LightTextPrimary,
    surfaceVariant      = LightSurfaceHigh,
    onSurfaceVariant    = LightTextSecondary,

    outline             = Color(0xFF9090A0),
    outlineVariant      = LightBorder
)

@Composable
fun CipherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
