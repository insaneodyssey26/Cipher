package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.DebtDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.DebtItem
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val widgetSyncManager: WidgetSyncManager
) {
    fun getAllDebtItems(): Flow<List<DebtItem>> {
        return combine(
            debtDao.getAllDebts(),
            debtDao.getAllRepayments(),
            accountDao.getAllAccountsFlow()
        ) { debts, repayments, accounts ->
            val repaymentsByDebt = repayments.groupBy { it.debtId }
            val accountMap = accounts.associateBy { it.id }

            debts.map { debt ->
                DebtItem(
                    debt = debt,
                    repayments = repaymentsByDebt[debt.id] ?: emptyList(),
                    accountName = debt.accountId?.let { accountMap[it]?.name }
                )
            }
        }
    }

    fun getDebtItemById(id: Long): Flow<DebtItem?> {
        return combine(
            debtDao.getDebtByIdFlow(id),
            debtDao.getRepaymentsForDebt(id),
            accountDao.getAllAccountsFlow()
        ) { debt, repayments, accounts ->
            if (debt == null) null
            else {
                val accountMap = accounts.associateBy { it.id }
                DebtItem(
                    debt = debt,
                    repayments = repayments,
                    accountName = debt.accountId?.let { accountMap[it]?.name }
                )
            }
        }
    }

    suspend fun createDebt(
        personName: String,
        amount: Double,
        type: DebtType,
        dueDate: Long? = null,
        note: String? = null,
        accountId: Long? = null,
        syncLedger: Boolean = false,
        interestRate: Double = 0.0
    ): Long {
        var createdTxId: Long? = null
        if (syncLedger && accountId != null && amount > 0.0) {
            val isIncome = (type == DebtType.BORROWED)
            val merchantName = if (type == DebtType.BORROWED) "Loan from $personName" else "Loan to $personName"
            val tx = TransactionEntity(
                amount = amount,
                merchant = merchantName,
                currency = "INR",
                category = "OTHERS",
                timestamp = System.currentTimeMillis(),
                rawSms = null,
                isIncome = isIncome,
                note = note,
                accountId = accountId
            )
            createdTxId = transactionDao.insertTransaction(tx)
            widgetSyncManager.syncWidget()
        }

        val debt = DebtEntity(
            personName = personName.trim(),
            amount = amount,
            remainingAmount = amount,
            type = type.key,
            dueDate = dueDate,
            note = note?.trim()?.ifBlank { null },
            accountId = accountId,
            isSettled = false,
            interestRate = interestRate,
            transactionId = createdTxId
        )
        return debtDao.insertDebt(debt)
    }

    suspend fun recordRepayment(
        debtId: Long,
        amount: Double,
        timestamp: Long = System.currentTimeMillis(),
        accountId: Long? = null,
        note: String? = null,
        syncLedger: Boolean = false
    ): Long {
        val debt = debtDao.getDebtById(debtId) ?: return 0L
        var createdTxId: Long? = null

        if (syncLedger && accountId != null && amount > 0.0) {
            val isIncome = (debt.type == DebtType.LENT.key)
            val merchantName = if (isIncome) "Repayment from ${debt.personName}" else "Repayment to ${debt.personName}"
            val tx = TransactionEntity(
                amount = amount,
                merchant = merchantName,
                currency = "INR",
                category = "OTHERS",
                timestamp = timestamp,
                rawSms = null,
                isIncome = isIncome,
                note = note,
                accountId = accountId
            )
            createdTxId = transactionDao.insertTransaction(tx)
            widgetSyncManager.syncWidget()
        }

        val repayment = DebtRepaymentEntity(
            debtId = debtId,
            amount = amount,
            timestamp = timestamp,
            accountId = accountId,
            note = note?.trim()?.ifBlank { null },
            transactionId = createdTxId
        )
        val repaymentId = debtDao.insertRepayment(repayment)

        val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0.0)
        val updatedDebt = debt.copy(
            remainingAmount = newRemaining,
            isSettled = newRemaining <= 0.001
        )
        debtDao.updateDebt(updatedDebt)

        return repaymentId
    }

    suspend fun settleDebt(debtId: Long) {
        val debt = debtDao.getDebtById(debtId) ?: return
        if (debt.remainingAmount > 0.0) {
            val repayment = DebtRepaymentEntity(
                debtId = debtId,
                amount = debt.remainingAmount,
                timestamp = System.currentTimeMillis(),
                accountId = debt.accountId,
                note = "Settled in full"
            )
            debtDao.insertRepayment(repayment)
        }
        debtDao.updateDebt(debt.copy(remainingAmount = 0.0, isSettled = true))
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    suspend fun getDebtWithRepaymentsAndTransactions(debtId: Long): Triple<DebtEntity?, List<DebtRepaymentEntity>, List<TransactionEntity>> {
        val debt = debtDao.getDebtById(debtId) ?: return Triple(null, emptyList(), emptyList())
        val repayments = debtDao.getRepaymentsForDebtList(debtId)
        val txIds = mutableListOf<Long>()
        debt.transactionId?.let { txIds.add(it) }
        repayments.forEach { rep -> rep.transactionId?.let { txIds.add(it) } }
        val transactions = txIds.mapNotNull { transactionDao.getTransactionById(it) }
        return Triple(debt, repayments, transactions)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        val repayments = debtDao.getRepaymentsForDebtList(debt.id)
        debt.transactionId?.let { txId ->
            val tx = transactionDao.getTransactionById(txId)
            if (tx != null) transactionDao.deleteTransaction(tx)
        }
        repayments.forEach { repayment ->
            repayment.transactionId?.let { txId ->
                val tx = transactionDao.getTransactionById(txId)
                if (tx != null) transactionDao.deleteTransaction(tx)
            }
        }
        debtDao.deleteDebt(debt)
        widgetSyncManager.syncWidget()
    }

    suspend fun restoreDebt(
        debt: DebtEntity,
        repayments: List<DebtRepaymentEntity>,
        transactions: List<TransactionEntity>
    ) {
        transactions.forEach { tx ->
            transactionDao.insertTransaction(tx)
        }
        debtDao.insertDebt(debt)
        repayments.forEach { repayment ->
            debtDao.insertRepayment(repayment)
        }
        widgetSyncManager.syncWidget()
    }

    suspend fun deleteRepayment(repayment: DebtRepaymentEntity) {
        val debt = debtDao.getDebtById(repayment.debtId)
        repayment.transactionId?.let { txId ->
            val tx = transactionDao.getTransactionById(txId)
            if (tx != null) transactionDao.deleteTransaction(tx)
        }
        debtDao.deleteRepayment(repayment)
        if (debt != null) {
            val allRepayments = debtDao.getRepaymentsForDebtList(debt.id)
            val totalRepaid = allRepayments.sumOf { it.amount }
            val newRemaining = (debt.amount - totalRepaid).coerceAtLeast(0.0)
            debtDao.updateDebt(debt.copy(remainingAmount = newRemaining, isSettled = newRemaining <= 0.001))
        }
        widgetSyncManager.syncWidget()
    }

    fun getTotalLentRemaining(): Flow<Double> = debtDao.getTotalLentRemaining()

    fun getTotalBorrowedRemaining(): Flow<Double> = debtDao.getTotalBorrowedRemaining()
}
