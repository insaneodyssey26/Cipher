package com.masum.cipher.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.masum.cipher.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE merchant LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 0")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 1")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 0 AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 1 AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE isIncome = 0 AND timestamp >= :startTime")
    fun getExpensesSince(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime")
    fun getTransactionsSince(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timestamp BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findDuplicate(amount: Double, startTime: Long, endTime: Long): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE isIncome = 0 AND timestamp >= :startTime")
    suspend fun sumExpensesSince(startTime: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE isIncome = 1 AND timestamp >= :startTime")
    suspend fun sumIncomeSince(startTime: Long): Double

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT COUNT(*) FROM transactions WHERE category = 'OTHERS'")
    suspend fun getUncategorizedCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE isIncome = 0 AND timestamp >= :startTime")
    suspend fun getExpensesCountSince(startTime: Long): Int
}
