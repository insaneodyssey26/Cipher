package com.masum.cipher.ui.settings.rules

import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

class SmartRulesContract {
    sealed class Intent : UiIntent {
        object LoadRules : Intent()
        data class DeleteRule(val rule: CategoryRuleEntity) : Intent()
        data class RestoreRule(val rule: CategoryRuleEntity) : Intent()
        data class AddOrUpdateRule(val merchantName: String, val category: String) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val rules: List<CategoryRuleEntity> = emptyList(),
        val isHapticsEnabled: Boolean = true
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowUndoDelete(val rule: CategoryRuleEntity) : Effect()
    }
}
