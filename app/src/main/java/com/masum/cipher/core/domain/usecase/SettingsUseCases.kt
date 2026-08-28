package com.masum.cipher.core.domain.usecase

import android.net.Uri
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.BackupRepository
import com.masum.cipher.core.util.AppFormatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val userPreferences: UserPreferences,
    private val transactionRepository: com.masum.cipher.core.data.repository.TransactionRepository
) {
    suspend fun theme(theme: AppTheme) {
        userPreferences.setTheme(theme)
        transactionRepository.refreshWidgets()
    }
    suspend fun accentColor(color: com.masum.cipher.core.data.local.pref.AccentColor) {
        userPreferences.setAccentColor(color)
        transactionRepository.refreshWidgets()
    }
    suspend fun biometric(enabled: Boolean) = userPreferences.setBiometricEnabled(enabled)
    suspend fun privacyMode(enabled: Boolean) = userPreferences.setPrivacyModeEnabled(enabled)
    suspend fun haptics(enabled: Boolean) = userPreferences.setHapticsEnabled(enabled)
    suspend fun autoLockTimeout(timeout: Long) = userPreferences.setAutoLockTimeout(timeout)
    suspend fun monthlyBudget(amount: Double) {
        userPreferences.setMonthlyBudget(amount)
        transactionRepository.refreshWidgets()
    }

    suspend fun autoBackupEnabled(enabled: Boolean) = userPreferences.setAutoBackupEnabled(enabled)
    suspend fun autoBackupFrequency(frequency: com.masum.cipher.core.data.local.pref.AutoBackupFrequency) = userPreferences.setAutoBackupFrequency(frequency)
    suspend fun autoBackupUri(uri: String?) = userPreferences.setAutoBackupUri(uri)
    suspend fun autoBackupEncryptedPassword(password: String?) = userPreferences.setAutoBackupEncryptedPassword(password)
}

class ClearAllDataUseCase @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
    }
}

class ExportCsvUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val transactions = transactionDao.getAllTransactions().first()
            val csvHeader = "ID,Date,Merchant,Amount,Category,Type,Note\n"
            val csvData = transactions.joinToString("\n") { tx ->
                val date = AppFormatters.getFullDate().format(Date(tx.timestamp))
                val type = if (tx.isIncome) "Income" else "Expense"
                val noteStr = tx.note?.replace("\"", "\"\"") ?: ""
                "${tx.id},\"$date\",\"${tx.merchant}\",${tx.amount},\"${tx.category}\",\"$type\",\"$noteStr\""
            }
            
            backupRepository.provideOutputStream(uri)?.use { outputStream ->
                outputStream.write((csvHeader + csvData).toByteArray())
            } ?: throw Exception("Could not open file for writing")
        }
    }
}

class ExportPdfUseCase @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val transactionDao: TransactionDao,
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val transactions = transactionDao.getAllTransactions().first()
            
            backupRepository.provideOutputStream(uri)?.use { outputStream ->
                com.masum.cipher.core.util.PdfGenerator.generateStatement(context, transactions, outputStream)
            } ?: throw Exception("Could not open file for writing")
        }
    }
}

class ExportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(uri: Uri, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        val outputStream = backupRepository.provideOutputStream(uri)
            ?: return@withContext Result.failure(Exception("Could not open file for writing"))
        
        backupRepository.exportData(outputStream, password)
    }
}

class ImportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(uri: Uri, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        val inputStream = backupRepository.provideInputStream(uri)
            ?: return@withContext Result.failure(Exception("Could not open file for reading"))
        
        backupRepository.importData(inputStream, password)
    }
}
