package com.masum.cipher.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageInstallReceiver @Inject constructor(
    private val localNotificationManager: LocalNotificationManager
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            val data = intent.data ?: return
            val packageName = data.encodedSchemeSpecificPart ?: return

            // Avoid reacting to app updates (EXTRA_REPLACING will be true if it's an update)
            val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            if (isReplacing) return

            val pm = context.packageManager
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()

                if (isLikelyFinanceApp(packageName, appName)) {
                    localNotificationManager.showNewAppDetectedNotification(appName, packageName)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore if not found
            }
        }
    }

    private fun isLikelyFinanceApp(packageName: String, appName: String): Boolean {
        val keywords = listOf("pay", "bank", "upi", "finance", "money", "wallet", "cash", "crypto", "trade")
        val lowerPackage = packageName.lowercase()
        val lowerName = appName.lowercase()
        
        return keywords.any { lowerPackage.contains(it) || lowerName.contains(it) }
    }
}
