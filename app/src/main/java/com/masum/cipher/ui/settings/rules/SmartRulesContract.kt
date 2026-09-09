package com.masum.cipher.ui.settings.rules

import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

class SmartRulesContract {
    sealed class Intent : UiIntent {
        object LoadRules : Intent()
        data class SelectTab(val tabIndex: Int) : Intent()
        data class SetSearchQuery(val query: String) : Intent()
        data class DeleteCategoryRule(val rule: CategoryRuleEntity) : Intent()
        data class RestoreCategoryRule(val rule: CategoryRuleEntity) : Intent()
        data class AddOrUpdateCategoryRule(val merchantName: String, val category: String) : Intent()
        data class DeleteMerchantRule(val alias: MerchantAliasEntity) : Intent()
        data class RestoreMerchantRule(val alias: MerchantAliasEntity) : Intent()
        data class AddOrUpdateMerchantRule(val rawName: String, val cleanName: String) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val categoryRules: List<CategoryRuleEntity> = emptyList(),
        val merchantRules: List<MerchantAliasEntity> = emptyList(),
        val searchQuery: String = "",
        val selectedTab: Int = 0,
        val isHapticsEnabled: Boolean = true
    ) : UiState {
        val filteredCategoryRules: List<CategoryRuleEntity>
            get() {
                val q = searchQuery.trim().lowercase()
                if (q.isEmpty()) return categoryRules
                return categoryRules.filter {
                    it.merchantName.lowercase().contains(q) ||
                    it.customCategory.lowercase().contains(q)
                }
            }

        val filteredMerchantRules: List<MerchantAliasEntity>
            get() {
                val q = searchQuery.trim().lowercase()
                if (q.isEmpty()) return merchantRules
                return merchantRules.filter {
                    it.rawName.lowercase().contains(q) ||
                    it.cleanName.lowercase().contains(q)
                }
            }
    }

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowUndoDeleteCategoryRule(val rule: CategoryRuleEntity) : Effect()
        data class ShowUndoDeleteMerchantRule(val alias: MerchantAliasEntity) : Effect()
    }
}
