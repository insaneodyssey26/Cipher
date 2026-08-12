package com.masum.cipher.core.updates

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

object UpdateManager {
    private const val UPDATE_REQUEST_CODE = 1001

    fun checkForUpdates(activity: Activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    Log.e("UpdateManager", "Failed to start update flow", e)
                }
            }
        }.addOnFailureListener {
            Log.e("UpdateManager", "Failed to check for updates", it)
        }
    }
}
