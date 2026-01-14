package com.example.cipherspend.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsContract.State())
    val state: StateFlow<SettingsContract.State> = _state.asStateFlow()

    init {
        observeSettings()
    }

    fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.UpdateTheme -> updateTheme(intent.theme)
            is SettingsContract.Intent.SetBiometricEnabled -> updateBiometric(intent.enabled)
            is SettingsContract.Intent.SetPrivacyModeEnabled -> updatePrivacyMode(intent.enabled)
            is SettingsContract.Intent.SetAutoLockTimeout -> updateAutoLockTimeout(intent.timeoutMillis)
            is SettingsContract.Intent.SetCurrency -> updateCurrency(intent.currency)
            is SettingsContract.Intent.ClearAllData -> clearAllData()
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
                        autoLockTimeout = settings.autoLockTimeout,
                        currency = settings.currency
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

    private fun updateAutoLockTimeout(timeout: Long) {
        viewModelScope.launch { userPreferences.setAutoLockTimeout(timeout) }
    }

    private fun updateCurrency(currency: String) {
        viewModelScope.launch { userPreferences.setCurrency(currency) }
    }

    private fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteAllTransactions()
            _state.update { it.copy(isDataCleared = true) }
        }
    }
}