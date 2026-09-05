package com.masum.cipher.ui.settings

import android.net.Uri
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

class SettingsContract {
    sealed class Intent : UiIntent {
        data class UpdateTheme(val theme: AppTheme) : Intent()
        data class UpdateAccentColor(val color: com.masum.cipher.core.data.local.pref.AccentColor) : Intent()
        data class SetDynamicLogoEnabled(val enabled: Boolean) : Intent()
        data class SetBiometricEnabled(val enabled: Boolean) : Intent()
        data class SetAutoLockTimeout(val timeout: Long) : Intent()
        data class SetPrivacyModeEnabled(val enabled: Boolean) : Intent()
        data class SetNotifyAllTransactions(val enabled: Boolean) : Intent()
        data class SetNotifyBudgetAlerts(val enabled: Boolean) : Intent()
        data class SetNotifyDailySummary(val enabled: Boolean) : Intent()
        data class SetNotifyMonthlyWrapped(val enabled: Boolean) : Intent()
        data class SetNotifyUncategorizedReminder(val enabled: Boolean) : Intent()
        data class SetNotifySubscriptions(val enabled: Boolean) : Intent()
        data class SetNotifyNewAppDetected(val enabled: Boolean) : Intent()
        data class SetHapticsEnabled(val enabled: Boolean) : Intent()
        data class SetCurrency(val code: String, val symbol: String) : Intent()
        data class SetAppLanguage(val languageCode: String) : Intent()
        data class SetMonthlyBudget(val amount: Double, val isDynamic: Boolean = false) : Intent()
        object ClearAllData : Intent()
        data class ExportData(val uri: Uri, val password: CharArray) : Intent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ExportData

                if (uri != other.uri) return false
                if (!password.contentEquals(other.password)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = uri.hashCode()
                result = 31 * result + password.contentHashCode()
                return result
            }
        }

        data class ImportData(val uri: Uri, val password: CharArray) : Intent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ImportData

                if (uri != other.uri) return false
                if (!password.contentEquals(other.password)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = uri.hashCode()
                result = 31 * result + password.contentHashCode()
                return result
            }
        }

        data class ExportCsv(val uri: Uri) : Intent()
        data class ExportPdf(val uri: Uri) : Intent()
        data class SetAutoBackupEnabled(val enabled: Boolean) : Intent()
        data class SetAutoBackupFrequency(val frequency: com.masum.cipher.core.data.local.pref.AutoBackupFrequency) : Intent()
        data class SetAutoBackupUri(val uri: String?) : Intent()
        data class SetAutoBackupEncryptedPassword(val password: String?) : Intent()
    }

    data class State(
        val theme: AppTheme = AppTheme.SYSTEM,
        val accentColor: com.masum.cipher.core.data.local.pref.AccentColor = com.masum.cipher.core.data.local.pref.AccentColor.INDIGO,
        val isDynamicLogoEnabled: Boolean = true,
        val isBiometricEnabled: Boolean = false,
        val autoLockTimeout: Long = 0,
        val isPrivacyModeEnabled: Boolean = false,
        val notifyAllTransactions: Boolean = true,
        val notifyBudgetAlerts: Boolean = true,
        val notifyDailySummary: Boolean = true,
        val notifyMonthlyWrapped: Boolean = true,
        val notifyUncategorizedReminder: Boolean = true,
        val notifySubscriptions: Boolean = true,
        val notifyNewAppDetected: Boolean = true,
        val isHapticsEnabled: Boolean = true,
        val currencyCode: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().code,
        val currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
        val appLanguage: String = "system",
        val monthlyBudget: Double = 0.0,
        val isDynamicBudgetEnabled: Boolean = false,
        val thisMonthIncome: Double = 0.0,
        val autoBackupEnabled: Boolean = false,
        val autoBackupFrequency: com.masum.cipher.core.data.local.pref.AutoBackupFrequency = com.masum.cipher.core.data.local.pref.AutoBackupFrequency.NEVER,
        val autoBackupUri: String? = null,
        val autoBackupEncryptedPassword: String? = null,
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val isExportingCsv: Boolean = false,
        val isExportingPdf: Boolean = false,
        val message: String? = null
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
    }
}
