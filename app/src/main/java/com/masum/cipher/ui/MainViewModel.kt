package com.masum.cipher.ui

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.mvi.BaseViewModel
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val addTransactionUseCase: AddTransactionUseCase
) : BaseViewModel<MainContract.State, MainContract.Intent, MainContract.Effect>(
    initialState = MainContract.State()
) {

    init {
        userPreferences.settingsFlow
            .onEach { settings ->
                updateState {
                    copy(
                        settings = settings,
                        isOnboardingRequired = !settings.hasCompletedOnboarding
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: MainContract.Intent) {
        when (intent) {
            is MainContract.Intent.CheckAuthentication -> checkAuth()
            is MainContract.Intent.OnAppStop -> onStop()
            is MainContract.Intent.SetOnboardingCompleted -> setOnboardingCompleted(intent.completed)
            is MainContract.Intent.Authenticate -> updateState { copy(isAuthenticated = true) }
            is MainContract.Intent.AddTransaction -> addTransaction(intent.transaction)
            is MainContract.Intent.SaveTrackedApps -> saveTrackedApps(intent.apps)
            is MainContract.Intent.SaveAccentColor -> saveAccentColor(intent.color)
        }
    }

    private fun saveTrackedApps(apps: Set<String>) {
        viewModelScope.launch {
            userPreferences.setTrackedApps(apps)
        }
    }

    private fun saveAccentColor(color: com.masum.cipher.core.data.local.pref.AccentColor) {
        viewModelScope.launch {
            userPreferences.setAccentColor(color)
        }
    }

    private fun addTransaction(transaction: com.masum.cipher.core.data.local.entity.TransactionEntity) {
        viewModelScope.launch {
            addTransactionUseCase(transaction)
        }
    }

    private fun checkAuth() {
        val settings = currentState.settings ?: return
        val shouldLock = settings.isBiometricEnabled && biometricAuthenticator.isBiometricAvailable()

        if (shouldLock) {
            val timeDiff = System.currentTimeMillis() - settings.lastStopTime
            val isGracePeriodOver = timeDiff >= settings.autoLockTimeout
            if (isGracePeriodOver) {
                updateState { copy(isAuthenticated = false) }
                emitEffect(MainContract.Effect.TriggerBiometricPrompt)
            } else {
                updateState { copy(isAuthenticated = true) }
            }
        } else {
            updateState { copy(isAuthenticated = true) }
        }
    }

    private fun onStop() {
        viewModelScope.launch {
            userPreferences.setLastStopTime(System.currentTimeMillis())
        }
    }

    private fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(completed)
            if (completed) {
                userPreferences.setHasSeenNotificationFeature(true)
                userPreferences.setLastSeenWhatsNewVersion("4.2.0")
            }
        }
    }
}
