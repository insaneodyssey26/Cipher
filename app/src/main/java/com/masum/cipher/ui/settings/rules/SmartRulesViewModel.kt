package com.masum.cipher.ui.settings.rules

import androidx.lifecycle.viewModelScope
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartRulesViewModel @Inject constructor(
    private val categoryRuleDao: CategoryRuleDao,
    private val merchantAliasDao: MerchantAliasDao,
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
            is SmartRulesContract.Intent.SelectTab -> updateState { copy(selectedTab = intent.tabIndex) }
            is SmartRulesContract.Intent.DeleteCategoryRule -> deleteCategoryRule(intent.rule)
            is SmartRulesContract.Intent.RestoreCategoryRule -> restoreCategoryRule(intent.rule)
            is SmartRulesContract.Intent.AddOrUpdateCategoryRule -> addOrUpdateCategoryRule(intent.merchantName, intent.category)
            is SmartRulesContract.Intent.DeleteMerchantRule -> deleteMerchantRule(intent.alias)
            is SmartRulesContract.Intent.RestoreMerchantRule -> restoreMerchantRule(intent.alias)
            is SmartRulesContract.Intent.AddOrUpdateMerchantRule -> addOrUpdateMerchantRule(intent.rawName, intent.cleanName)
        }
    }

    private fun observeRules() {
        viewModelScope.launch {
            combine(
                categoryRuleDao.getAllRules(),
                merchantAliasDao.getAllAliases(),
                userPreferences.settingsFlow
            ) { categoryRules, aliases, settings ->
                currentState.copy(
                    isLoading = false,
                    categoryRules = categoryRules,
                    merchantRules = aliases,
                    isHapticsEnabled = settings.isHapticsEnabled
                )
            }.collect { newState ->
                updateState { newState }
            }
        }
    }

    private fun deleteCategoryRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            categoryRuleDao.deleteRule(rule)
            emitEffect(SmartRulesContract.Effect.ShowUndoDeleteCategoryRule(rule))
        }
    }

    private fun restoreCategoryRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            categoryRuleDao.insertRule(rule)
        }
    }

    private fun addOrUpdateCategoryRule(merchantName: String, category: String) {
        viewModelScope.launch {
            categoryRuleDao.insertRule(CategoryRuleEntity(merchantName.trim(), category))
            emitEffect(SmartRulesContract.Effect.ShowToast("Rule saved"))
        }
    }

    private fun deleteMerchantRule(alias: MerchantAliasEntity) {
        viewModelScope.launch {
            merchantAliasDao.deleteAlias(alias.rawName)
            emitEffect(SmartRulesContract.Effect.ShowUndoDeleteMerchantRule(alias))
        }
    }

    private fun restoreMerchantRule(alias: MerchantAliasEntity) {
        viewModelScope.launch {
            merchantAliasDao.insertAlias(alias)
        }
    }

    private fun addOrUpdateMerchantRule(rawName: String, cleanName: String) {
        viewModelScope.launch {
            merchantAliasDao.insertAlias(
                MerchantAliasEntity(
                    rawName = rawName.uppercase().trim(),
                    cleanName = cleanName.trim(),
                    isUserDefined = true
                )
            )
            emitEffect(SmartRulesContract.Effect.ShowToast("Rule saved"))
        }
    }
}
