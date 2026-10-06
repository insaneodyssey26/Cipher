package com.masum.cipher.ui.debts

import androidx.compose.runtime.Immutable
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.DebtItem
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.mvi.UiEffect
import com.masum.cipher.core.mvi.UiIntent
import com.masum.cipher.core.mvi.UiState

enum class DebtFilterTab {
    ACTIVE,
    SETTLED,
    ALL
}

class DebtsContract {
    sealed class Intent : UiIntent {
        data class CreateDebt(
            val personName: String,
            val amount: Double,
            val type: DebtType,
            val dueDate: Long? = null,
            val note: String? = null,
            val accountId: Long? = null,
            val syncLedger: Boolean = false,
            val interestRate: Double = 0.0
        ) : Intent()

        data class UpdateDebt(
            val debtId: Long,
            val personName: String,
            val amount: Double,
            val type: DebtType,
            val dueDate: Long? = null,
            val note: String? = null,
            val accountId: Long? = null,
            val syncLedger: Boolean = false,
            val interestRate: Double = 0.0
        ) : Intent()

        data class RecordRepayment(
            val debtId: Long,
            val amount: Double,
            val timestamp: Long = System.currentTimeMillis(),
            val accountId: Long? = null,
            val note: String? = null,
            val syncLedger: Boolean = false
        ) : Intent()

        data class SettleDebt(val debtId: Long) : Intent()
        data class DeleteDebt(val debt: DebtEntity) : Intent()
        data class DeleteRepayment(val repayment: DebtRepaymentEntity) : Intent()
        data class SetFilterTab(val tab: DebtFilterTab) : Intent()
        data class SetTypeFilter(val type: DebtType?) : Intent()
        data class RestoreDebt(
            val debt: DebtEntity,
            val repayments: List<DebtRepaymentEntity> = emptyList(),
            val transactions: List<TransactionEntity> = emptyList()
        ) : Intent()

        data class UpdateDraft(
            val personName: String,
            val amount: String,
            val type: DebtType,
            val dueDate: Long?,
            val note: String,
            val accountId: Long?,
            val syncLedger: Boolean
        ) : Intent()

        data object ClearDraft : Intent()

        data class SyncDebtToLedger(
            val debtId: Long,
            val accountId: Long
        ) : Intent()

        data class UnlogDebtFromLedger(
            val debtId: Long
        ) : Intent()
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val debts: List<DebtItem> = emptyList(),
        val accounts: List<AccountEntity> = emptyList(),
        val currencySymbol: String = "₹",
        val totalLent: Double = 0.0,
        val totalBorrowed: Double = 0.0,
        val netBalance: Double = 0.0,
        val isHapticsEnabled: Boolean = true,
        val isPro: Boolean = false,
        val filterTab: DebtFilterTab = DebtFilterTab.ACTIVE,
        val typeFilter: DebtType? = null,
        val draftPersonName: String = "",
        val draftAmount: String = "",
        val draftType: DebtType = DebtType.LENT,
        val draftDueDate: Long? = null,
        val draftNote: String = "",
        val draftAccountId: Long? = null,
        val draftSyncLedger: Boolean = false
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowUndoDelete(
            val debt: DebtEntity,
            val repayments: List<DebtRepaymentEntity> = emptyList(),
            val transactions: List<TransactionEntity> = emptyList()
        ) : Effect()
    }
}
