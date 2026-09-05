package com.masum.cipher.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetDashboardDataUseCase
import com.masum.cipher.core.domain.usecase.UpdateTransactionUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val sessionManager: com.masum.cipher.core.domain.SessionManager,
    private val transactionRepository: TransactionRepository,
    private val transactionSplitRepository: com.masum.cipher.core.data.repository.TransactionSplitRepository,
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: com.masum.cipher.core.data.local.dao.SubscriptionDao,
    private val updateSettingsUseCase: com.masum.cipher.core.domain.usecase.UpdateSettingsUseCase,
    userPreferences: com.masum.cipher.core.data.local.pref.UserPreferences
) : BaseViewModel<DashboardContract.State, DashboardContract.Intent, DashboardContract.Effect>(
    initialState = DashboardContract.State(
        currencyCode = userPreferences.getCachedCurrencyCode(),
        currencySymbol = userPreferences.getCachedCurrencySymbol()
    )
) {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DashboardFilter())
    private val _draftTransaction = MutableStateFlow<TransactionEntity?>(null)
    private val _promptCategoryRuleFor = MutableStateFlow<TransactionEntity?>(null)

    init {
        observeDashboardData()
    }

    override fun handleIntent(intent: DashboardContract.Intent) {
        when (intent) {
            is DashboardContract.Intent.LoadDashboard -> { }
            is DashboardContract.Intent.DeleteTransaction -> deleteTransaction(intent.transaction)
            is DashboardContract.Intent.UpdateTransaction -> updateTransaction(intent.transaction)
            is DashboardContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction, intent.splits)
            is DashboardContract.Intent.AddTransaction -> addTransaction(intent.transaction, intent.splits)
            is DashboardContract.Intent.SearchTransactions -> _searchQuery.value = intent.query
            is DashboardContract.Intent.FilterTransactions -> _activeFilter.value = _activeFilter.value.copy(type = intent.filter)
            is DashboardContract.Intent.SetDashboardFilter -> _activeFilter.value = intent.filter
            is DashboardContract.Intent.ResetDashboardFilter -> _activeFilter.value = DashboardFilter()
            is DashboardContract.Intent.SetTimePeriod -> sessionManager.setTimePeriod(intent.period, intent.customStart, intent.customEnd)
            is DashboardContract.Intent.UpdateDraftTransaction -> _draftTransaction.value = intent.transaction
            is DashboardContract.Intent.SaveCategoryRule -> saveCategoryRule(intent.merchantName, intent.category)
            is DashboardContract.Intent.DismissCategoryRulePrompt -> _promptCategoryRuleFor.value = null
            is DashboardContract.Intent.ApproveSubscription -> approveSubscription(intent.subscription)
            is DashboardContract.Intent.SkipSubscription -> skipSubscription(intent.subscription)
            is DashboardContract.Intent.UpdateMonthlyBudget -> updateMonthlyBudget(intent.budget, intent.isDynamic)
            is DashboardContract.Intent.SaveTransactionSplits -> saveSplits(intent.transactionId, intent.splits)
            is DashboardContract.Intent.UpdateSplitPaidStatus -> updateSplitPaidStatus(intent.splitId, intent.isPaid)
        }
    }

    private suspend fun persistSplits(transactionId: Long, splits: List<com.masum.cipher.core.domain.model.SplitParticipant>) {
        val entities = splits.map {
            com.masum.cipher.core.data.local.entity.TransactionSplitEntity(
                transactionId = transactionId,
                name = it.name,
                amount = it.amount,
                isPaid = it.isPaid,
                isCurrentUser = it.isCurrentUser
            )
        }
        transactionSplitRepository.saveSplits(transactionId, entities)
    }

    private fun saveSplits(transactionId: Long, splits: List<com.masum.cipher.core.domain.model.SplitParticipant>) {
        viewModelScope.launch {
            persistSplits(transactionId, splits)
        }
    }

    private fun updateSplitPaidStatus(splitId: Long, isPaid: Boolean) {
        viewModelScope.launch {
            transactionSplitRepository.updateSplitPaidStatus(splitId, isPaid)
        }
    }

    private fun updateMonthlyBudget(budget: Double, isDynamic: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.monthlyBudget(budget)
            updateSettingsUseCase.dynamicBudget(isDynamic)
        }
    }

    private fun approveSubscription(subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) {
        viewModelScope.launch {
            val newTransaction = TransactionEntity(
                merchant = subscription.merchant,
                amount = subscription.amount,
                currency = "INR",
                rawSms = null,
                category = subscription.category,
                timestamp = System.currentTimeMillis(),
                isIncome = false,
                note = "Approved subscription"
            )
            transactionRepository.insertTransaction(newTransaction)
            val intervalMs = java.util.concurrent.TimeUnit.DAYS.toMillis(subscription.frequencyDays.toLong())
            subscriptionDao.update(subscription.copy(nextExpectedDate = subscription.nextExpectedDate + intervalMs))
        }
    }

    private fun skipSubscription(subscription: com.masum.cipher.core.data.local.entity.SubscriptionEntity) {
        viewModelScope.launch {
            val intervalMs = java.util.concurrent.TimeUnit.DAYS.toMillis(subscription.frequencyDays.toLong())
            subscriptionDao.update(subscription.copy(nextExpectedDate = subscription.nextExpectedDate + intervalMs))
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(_searchQuery, _activeFilter, sessionManager.selectedTimeRange) { query, filter, timeRange ->
                Triple(query, filter, timeRange)
            }.flatMapLatest { (query, filter, timeRange) ->
                getDashboardDataUseCase(query, filter, timeRange)
            }.combine(_draftTransaction) { state, draft ->
                state.copy(draftTransaction = draft)
            }.combine(_promptCategoryRuleFor) { state, prompt ->
                state.copy(promptCategoryRuleFor = prompt)
            }.combine(subscriptionDao.getAllSubscriptions()) { state, subscriptions ->
                val currentTime = System.currentTimeMillis()
                val pending = subscriptions.filter { it.nextExpectedDate <= currentTime }
                state.copy(pendingSubscriptions = pending)
            }.combine(transactionSplitRepository.getAllSplitsFlow()) { state, allSplits ->
                state.copy(splitsByTransactionId = allSplits.groupBy { it.transactionId })
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val splits = transactionSplitRepository.getSplitsForTransactionSync(transaction.id)
            deleteTransactionUseCase(transaction)
            emitEffect(DashboardContract.Effect.ShowUndoDelete(transaction, splits))
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val existing = transactionRepository.getTransactionById(transaction.id)
            val categoryChanged = existing != null && existing.category != transaction.category && existing.merchant == transaction.merchant
            
            updateTransactionUseCase(transaction)
            
            if (categoryChanged) {
                _promptCategoryRuleFor.value = transaction
            }
        }
    }

    private fun saveCategoryRule(merchantName: String, category: String) {
        viewModelScope.launch {
            categoryRuleDao.insertRule(
                com.masum.cipher.core.data.local.entity.CategoryRuleEntity(
                    merchantName = merchantName.trim(),
                    customCategory = category
                )
            )
            _promptCategoryRuleFor.value = null
        }
    }

    private fun restoreTransaction(
        transaction: TransactionEntity,
        splits: List<com.masum.cipher.core.data.local.entity.TransactionSplitEntity> = emptyList()
    ) {
        viewModelScope.launch {
            val savedTx = addTransactionUseCase(transaction)
            if (savedTx != null && splits.isNotEmpty()) {
                val entities = splits.map { it.copy(id = 0, transactionId = savedTx.id) }
                transactionSplitRepository.saveSplits(savedTx.id, entities)
            }
        }
    }

    private fun addTransaction(transaction: TransactionEntity, splits: List<com.masum.cipher.core.domain.model.SplitParticipant> = emptyList()) {
        viewModelScope.launch {
            val savedTx = addTransactionUseCase(transaction)
            if (savedTx != null && splits.isNotEmpty()) {
                persistSplits(savedTx.id, splits)
            }
        }
    }
}
