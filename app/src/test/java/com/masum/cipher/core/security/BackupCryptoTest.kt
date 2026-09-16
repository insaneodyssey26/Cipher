package com.masum.cipher.core.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupCryptoTest {

    private lateinit var crypto: BackupCrypto

    @Before
    fun setup() {
        crypto = BackupCrypto()
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val tmp = factory.generateSecret(PBEKeySpec(password, salt, iterations, 256))
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /** Builds a backup file in an older format (V1 header, or no header at all), to simulate a real legacy backup. */
    private fun buildLegacyBackup(plaintext: ByteArray, password: CharArray, header: String?): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val secretKey = deriveKey(password, salt, BackupCrypto.LEGACY_KDF_ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plaintext)
        val headerBytes = header?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
        return headerBytes + salt + iv + encrypted
    }

    @Test
    fun encryptThenDecryptRoundTripsToOriginalPlaintext() {
        val plaintext = "{\"transactions\":[]}".toByteArray(Charsets.UTF_8)
        val password = "correct horse battery staple".toCharArray()

        val payload = crypto.encrypt(plaintext, password)
        val decrypted = crypto.decrypt(payload, password)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun encryptedPayloadStartsWithCurrentHeader() {
        val payload = crypto.encrypt("data".toByteArray(), "pw".toCharArray())
        val header = String(payload, 0, BackupCrypto.HEADER_V2.length, Charsets.UTF_8)

        assertTrue(header == BackupCrypto.HEADER_V2)
    }

    @Test(expected = AEADBadTagException::class)
    fun decryptingWithWrongPasswordThrowsAuthenticationFailure() {
        val payload = crypto.encrypt("secret data".toByteArray(), "correct-password".toCharArray())

        crypto.decrypt(payload, "wrong-password".toCharArray())
    }

    @Test(expected = AEADBadTagException::class)
    fun tamperedCiphertextFailsIntegrityCheckEvenWithCorrectPassword() {
        val password = "correct-password".toCharArray()
        val payload = crypto.encrypt("secret data".toByteArray(), password)
        payload[payload.size - 1] = (payload[payload.size - 1] + 1).toByte()

        crypto.decrypt(payload, password)
    }

    @Test
    fun legacyV1HeaderBackupIsReadableAtOriginalIterationCount() {
        val plaintext = "legacy backup content".toByteArray(Charsets.UTF_8)
        val password = "old-password".toCharArray()
        val legacyPayload = buildLegacyBackup(plaintext, password, BackupCrypto.HEADER_V1)

        val decrypted = crypto.decrypt(legacyPayload, password)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun preHeaderBackupWithNoHeaderAtAllIsStillReadable() {
        val plaintext = "very old backup, before headers existed".toByteArray(Charsets.UTF_8)
        val password = "ancient-password".toCharArray()
        val noHeaderPayload = buildLegacyBackup(plaintext, password, header = null)

        val decrypted = crypto.decrypt(noHeaderPayload, password)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun truncatedV2HeaderWithoutIterationCountThrowsCorruptedFileError() {
        val truncated = BackupCrypto.HEADER_V2.toByteArray(Charsets.UTF_8) + byteArrayOf(1, 2)

        try {
            crypto.decrypt(truncated, "any".toCharArray())
            fail("expected BackupCryptoException")
        } catch (e: BackupCryptoException) {
            assertTrue(e.message!!.contains("corrupted", ignoreCase = true))
        }
    }

    @Test
    fun tooShortToContainSaltAndIvThrowsCorruptedFileError() {
        val tooShort = BackupCrypto.HEADER_V2.toByteArray(Charsets.UTF_8) +
            byteArrayOf(0, 0, 0, 1) + // iterations field
            ByteArray(10) // way less than salt(16)+iv(12)

        try {
            crypto.decrypt(tooShort, "any".toCharArray())
            fail("expected BackupCryptoException")
        } catch (e: BackupCryptoException) {
            assertTrue(e.message!!.contains("corrupted", ignoreCase = true))
        }
    }

    @Test
    fun emptyByteArrayThrowsCorruptedFileError() {
        try {
            crypto.decrypt(ByteArray(0), "any".toCharArray())
            fail("expected BackupCryptoException")
        } catch (e: BackupCryptoException) {
            assertTrue(e.message!!.contains("corrupted", ignoreCase = true))
        }
    }

    @Test
    fun eachEncryptionUsesFreshRandomSaltAndIv() {
        val plaintext = "same content every time".toByteArray()
        val password = "same-password".toCharArray()

        val first = crypto.encrypt(plaintext, password)
        val second = crypto.encrypt(plaintext, password)

        assertFalse("two encryptions of the same data should not produce identical ciphertext", first.contentEquals(second))
    }

    @Test
    fun encryptedFileCarriesTheCurrentIterationCount() {
        val payload = crypto.encrypt("data".toByteArray(), "pw".toCharArray())
        val iterationsOffset = BackupCrypto.HEADER_V2.length
        val iterations = java.nio.ByteBuffer.wrap(payload, iterationsOffset, 4).int

        assertTrue(iterations == BackupCrypto.CURRENT_KDF_ITERATIONS)
    }
}
