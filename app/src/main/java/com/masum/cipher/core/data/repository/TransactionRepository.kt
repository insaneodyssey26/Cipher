package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.usecase.ProcessIncomingTransactionUseCase
import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val processIncomingTransactionUseCase: ProcessIncomingTransactionUseCase,
    private val widgetSyncManager: WidgetSyncManager
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity): TransactionEntity? {
        return processIncomingTransactionUseCase(transaction)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
        widgetSyncManager.syncWidget()
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        widgetSyncManager.syncWidget()
    }

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double?> =
        transactionDao.getTotalExpensesBetween(startTime, endTime)

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> =
        transactionDao.getTotalIncomeBetween(startTime, endTime)

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> = transactionDao.getExpensesSince(startTime)

    suspend fun refreshWidgets() = widgetSyncManager.syncWidget()
}
