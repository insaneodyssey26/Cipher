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
        data class AddTransaction(val transaction: com.masum.cipher.core.data.local.entity.TransactionEntity, val splits: List<com.masum.cipher.core.domain.model.SplitParticipant> = emptyList()) : Intent()
        data class SaveTrackedApps(val apps: Set<String>) : Intent()
        data class SaveAccentColor(val color: com.masum.cipher.core.data.local.pref.AccentColor) : Intent()
        data class SaveTheme(val theme: com.masum.cipher.core.data.local.pref.AppTheme) : Intent()
        data class SaveLanguage(val languageCode: String) : Intent()
        data class SaveCurrency(val currencyCode: String, val currencySymbol: String) : Intent()
        data class UpdateDraftTransaction(val transaction: com.masum.cipher.core.data.local.entity.TransactionEntity?) : Intent()
        data class SetNavBarCompressed(val compressed: Boolean) : Intent()
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
