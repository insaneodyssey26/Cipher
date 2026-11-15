package com.masum.cipher.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.GoalRepository
import com.masum.cipher.core.data.repository.TransactionRepository
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
import kotlin.math.abs

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(GoalsContract.State())
    val state: StateFlow<GoalsContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GoalsContract.Effect>()
    val effect: SharedFlow<GoalsContract.Effect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                goalRepository.allGoals,
                accountRepository.getAllAccountsFlow(),
                userPreferences.settingsFlow
            ) { goals, accounts, settings ->
                val totalSaved = goals.sumOf { it.savedAmount }
                val totalTarget = goals.sumOf { it.targetAmount }
                _state.value.copy(
                    goals = goals,
                    accounts = accounts,
                    totalSaved = totalSaved,
                    totalTarget = totalTarget,
                    currencySymbol = settings.currencySymbol,
                    isHapticsEnabled = settings.isHapticsEnabled,
                    isPro = settings.isPro,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun handleIntent(intent: GoalsContract.Intent) {
        viewModelScope.launch {
            when (intent) {
                is GoalsContract.Intent.ShowProGate -> {
                    _state.value = _state.value.copy(showProGateSheet = true)
                }
                is GoalsContract.Intent.DismissProGate -> {
                    _state.value = _state.value.copy(showProGateSheet = false)
                }
                is GoalsContract.Intent.CreateGoal -> {
                    if (!_state.value.isPro && _state.value.goals.size >= _state.value.freeGoalLimit) {
                        _state.value = _state.value.copy(showProGateSheet = true)
                        return@launch
                    }
                    goalRepository.createGoal(
                        name = intent.name,
                        targetAmount = intent.targetAmount,
                        savedAmount = intent.initialSaved,
                        colorHex = intent.colorHex,
                        iconName = intent.iconName
                    )
                }
                is GoalsContract.Intent.UpdateGoal -> {
                    goalRepository.updateGoal(
                        id = intent.id,
                        name = intent.name,
                        targetAmount = intent.targetAmount,
                        savedAmount = intent.savedAmount,
                        colorHex = intent.colorHex,
                        iconName = intent.iconName
                    )
                }
                is GoalsContract.Intent.AdjustSavedAmount -> {
                    val existingGoal = goalRepository.getGoalById(intent.id)
                    goalRepository.adjustSavedAmount(intent.id, intent.delta)
                    if (intent.accountId != null && intent.accountId > 0L && existingGoal != null) {
                        val amount = abs(intent.delta)
                        if (amount > 0.0) {
                            val isDeposit = intent.delta > 0
                            val currencyCode = userPreferences.getCachedCurrencyCode()
                            val tx = TransactionEntity(
                                amount = amount,
                                merchant = if (isDeposit) "Goal: ${existingGoal.name}" else "Goal Withdrawal: ${existingGoal.name}",
                                currency = currencyCode,
                                category = com.masum.cipher.core.domain.model.TransactionCategory.INVESTMENT.name,
                                timestamp = System.currentTimeMillis(),
                                rawSms = null,
                                isIncome = !isDeposit,
                                note = if (isDeposit) "Deposit into ${existingGoal.name}" else "Withdrawal from ${existingGoal.name}",
                                accountId = intent.accountId
                            )
                            transactionRepository.insertTransaction(tx)
                        }
                    }
                }
                is GoalsContract.Intent.DeleteGoal -> {
                    goalRepository.deleteGoal(intent.goal)
                    _effect.emit(GoalsContract.Effect.ShowUndoDelete(intent.goal))
                }
                is GoalsContract.Intent.RestoreGoal -> {
                    goalRepository.restoreGoal(intent.goal)
                }
            }
        }
    }
}
