package com.masum.cipher.ui.pro

import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationEligibilityTest {

    private fun createSettings(
        isPro: Boolean,
        proOrderId: String? = null,
        proLicenseToken: String? = null,
        isMigrationBannerDismissed: Boolean = false
    ): UserSettings {
        return UserSettings(
            theme = AppTheme.SYSTEM,
            isBiometricEnabled = false,
            isPrivacyModeEnabled = false,
            isHapticsEnabled = true,
            currency = "USD",
            autoLockTimeout = 0L,
            lastStopTime = 0L,
            monthlyBudget = 0.0,
            isPro = isPro,
            proOrderId = proOrderId,
            proLicenseToken = proLicenseToken,
            isMigrationBannerDismissed = isMigrationBannerDismissed
        )
    }

    @Test
    fun freeUser_neverShowsMigrationBanner() {
        val settings = createSettings(isPro = false)
        assertFalse(settings.isGooglePlayPro)
        assertFalse(settings.isLegacyPro)
        assertFalse(settings.shouldShowMigrationBanner)
    }

    @Test
    fun legacyDodoUser_showsMigrationBanner() {
        val settings = createSettings(
            isPro = true,
            proOrderId = "dodo_ord_12345",
            proLicenseToken = "cip_lic_abcdef123456"
        )
        assertFalse(settings.isGooglePlayPro)
        assertTrue(settings.isLegacyPro)
        assertTrue(settings.shouldShowMigrationBanner)
    }

    @Test
    fun googlePlayUser_hidesMigrationBanner() {
        val settingsWithGpaOrder = createSettings(
            isPro = true,
            proOrderId = "GPA.1234-5678-9012-34567",
            proLicenseToken = "short_token"
        )
        assertTrue(settingsWithGpaOrder.isGooglePlayPro)
        assertFalse(settingsWithGpaOrder.shouldShowMigrationBanner)

        val settingsWithPlayToken = createSettings(
            isPro = true,
            proOrderId = null,
            proLicenseToken = "a".repeat(85)
        )
        assertTrue(settingsWithPlayToken.isGooglePlayPro)
        assertFalse(settingsWithPlayToken.shouldShowMigrationBanner)
    }

    @Test
    fun dismissedUser_hidesMigrationBanner() {
        val settings = createSettings(
            isPro = false,
            isMigrationBannerDismissed = true
        )
        assertFalse(settings.isGooglePlayPro)
        assertFalse(settings.shouldShowMigrationBanner)
    }
}
