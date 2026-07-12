package com.masum.cipher.core.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.masum.cipher.core.data.local.pref.AutoBackupFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoBackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val WORK_NAME = "CipherAutoBackupWork"
    }

    fun scheduleBackup(frequency: AutoBackupFrequency) {
        val workManager = WorkManager.getInstance(context)

        if (frequency == AutoBackupFrequency.NEVER) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val repeatInterval = when (frequency) {
            AutoBackupFrequency.DAILY -> 1L to TimeUnit.DAYS
            AutoBackupFrequency.WEEKLY -> 7L to TimeUnit.DAYS
            AutoBackupFrequency.EVERY_CHANGE -> 6L to TimeUnit.HOURS
            else -> return
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            repeatInterval.first, repeatInterval.second
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun triggerImmediateBackup() {
        val workManager = WorkManager.getInstance(context)
        
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<AutoBackupWorker>()
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME + "_Immediate",
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelBackup() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
