package com.masum.cipher.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetDashboardDataUseCase
import com.masum.cipher.core.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DashboardContract.FilterType.ALL)

    private val _state = MutableStateFlow(DashboardContract.State())
    val state: StateFlow<DashboardContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardContract.Effect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<DashboardContract.Effect> = _effect.asSharedFlow()

    init {
        observeDashboardData()
    }

    fun handleIntent(intent: DashboardContract.Intent) {
        when (intent) {
            is DashboardContract.Intent.LoadDashboard -> { }
            is DashboardContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
            is DashboardContract.Intent.UpdateTransaction -> updateTransaction(intent.transaction)
            is DashboardContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction)
            is DashboardContract.Intent.AddTransaction -> addTransaction(intent.transaction)
            is DashboardContract.Intent.SearchTransactions -> _searchQuery.value = intent.query
            is DashboardContract.Intent.FilterTransactions -> _activeFilter.value = intent.filter
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(_searchQuery, _activeFilter) { query, filter ->
                query to filter
            }.flatMapLatest { (query, filter) ->
                getDashboardDataUseCase(query, filter)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
            _effect.emit(DashboardContract.Effect.ShowUndoDelete(transaction))
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            updateTransactionUseCase(transaction)
        }
    }

    private fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            addTransactionUseCase(transaction)
        }
    }

    private fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            addTransactionUseCase(transaction)
        }
    }
}
