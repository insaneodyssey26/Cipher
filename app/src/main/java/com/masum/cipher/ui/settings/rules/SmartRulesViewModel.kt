package com.masum.cipher.ui.settings.rules

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartRulesViewModel @Inject constructor(
    private val categoryRuleDao: CategoryRuleDao,
    private val userPreferences: UserPreferences
) : BaseViewModel<SmartRulesContract.State, SmartRulesContract.Intent, SmartRulesContract.Effect>(
    initialState = SmartRulesContract.State()
) {
    init {
        handleIntent(SmartRulesContract.Intent.LoadRules)
    }

    override fun handleIntent(intent: SmartRulesContract.Intent) {
        when (intent) {
            is SmartRulesContract.Intent.LoadRules -> observeRules()
            is SmartRulesContract.Intent.DeleteRule -> deleteRule(intent.rule)
            is SmartRulesContract.Intent.RestoreRule -> restoreRule(intent.rule)
            is SmartRulesContract.Intent.AddOrUpdateRule -> addOrUpdateRule(intent.merchantName, intent.category)
        }
    }

    private fun observeRules() {
        viewModelScope.launch {
            combine(
                categoryRuleDao.getAllRules(),
                userPreferences.settingsFlow
            ) { rules, settings ->
                SmartRulesContract.State(
                    isLoading = false,
                    rules = rules,
                    isHapticsEnabled = settings.isHapticsEnabled
                )
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    private fun deleteRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            categoryRuleDao.deleteRule(rule)
            emitEffect(SmartRulesContract.Effect.ShowUndoDelete(rule))
        }
    }

    private fun restoreRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            categoryRuleDao.insertRule(rule)
        }
    }

    private fun addOrUpdateRule(merchantName: String, category: String) {
        viewModelScope.launch {
            categoryRuleDao.insertRule(CategoryRuleEntity(merchantName.trim(), category))
            emitEffect(SmartRulesContract.Effect.ShowToast("Rule saved"))
        }
    }
}
