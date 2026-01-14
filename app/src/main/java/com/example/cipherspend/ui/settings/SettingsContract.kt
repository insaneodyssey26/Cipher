package com.example.cipherspend.ui.settings

import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState

class SettingsContract {
    sealed class Intent : UiIntent {
        data class UpdateTheme(val theme: AppTheme) : Intent()
        data class SetBiometricEnabled(val enabled: Boolean) : Intent()
        data class SetPrivacyModeEnabled(val enabled: Boolean) : Intent()
        data class SetCurrency(val currency: String) : Intent()
        object ClearAllData : Intent()
    }

    data class State(
        val theme: AppTheme = AppTheme.SYSTEM,
        val isBiometricEnabled: Boolean = true,
        val isPrivacyModeEnabled: Boolean = false,
        val currency: String = "INR",
        val isDataCleared: Boolean = false
    ) : UiState
}