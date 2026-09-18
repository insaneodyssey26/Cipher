package com.masum.cipher.ui.accounts.details

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import com.masum.cipher.core.domain.model.AccountItem
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

import androidx.annotation.StringRes
import com.masum.cipher.R

enum class AccountTransactionFilter(@StringRes val labelRes: Int) {
    ALL(R.string.account_details_filter_all),
    EXPENSE(R.string.account_details_filter_outflow),
    INCOME(R.string.account_details_filter_inflow),
    TRANSFER(R.string.account_details_filter_transfer)
}

data class GroupedDayTransactions(
    val title: String,
    val netTotal: Double,
    val transactions: ImmutableList<TransactionEntity>
)

object AccountDetailsContract {

    data class State(
        val account: AccountEntity? = null,
        val accountItem: AccountItem? = null,
        val allAccounts: ImmutableList<AccountItem> = persistentListOf(),
        val allTransactions: ImmutableList<TransactionEntity> = persistentListOf(),
        val filteredTransactions: ImmutableList<TransactionEntity> = persistentListOf(),
        val groupedDays: ImmutableList<GroupedDayTransactions> = persistentListOf(),
        val splits: ImmutableList<TransactionSplitEntity> = persistentListOf(),
        val customCategories: ImmutableList<CustomCategoryEntity> = persistentListOf(),
        val searchQuery: String = "",
        val selectedFilter: AccountTransactionFilter = AccountTransactionFilter.ALL,
        val totalInflow: Double = 0.0,
        val totalOutflow: Double = 0.0,
        val netFlow: Double = 0.0,
        val currentBalance: Double = 0.0,
        val currencySymbol: String = "₹",
        val isHapticsEnabled: Boolean = true,
        val isPrivacyMode: Boolean = false,
        val isLoading: Boolean = true,
        val isPro: Boolean = false,
        val isExportingCsv: Boolean = false,
        val isExportingPdf: Boolean = false,
        val showTransferSheet: Boolean = false,
        val showExportSheet: Boolean = false,
        val showProGateSheet: Boolean = false,
        val transactionToEdit: TransactionEntity? = null,
        val transactionToDelete: TransactionEntity? = null,
        val showDeleteDialog: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadAccount(val accountId: Long) : Intent
        data class UpdateSearchQuery(val query: String) : Intent
        data class SelectFilter(val filter: AccountTransactionFilter) : Intent
        data object OpenTransferSheet : Intent
        data object DismissTransferSheet : Intent
        data object OpenExportSheet : Intent
        data object DismissExportSheet : Intent
        data class ExportCsv(val uri: android.net.Uri) : Intent
        data class ExportPdf(val uri: android.net.Uri) : Intent
        data object OpenProGate : Intent
        data object DismissProGate : Intent
        data class TransferFunds(
            val fromAccount: AccountEntity,
            val toAccount: AccountEntity,
            val amount: Double,
            val note: String?,
            val outflowMerchantText: String,
            val inflowMerchantText: String
        ) : Intent
        data class OpenTransactionDetails(val transaction: TransactionEntity) : Intent
        data object DismissTransactionDetails : Intent
        data class SaveTransaction(
            val transaction: TransactionEntity,
            val splits: List<SplitParticipant>? = null
        ) : Intent
        data class RequestDeleteTransaction(val transaction: TransactionEntity) : Intent
        data object ConfirmDeleteTransaction : Intent
        data object DismissDeleteDialog : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
        data object NavigateToPro : Effect
    }
}
