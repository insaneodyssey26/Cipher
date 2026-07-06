package com.masum.cipher.ui.insights

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetInsightsUseCase
import com.masum.cipher.core.domain.usecase.UpdateTransactionUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getInsightsUseCase: GetInsightsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase
) : BaseViewModel<InsightsContract.State, InsightsContract.Intent, InsightsContract.Effect>(
    initialState = InsightsContract.State()
) {

    init {
        loadInsights()
    }

    override fun handleIntent(intent: InsightsContract.Intent) {
        when (intent) {
            is InsightsContract.Intent.LoadInsights -> loadInsights()
            is InsightsContract.Intent.SelectDay -> {
                updateState { copy(selectedDayTimestamp = intent.timestamp) }
            }
            is InsightsContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
            is InsightsContract.Intent.UpdateTransaction -> updateTransaction(intent.transaction)
            is InsightsContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction)
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            getInsightsUseCase().collect { newState ->
                updateState { 
                    newState.copy(selectedDayTimestamp = this.selectedDayTimestamp)
                }
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
            emitEffect(InsightsContract.Effect.ShowUndoDelete(transaction))
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { updateTransactionUseCase(transaction) }
    }

    private fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { addTransactionUseCase(transaction) }
    }
}
