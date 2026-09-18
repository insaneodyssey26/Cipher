package com.masum.cipher.ui.accounts

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.domain.usecase.TransferFundsUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transferFundsUseCase: TransferFundsUseCase,
    private val userPreferences: UserPreferences
) : BaseViewModel<AccountsContract.State, AccountsContract.Intent, AccountsContract.Effect>(
    initialState = AccountsContract.State(
        currencySymbol = userPreferences.getCachedCurrencySymbol(),
        isPro = userPreferences.isCachedPro()
    )
) {

    init {
        observeAccounts()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            combine(
                accountRepository.getAllAccountsWithBalancesFlow(),
                userPreferences.settingsFlow
            ) { accounts, settings ->
                val netWorth = accounts.sumOf { it.currentBalance }
                val liquid = accounts.filter { it.type != AccountType.CREDIT_CARD && it.type != AccountType.INVESTMENT }
                    .sumOf { it.currentBalance }
                val debt = accounts.filter { it.type == AccountType.CREDIT_CARD }
                    .sumOf { if (it.currentBalance < 0) kotlin.math.abs(it.currentBalance) else 0.0 }

                currentState.copy(
                    accounts = accounts.toPersistentList(),
                    totalNetWorth = netWorth,
                    totalLiquidBalance = liquid,
                    totalDebt = debt,
                    currencySymbol = settings.currencySymbol,
                    isPro = settings.isPro,
                    isHapticsEnabled = settings.isHapticsEnabled
                )
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    override fun handleIntent(intent: AccountsContract.Intent) {
        when (intent) {
            is AccountsContract.Intent.LoadAccounts -> {}
            is AccountsContract.Intent.OpenCreateAccountSheet -> {
                if (!currentState.isPro && currentState.accounts.size >= currentState.freeAccountLimit) {
                    updateState { copy(showProGateSheet = true) }
                } else {
                    updateState { copy(showCreateEditSheet = true, accountToEdit = null) }
                }
            }
            is AccountsContract.Intent.OpenEditAccountSheet -> {
                updateState { copy(showCreateEditSheet = true, accountToEdit = intent.account) }
            }
            is AccountsContract.Intent.DismissCreateEditSheet -> {
                updateState { copy(showCreateEditSheet = false, accountToEdit = null) }
            }
            is AccountsContract.Intent.OpenTransferSheet -> {
                updateState { copy(showTransferSheet = true) }
            }
            is AccountsContract.Intent.DismissTransferSheet -> {
                updateState { copy(showTransferSheet = false) }
            }
            is AccountsContract.Intent.TransferFunds -> {
                viewModelScope.launch {
                    val settings = userPreferences.settingsFlow
                    transferFundsUseCase(
                        fromAccount = intent.fromAccount,
                        toAccount = intent.toAccount,
                        amount = intent.amount,
                        currency = currentState.currencySymbol,
                        note = intent.note,
                        outflowMerchantText = intent.outflowMerchantText,
                        inflowMerchantText = intent.inflowMerchantText
                    )
                    updateState { copy(showTransferSheet = false) }
                }
            }
            is AccountsContract.Intent.DismissProGate -> {
                updateState { copy(showProGateSheet = false) }
            }
            is AccountsContract.Intent.SaveAccount -> {
                saveAccount(intent)
            }
            is AccountsContract.Intent.SetDefaultAccount -> {
                viewModelScope.launch {
                    accountRepository.setDefaultAccount(intent.accountId)
                    emitEffect(AccountsContract.Effect.ShowToast("Set as primary account"))
                }
            }
            is AccountsContract.Intent.RequestDeleteAccount -> {
                if (currentState.accounts.size <= 1) {
                    emitEffect(AccountsContract.Effect.ShowToast("Cannot delete the only account"))
                } else {
                    updateState { copy(showDeleteConfirmDialog = true, accountToDelete = intent.account) }
                }
            }
            is AccountsContract.Intent.ConfirmDeleteAccount -> {
                val acc = currentState.accountToDelete
                if (acc != null) {
                    viewModelScope.launch {
                        accountRepository.deleteAccount(acc)
                        updateState { copy(showDeleteConfirmDialog = false, accountToDelete = null) }
                        emitEffect(AccountsContract.Effect.ShowToast("${acc.name} deleted"))
                    }
                }
            }
            is AccountsContract.Intent.DismissDeleteDialog -> {
                updateState { copy(showDeleteConfirmDialog = false, accountToDelete = null) }
            }
        }
    }

    private fun saveAccount(intent: AccountsContract.Intent.SaveAccount) {
        viewModelScope.launch {
            val toEdit = if (intent.accountId != null) {
                currentState.accounts.find { it.id == intent.accountId }?.entity 
                    ?: accountRepository.getAccountById(intent.accountId)
                    ?: currentState.accountToEdit
            } else {
                currentState.accountToEdit
            }

            if (toEdit == null) {
                val newEntity = AccountEntity(
                    name = intent.name,
                    type = intent.type,
                    initialBalance = intent.initialBalance,
                    colorHex = intent.colorHex,
                    iconName = intent.iconName,
                    isDefault = intent.isDefault || currentState.accounts.isEmpty(),
                    accountNumberLast4 = intent.last4
                )
                accountRepository.createAccount(newEntity)
                emitEffect(AccountsContract.Effect.ShowToast("${intent.name} created"))
            } else {
                val updated = toEdit.copy(
                    name = intent.name,
                    type = intent.type,
                    initialBalance = intent.initialBalance,
                    colorHex = intent.colorHex,
                    iconName = intent.iconName,
                    isDefault = intent.isDefault,
                    accountNumberLast4 = intent.last4
                )
                accountRepository.updateAccount(updated)
                emitEffect(AccountsContract.Effect.ShowToast("${intent.name} updated"))
            }
            updateState { copy(showCreateEditSheet = false, accountToEdit = null) }
        }
    }
}
