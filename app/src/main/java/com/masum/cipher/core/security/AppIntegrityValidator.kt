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
        private const val PLAY_STORE_SIGNING_FINGERPRINT =
            "c9218e4165ca7fc9fe8c71a1f5598c16cdaf78f4eeb4d3d08831ddb01ceb656e"
        private const val LOCAL_UPLOAD_KEY_FINGERPRINT =
            "cd99a42a52535bd4c98488d5509cb5d1da0b8c55e60e43831393b185040cea17"
        private const val DEBUG_KEY_FINGERPRINT =
            "f571ab304a78047e01a4e8e3017d4a49e156363d5313c572496131cb70a08a0a"

        private val SUSPICIOUS_FRAMEWORK_CLASSES = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "org.lsposed.lspd.nativebridge",
            "com.elderdrivers.riru.Riru",
            "io.github.lsposed.core.Main"
        )

        private val SUSPICIOUS_MAP_ENTRIES = listOf(
            "frida-agent",
            "frida-gadget",
            "gadget.so",
            "libfrida",
            "xposed.prop",
            "XposedBridge.jar"
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
    private fun verifyCertificateSignature(): Boolean {
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

            val md = MessageDigest.getInstance("SHA-256")
            signatures.any { sig ->
                val digest = md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
                digest.equals(PLAY_STORE_SIGNING_FINGERPRINT, ignoreCase = true) ||
                    digest.equals(LOCAL_UPLOAD_KEY_FINGERPRINT, ignoreCase = true) ||
                    (BuildConfig.DEBUG && digest.equals(DEBUG_KEY_FINGERPRINT, ignoreCase = true))
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isHookingDetected(): Boolean {
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
        return SUSPICIOUS_FRAMEWORK_CLASSES.any { className ->
            try {
                Class.forName(className)
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
            mapsFile.useLines { lines ->
                lines.any { line ->
                    val lower = line.lowercase()
                    SUSPICIOUS_MAP_ENTRIES.any { lower.contains(it) }
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
