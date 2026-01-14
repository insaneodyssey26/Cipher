package com.example.cipherspend.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsContract.State())
    val state: StateFlow<SettingsContract.State> = _state.asStateFlow()

    init {
        observeTheme()
    }

    fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.UpdateTheme -> updateTheme(intent.theme)
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferences.themeFlow.collect { theme ->
                _state.update { it.copy(currentTheme = theme) }
            }
        }
    }

    private fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            themePreferences.setTheme(theme)
        }
    }
}