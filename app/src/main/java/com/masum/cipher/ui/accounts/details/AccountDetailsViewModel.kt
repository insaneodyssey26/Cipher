package com.masum.cipher.ui.accounts.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.CategoryRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.data.repository.TransactionSplitRepository
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val splitRepository: TransactionSplitRepository,
    private val userPreferences: UserPreferences
) : BaseViewModel<AccountDetailsContract.State, AccountDetailsContract.Intent, AccountDetailsContract.Effect>(
    initialState = AccountDetailsContract.State(
        currencySymbol = userPreferences.getCachedCurrencySymbol()
    )
) {
    private var currentAccountId: Long = savedStateHandle.get<Long>("accountId") ?: 0L

    init {
        if (currentAccountId != 0L) {
            viewModelScope.launch(Dispatchers.IO) {
                val initialAccount = accountRepository.getAccountById(currentAccountId)
                if (initialAccount != null) {
                    updateState {
                        copy(
                            account = initialAccount,
                            currentBalance = initialAccount.initialBalance,
                            isLoading = false
                        )
                    }
                }
            }
            observeAccountDetails(currentAccountId)
        }
    }

    private fun observeAccountDetails(accountId: Long) {
        currentAccountId = accountId
        viewModelScope.launch {
            combine(
                accountRepository.getAllAccountsWithBalancesFlow(),
                transactionRepository.getAllTransactions(),
                categoryRepository.getAllCustomCategoriesFlow(),
                splitRepository.getAllSplitsFlow(),
                userPreferences.settingsFlow
            ) { accountItems, allTxs, categories, splits, settings ->
                val targetItem = accountItems.find { it.id == accountId } ?: accountItems.firstOrNull()
                val targetEntity = targetItem?.entity ?: accountRepository.getAccountById(accountId)

                val isTargetDefault = targetEntity?.isDefault ?: false
                val accTxs = if (targetEntity != null) {
                    allTxs.filter { tx ->
                        tx.accountId == targetEntity.id || (tx.accountId == null && isTargetDefault)
                    }
                } else {
                    emptyList()
                }

                val inflow = accTxs.filter { it.isIncome }.sumOf { it.amount }
                val outflow = accTxs.filter { !it.isIncome }.sumOf { it.amount }
                val net = inflow - outflow
                val balance = (targetEntity?.initialBalance ?: 0.0) + net

                val filtered = filterTransactions(accTxs, currentState.searchQuery, currentState.selectedFilter)
                val grouped = groupTransactionsByDay(filtered, Locale.getDefault())

                currentState.copy(
                    account = targetEntity,
                    accountItem = targetItem,
                    allTransactions = accTxs.toPersistentList(),
                    filteredTransactions = filtered.toPersistentList(),
                    groupedDays = grouped.toPersistentList(),
                    customCategories = categories.toPersistentList(),
                    splits = splits.toPersistentList(),
                    totalInflow = inflow,
                    totalOutflow = outflow,
                    netFlow = net,
                    currentBalance = balance,
                    currencySymbol = settings.currencySymbol,
                    isHapticsEnabled = settings.isHapticsEnabled,
                    isPrivacyMode = settings.isPrivacyModeEnabled,
                    isLoading = false
                )
            }
            .flowOn(Dispatchers.Default)
            .collect { newState ->
                updateState { newState }
            }
        }
    }

    override fun handleIntent(intent: AccountDetailsContract.Intent) {
        when (intent) {
            is AccountDetailsContract.Intent.LoadAccount -> {
                observeAccountDetails(intent.accountId)
            }
            is AccountDetailsContract.Intent.UpdateSearchQuery -> {
                val filtered = filterTransactions(currentState.allTransactions, intent.query, currentState.selectedFilter)
                val grouped = groupTransactionsByDay(filtered, Locale.getDefault())
                updateState {
                    copy(
                        searchQuery = intent.query,
                        filteredTransactions = filtered.toPersistentList(),
                        groupedDays = grouped.toPersistentList()
                    )
                }
            }
            is AccountDetailsContract.Intent.SelectFilter -> {
                val filtered = filterTransactions(currentState.allTransactions, currentState.searchQuery, intent.filter)
                val grouped = groupTransactionsByDay(filtered, Locale.getDefault())
                updateState {
                    copy(
                        selectedFilter = intent.filter,
                        filteredTransactions = filtered.toPersistentList(),
                        groupedDays = grouped.toPersistentList()
                    )
                }
            }
            is AccountDetailsContract.Intent.OpenTransactionDetails -> {
                updateState { copy(transactionToEdit = intent.transaction) }
            }
            is AccountDetailsContract.Intent.DismissTransactionDetails -> {
                updateState { copy(transactionToEdit = null) }
            }
            is AccountDetailsContract.Intent.SaveTransaction -> {
                viewModelScope.launch {
                    val assignedTx = if (intent.transaction.accountId == null && currentState.account != null) {
                        intent.transaction.copy(accountId = currentState.account?.id)
                    } else {
                        intent.transaction
                    }
                    transactionRepository.updateTransaction(assignedTx)
                    if (intent.splits != null) {
                        val splitEntities = intent.splits.map { participant ->
                            TransactionSplitEntity(
                                transactionId = assignedTx.id,
                                name = participant.name,
                                amount = participant.amount,
                                isPaid = participant.isPaid,
                                isCurrentUser = participant.isCurrentUser
                            )
                        }
                        splitRepository.saveSplits(assignedTx.id, splitEntities)
                    }
                    updateState { copy(transactionToEdit = null) }
                    emitEffect(AccountDetailsContract.Effect.ShowToast("Transaction updated"))
                }
            }
            is AccountDetailsContract.Intent.RequestDeleteTransaction -> {
                updateState { copy(transactionToDelete = intent.transaction, showDeleteDialog = true, transactionToEdit = null) }
            }
            is AccountDetailsContract.Intent.ConfirmDeleteTransaction -> {
                val target = currentState.transactionToDelete
                if (target != null) {
                    viewModelScope.launch {
                        transactionRepository.deleteTransaction(target)
                        updateState { copy(transactionToDelete = null, showDeleteDialog = false) }
                        emitEffect(AccountDetailsContract.Effect.ShowToast("Transaction deleted"))
                    }
                }
            }
            is AccountDetailsContract.Intent.DismissDeleteDialog -> {
                updateState { copy(transactionToDelete = null, showDeleteDialog = false) }
            }
        }
    }

    private fun filterTransactions(
        list: List<TransactionEntity>,
        query: String,
        filter: AccountTransactionFilter
    ): List<TransactionEntity> {
        val trimmedQuery = query.trim().lowercase()
        return list.filter { tx ->
            val matchesType = when (filter) {
                AccountTransactionFilter.ALL -> true
                AccountTransactionFilter.EXPENSE -> !tx.isIncome
                AccountTransactionFilter.INCOME -> tx.isIncome
            }
            val matchesQuery = trimmedQuery.isEmpty() ||
                tx.merchant.lowercase().contains(trimmedQuery) ||
                tx.category.lowercase().contains(trimmedQuery) ||
                (tx.note?.lowercase()?.contains(trimmedQuery) == true)
            matchesType && matchesQuery
        }
    }

    private fun groupTransactionsByDay(
        transactions: List<TransactionEntity>,
        locale: Locale
    ): List<GroupedDayTransactions> {
        if (transactions.isEmpty()) return emptyList()
        val sameYearFormat = SimpleDateFormat("d MMMM", locale)
        val diffYearFormat = SimpleDateFormat("d MMMM yyyy", locale)
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentDayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val txCal = Calendar.getInstance()
        val txDate = Date()

        val grouped = transactions.groupBy { tx ->
            txCal.timeInMillis = tx.timestamp
            val txYear = txCal.get(Calendar.YEAR)
            val txDayOfYear = txCal.get(Calendar.DAY_OF_YEAR)
            when {
                txYear == currentYear && txDayOfYear == currentDayOfYear -> "Today"
                txYear == currentYear && txDayOfYear == currentDayOfYear - 1 -> "Yesterday"
                txYear == currentYear -> {
                    txDate.time = tx.timestamp
                    sameYearFormat.format(txDate)
                }
                else -> {
                    txDate.time = tx.timestamp
                    diffYearFormat.format(txDate)
                }
            }
        }

        return grouped.map { (dateTitle, txList) ->
            val dayTotal = txList.sumOf { if (it.isIncome) it.amount else -it.amount }
            GroupedDayTransactions(
                title = dateTitle,
                netTotal = dayTotal,
                transactions = txList.toPersistentList()
            )
        }
    }
}
