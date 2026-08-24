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
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: com.masum.cipher.core.data.local.dao.SubscriptionDao
) : BaseViewModel<DashboardContract.State, DashboardContract.Intent, DashboardContract.Effect>(
    initialState = DashboardContract.State()
) {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DashboardContract.FilterType.ALL)
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
            is DashboardContract.Intent.RestoreTransaction -> restoreTransaction(intent.transaction)
            is DashboardContract.Intent.AddTransaction -> addTransaction(intent.transaction)
            is DashboardContract.Intent.SearchTransactions -> _searchQuery.value = intent.query
            is DashboardContract.Intent.FilterTransactions -> _activeFilter.value = intent.filter
            is DashboardContract.Intent.SetTimePeriod -> sessionManager.setTimePeriod(intent.period)
            is DashboardContract.Intent.UpdateDraftTransaction -> _draftTransaction.value = intent.transaction
            is DashboardContract.Intent.SaveCategoryRule -> saveCategoryRule(intent.merchantName, intent.category)
            is DashboardContract.Intent.DismissCategoryRulePrompt -> _promptCategoryRuleFor.value = null
            is DashboardContract.Intent.ApproveSubscription -> approveSubscription(intent.subscription)
            is DashboardContract.Intent.SkipSubscription -> skipSubscription(intent.subscription)
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
            combine(_searchQuery, _activeFilter, sessionManager.selectedTimePeriod) { query, filter, period ->
                Triple(query, filter, period)
            }.flatMapLatest { (query, filter, period) ->
                val timeRange = com.masum.cipher.core.domain.model.TimeRange.from(period)
                getDashboardDataUseCase(query, filter, timeRange)
            }.combine(_draftTransaction) { state, draft ->
                state.copy(draftTransaction = draft)
            }.combine(_promptCategoryRuleFor) { state, prompt ->
                state.copy(promptCategoryRuleFor = prompt)
            }.combine(subscriptionDao.getAllSubscriptions()) { state, subscriptions ->
                val currentTime = System.currentTimeMillis()
                val pending = subscriptions.filter { it.nextExpectedDate <= currentTime }
                state.copy(pendingSubscriptions = pending)
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
                    merchantName = merchantName,
                    customCategory = category
                )
            )
            _promptCategoryRuleFor.value = null
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
