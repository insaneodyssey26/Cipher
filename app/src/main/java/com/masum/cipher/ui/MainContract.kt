package com.masum.cipher.ui

import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

class MainContract {
    sealed class Intent : UiIntent {
        object CheckAuthentication : Intent()
        object OnAppStop : Intent()
        data class SetOnboardingCompleted(val completed: Boolean) : Intent()
        object Authenticate : Intent()
        data class AddTransaction(val transaction: com.masum.cipher.core.data.local.entity.TransactionEntity) : Intent()
        data class SaveTrackedApps(val apps: Set<String>) : Intent()
        data class SaveAccentColor(val color: com.masum.cipher.core.data.local.pref.AccentColor) : Intent()
        data class UpdateDraftTransaction(val transaction: com.masum.cipher.core.data.local.entity.TransactionEntity?) : Intent()
    }

    data class State(
        val settings: UserSettings? = null,
        val isAuthenticated: Boolean = false,
        val isOnboardingRequired: Boolean = false,
        val draftTransaction: com.masum.cipher.core.data.local.entity.TransactionEntity? = null
    ) : UiState

    sealed class Effect : UiEffect {
        object TriggerBiometricPrompt : Effect()
    }
}
