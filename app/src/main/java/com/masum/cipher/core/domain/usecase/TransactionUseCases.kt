package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AutoBackupFrequency
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.worker.AutoBackupScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences,
    private val autoBackupScheduler: AutoBackupScheduler
) {
    suspend operator fun invoke(transaction: TransactionEntity) {
        repository.insertTransaction(transaction)
        triggerBackupIfRequired(userPreferences, autoBackupScheduler)
    }
}

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences,
    private val autoBackupScheduler: AutoBackupScheduler
) {
    suspend operator fun invoke(transaction: TransactionEntity) {
        repository.deleteTransaction(transaction)
        triggerBackupIfRequired(userPreferences, autoBackupScheduler)
    }
}

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences,
    private val autoBackupScheduler: AutoBackupScheduler
) {
    suspend operator fun invoke(transaction: TransactionEntity) {
        repository.updateTransaction(transaction)
        triggerBackupIfRequired(userPreferences, autoBackupScheduler)
    }
}

private suspend fun triggerBackupIfRequired(
    userPreferences: UserPreferences,
    autoBackupScheduler: AutoBackupScheduler
) {
    try {
        val settings = userPreferences.settingsFlow.first()
        if (settings.autoBackupEnabled && settings.autoBackupFrequency == AutoBackupFrequency.EVERY_CHANGE) {
            autoBackupScheduler.triggerImmediateBackup()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
