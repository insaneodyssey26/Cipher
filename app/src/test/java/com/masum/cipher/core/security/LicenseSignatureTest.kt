package com.masum.cipher.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class LicenseSignatureTest {

    private val licenseEngine = LicenseEngine()

    private fun generateTestSignature(
        secret: String,
        licenseKey: String,
        tier: String,
        deviceId: String,
        expiresAt: Long
    ): String {
        val canonical = "${licenseKey.trim().uppercase()}:${tier.trim().uppercase()}:${deviceId.trim()}:$expiresAt"
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val bytes = mac.doFinal(canonical.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testValidSignatureVerification() {
        val secret = "cipher-license-v1-k9f3x8b2m4q7w1z5p0"
        val key = "CIPHER-VALID-KEY"
        val tier = "LIFETIME"
        val deviceId = "device-pixel-8"
        val expiresAt = 0L

        val signature = generateTestSignature(secret, key, tier, deviceId, expiresAt)
        val isValid = licenseEngine.verifyLicenseSignature(key, tier, deviceId, expiresAt, signature)

        assertTrue(isValid)
    }

    @Test
    fun testSignatureRejectionOnForgedTier() {
        val secret = "cipher-license-v1-k9f3x8b2m4q7w1z5p0"
        val key = "CIPHER-PROMO-KEY"
        val realTier = "MONTHLY"
        val forgedTier = "LIFETIME"
        val deviceId = "device-galaxy-s24"
        val expiresAt = 1800000000000L

        val signature = generateTestSignature(secret, key, realTier, deviceId, expiresAt)
        val isValid = licenseEngine.verifyLicenseSignature(key, forgedTier, deviceId, expiresAt, signature)

        assertFalse(isValid)
    }

    @Test
    fun testSignatureRejectionOnForgedDeviceId() {
        val secret = "cipher-license-v1-k9f3x8b2m4q7w1z5p0"
        val key = "CIPHER-KEY-1"
        val tier = "ANNUAL"
        val originalDevice = "device-legit"
        val attackerDevice = "device-attacker"
        val expiresAt = 1800000000000L

        val signature = generateTestSignature(secret, key, tier, originalDevice, expiresAt)
        val isValid = licenseEngine.verifyLicenseSignature(key, tier, attackerDevice, expiresAt, signature)

        assertFalse(isValid)
    }

    @Test
    fun testSignatureRejectionOnForgedExpiry() {
        val secret = "cipher-license-v1-k9f3x8b2m4q7w1z5p0"
        val key = "CIPHER-SUB-KEY"
        val tier = "ANNUAL"
        val deviceId = "device-123"
        val realExpiry = 1750000000000L
        val forgedExpiry = 2000000000000L

        val signature = generateTestSignature(secret, key, tier, deviceId, realExpiry)
        val isValid = licenseEngine.verifyLicenseSignature(key, tier, deviceId, forgedExpiry, signature)

        assertFalse(isValid)
    }

    @Test
    fun testSignatureRejectionOnEmptyOrNullSignature() {
        assertFalse(licenseEngine.verifyLicenseSignature("KEY", "LIFETIME", "dev", 0L, null))
        assertFalse(licenseEngine.verifyLicenseSignature("KEY", "LIFETIME", "dev", 0L, ""))
        assertFalse(licenseEngine.verifyLicenseSignature("KEY", "LIFETIME", "dev", 0L, "   "))
        assertFalse(licenseEngine.verifyLicenseSignature("KEY", "LIFETIME", "dev", 0L, "invalid-hex"))
    }

    @Test
    fun testSignatureRejectionWithWrongSecret() {
        val wrongSecret = "attacker-secret-key-that-is-wrong"
        val key = "CIPHER-TEST-KEY"
        val tier = "LIFETIME"
        val deviceId = "device-123"
        val expiresAt = 0L

        val signature = generateTestSignature(wrongSecret, key, tier, deviceId, expiresAt)
        val isValid = licenseEngine.verifyLicenseSignature(key, tier, deviceId, expiresAt, signature)

        assertFalse(isValid)
    }
}
