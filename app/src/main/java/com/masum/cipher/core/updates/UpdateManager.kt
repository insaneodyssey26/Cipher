package com.masum.cipher.core.updates

import android.app.Activity
import android.os.Build
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

object UpdateManager {
    private const val UPDATE_REQUEST_CODE = 1001

    fun checkForUpdates(activity: Activity, onUpdateDownloaded: (() -> Unit)? = null) {
        val isInstalledFromPlayStore = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sourceInfo = activity.packageManager.getInstallSourceInfo(activity.packageName)
                sourceInfo.installingPackageName == "com.android.vending"
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getInstallerPackageName(activity.packageName) == "com.android.vending"
            }
        } catch (_: Exception) {
            false
        }

        if (!isInstalledFromPlayStore) {
            return
        }

        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        
        var listener: InstallStateUpdatedListener? = null
        listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded?.invoke()
                listener?.let { appUpdateManager.unregisterListener(it) }
            }
        }
        appUpdateManager.registerListener(listener)

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded?.invoke()
                appUpdateManager.unregisterListener(listener)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        com.google.android.play.core.appupdate.AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    Log.e("UpdateManager", "Failed to start update flow", e)
                }
            } else {
                appUpdateManager.unregisterListener(listener)
            }
        }.addOnFailureListener {
            Log.e("UpdateManager", "Failed to check for updates", it)
            appUpdateManager.unregisterListener(listener)
        }
    }
    
    fun completeUpdate(activity: Activity) {
        AppUpdateManagerFactory.create(activity).completeUpdate()
    }
}
