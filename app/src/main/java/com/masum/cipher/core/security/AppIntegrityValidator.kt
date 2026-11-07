package com.masum.cipher.core.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.masum.cipher.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIntegrityValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MASK_SALT: Byte = 0x5A

        private fun decode(encoded: ByteArray): String {
            val out = ByteArray(encoded.size)
            for (i in encoded.indices) {
                out[i] = (encoded[i].toInt() xor MASK_SALT.toInt()).toByte()
            }
            return String(out, Charsets.UTF_8)
        }

        private val MASKED_PLAY_STORE_FINGERPRINT = byteArrayOf(
            57, 99, 104, 107, 98, 63, 110, 107, 108, 111, 57, 59, 109, 60, 57, 99,
            60, 63, 98, 57, 109, 107, 59, 107, 60, 111, 111, 99, 98, 57, 107, 108,
            57, 62, 59, 60, 109, 98, 60, 110, 63, 63, 56, 110, 62, 105, 62, 106,
            98, 98, 105, 107, 62, 62, 56, 106, 107, 57, 63, 56, 108, 111, 108, 63
        )
        private val MASKED_LOCAL_UPLOAD_FINGERPRINT = byteArrayOf(
            57, 62, 99, 99, 59, 110, 104, 59, 111, 104, 111, 105, 111, 56, 62, 110,
            57, 99, 98, 110, 98, 98, 62, 111, 111, 106, 99, 57, 56, 111, 62, 107,
            62, 59, 106, 56, 98, 57, 111, 111, 63, 108, 106, 63, 110, 105, 98, 105,
            107, 105, 99, 105, 56, 107, 98, 111, 106, 110, 106, 57, 63, 59, 107, 109
        )
        private val MASKED_DEBUG_FINGERPRINT = byteArrayOf(
            60, 111, 109, 107, 59, 56, 105, 106, 110, 59, 109, 98, 106, 110, 109, 63,
            106, 107, 59, 110, 63, 98, 63, 105, 106, 107, 109, 62, 110, 59, 110, 99,
            63, 107, 111, 108, 105, 108, 105, 62, 111, 105, 107, 105, 57, 111, 109, 104,
            110, 99, 108, 107, 105, 107, 57, 56, 109, 106, 59, 106, 98, 59, 106, 59
        )

        private val MASKED_FRAMEWORK_CLASSES = listOf(
            byteArrayOf(62, 63, 116, 40, 53, 56, 44, 116, 59, 52, 62, 40, 53, 51, 62, 116, 34, 42, 53, 41, 63, 62, 116, 2, 42, 53, 41, 63, 62, 24, 40, 51, 62, 61, 63),
            byteArrayOf(62, 63, 116, 40, 53, 56, 44, 116, 59, 52, 62, 40, 53, 51, 62, 116, 34, 42, 53, 41, 63, 62, 116, 2, 42, 53, 41, 63, 62, 18, 63, 54, 42, 63, 40, 41),
            byteArrayOf(53, 40, 61, 116, 54, 41, 42, 53, 41, 63, 62, 116, 54, 41, 42, 62, 116, 52, 59, 46, 51, 44, 63, 56, 40, 51, 62, 61, 63),
            byteArrayOf(57, 53, 55, 116, 63, 54, 62, 63, 40, 62, 40, 51, 44, 63, 40, 41, 116, 40, 51, 40, 47, 116, 8, 51, 40, 47),
            byteArrayOf(51, 53, 116, 61, 51, 46, 50, 47, 56, 116, 54, 41, 42, 53, 41, 63, 62, 116, 57, 53, 40, 63, 116, 23, 59, 51, 52)
        )

        private val MASKED_MAP_ENTRIES = listOf(
            byteArrayOf(60, 40, 51, 62, 59, 119, 59, 61, 63, 52, 46),
            byteArrayOf(60, 40, 51, 62, 59, 119, 61, 59, 62, 61, 63, 46),
            byteArrayOf(61, 59, 62, 61, 63, 46, 116, 41, 53),
            byteArrayOf(54, 51, 56, 60, 40, 51, 62, 59),
            byteArrayOf(34, 42, 53, 41, 63, 62, 116, 42, 40, 53, 42),
            byteArrayOf(2, 42, 53, 41, 63, 62, 24, 40, 51, 62, 61, 63, 116, 48, 59, 40)
        )
    }

    private var cachedIntegrityResult: Boolean? = null

    fun isEnvironmentValid(): Boolean {
        cachedIntegrityResult?.let { return it }
        val valid = verifyCertificateSignature() && !isHookingDetected()
        cachedIntegrityResult = valid
        return valid
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun getPrimarySignatureDigest(): String {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo ?: return ""
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return ""

            val md = MessageDigest.getInstance("SHA-256")
            md.digest(signatures[0].toByteArray()).joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun verifyCertificateSignature(): Boolean {
        if (BuildConfig.DEBUG) return true

        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo ?: return false
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return false

            val expectedPlay = decode(MASKED_PLAY_STORE_FINGERPRINT)
            val expectedLocal = decode(MASKED_LOCAL_UPLOAD_FINGERPRINT)
            val expectedDebug = decode(MASKED_DEBUG_FINGERPRINT)

            val md = MessageDigest.getInstance("SHA-256")
            signatures.any { sig ->
                val digest = md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
                digest.equals(expectedPlay, ignoreCase = true) ||
                    digest.equals(expectedLocal, ignoreCase = true) ||
                    (BuildConfig.DEBUG && digest.equals(expectedDebug, ignoreCase = true))
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isHookingDetected(): Boolean {
        if (BuildConfig.DEBUG) return false

        return try {
            if (Debug.isDebuggerConnected()) return true
            if (hasHookClasses()) return true
            if (hasSuspiciousMemoryMappings()) return true
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun hasHookClasses(): Boolean {
        return MASKED_FRAMEWORK_CLASSES.any { masked ->
            try {
                Class.forName(decode(masked))
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    private fun hasSuspiciousMemoryMappings(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists() || !mapsFile.canRead()) return false
            val decodedEntries = MASKED_MAP_ENTRIES.map { decode(it) }
            mapsFile.useLines { lines ->
                lines.any { line ->
                    val lower = line.lowercase()
                    decodedEntries.any { lower.contains(it) }
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
