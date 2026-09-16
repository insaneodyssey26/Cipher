package com.masum.cipher.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

class BackupCryptoException(message: String) : Exception(message)

@Singleton
class BackupCrypto @Inject constructor() {

    companion object {
        const val HEADER_V1 = "CIPHER_VAULT_V1"
        const val HEADER_V2 = "CIPHER_VAULT_V2"
        const val LEGACY_KDF_ITERATIONS = 65536
        const val CURRENT_KDF_ITERATIONS = 600000
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val ITERATIONS_FIELD_SIZE = 4
    }

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }

        val secretKey = deriveKey(password, salt, CURRENT_KDF_ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plaintext)

        val iterationsBytes = ByteBuffer.allocate(ITERATIONS_FIELD_SIZE).putInt(CURRENT_KDF_ITERATIONS).array()
        val header = HEADER_V2.toByteArray(Charsets.UTF_8)

        return header + iterationsBytes + salt + iv + encrypted
    }

    fun decrypt(bytes: ByteArray, password: CharArray): ByteArray {
        val headerV2 = HEADER_V2.toByteArray(Charsets.UTF_8)
        val headerV1 = HEADER_V1.toByteArray(Charsets.UTF_8)
        val hasHeaderV2 = bytes.size >= headerV2.size && bytes.copyOfRange(0, headerV2.size).contentEquals(headerV2)
        val hasHeaderV1 = !hasHeaderV2 && bytes.size >= headerV1.size && bytes.copyOfRange(0, headerV1.size).contentEquals(headerV1)

        val iterations: Int
        val offset: Int
        when {
            hasHeaderV2 -> {
                if (bytes.size < headerV2.size + ITERATIONS_FIELD_SIZE) {
                    throw BackupCryptoException("This backup file appears to be corrupted or invalid.")
                }
                iterations = ByteBuffer.wrap(bytes, headerV2.size, ITERATIONS_FIELD_SIZE).int
                offset = headerV2.size + ITERATIONS_FIELD_SIZE
            }
            hasHeaderV1 -> {
                iterations = LEGACY_KDF_ITERATIONS
                offset = headerV1.size
            }
            else -> {
                iterations = LEGACY_KDF_ITERATIONS
                offset = 0
            }
        }

        if (bytes.size < offset + SALT_SIZE + IV_SIZE) {
            throw BackupCryptoException("This backup file appears to be corrupted or invalid.")
        }

        val salt = bytes.copyOfRange(offset, offset + SALT_SIZE)
        val iv = bytes.copyOfRange(offset + SALT_SIZE, offset + SALT_SIZE + IV_SIZE)
        val encryptedData = bytes.copyOfRange(offset + SALT_SIZE + IV_SIZE, bytes.size)
        val secretKey = deriveKey(password, salt, iterations)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(encryptedData)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, iterations, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
