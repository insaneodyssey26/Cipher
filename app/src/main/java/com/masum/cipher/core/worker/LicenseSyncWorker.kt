package com.masum.cipher.core.worker

import android.content.Context
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.security.LicenseEngine
import com.masum.cipher.core.security.RemoteLicenseCheckResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class LicenseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val OFFLINE_GRACE_PERIOD_MS = 30L * 24L * 60L * 60L * 1000L
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun userPreferences(): UserPreferences
        fun licenseEngine(): LicenseEngine
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val userPreferences = entryPoint.userPreferences()
        val licenseEngine = entryPoint.licenseEngine()

        if (!userPreferences.isCachedPro()) {
            return Result.success()
        }

        val token = userPreferences.getCachedLicenseToken()
        if (token.isNullOrBlank()) {
            userPreferences.deactivatePro()
            return Result.success()
        }

        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "device_${System.currentTimeMillis()}"

        val lastSync = userPreferences.getCachedLastLicenseSyncTime()
        val now = System.currentTimeMillis()

        return when (val result = licenseEngine.checkLicenseRemoteStatus(token, deviceId)) {
            is RemoteLicenseCheckResult.Valid -> {
                userPreferences.setLastLicenseSyncTime(now)
                Result.success()
            }
            is RemoteLicenseCheckResult.Revoked,
            is RemoteLicenseCheckResult.Expired,
            is RemoteLicenseCheckResult.NotFound -> {
                userPreferences.deactivatePro()
                Result.success()
            }
            is RemoteLicenseCheckResult.NetworkError -> {
                if (lastSync > 0L && (now - lastSync) > OFFLINE_GRACE_PERIOD_MS) {
                    userPreferences.deactivatePro()
                }
                Result.retry()
            }
        }
    }
}
