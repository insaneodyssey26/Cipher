package com.masum.cipher.ui.insights

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.AddTransactionUseCase
import com.masum.cipher.core.domain.usecase.DeleteTransactionUseCase
import com.masum.cipher.core.domain.usecase.GetInsightsUseCase
import com.masum.cipher.core.domain.usecase.SaveCategoryRuleUseCase
import com.masum.cipher.core.domain.usecase.SaveMerchantRuleUseCase
import com.masum.cipher.core.domain.usecase.TransactionUpdateResult
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
    private val categoryRepository: com.masum.cipher.core.data.repository.CategoryRepository,
    private val saveCategoryRuleUseCase: SaveCategoryRuleUseCase,
    private val saveMerchantRuleUseCase: SaveMerchantRuleUseCase,
    private val subscriptionDao: SubscriptionDao,
    private val transactionSplitRepository: com.masum.cipher.core.data.repository.TransactionSplitRepository,
    private val userPreferences: com.masum.cipher.core.data.local.pref.UserPreferences
) : BaseViewModel<InsightsContract.State, InsightsContract.Intent, InsightsContract.Effect>(
    initialState = InsightsContract.State(
        currencyCode = userPreferences.getCachedCurrencyCode(),
        currencySymbol = userPreferences.getCachedCurrencySymbol()
    )
) {

    private val _draftTransaction = MutableStateFlow<TransactionEntity?>(null)
    private val _promptCategoryRuleFor = MutableStateFlow<TransactionEntity?>(null)
    private val _promptMerchantRuleFor = MutableStateFlow<com.masum.cipher.core.domain.model.MerchantRenameRulePrompt?>(null)

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
            is InsightsContract.Intent.SetTimePeriod -> sessionManager.setTimePeriod(intent.period, intent.customStart, intent.customEnd)
            is InsightsContract.Intent.UpdateDraftTransaction -> _draftTransaction.value = intent.transaction
            is InsightsContract.Intent.SaveCategoryRule -> saveCategoryRule(intent.merchantName, intent.category)
            is InsightsContract.Intent.DismissCategoryRulePrompt -> _promptCategoryRuleFor.value = null
            is InsightsContract.Intent.SaveMerchantRule -> saveMerchantRule(intent.rawName, intent.cleanName)
            is InsightsContract.Intent.DismissMerchantRulePrompt -> _promptMerchantRuleFor.value = null
            is InsightsContract.Intent.SaveSubscription -> saveSubscription(intent)
            is InsightsContract.Intent.DeleteSubscription -> deleteSubscription(intent.merchant)
            is InsightsContract.Intent.IgnoreSubscription -> ignoreSubscription(intent.merchant)
            is InsightsContract.Intent.RestoreSubscription -> {
                viewModelScope.launch { subscriptionDao.insert(intent.subscription) }
            }
            is InsightsContract.Intent.SetCategoryBudget -> setCategoryBudget(intent.category, intent.limit)
            is InsightsContract.Intent.SetDynamicBudget -> setDynamicBudget(intent.enabled)
            is InsightsContract.Intent.SaveTransactionSplits -> saveSplits(intent.transactionId, intent.splits)
            is InsightsContract.Intent.CreateCustomCategory -> {
                viewModelScope.launch {
                    categoryRepository.addCustomCategory(intent.name, intent.iconName, intent.colorHex)
                }
            }
            is InsightsContract.Intent.UpdateCustomCategory -> {
                viewModelScope.launch {
                    categoryRepository.updateCustomCategory(intent.id, intent.oldName, intent.newName, intent.iconName, intent.colorHex)
                }
            }
            is InsightsContract.Intent.DeleteCustomCategory -> {
                viewModelScope.launch {
                    categoryRepository.deleteCustomCategory(intent.category)
                }
            }
        }
    }

    private fun saveSplits(transactionId: Long, splits: List<com.masum.cipher.core.domain.model.SplitParticipant>) {
        viewModelScope.launch {
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
    }

    private fun setDynamicBudget(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDynamicBudgetEnabled(enabled)
        }
    }

    private fun setCategoryBudget(category: String, limit: Double) {
        viewModelScope.launch {
            userPreferences.setCategoryBudget(category, limit)
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

    private fun ignoreSubscription(merchant: String) {
        viewModelScope.launch {
            userPreferences.addIgnoredSubscription(merchant)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadInsights() {
        viewModelScope.launch {
            sessionManager.selectedTimeRange.flatMapLatest { timeRange ->
                getInsightsUseCase(timeRange)
            }.combine(_draftTransaction) { state, draft ->
                state.copy(draftTransaction = draft)
            }.combine(_promptCategoryRuleFor) { state, prompt ->
                state.copy(promptCategoryRuleFor = prompt)
            }.combine(_promptMerchantRuleFor) { state, prompt ->
                state.copy(promptMerchantRuleFor = prompt)
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
            when (val result = updateTransactionUseCase(transaction)) {
                is TransactionUpdateResult.MerchantRenamed -> _promptMerchantRuleFor.value = result.prompt
                is TransactionUpdateResult.CategoryChanged -> _promptCategoryRuleFor.value = result.transaction
                TransactionUpdateResult.NoRulePrompt -> {}
            }
        }
    }

    private fun saveMerchantRule(rawName: String, cleanName: String) {
        viewModelScope.launch {
            saveMerchantRuleUseCase(rawName, cleanName)
            _promptMerchantRuleFor.value = null
        }
    }

    private fun saveCategoryRule(merchantName: String, category: String) {
        viewModelScope.launch {
            saveCategoryRuleUseCase(merchantName, category)
            _promptCategoryRuleFor.value = null
        }
    }

    private fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { addTransactionUseCase(transaction) }
    }
}
