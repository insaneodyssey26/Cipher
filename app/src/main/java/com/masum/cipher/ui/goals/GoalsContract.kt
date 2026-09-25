package com.masum.cipher.ui.goals

import com.masum.cipher.core.data.local.entity.GoalEntity

object GoalsContract {
    data class State(
        val goals: List<GoalEntity> = emptyList(),
        val totalSaved: Double = 0.0,
        val totalTarget: Double = 0.0,
        val currencySymbol: String = "₹",
        val isHapticsEnabled: Boolean = true,
        val isPro: Boolean = false,
        val freeGoalLimit: Int = 2,
        val showProGateSheet: Boolean = false,
        val isLoading: Boolean = false
    )

    sealed interface Effect {
        data class ShowUndoDelete(val goal: GoalEntity) : Effect
    }

    sealed interface Intent {
        data object ShowProGate : Intent
        data object DismissProGate : Intent
        data class CreateGoal(
            val name: String,
            val targetAmount: Double,
            val initialSaved: Double,
            val colorHex: Long,
            val iconName: String
        ) : Intent

        data class UpdateGoal(
            val id: Long,
            val name: String,
            val targetAmount: Double,
            val savedAmount: Double,
            val colorHex: Long,
            val iconName: String
        ) : Intent

        data class AdjustSavedAmount(
            val id: Long,
            val delta: Double
        ) : Intent

        data class DeleteGoal(val goal: GoalEntity) : Intent

        data class RestoreGoal(val goal: GoalEntity) : Intent
    }
}
