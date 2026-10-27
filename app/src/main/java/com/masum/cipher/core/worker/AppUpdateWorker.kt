package com.masum.cipher.core.worker

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.notifications.LocalNotificationManager
import com.masum.cipher.core.updates.GitHubUpdateManager
import com.masum.cipher.core.updates.UpdateManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AppUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun userPreferences(): UserPreferences
        fun localNotificationManager(): LocalNotificationManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        val userPreferences = entryPoint.userPreferences()
        val localNotificationManager = entryPoint.localNotificationManager()

        val settings = userPreferences.settingsFlow.first()
        if (!settings.notifyAppUpdates) {
            return Result.success()
        }

        val isInstalledFromPlayStore = UpdateManager.isInstalledFromPlayStore(applicationContext)

        if (isInstalledFromPlayStore) {
            val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
            val appUpdateInfo: AppUpdateInfo? = suspendCancellableCoroutine { cont ->
                appUpdateManager.appUpdateInfo
                    .addOnSuccessListener { info ->
                        if (cont.isActive) cont.resume(info)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }

            if (appUpdateInfo != null && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val availableVersionCode = appUpdateInfo.availableVersionCode()
                val currentVersionCode = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionCode
                    }
                } catch (_: Exception) {
                    0
                }

                if (availableVersionCode > currentVersionCode && availableVersionCode > settings.lastNotifiedUpdateVersionCode) {
                    localNotificationManager.showAppUpdateAvailableNotification()
                    userPreferences.setLastNotifiedUpdateVersionCode(availableVersionCode)
                }
            }
        } else {
            val result = GitHubUpdateManager.checkForUpdate()
            if (result.isUpdateAvailable) {
                val numericCode = result.latestVersion.filter { it.isDigit() }.toIntOrNull() ?: 0
                if (numericCode > settings.lastNotifiedUpdateVersionCode) {
                    localNotificationManager.showAppUpdateAvailableNotification(versionName = result.latestVersion)
                    userPreferences.setLastNotifiedUpdateVersionCode(numericCode)
                }
            }
        }

        return Result.success()
    }
}
