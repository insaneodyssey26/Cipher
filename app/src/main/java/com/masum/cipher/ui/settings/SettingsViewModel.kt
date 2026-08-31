package com.masum.cipher.ui.settings

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.usecase.ClearAllDataUseCase
import com.masum.cipher.core.domain.usecase.ExportCsvUseCase
import com.masum.cipher.core.domain.usecase.ExportDataUseCase
import com.masum.cipher.core.domain.usecase.ExportPdfUseCase
import com.masum.cipher.core.domain.usecase.ImportDataUseCase
import com.masum.cipher.core.domain.usecase.UpdateSettingsUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import com.masum.cipher.core.security.KeystoreManager
import com.masum.cipher.core.worker.AutoBackupScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val exportPdfUseCase: ExportPdfUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val localNotificationManager: com.masum.cipher.core.notifications.LocalNotificationManager,
    private val keystoreManager: KeystoreManager,
    private val autoBackupScheduler: AutoBackupScheduler,
    private val transactionDao: TransactionDao
) : BaseViewModel<SettingsContract.State, SettingsContract.Intent, SettingsContract.Effect>(
    initialState = SettingsContract.State()
) {

    init {
        observeSettings()
    }

    override fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.UpdateTheme -> updateTheme(intent.theme)
            is SettingsContract.Intent.UpdateAccentColor -> updateAccentColor(intent.color)
            is SettingsContract.Intent.SetBiometricEnabled -> updateBiometric(intent.enabled)
            is SettingsContract.Intent.SetPrivacyModeEnabled -> updatePrivacyMode(intent.enabled)
            is SettingsContract.Intent.SetNotifyAllTransactions -> updateNotifyAllTransactions(intent.enabled)
            is SettingsContract.Intent.SetNotifyBudgetAlerts -> updateNotifyBudgetAlerts(intent.enabled)
            is SettingsContract.Intent.SetNotifyDailySummary -> updateNotifyDailySummary(intent.enabled)
            is SettingsContract.Intent.SetNotifyMonthlyWrapped -> updateNotifyMonthlyWrapped(intent.enabled)
            is SettingsContract.Intent.SetNotifyUncategorizedReminder -> updateNotifyUncategorizedReminder(intent.enabled)
            is SettingsContract.Intent.SetNotifySubscriptions -> updateNotifySubscriptions(intent.enabled)
            is SettingsContract.Intent.SetNotifyNewAppDetected -> updateNotifyNewAppDetected(intent.enabled)
            is SettingsContract.Intent.SetHapticsEnabled -> updateHaptics(intent.enabled)
            is SettingsContract.Intent.SetAutoLockTimeout -> updateAutoLockTimeout(intent.timeout)
            is SettingsContract.Intent.SetMonthlyBudget -> updateMonthlyBudget(intent.amount, intent.isDynamic)
            is SettingsContract.Intent.ClearAllData -> clearAllData()
            is SettingsContract.Intent.ExportData -> exportData(intent.uri, intent.password)
            is SettingsContract.Intent.ImportData -> importData(intent.uri, intent.password)
            is SettingsContract.Intent.ExportCsv -> exportCsv(intent.uri)
            is SettingsContract.Intent.ExportPdf -> exportPdf(intent.uri)
            is SettingsContract.Intent.SetAutoBackupEnabled -> updateAutoBackupEnabled(intent.enabled)
            is SettingsContract.Intent.SetAutoBackupFrequency -> updateAutoBackupFrequency(intent.frequency)
            is SettingsContract.Intent.SetAutoBackupUri -> updateAutoBackupUri(intent.uri)
            is SettingsContract.Intent.SetAutoBackupEncryptedPassword -> updateAutoBackupPassword(intent.password)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPreferences.settingsFlow.collect { settings ->
                val monthIncome = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val start = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    transactionDao.sumIncomeSince(start)
                }
                updateState {
                    copy(
                        theme = settings.theme,
                        accentColor = settings.accentColor,
                        isBiometricEnabled = settings.isBiometricEnabled,
                        isPrivacyModeEnabled = settings.isPrivacyModeEnabled,
                        notifyAllTransactions = settings.notifyAllTransactions,
                        notifyBudgetAlerts = settings.notifyBudgetAlerts,
                        notifyDailySummary = settings.notifyDailySummary,
                        notifyMonthlyWrapped = settings.notifyMonthlyWrapped,
                        notifyUncategorizedReminder = settings.notifyUncategorizedReminder,
                        notifySubscriptions = settings.notifySubscriptions,
                        notifyNewAppDetected = settings.notifyNewAppDetected,
                        isHapticsEnabled = settings.isHapticsEnabled,
                        autoLockTimeout = settings.autoLockTimeout,
                        monthlyBudget = settings.monthlyBudget,
                        isDynamicBudgetEnabled = settings.isDynamicBudgetEnabled,
                        thisMonthIncome = monthIncome,
                        autoBackupEnabled = settings.autoBackupEnabled,
                        autoBackupFrequency = settings.autoBackupFrequency,
                        autoBackupUri = settings.autoBackupUri
                    )
                }
            }
        }
    }

    private fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { updateSettingsUseCase.theme(theme) }
    }

    private fun updateAccentColor(color: com.masum.cipher.core.data.local.pref.AccentColor) {
        viewModelScope.launch { updateSettingsUseCase.accentColor(color) }
    }

    private fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch { updateSettingsUseCase.biometric(enabled) }
    }

    private fun updateNotifyAllTransactions(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyAllTransactions(enabled)
            updateState { copy(notifyAllTransactions = enabled) }
        }
    }

    private fun updateNotifyBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyBudgetAlerts(enabled)
            updateState { copy(notifyBudgetAlerts = enabled) }
        }
    }

    private fun updateNotifyDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyDailySummary(enabled)
            updateState { copy(notifyDailySummary = enabled) }
        }
    }

    private fun updateNotifyMonthlyWrapped(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyMonthlyWrapped(enabled)
            updateState { copy(notifyMonthlyWrapped = enabled) }
        }
    }

    private fun updateNotifyUncategorizedReminder(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyUncategorizedReminder(enabled)
            updateState { copy(notifyUncategorizedReminder = enabled) }
        }
    }

    private fun updateNotifySubscriptions(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifySubscriptions(enabled)
            updateState { copy(notifySubscriptions = enabled) }
        }
    }

    private fun updateNotifyNewAppDetected(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyNewAppDetected(enabled)
            updateState { copy(notifyNewAppDetected = enabled) }
        }
    }

    private fun updatePrivacyMode(enabled: Boolean) {
        viewModelScope.launch { updateSettingsUseCase.privacyMode(enabled) }
    }

    private fun updateHaptics(enabled: Boolean) {
        viewModelScope.launch { updateSettingsUseCase.haptics(enabled) }
    }

    private fun updateAutoLockTimeout(timeout: Long) {
        viewModelScope.launch { updateSettingsUseCase.autoLockTimeout(timeout) }
    }

    private fun updateMonthlyBudget(amount: Double, isDynamic: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.monthlyBudget(amount)
            updateSettingsUseCase.dynamicBudget(isDynamic)
        }
    }

    private fun updateAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch { 
            updateSettingsUseCase.autoBackupEnabled(enabled) 
            if (!enabled) {
                autoBackupScheduler.cancelBackup()
            } else {
                autoBackupScheduler.scheduleBackup(currentState.autoBackupFrequency)
            }
        }
    }

    private fun updateAutoBackupFrequency(frequency: com.masum.cipher.core.data.local.pref.AutoBackupFrequency) {
        viewModelScope.launch { 
            updateSettingsUseCase.autoBackupFrequency(frequency) 
            if (currentState.autoBackupEnabled) {
                autoBackupScheduler.scheduleBackup(frequency)
            }
        }
    }

    private fun updateAutoBackupUri(uri: String?) {
        viewModelScope.launch { updateSettingsUseCase.autoBackupUri(uri) }
    }

    private fun updateAutoBackupPassword(password: String?) {
        viewModelScope.launch {
            if (password == null) {
                updateSettingsUseCase.autoBackupEncryptedPassword(null)
            } else {
                val encrypted = keystoreManager.encrypt(password)
                updateSettingsUseCase.autoBackupEncryptedPassword(encrypted)
            }
        }
    }

    private fun clearAllData() {
        viewModelScope.launch {
            clearAllDataUseCase()
            emitEffect(SettingsContract.Effect.ShowToast("All data cleared successfully"))
        }
    }

    private fun exportCsv(uri: android.net.Uri) {
        viewModelScope.launch {
            updateState { copy(isExportingCsv = true) }
            val result = exportCsvUseCase(uri)
            updateState { copy(isExportingCsv = false) }
            
            val message = if (result.isSuccess) "CSV Report generated successfully" else "Failed to export CSV: ${result.exceptionOrNull()?.message}"
            emitEffect(SettingsContract.Effect.ShowToast(message))
        }
    }

    private fun exportPdf(uri: android.net.Uri) {
        viewModelScope.launch {
            updateState { copy(isExportingPdf = true) }
            val result = exportPdfUseCase(uri)
            updateState { copy(isExportingPdf = false) }
            
            if (result.isSuccess) {
                emitEffect(SettingsContract.Effect.ShowToast("PDF Statement generated successfully"))
                localNotificationManager.showPdfGeneratedNotification(uri)
            } else {
                emitEffect(SettingsContract.Effect.ShowToast("Failed to export PDF: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private fun exportData(uri: android.net.Uri, password: CharArray) {
        viewModelScope.launch {
            updateState { copy(isExporting = true) }
            val result = exportDataUseCase(uri, password)
            updateState { copy(isExporting = false) }
            
            val message = if (result.isSuccess) "Data exported successfully" else "Export failed: ${result.exceptionOrNull()?.message}"
            emitEffect(SettingsContract.Effect.ShowToast(message))
        }
    }

    private fun importData(uri: android.net.Uri, password: CharArray) {
        viewModelScope.launch {
            updateState { copy(isImporting = true) }
            val result = importDataUseCase(uri, password)
            updateState { copy(isImporting = false) }
            
            val message = if (result.isSuccess) "Data imported successfully" else "Import failed: ${result.exceptionOrNull()?.message}"
            emitEffect(SettingsContract.Effect.ShowToast(message))
        }
    }
}
