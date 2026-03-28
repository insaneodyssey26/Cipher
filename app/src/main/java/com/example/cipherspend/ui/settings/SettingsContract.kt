package com.example.cipherspend.ui.settings

import android.net.Uri
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.mvi.UiEffect
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState

class SettingsContract {
    sealed class Intent : UiIntent {
        data class UpdateTheme(val theme: AppTheme) : Intent()
        data class SetBiometricEnabled(val enabled: Boolean) : Intent()
        data class SetAutoLockTimeout(val timeout: Long) : Intent()
        data class SetPrivacyModeEnabled(val enabled: Boolean) : Intent()
        data class SetHapticsEnabled(val enabled: Boolean) : Intent()
        data class SetMonthlyBudget(val amount: Double) : Intent()
        object ClearAllData : Intent()
        data class ExportData(val uri: Uri, val password: CharArray) : Intent()
        data class ImportData(val uri: Uri, val password: CharArray) : Intent()
        data class ExportCsv(val uri: Uri) : Intent()
    }

    data class State(
        val theme: AppTheme = AppTheme.SYSTEM,
        val isBiometricEnabled: Boolean = false,
        val autoLockTimeout: Long = 0,
        val isPrivacyModeEnabled: Boolean = false,
        val isHapticsEnabled: Boolean = true,
        val monthlyBudget: Double = 0.0,
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val isExportingCsv: Boolean = false,
        val message: String? = null
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
    }
}
