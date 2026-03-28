package com.example.cipherspend.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.core.data.repository.BackupRepository
import com.example.cipherspend.core.util.AppFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val transactionDao: TransactionDao,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsContract.State())
    val state: StateFlow<SettingsContract.State> = _state.asStateFlow()

    private val _effect = Channel<SettingsContract.Effect>()
    val effect: Flow<SettingsContract.Effect> = _effect.receiveAsFlow()

    init {
        observeSettings()
    }

    fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.UpdateTheme -> updateTheme(intent.theme)
            is SettingsContract.Intent.SetBiometricEnabled -> updateBiometric(intent.enabled)
            is SettingsContract.Intent.SetPrivacyModeEnabled -> updatePrivacyMode(intent.enabled)
            is SettingsContract.Intent.SetHapticsEnabled -> updateHaptics(intent.enabled)
            is SettingsContract.Intent.SetAutoLockTimeout -> updateAutoLockTimeout(intent.timeout)
            is SettingsContract.Intent.SetMonthlyBudget -> updateMonthlyBudget(intent.amount)
            is SettingsContract.Intent.ClearAllData -> clearAllData()
            is SettingsContract.Intent.ExportData -> exportData(intent.uri, intent.password)
            is SettingsContract.Intent.ImportData -> importData(intent.uri, intent.password)
            is SettingsContract.Intent.ExportCsv -> exportCsv(intent.uri)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPreferences.settingsFlow.collect { settings ->
                _state.update { 
                    it.copy(
                        theme = settings.theme,
                        isBiometricEnabled = settings.isBiometricEnabled,
                        isPrivacyModeEnabled = settings.isPrivacyModeEnabled,
                        isHapticsEnabled = settings.isHapticsEnabled,
                        autoLockTimeout = settings.autoLockTimeout,
                        monthlyBudget = settings.monthlyBudget
                    )
                }
            }
        }
    }

    private fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { userPreferences.setTheme(theme) }
    }

    private fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setBiometricEnabled(enabled) }
    }

    private fun updatePrivacyMode(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPrivacyModeEnabled(enabled) }
    }

    private fun updateHaptics(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setHapticsEnabled(enabled) }
    }

    private fun updateAutoLockTimeout(timeout: Long) {
        viewModelScope.launch { userPreferences.setAutoLockTimeout(timeout) }
    }

    private fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch { userPreferences.setMonthlyBudget(amount) }
    }

    private fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteAllTransactions()
            _effect.send(SettingsContract.Effect.ShowToast("All data cleared successfully"))
        }
    }

    private fun exportCsv(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isExportingCsv = true) }
            try {
                val transactions = transactionDao.getAllTransactions().first()
                val csvHeader = "ID,Date,Merchant,Amount,Category,Type\n"
                val csvData = transactions.joinToString("\n") { tx ->
                    val date = AppFormatters.getFullDate().format(Date(tx.timestamp))
                    val type = if (tx.isIncome) "Income" else "Expense"
                    "${tx.id},\"$date\",\"${tx.merchant}\",${tx.amount},\"${tx.category}\",\"$type\""
                }
                
                backupRepository.provideOutputStream(uri)?.use { outputStream ->
                    outputStream.write((csvHeader + csvData).toByteArray())
                }
                _effect.send(SettingsContract.Effect.ShowToast("CSV Report generated successfully"))
            } catch (e: Exception) {
                _effect.send(SettingsContract.Effect.ShowToast("Failed to export CSV: ${e.message}"))
            } finally {
                _state.update { it.copy(isExportingCsv = false) }
            }
        }
    }

    private fun exportData(uri: android.net.Uri, password: CharArray) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            val outputStream = backupRepository.provideOutputStream(uri)
            if (outputStream == null) {
                _state.update { it.copy(isExporting = false) }
                _effect.send(SettingsContract.Effect.ShowToast("Could not open file for writing"))
                return@launch
            }
            val result = backupRepository.exportData(outputStream, password)
            _state.update { it.copy(isExporting = false) }
            
            val message = if (result.isSuccess) "Data exported successfully" else "Export failed: ${result.exceptionOrNull()?.message}"
            _effect.send(SettingsContract.Effect.ShowToast(message))
        }
    }

    private fun importData(uri: android.net.Uri, password: CharArray) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true) }
            val inputStream = backupRepository.provideInputStream(uri)
            if (inputStream == null) {
                _state.update { it.copy(isImporting = false) }
                _effect.send(SettingsContract.Effect.ShowToast("Could not open file for reading"))
                return@launch
            }
            val result = backupRepository.importData(inputStream, password)
            _state.update { it.copy(isImporting = false) }
            
            val message = if (result.isSuccess) "Data imported successfully" else "Import failed: ${result.exceptionOrNull()?.message}"
            _effect.send(SettingsContract.Effect.ShowToast(message))
        }
    }
}
