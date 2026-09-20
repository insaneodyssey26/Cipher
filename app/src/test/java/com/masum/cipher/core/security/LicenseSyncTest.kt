package com.masum.cipher.core.security

import com.masum.cipher.core.worker.LicenseSyncWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseSyncTest {

    @Test
    fun testRemoteLicenseCheckResultTypes() {
        val valid = RemoteLicenseCheckResult.Valid(ProTier.ANNUAL, deviceCount = 2, maxDevices = 3)
        assertEquals(ProTier.ANNUAL, valid.tier)
        assertEquals(2, valid.deviceCount)
        assertEquals(3, valid.maxDevices)

        val revoked = RemoteLicenseCheckResult.Revoked("refund.succeeded")
        assertEquals("refund.succeeded", revoked.reason)

        val expired = RemoteLicenseCheckResult.Expired("subscription.expired")
        assertEquals("subscription.expired", expired.reason)

        val notFound = RemoteLicenseCheckResult.NotFound("Key not in KV")
        assertEquals("Key not in KV", notFound.message)

        val networkErr = RemoteLicenseCheckResult.NetworkError("Connect timeout")
        assertEquals("Connect timeout", networkErr.error)
    }

    @Test
    fun testOfflineGracePeriodCalculation() {
        val gracePeriodMs = LicenseSyncWorker.OFFLINE_GRACE_PERIOD_MS
        assertEquals(30L * 24L * 60L * 60L * 1000L, gracePeriodMs)

        val now = System.currentTimeMillis()
        val sync7DaysAgo = now - (7L * 24L * 60L * 60L * 1000L)
        val sync29DaysAgo = now - (29L * 24L * 60L * 60L * 1000L)
        val sync31DaysAgo = now - (31L * 24L * 60L * 60L * 1000L)

        val isGraceValid7 = (now - sync7DaysAgo) <= gracePeriodMs
        val isGraceValid29 = (now - sync29DaysAgo) <= gracePeriodMs
        val isGraceValid31 = (now - sync31DaysAgo) <= gracePeriodMs

        assertTrue(isGraceValid7)
        assertTrue(isGraceValid29)
        assertFalse(isGraceValid31)
    }

    @Test
    fun testProTierParsing() {
        val lifetime = ProTier.entries.firstOrNull { it.identifier.equals("LIFETIME", ignoreCase = true) }
        val annual = ProTier.entries.firstOrNull { it.identifier.equals("ANNUAL", ignoreCase = true) }
        val monthly = ProTier.entries.firstOrNull { it.identifier.equals("MONTHLY", ignoreCase = true) }
        val halfYearly = ProTier.entries.firstOrNull { it.identifier.equals("HALF_YEARLY", ignoreCase = true) }

        assertEquals(ProTier.LIFETIME, lifetime)
        assertEquals(ProTier.ANNUAL, annual)
        assertEquals(ProTier.MONTHLY, monthly)
        assertEquals(ProTier.HALF_YEARLY, halfYearly)
    }
}
