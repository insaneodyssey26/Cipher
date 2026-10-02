package com.masum.cipher.core.security

import com.masum.cipher.core.data.local.pref.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
private const val REJECTED_REASON = "REVOKED"

class PendingLicenseActivator @Inject constructor(
    private val userPreferences: UserPreferences,
    private val licenseEngine: LicenseEngine,
    private val deviceIdProvider: DeviceIdProvider
) {

    suspend fun activate() {
        val token = userPreferences.getPendingLicenseToken() ?: return
        val deviceId = deviceIdProvider.get() ?: return

        val result = licenseEngine.activateLicenseRemote(token, email = null, deviceId = deviceId)

        when {
            result.isValid -> {
                userPreferences.setProStatus(
                    isPro = true,
                    tier = result.tier.identifier,
                    token = token,
                    orderId = result.orderId,
                    expiresAt = result.expiresAtEpochMs
                )
                userPreferences.setPendingLicenseToken(null)
            }
            result.isNetworkFailure -> Unit
            else -> {
                userPreferences.setPendingLicenseToken(null)
                if (!userPreferences.isCachedPro()) {
                    userPreferences.markLicenseRevoked(REJECTED_REASON)
                }
            }
        }
    }
}
