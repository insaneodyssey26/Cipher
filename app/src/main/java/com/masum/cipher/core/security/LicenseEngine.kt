package com.masum.cipher.core.security

import android.os.Build
import com.masum.cipher.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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

data class ActiveDeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val activatedAtEpochMs: Long = 0L,
    val isCurrentDevice: Boolean = false
)

data class LicenseValidationResult(
    val isValid: Boolean,
    val tier: ProTier = ProTier.FREE,
    val orderId: String? = null,
    val customerEmail: String? = null,
    val issuedAtEpochMs: Long = 0L,
    val expiresAtEpochMs: Long = 0L,
    val isExpired: Boolean = false,
    val deviceCount: Int = 1,
    val maxDevices: Int = 3,
    val activeDevices: List<ActiveDeviceInfo> = emptyList(),
    val errorMessage: String? = null,
    val isNetworkFailure: Boolean = false
)

sealed class RemoteLicenseCheckResult {
    data class Valid(
        val tier: ProTier,
        val deviceCount: Int,
        val maxDevices: Int,
        val expiresAtEpochMs: Long = 0L
    ) : RemoteLicenseCheckResult()
    data class Revoked(val reason: String) : RemoteLicenseCheckResult()
    data class Expired(val reason: String) : RemoteLicenseCheckResult()
    data class NotFound(val message: String) : RemoteLicenseCheckResult()
    data class NetworkError(val error: String) : RemoteLicenseCheckResult()
}

@Singleton
class LicenseEngine @Inject constructor() {

    companion object {
        private val USER_AGENT = "Cipher-Android/${BuildConfig.VERSION_NAME}"
        private const val LICENSE_SIGNING_SECRET = "cipher-license-v1-k9f3x8b2m4q7w1z5p0"
    }

    fun verifyLicenseSignature(
        licenseKey: String,
        tier: String,
        deviceId: String,
        expiresAt: Long,
        signatureHex: String?
    ): Boolean {
        if (signatureHex.isNullOrBlank()) return false
        return try {
            val canonical = "${licenseKey.trim().uppercase()}:${tier.trim().uppercase()}:${deviceId.trim()}:$expiresAt"
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(LICENSE_SIGNING_SECRET.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            mac.init(secretKey)
            val computedBytes = mac.doFinal(canonical.toByteArray(StandardCharsets.UTF_8))
            val computedHex = computedBytes.joinToString("") { "%02x".format(it) }
            MessageDigest.isEqual(
                computedHex.toByteArray(StandardCharsets.UTF_8),
                signatureHex.trim().lowercase().toByteArray(StandardCharsets.UTF_8)
            )
        } catch (_: Exception) {
            false
        }
    }

    private fun parseTier(identifier: String): ProTier {
        return ProTier.entries.firstOrNull { it.identifier.equals(identifier, ignoreCase = true) }
            ?: ProTier.LIFETIME
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

        try {
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/activate")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
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
                    val tierStr = jsonResponse.optString("tier", ProTier.LIFETIME.identifier)
                    val devCount = jsonResponse.optInt("deviceCount", 1)
                    val maxDev = jsonResponse.optInt("maxDevices", 3)
                    val deviceList = parseActiveDevices(jsonResponse.optJSONArray("devices"), deviceId)
                    val parsedTier = parseTier(tierStr)
                    val now = System.currentTimeMillis()
                    val serverExpiry = jsonResponse.optLong("expiresAt", 0L)
                    val signature = jsonResponse.optString("signature", "")

                    if (!verifyLicenseSignature(sanitized, tierStr, deviceId, serverExpiry, signature)) {
                        return@withContext LicenseValidationResult(
                            isValid = false,
                            errorMessage = "Cryptographic signature verification failed. Activation denied."
                        )
                    }

                    val expiryMs = if (serverExpiry > 0L) serverExpiry else when (parsedTier) {
                        ProTier.MONTHLY -> now + 30L * 24L * 60L * 60L * 1000L
                        ProTier.HALF_YEARLY, ProTier.SIX_MONTH -> now + 180L * 24L * 60L * 60L * 1000L
                        ProTier.ANNUAL -> now + 365L * 24L * 60L * 60L * 1000L
                        ProTier.LIFETIME, ProTier.PROMO, ProTier.DEVELOPER -> 0L
                        ProTier.FREE -> 0L
                    }
                    LicenseValidationResult(
                        isValid = true,
                        tier = parsedTier,
                        orderId = "DODO-${sanitized.takeLast(6).uppercase()}",
                        customerEmail = email,
                        issuedAtEpochMs = now,
                        expiresAtEpochMs = expiryMs,
                        isExpired = false,
                        deviceCount = devCount,
                        maxDevices = maxDev,
                        activeDevices = deviceList
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
            LicenseValidationResult(
                isValid = false,
                errorMessage = "Internet connection required to activate this license key.",
                isNetworkFailure = true
            )
        }
    }

    suspend fun fetchActiveDevicesRemote(
        licenseToken: String,
        currentDeviceId: String
    ): Pair<Int, List<ActiveDeviceInfo>> = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) return@withContext Pair(1, emptyList())
        try {
            val encodedKey = URLEncoder.encode(sanitized, "UTF-8")
            val encodedDev = URLEncoder.encode(currentDeviceId, "UTF-8")
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/devices?licenseKey=$encodedKey&deviceId=$encodedDev")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val text = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(text)
                val count = json.optInt("deviceCount", 1)
                val list = parseActiveDevices(json.optJSONArray("devices"), currentDeviceId)
                Pair(count, list)
            } else {
                Pair(1, emptyList())
            }
        } catch (_: Exception) {
            Pair(1, emptyList())
        }
    }

    private fun parseActiveDevices(devicesArray: JSONArray?, currentDeviceId: String): List<ActiveDeviceInfo> {
        if (devicesArray == null) return emptyList()
        val result = mutableListOf<ActiveDeviceInfo>()
        for (i in 0 until devicesArray.length()) {
            val obj = devicesArray.optJSONObject(i) ?: continue
            val devId = obj.optString("deviceId", "")
            val devName = obj.optString("deviceName", "Android Device")
            val activatedAt = obj.optLong("activatedAt", 0L)
            val isCurrent = obj.optBoolean("isCurrent", devId == currentDeviceId)
            result.add(
                ActiveDeviceInfo(
                    deviceId = devId,
                    deviceName = devName,
                    activatedAtEpochMs = activatedAt,
                    isCurrentDevice = isCurrent
                )
            )
        }
        return result
    }

    suspend fun checkLicenseRemoteStatus(
        licenseToken: String,
        currentDeviceId: String
    ): RemoteLicenseCheckResult = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) return@withContext RemoteLicenseCheckResult.NotFound("Empty license token")
        try {
            val encodedKey = URLEncoder.encode(sanitized, "UTF-8")
            val encodedDev = URLEncoder.encode(currentDeviceId, "UTF-8")
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/devices?licenseKey=$encodedKey&deviceId=$encodedDev")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val text = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(text)
                val isSuccess = json.optBoolean("success", true)
                if (!isSuccess) {
                    val err = json.optString("error", "License verification failed")
                    return@withContext RemoteLicenseCheckResult.Revoked(err)
                }
                val tierStr = json.optString("tier", "LIFETIME")
                val deviceCount = json.optInt("deviceCount", 1)
                val maxDevices = json.optInt("maxDevices", 3)
                val devicesArray = json.optJSONArray("devices")
                val isDeviceRegistered = if (devicesArray != null && currentDeviceId.isNotBlank()) {
                    var found = false
                    for (i in 0 until devicesArray.length()) {
                        val dev = devicesArray.optJSONObject(i)
                        if (dev?.optString("deviceId", "") == currentDeviceId || dev?.optBoolean("isCurrent", false) == true) {
                            found = true
                            break
                        }
                    }
                    found
                } else {
                    true
                }

                val parsedTier = parseTier(tierStr)
                val serverExpiry = json.optLong("expiresAt", 0L)
                val signature = json.optString("signature", "")

                if (signature.isNotBlank() && !verifyLicenseSignature(sanitized, tierStr, currentDeviceId, serverExpiry, signature)) {
                    return@withContext RemoteLicenseCheckResult.Revoked("Cryptographic signature verification failed")
                }

                val now = System.currentTimeMillis()
                val expiryMs = if (serverExpiry > 0L) serverExpiry else when (parsedTier) {
                    ProTier.MONTHLY -> now + 30L * 24L * 60L * 60L * 1000L
                    ProTier.HALF_YEARLY, ProTier.SIX_MONTH -> now + 180L * 24L * 60L * 60L * 1000L
                    ProTier.ANNUAL -> now + 365L * 24L * 60L * 60L * 1000L
                    ProTier.LIFETIME, ProTier.PROMO, ProTier.DEVELOPER -> 0L
                    ProTier.FREE -> 0L
                }

                if (!isDeviceRegistered) {
                    RemoteLicenseCheckResult.Revoked("Device has been revoked from this license")
                } else {
                    RemoteLicenseCheckResult.Valid(parsedTier, deviceCount, maxDevices, expiryMs)
                }
            } else {
                val errText = conn.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
                val json = try { JSONObject(errText) } catch (_: Exception) { JSONObject() }
                val status = json.optString("status", "")
                val errorMsg = json.optString("error", "Error code $responseCode")
                when {
                    responseCode == 403 && status.equals("REVOKED", ignoreCase = true) -> RemoteLicenseCheckResult.Revoked(errorMsg)
                    responseCode == 403 && status.equals("EXPIRED", ignoreCase = true) -> RemoteLicenseCheckResult.Expired(errorMsg)
                    responseCode == 404 -> RemoteLicenseCheckResult.NotFound(errorMsg)
                    else -> RemoteLicenseCheckResult.NetworkError("HTTP $responseCode: $errorMsg")
                }
            }
        } catch (e: Exception) {
            RemoteLicenseCheckResult.NetworkError(e.message ?: "Network error")
        }
    }

    suspend fun revokeDeviceRemote(
        licenseToken: String,
        deviceId: String,
        currentDeviceId: String = ""
    ): Pair<Int, List<ActiveDeviceInfo>> = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) return@withContext Pair(0, emptyList())
        try {
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/revoke")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val jsonBody = JSONObject().apply {
                put("licenseKey", sanitized)
                put("deviceId", deviceId)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val text = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(text)
                val count = json.optInt("deviceCount", 0)
                val list = parseActiveDevices(json.optJSONArray("devices"), currentDeviceId)
                Pair(count, list)
            } else {
                Pair(0, emptyList())
            }
        } catch (_: Exception) {
            Pair(0, emptyList())
        }
    }

    suspend fun deactivateLicenseRemote(
        licenseToken: String,
        deviceId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) return@withContext true
        try {
            val endpoint = URL("https://cipher-license-api.skmasumali-main.workers.dev/api/deactivate")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val jsonBody = JSONObject().apply {
                put("licenseKey", sanitized)
                put("deviceId", deviceId)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            responseCode in 200..299
        } catch (_: Exception) {
            true
        }
    }
}
