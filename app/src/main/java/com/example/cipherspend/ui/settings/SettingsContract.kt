package com.example.cipherspend.ui.settings

import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState

class SettingsContract {
    sealed class Intent : UiIntent {
        data class UpdateTheme(val theme: AppTheme) : Intent()
    }

    data class State(
        val currentTheme: AppTheme = AppTheme.SYSTEM
    ) : UiState
}