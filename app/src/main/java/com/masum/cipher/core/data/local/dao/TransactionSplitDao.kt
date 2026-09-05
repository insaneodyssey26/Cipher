package com.masum.cipher.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionSplitDao {
    @Query("SELECT * FROM transaction_splits WHERE transactionId = :transactionId ORDER BY isCurrentUser DESC, id ASC")
    suspend fun getSplitsForTransactionSync(transactionId: Long): List<TransactionSplitEntity>

    @Query("SELECT * FROM transaction_splits")
    fun getAllSplitsFlow(): Flow<List<TransactionSplitEntity>>

    @Query("SELECT * FROM transaction_splits")
    suspend fun getAllSplits(): List<TransactionSplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<TransactionSplitEntity>)

    @Update
    suspend fun updateSplit(split: TransactionSplitEntity)

    @Query("UPDATE transaction_splits SET isPaid = :isPaid WHERE id = :splitId")
    suspend fun updateSplitPaidStatus(splitId: Long, isPaid: Boolean)

    @Query("DELETE FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun deleteSplitsForTransaction(transactionId: Long)

    @Query("DELETE FROM transaction_splits")
    suspend fun deleteAllSplits()
}
