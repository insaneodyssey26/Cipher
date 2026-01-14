package com.example.cipherspend.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun createMasterKey(): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private fun createSharedPreferences(): EncryptedSharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            createMasterKey(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun getDatabasePassphrase(): ByteArray {
        val sharedPrefs = createSharedPreferences()
        val existingPassphrase = sharedPrefs.getString(KEY_DB_PASSPHRASE, null)
        return if (existingPassphrase != null) {
            decodePassphrase(existingPassphrase)
        } else {
            val newPassphrase = generateRandomPassphrase()
            sharedPrefs.edit()
                .putString(KEY_DB_PASSPHRASE, encodePassphrase(newPassphrase))
                .apply()
            newPassphrase
        }
    }

    private fun generateRandomPassphrase(): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return bytes
    }

    private fun encodePassphrase(passphrase: ByteArray): String {
        return android.util.Base64.encodeToString(passphrase, android.util.Base64.DEFAULT)
    }

    private fun decodePassphrase(encoded: String): ByteArray {
        return android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "cipher_spend_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }
}