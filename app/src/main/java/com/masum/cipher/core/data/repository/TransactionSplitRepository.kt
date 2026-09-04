package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.TransactionSplitDao
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionSplitRepository @Inject constructor(
    private val transactionSplitDao: TransactionSplitDao
) {
    fun getSplitsForTransaction(transactionId: Long): Flow<List<TransactionSplitEntity>> =
        transactionSplitDao.getSplitsForTransaction(transactionId)

    suspend fun getSplitsForTransactionSync(transactionId: Long): List<TransactionSplitEntity> =
        transactionSplitDao.getSplitsForTransactionSync(transactionId)

    fun getAllPendingSplits(): Flow<List<TransactionSplitEntity>> =
        transactionSplitDao.getAllPendingSplits()

    fun getAllSplitsFlow(): Flow<List<TransactionSplitEntity>> =
        transactionSplitDao.getAllSplitsFlow()

    suspend fun saveSplits(transactionId: Long, splits: List<TransactionSplitEntity>) {
        transactionSplitDao.deleteSplitsForTransaction(transactionId)
        if (splits.isNotEmpty()) {
            val entities = splits.map { it.copy(transactionId = transactionId) }
            transactionSplitDao.insertSplits(entities)
        }
    }

    suspend fun updateSplitPaidStatus(splitId: Long, isPaid: Boolean) {
        transactionSplitDao.updateSplitPaidStatus(splitId, isPaid)
    }

    suspend fun deleteSplitsForTransaction(transactionId: Long) {
        transactionSplitDao.deleteSplitsForTransaction(transactionId)
    }
}
