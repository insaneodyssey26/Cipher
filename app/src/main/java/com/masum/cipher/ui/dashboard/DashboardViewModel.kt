package com.masum.cipher.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetDashboardDataUseCase
import com.masum.cipher.core.domain.usecase.UpdateTransactionUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
) : BaseViewModel<DashboardContract.State, DashboardContract.Intent, DashboardContract.Effect>(
    initialState = DashboardContract.State()
) {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DashboardContract.FilterType.ALL)
    private val _selectedTimePeriod = MutableStateFlow(com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH)
    private val _draftTransaction = MutableStateFlow<TransactionEntity?>(null)

    init {
        observeDashboardData()
    }

    override fun handleIntent(intent: DashboardContract.Intent) {
        when (intent) {
            is DashboardContract.Intent.LoadDashboard -> { }
            is DashboardContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
            is DashboardContract.Intent.UpdateTransaction -> updateTransaction(intent.transaction)
            is DashboardContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction)
            is DashboardContract.Intent.AddTransaction -> addTransaction(intent.transaction)
            is DashboardContract.Intent.SearchTransactions -> _searchQuery.value = intent.query
            is DashboardContract.Intent.FilterTransactions -> _activeFilter.value = intent.filter
            is DashboardContract.Intent.SetTimePeriod -> _selectedTimePeriod.value = intent.period
            is DashboardContract.Intent.UpdateDraftTransaction -> _draftTransaction.value = intent.transaction
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(_searchQuery, _activeFilter, _selectedTimePeriod) { query, filter, period ->
                Triple(query, filter, period)
            }.flatMapLatest { (query, filter, period) ->
                val timeRange = com.masum.cipher.core.domain.model.TimeRange.from(period)
                getDashboardDataUseCase(query, filter, timeRange)
            }.combine(_draftTransaction) { state, draft ->
                state.copy(draftTransaction = draft)
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
            emitEffect(DashboardContract.Effect.ShowUndoDelete(transaction))
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
