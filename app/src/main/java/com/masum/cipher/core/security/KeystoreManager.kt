package com.masum.cipher.core.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS_AUTO_BACKUP = "cipher_auto_backup_key"
        private const val ALIAS_ENTITLEMENT_HMAC = "cipher_entitlement_hmac_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"
        private const val HMAC_ALGORITHM = "HmacSHA256"
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun buildKeyGenParameterSpec(strongBox: Boolean): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            ALIAS_AUTO_BACKUP,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

        if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }

        return builder.build()
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                keyGenerator.init(buildKeyGenParameterSpec(strongBox = true))
                keyGenerator.generateKey()
                return
            } catch (_: StrongBoxUnavailableException) {
            }
        }
        keyGenerator.init(buildKeyGenParameterSpec(strongBox = false))
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(ALIAS_AUTO_BACKUP)) {
            generateKey()
        }
        return keyStore.getKey(ALIAS_AUTO_BACKUP, null) as SecretKey
    }

    fun encrypt(textToEncrypt: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
        
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedString = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        
        return "$ivString$IV_SEPARATOR$encryptedString"
    }

    fun decrypt(encryptedText: String): String? {
        return try {
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) return null
            
            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val encryptedBytes = Base64.decode(parts[1], Base64.DEFAULT)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun getHmacSecretKey(): SecretKey {
        if (!keyStore.containsAlias(ALIAS_ENTITLEMENT_HMAC)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
            val builder = KeyGenParameterSpec.Builder(
                ALIAS_ENTITLEMENT_HMAC,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    builder.setIsStrongBoxBacked(true)
                    keyGenerator.init(builder.build())
                    keyGenerator.generateKey()
                    return keyStore.getKey(ALIAS_ENTITLEMENT_HMAC, null) as SecretKey
                } catch (_: StrongBoxUnavailableException) {
                    builder.setIsStrongBoxBacked(false)
                }
            }
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
        return keyStore.getKey(ALIAS_ENTITLEMENT_HMAC, null) as SecretKey
    }

    fun generateEntitlementHmac(payload: String): String {
        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(getHmacSecretKey())
            val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    fun verifyEntitlementHmac(payload: String, expectedHmac: String): Boolean {
        if (expectedHmac.isBlank()) return false
        val computed = generateEntitlementHmac(payload)
        if (computed.isBlank()) return false
        return MessageDigest.isEqual(
            computed.toByteArray(Charsets.UTF_8),
            expectedHmac.toByteArray(Charsets.UTF_8)
        )
    }
}
