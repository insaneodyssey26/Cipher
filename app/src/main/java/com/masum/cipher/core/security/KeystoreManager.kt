package com.masum.cipher.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS_AUTO_BACKUP = "cipher_auto_backup_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(ALIAS_AUTO_BACKUP)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                ALIAS_AUTO_BACKUP,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
