package com.masum.cipher.core.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.masum.cipher.core.data.local.pref.AutoBackupFrequency
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.BackupRepository
import com.masum.cipher.core.security.KeystoreManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun userPreferences(): UserPreferences
        fun backupRepository(): BackupRepository
        fun keystoreManager(): KeystoreManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val userPreferences = entryPoint.userPreferences()
        val backupRepository = entryPoint.backupRepository()
        val keystoreManager = entryPoint.keystoreManager()

        val settings = userPreferences.settingsFlow.first()
        
        if (!settings.autoBackupEnabled || settings.autoBackupFrequency == AutoBackupFrequency.NEVER) {
            return Result.success()
        }

        val uriString = settings.autoBackupUri ?: return Result.failure()
        val encryptedPassword = settings.autoBackupEncryptedPassword ?: return Result.failure()

        val plainPassword = keystoreManager.decrypt(encryptedPassword) ?: return Result.failure()

        return try {
            val directoryUri = uriString.toUri()
            val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(applicationContext, directoryUri)
            
            if (documentFile == null || !documentFile.exists() || !documentFile.isDirectory || !documentFile.canWrite()) {
                return Result.failure()
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val filename = "Cipher_AutoBackup_${dateFormat.format(Date())}.cipher"
            
            val newFile = documentFile.createFile("application/octet-stream", filename)
                ?: return Result.failure()
                
            val outputStream = applicationContext.contentResolver.openOutputStream(newFile.uri)
                ?: return Result.failure()
                
            val result = backupRepository.exportData(outputStream, plainPassword.toCharArray())
            
            if (result.isSuccess) {
                val files = documentFile.listFiles()
                    .filter { it.name?.startsWith("Cipher_AutoBackup_") == true && it.name?.endsWith(".cipher") == true }
                    .sortedByDescending { it.lastModified() }
                    
                if (files.size > 5) {
                    files.drop(5).forEach { it.delete() }
                }
                
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
