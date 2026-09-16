package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AutoBackupFrequency
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.MerchantRenameRulePrompt
import com.masum.cipher.core.worker.AutoBackupScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class TransactionUpdateResult {
    data class MerchantRenamed(val prompt: MerchantRenameRulePrompt) : TransactionUpdateResult()
    data class CategoryChanged(val transaction: TransactionEntity) : TransactionUpdateResult()
    object NoRulePrompt : TransactionUpdateResult()
}

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferences,
    private val autoBackupScheduler: AutoBackupScheduler
) {
    suspend operator fun invoke(transaction: TransactionEntity): TransactionEntity? {
        val result = repository.insertTransaction(transaction)
        triggerBackupIfRequired(userPreferences, autoBackupScheduler)
        return result
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
    suspend operator fun invoke(transaction: TransactionEntity): TransactionUpdateResult {
        val existing = repository.getTransactionById(transaction.id)
        val oldMerchant = existing?.merchant?.trim().orEmpty()
        val newMerchant = transaction.merchant.trim()
        val merchantChanged = existing != null && oldMerchant.isNotBlank() && newMerchant.isNotBlank() && !oldMerchant.equals(newMerchant, ignoreCase = true)
        val categoryChanged = existing != null && existing.category != transaction.category && existing.merchant == transaction.merchant

        repository.updateTransaction(transaction)
        triggerBackupIfRequired(userPreferences, autoBackupScheduler)

        return when {
            merchantChanged -> TransactionUpdateResult.MerchantRenamed(MerchantRenameRulePrompt(oldMerchant, newMerchant))
            categoryChanged -> TransactionUpdateResult.CategoryChanged(transaction)
            else -> TransactionUpdateResult.NoRulePrompt
        }
    }
}

class SaveMerchantRuleUseCase @Inject constructor(
    private val merchantAliasDao: MerchantAliasDao
) {
    suspend operator fun invoke(rawName: String, cleanName: String) {
        merchantAliasDao.insertAlias(
            MerchantAliasEntity(
                rawName = rawName.trim(),
                cleanName = cleanName.trim(),
                isUserDefined = true
            )
        )
    }
}

class SaveCategoryRuleUseCase @Inject constructor(
    private val categoryRuleDao: CategoryRuleDao
) {
    suspend operator fun invoke(merchantName: String, category: String) {
        categoryRuleDao.insertRule(
            CategoryRuleEntity(
                merchantName = merchantName.trim(),
                customCategory = category
            )
        )
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
