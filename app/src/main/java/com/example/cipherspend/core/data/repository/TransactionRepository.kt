package com.example.cipherspend.core.data.repository

import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()

    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>> = transactionDao.getExpensesSince(startTime)
}