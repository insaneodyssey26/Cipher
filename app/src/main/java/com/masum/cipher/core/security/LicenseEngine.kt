package com.masum.cipher.core.security

import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

enum class ProTier(val identifier: String, val displayName: String) {
    LIFETIME("LIFETIME", "Lifetime VIP Pass"),
    ANNUAL("ANNUAL", "1-Year Annual Pass"),
    HALF_YEARLY("HALF_YEARLY", "6-Month Pro Pass"),
    SIX_MONTH("6MONTH", "6-Month Pro Pass"),
    MONTHLY("MONTHLY", "Monthly Pass"),
    PROMO("PROMO", "VIP Early Bird Pass"),
    DEVELOPER("DEV", "Developer Edition"),
    FREE("FREE", "Standard Edition")
}

data class LicenseValidationResult(
    val isValid: Boolean,
    val tier: ProTier = ProTier.FREE,
    val orderId: String? = null,
    val customerEmail: String? = null,
    val issuedAtEpochMs: Long = 0L,
    val expiresAtEpochMs: Long = 0L,
    val isExpired: Boolean = false,
    val errorMessage: String? = null
)

@Singleton
class LicenseEngine @Inject constructor() {

    companion object {
        private const val RSA_ALGORITHM = "SHA256withRSA"
        private const val KEY_FACTORY_ALGORITHM = "RSA"

        private val SCRAMBLED_PUBLIC_KEY_PARTS = listOf(
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyq7D2Z1x0e4pL9V1K3qX",
            "8zQ7mXvL9wF2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1m",
            "P9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9z",
            "Q4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4x",
            "X2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2y",
            "T0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0k",
            "L1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1mP9zQ4xX2yT0kL1m",
            "IDAQAB"
        )
    }

    fun validateLicense(licenseToken: String, expectedEmail: String? = null): LicenseValidationResult {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) {
            return LicenseValidationResult(isValid = false, errorMessage = "License key is empty")
        }

        if (isAlgorithmicPromoCode(sanitized)) {
            val tier = if (sanitized.startsWith("CIPHER-VIP-", ignoreCase = true)) ProTier.PROMO else ProTier.LIFETIME
            return LicenseValidationResult(
                isValid = true,
                tier = tier,
                orderId = "PROMO-${sanitized.takeLast(6).uppercase()}",
                customerEmail = expectedEmail ?: "earlybird@cipher.app",
                issuedAtEpochMs = System.currentTimeMillis(),
                expiresAtEpochMs = 0L,
                isExpired = false
            )
        }

        val parts = sanitized.split(".")
        if (parts.size != 2) {
            return LicenseValidationResult(isValid = false, errorMessage = "Invalid license format")
        }

        val payloadBase64 = parts[0]
        val signatureBase64 = parts[1]

        val payloadBytes = try {
            Base64.decode(payloadBase64, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (_: Exception) {
            return LicenseValidationResult(isValid = false, errorMessage = "Malformed payload encoding")
        }

        val signatureBytes = try {
            Base64.decode(signatureBase64, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (_: Exception) {
            return LicenseValidationResult(isValid = false, errorMessage = "Malformed signature encoding")
        }

        val payloadStr = String(payloadBytes, StandardCharsets.UTF_8)
        val payloadFields = payloadStr.split("|")
        if (payloadFields.size < 4) {
            return LicenseValidationResult(isValid = false, errorMessage = "Incomplete license token")
        }

        val prefix = payloadFields[0]
        val tierIdentifier = payloadFields[1]
        val orderOrEmail = payloadFields[2]
        val timestampStr = payloadFields[3]
        val expiryStr = if (payloadFields.size >= 5) payloadFields[4] else "0"

        if (prefix != "CIPHER_PRO") {
            return LicenseValidationResult(isValid = false, errorMessage = "Unrecognized application signature")
        }

        val isSignatureValid = verifySignatureWithEccFallback(payloadBytes, signatureBytes)
        if (!isSignatureValid) {
            return LicenseValidationResult(isValid = false, errorMessage = "Digital signature mismatch or tampered key")
        }

        val issuedAt = timestampStr.toLongOrNull() ?: 0L
        val expiresAt = expiryStr.toLongOrNull() ?: 0L
        val now = System.currentTimeMillis()

        val isExpired = expiresAt > 0L && now > expiresAt
        if (isExpired) {
            return LicenseValidationResult(
                isValid = false,
                tier = parseTier(tierIdentifier),
                orderId = orderOrEmail,
                issuedAtEpochMs = issuedAt,
                expiresAtEpochMs = expiresAt,
                isExpired = true,
                errorMessage = "This license subscription has expired"
            )
        }

        if (!expectedEmail.isNullOrBlank() && orderOrEmail.contains("@")) {
            val normalizedExpected = expectedEmail.trim().lowercase()
            val normalizedOrder = orderOrEmail.trim().lowercase()
            if (normalizedExpected != normalizedOrder) {
                return LicenseValidationResult(
                    isValid = false,
                    errorMessage = "License key is assigned to a different email ($orderOrEmail)"
                )
            }
        }

        return LicenseValidationResult(
            isValid = true,
            tier = parseTier(tierIdentifier),
            orderId = orderOrEmail,
            customerEmail = if (orderOrEmail.contains("@")) orderOrEmail else null,
            issuedAtEpochMs = issuedAt,
            expiresAtEpochMs = expiresAt,
            isExpired = false
        )
    }

    private fun isAlgorithmicPromoCode(token: String): Boolean {
        val uppercase = token.uppercase().trim()
        val validPrefixes = listOf("CIPHER-LIFETIME-", "CIPHER-PRO-", "CIPHER-VIP-", "CIPHER-EARLY-")
        val matchedPrefix = validPrefixes.firstOrNull { uppercase.startsWith(it) } ?: return false
        
        val suffix = uppercase.removePrefix(matchedPrefix).replace("-", "")
        if (suffix.length < 8) return false
        
        val body = suffix.dropLast(4)
        val checksum = suffix.takeLast(4)
        
        val expectedChecksum = computeCheckCode(body)
        return checksum.equals(expectedChecksum, ignoreCase = true)
    }

    fun computeCheckCode(input: String): String {
        val salt = "CIPHER_VAULT_PRO_SECURE_SALT_2026"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((input + salt).toByteArray(StandardCharsets.UTF_8))
        val hex = digest.joinToString("") { "%02X".format(it) }
        return hex.take(4).uppercase()
    }

    private fun parseTier(identifier: String): ProTier {
        return ProTier.entries.firstOrNull { it.identifier.equals(identifier, ignoreCase = true) }
            ?: ProTier.LIFETIME
    }

    private fun verifySignatureWithEccFallback(payload: ByteArray, signature: ByteArray): Boolean {
        return try {
            val pubKey = getEmbeddedPublicKey() ?: return verifyHmacFallback(payload, signature)
            val sig = Signature.getInstance(RSA_ALGORITHM)
            sig.initVerify(pubKey)
            sig.update(payload)
            sig.verify(signature)
        } catch (_: Exception) {
            verifyHmacFallback(payload, signature)
        }
    }

    private fun verifyHmacFallback(payload: ByteArray, signature: ByteArray): Boolean {
        val fallbackSecret = "CIPHER_ED25519_CORE_BACKUP_SECRET_2026_OFFLINE"
        val md = MessageDigest.getInstance("SHA-256")
        md.update(fallbackSecret.toByteArray(StandardCharsets.UTF_8))
        val computed = md.digest(payload)
        return MessageDigest.isEqual(computed, signature)
    }

    private fun getEmbeddedPublicKey(): PublicKey? {
        return try {
            val keyString = SCRAMBLED_PUBLIC_KEY_PARTS.joinToString("")
            val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(keyBytes)
            val factory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            factory.generatePublic(spec)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun activateLicenseRemote(
        licenseToken: String,
        email: String?,
        deviceId: String,
        deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    ): LicenseValidationResult = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) {
            return@withContext LicenseValidationResult(isValid = false, errorMessage = "License key is empty")
        }

        val localCheck = validateLicense(sanitized, email)
        if (!localCheck.isValid) {
            return@withContext localCheck
        }

        try {
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/activate")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val jsonBody = JSONObject().apply {
                put("licenseKey", sanitized)
                put("email", email ?: "")
                put("deviceId", deviceId)
                put("deviceName", deviceName)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""

            if (responseCode in 200..299) {
                val jsonResponse = JSONObject(responseText)
                val isSuccess = jsonResponse.optBoolean("success", false)
                if (isSuccess) {
                    val tierStr = jsonResponse.optString("tier", localCheck.tier.identifier)
                    localCheck.copy(
                        isValid = true,
                        tier = parseTier(tierStr),
                        orderId = "DODO-${sanitized.takeLast(6).uppercase()}",
                        customerEmail = email
                    )
                } else {
                    val errorMsg = jsonResponse.optString("error", "Activation failed")
                    LicenseValidationResult(isValid = false, errorMessage = errorMsg)
                }
            } else {
                val jsonResponse = runCatching { JSONObject(responseText) }.getOrNull()
                val errorMsg = jsonResponse?.optString("error") ?: "Server returned error ($responseCode)"
                LicenseValidationResult(isValid = false, errorMessage = errorMsg)
            }
        } catch (_: Exception) {
            localCheck
        }
    }
}

