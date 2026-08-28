package com.masum.cipher.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {

    private fun getStandardSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun getLegacyEncryptedSharedPreferences(): SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
                
            EncryptedSharedPreferences.create(
                context,
                LEGACY_ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getDatabasePassphrase(): ByteArray {
        val sharedPrefs = getStandardSharedPreferences()
        val encryptedPassphraseHex = sharedPrefs.getString(KEY_DB_PASSPHRASE_V2, null)

        if (encryptedPassphraseHex != null) {
            val decryptedBase64 = keystoreManager.decrypt(encryptedPassphraseHex)
            if (decryptedBase64 != null) {
                return decodePassphrase(decryptedBase64)
            }
        }
        
        val legacyPrefs = getLegacyEncryptedSharedPreferences()
        val legacyPassphrase = legacyPrefs?.getString(LEGACY_KEY_DB_PASSPHRASE, null)
        
        if (legacyPassphrase != null) {
            val encryptedHex = keystoreManager.encrypt(legacyPassphrase)
            sharedPrefs.edit {
                putString(KEY_DB_PASSPHRASE_V2, encryptedHex)
            }
                
            legacyPrefs.edit { clear() }
            
            return decodePassphrase(legacyPassphrase)
        }

        return generateAndSaveNewPassphrase(sharedPrefs)
    }

    private fun generateAndSaveNewPassphrase(sharedPrefs: SharedPreferences): ByteArray {
        val newPassphrase = generateRandomPassphrase()
        val base64Passphrase = encodePassphrase(newPassphrase)
        
        val encryptedHex = keystoreManager.encrypt(base64Passphrase)
        
        sharedPrefs.edit {
            putString(KEY_DB_PASSPHRASE_V2, encryptedHex)
        }
            
        return newPassphrase
    }

    private fun generateRandomPassphrase(): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return bytes
    }

    private fun encodePassphrase(passphrase: ByteArray): String {
        return Base64.encodeToString(passphrase, Base64.DEFAULT)
    }

    private fun decodePassphrase(encoded: String): ByteArray {
        return Base64.decode(encoded, Base64.DEFAULT)
    }

    companion object {
        private const val PREFS_NAME = "cipher_spend_standard_prefs"
        private const val KEY_DB_PASSPHRASE_V2 = "db_passphrase_keystore_v2"
        
        private const val LEGACY_ENCRYPTED_PREFS_NAME = "cipher_spend_secure_prefs"
        private const val LEGACY_KEY_DB_PASSPHRASE = "db_passphrase"
    }
}