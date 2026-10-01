package com.masum.cipher.ui.onboarding

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.domain.usecase.ImportDataUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestoreBackupViewModel @Inject constructor(
    private val importDataUseCase: ImportDataUseCase
) : BaseViewModel<RestoreBackupContract.State, RestoreBackupContract.Intent, RestoreBackupContract.Effect>(
    initialState = RestoreBackupContract.State()
) {

    override fun handleIntent(intent: RestoreBackupContract.Intent) {
        when (intent) {
            is RestoreBackupContract.Intent.Restore -> restore(intent.uri, intent.password)
            RestoreBackupContract.Intent.DismissError -> updateState { copy(errorMessage = null) }
        }
    }

    private fun restore(uri: Uri, password: CharArray) {
        if (currentState.isRestoring) return
        viewModelScope.launch {
            updateState { copy(isRestoring = true, errorMessage = null) }
            importDataUseCase(uri, password).fold(
                onSuccess = {
                    updateState { copy(isRestoring = false) }
                    emitEffect(RestoreBackupContract.Effect.Restored)
                },
                onFailure = { error ->
                    updateState { copy(isRestoring = false, errorMessage = error.message.orEmpty()) }
                }
            )
        }
    }
}
