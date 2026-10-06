package com.masum.cipher.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isSettled = 0 ORDER BY dueDate ASC, createdAt DESC")
    fun getActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isSettled = 1 ORDER BY createdAt DESC")
    fun getSettledDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtByIdFlow(id: Long): Flow<DebtEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("SELECT * FROM debt_repayments WHERE debtId = :debtId ORDER BY timestamp DESC")
    fun getRepaymentsForDebt(debtId: Long): Flow<List<DebtRepaymentEntity>>

    @Query("SELECT * FROM debt_repayments WHERE debtId = :debtId ORDER BY timestamp DESC")
    suspend fun getRepaymentsForDebtList(debtId: Long): List<DebtRepaymentEntity>

    @Query("SELECT * FROM debt_repayments ORDER BY timestamp DESC")
    fun getAllRepayments(): Flow<List<DebtRepaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: DebtRepaymentEntity): Long

    @Delete
    suspend fun deleteRepayment(repayment: DebtRepaymentEntity)

    @Query("SELECT COALESCE(SUM(remainingAmount), 0.0) FROM debts WHERE isSettled = 0 AND type = 'LENT'")
    fun getTotalLentRemaining(): Flow<Double>

    @Query("SELECT COALESCE(SUM(remainingAmount), 0.0) FROM debts WHERE isSettled = 0 AND type = 'BORROWED'")
    fun getTotalBorrowedRemaining(): Flow<Double>

    @Query("SELECT * FROM debts WHERE isSettled = 0 AND dueDate IS NOT NULL AND dueDate <= :thresholdTimestamp")
    suspend fun getUpcomingDueDebts(thresholdTimestamp: Long): List<DebtEntity>
}
