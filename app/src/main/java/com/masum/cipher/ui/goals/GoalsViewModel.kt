package com.masum.cipher.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.GoalRepository
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
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
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
                userPreferences.settingsFlow
            ) { goals, settings ->
                val totalSaved = goals.sumOf { it.savedAmount }
                val totalTarget = goals.sumOf { it.targetAmount }
                GoalsContract.State(
                    goals = goals,
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
                is GoalsContract.Intent.CreateGoal -> {
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
                    goalRepository.adjustSavedAmount(intent.id, intent.delta)
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
