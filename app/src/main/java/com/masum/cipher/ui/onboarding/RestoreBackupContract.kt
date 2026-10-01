package com.masum.cipher.ui.onboarding

import android.net.Uri
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

object RestoreBackupContract {

    data class State(
        val isRestoring: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        class Restore(val uri: Uri, val password: CharArray) : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object Restored : Effect
    }
}
