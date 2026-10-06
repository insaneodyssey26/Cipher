package com.masum.cipher.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.DebtRepository
import com.masum.cipher.core.domain.model.DebtType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val accountDao: AccountDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(
        DebtsContract.State(
            currencySymbol = userPreferences.getCachedCurrencySymbol(),
            isPro = userPreferences.isCachedPro()
        )
    )
    val state: StateFlow<DebtsContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DebtsContract.Effect>()
    val effect: SharedFlow<DebtsContract.Effect> = _effect.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                debtRepository.getAllDebtItems(),
                accountDao.getAllAccountsFlow(),
                userPreferences.settingsFlow
            ) { debtItems, accounts, settings ->
                val totalLent = debtItems.filter { !it.isSettled && it.type == DebtType.LENT }.sumOf { it.remainingAmount }
                val totalBorrowed = debtItems.filter { !it.isSettled && it.type == DebtType.BORROWED }.sumOf { it.remainingAmount }
                val netBalance = totalLent - totalBorrowed

                _state.value.copy(
                    isLoading = false,
                    debts = debtItems,
                    accounts = accounts,
                    currencySymbol = settings.currencySymbol,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    netBalance = netBalance,
                    isHapticsEnabled = settings.isHapticsEnabled,
                    isPro = settings.isPro
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun handleIntent(intent: DebtsContract.Intent) {
        when (intent) {
            is DebtsContract.Intent.CreateDebt -> createDebt(intent)
            is DebtsContract.Intent.UpdateDebt -> updateDebt(intent)
            is DebtsContract.Intent.RecordRepayment -> recordRepayment(intent)
            is DebtsContract.Intent.SettleDebt -> settleDebt(intent.debtId)
            is DebtsContract.Intent.DeleteDebt -> deleteDebt(intent.debt)
            is DebtsContract.Intent.DeleteRepayment -> deleteRepayment(intent.repayment)
            is DebtsContract.Intent.SetFilterTab -> _state.value = _state.value.copy(filterTab = intent.tab)
            is DebtsContract.Intent.SetTypeFilter -> _state.value = _state.value.copy(typeFilter = intent.type)
            is DebtsContract.Intent.RestoreDebt -> restoreDebt(intent)
            is DebtsContract.Intent.UpdateDraft -> updateDraft(intent)
            is DebtsContract.Intent.ClearDraft -> clearDraft()
            is DebtsContract.Intent.SyncDebtToLedger -> syncDebtToLedger(intent.debtId, intent.accountId)
            is DebtsContract.Intent.UnlogDebtFromLedger -> unlogDebtFromLedger(intent.debtId)
        }
    }

    private fun updateDraft(intent: DebtsContract.Intent.UpdateDraft) {
        _state.value = _state.value.copy(
            draftPersonName = intent.personName,
            draftAmount = intent.amount,
            draftType = intent.type,
            draftDueDate = intent.dueDate,
            draftNote = intent.note,
            draftAccountId = intent.accountId,
            draftSyncLedger = intent.syncLedger
        )
    }

    private fun clearDraft() {
        _state.value = _state.value.copy(
            draftPersonName = "",
            draftAmount = "",
            draftType = DebtType.LENT,
            draftDueDate = null,
            draftNote = "",
            draftAccountId = null,
            draftSyncLedger = false
        )
    }

    private fun syncDebtToLedger(debtId: Long, accountId: Long) {
        viewModelScope.launch {
            debtRepository.syncDebtToLedger(debtId, accountId)
            _effect.emit(DebtsContract.Effect.ShowToast("Logged to Dashboard"))
        }
    }

    private fun unlogDebtFromLedger(debtId: Long) {
        viewModelScope.launch {
            debtRepository.unlogDebtFromLedger(debtId)
            _effect.emit(DebtsContract.Effect.ShowToast("Unlinked from Dashboard"))
        }
    }

    private fun createDebt(intent: DebtsContract.Intent.CreateDebt) {
        viewModelScope.launch {
            debtRepository.createDebt(
                personName = intent.personName,
                amount = intent.amount,
                type = intent.type,
                dueDate = intent.dueDate,
                note = intent.note,
                accountId = intent.accountId,
                syncLedger = intent.syncLedger,
                interestRate = intent.interestRate
            )
            clearDraft()
        }
    }

    private fun updateDebt(intent: DebtsContract.Intent.UpdateDebt) {
        viewModelScope.launch {
            debtRepository.updateDebtDetails(
                debtId = intent.debtId,
                personName = intent.personName,
                amount = intent.amount,
                type = intent.type,
                dueDate = intent.dueDate,
                note = intent.note,
                accountId = intent.accountId,
                syncLedger = intent.syncLedger,
                interestRate = intent.interestRate
            )
            clearDraft()
        }
    }

    private fun recordRepayment(intent: DebtsContract.Intent.RecordRepayment) {
        viewModelScope.launch {
            debtRepository.recordRepayment(
                debtId = intent.debtId,
                amount = intent.amount,
                timestamp = intent.timestamp,
                accountId = intent.accountId,
                note = intent.note,
                syncLedger = intent.syncLedger
            )
        }
    }

    private fun settleDebt(debtId: Long) {
        viewModelScope.launch {
            debtRepository.settleDebt(debtId)
        }
    }

    private fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            val (debtEntity, repayments, transactions) = debtRepository.getDebtWithRepaymentsAndTransactions(debt.id)
            if (debtEntity != null) {
                debtRepository.deleteDebt(debtEntity)
                _effect.emit(DebtsContract.Effect.ShowUndoDelete(debtEntity, repayments, transactions))
            }
        }
    }

    private fun restoreDebt(intent: DebtsContract.Intent.RestoreDebt) {
        viewModelScope.launch {
            debtRepository.restoreDebt(intent.debt, intent.repayments, intent.transactions)
        }
    }

    private fun deleteRepayment(repayment: DebtRepaymentEntity) {
        viewModelScope.launch {
            debtRepository.deleteRepayment(repayment)
        }
    }
}
