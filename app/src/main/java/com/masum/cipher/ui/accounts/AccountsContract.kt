package com.masum.cipher.ui.accounts

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.domain.model.AccountItem
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

object AccountsContract {

    data class State(
        val accounts: ImmutableList<AccountItem> = persistentListOf(),
        val totalNetWorth: Double = 0.0,
        val totalLiquidBalance: Double = 0.0,
        val totalDebt: Double = 0.0,
        val currencySymbol: String = "₹",
        val isPro: Boolean = false,
        val freeAccountLimit: Int = 2,
        val accountToEdit: AccountEntity? = null,
        val showCreateEditSheet: Boolean = false,
        val showProGateSheet: Boolean = false,
        val accountToDelete: AccountEntity? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val isHapticsEnabled: Boolean = true
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadAccounts : Intent
        data object OpenCreateAccountSheet : Intent
        data class OpenEditAccountSheet(val account: AccountEntity) : Intent
        data object DismissCreateEditSheet : Intent
        data class SaveAccount(
            val accountId: Long? = null,
            val name: String,
            val type: String,
            val initialBalance: Double,
            val colorHex: Long,
            val iconName: String,
            val isDefault: Boolean,
            val last4: String?
        ) : Intent
        data class SetDefaultAccount(val accountId: Long) : Intent
        data class RequestDeleteAccount(val account: AccountEntity) : Intent
        data object ConfirmDeleteAccount : Intent
        data object DismissDeleteDialog : Intent
        data object DismissProGate : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
        data object NavigateToPro : Effect
    }
}
