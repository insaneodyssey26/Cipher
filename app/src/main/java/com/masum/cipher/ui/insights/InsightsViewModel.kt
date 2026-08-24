package com.masum.cipher.ui.insights

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetInsightsUseCase
import com.masum.cipher.core.domain.usecase.UpdateTransactionUseCase
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getInsightsUseCase: GetInsightsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val sessionManager: com.masum.cipher.core.domain.SessionManager,
    private val transactionRepository: TransactionRepository,
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: SubscriptionDao
) : BaseViewModel<InsightsContract.State, InsightsContract.Intent, InsightsContract.Effect>(
    initialState = InsightsContract.State()
) {

    private val _draftTransaction = MutableStateFlow<TransactionEntity?>(null)
    private val _promptCategoryRuleFor = MutableStateFlow<TransactionEntity?>(null)

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
            is InsightsContract.Intent.SetTimePeriod -> sessionManager.setTimePeriod(intent.period)
            is InsightsContract.Intent.UpdateDraftTransaction -> _draftTransaction.value = intent.transaction
            is InsightsContract.Intent.SaveCategoryRule -> saveCategoryRule(intent.merchantName, intent.category)
            is InsightsContract.Intent.DismissCategoryRulePrompt -> _promptCategoryRuleFor.value = null
            is InsightsContract.Intent.SaveSubscription -> saveSubscription(intent)
            is InsightsContract.Intent.DeleteSubscription -> deleteSubscription(intent.merchant)
            is InsightsContract.Intent.RestoreSubscription -> {
                viewModelScope.launch { subscriptionDao.insert(intent.subscription) }
            }
        }
    }

    private fun saveSubscription(intent: InsightsContract.Intent.SaveSubscription) {
        viewModelScope.launch {
            val existing = subscriptionDao.getAllSubscriptions().firstOrNull()?.find { it.merchant.equals(intent.merchant, ignoreCase = true) }
            val entity = SubscriptionEntity(
                id = existing?.id ?: 0,
                merchant = intent.merchant,
                amount = intent.amount,
                category = intent.category,
                frequencyDays = intent.frequencyDays,
                nextExpectedDate = intent.nextExpectedDate
            )
            subscriptionDao.insert(entity)
        }
    }

    private fun deleteSubscription(merchant: String) {
        viewModelScope.launch {
            val existing = subscriptionDao.getAllSubscriptions().firstOrNull()?.find { it.merchant.equals(merchant, ignoreCase = true) }
            if (existing != null) {
                subscriptionDao.delete(existing)
                emitEffect(InsightsContract.Effect.ShowUndoSubscriptionDelete(existing))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadInsights() {
        viewModelScope.launch {
            sessionManager.selectedTimePeriod.flatMapLatest { period ->
                val timeRange = com.masum.cipher.core.domain.model.TimeRange.from(period)
                getInsightsUseCase(timeRange)
            }.combine(_draftTransaction) { state, draft ->
                state.copy(draftTransaction = draft)
            }.combine(_promptCategoryRuleFor) { state, prompt ->
                state.copy(promptCategoryRuleFor = prompt)
            }.collect { newState ->
                updateState { 
                    newState.copy(
                        selectedDayTimestamp = this.selectedDayTimestamp,
                        selectedTimePeriod = sessionManager.selectedTimePeriod.value
                    )
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
        viewModelScope.launch { addTransactionUseCase(transaction) }
    }
}
