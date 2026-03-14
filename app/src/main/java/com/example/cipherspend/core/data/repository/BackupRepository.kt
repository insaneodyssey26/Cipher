package com.example.cipherspend.core.data.repository

import android.content.Context
import android.net.Uri
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val transactions: List<TransactionEntity>,
    val aliases: List<MerchantAliasEntity>
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun provideOutputStream(uri: Uri): OutputStream? {
        return context.contentResolver.openOutputStream(uri)
    }

    fun provideInputStream(uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    suspend fun exportData(outputStream: OutputStream, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = BackupData(
                transactions = transactionDao.getAllTransactions().first(),
                aliases = merchantAliasDao.getAllAliases().first()
            )
            val jsonString = json.encodeToString(data)
            
            val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
            
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
            
            val encryptedData = cipher.doFinal(jsonString.toByteArray())
            
            outputStream.use { os ->
                os.write(salt)
                os.write(iv)
                os.write(encryptedData)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(inputStream: InputStream, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            inputStream.use { isStream ->
                val salt = ByteArray(16)
                if (isStream.read(salt) != 16) return@withContext Result.failure(Exception("Invalid backup file: Salt missing"))
                
                val iv = ByteArray(12)
                if (isStream.read(iv) != 12) return@withContext Result.failure(Exception("Invalid backup file: IV missing"))
                
                val encryptedData = isStream.readBytes()
                
                val key = deriveKey(password, salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
                
                val jsonBytes = cipher.doFinal(encryptedData)
                val jsonString = String(jsonBytes)
                val data = json.decodeFromString<BackupData>(jsonString)
                
                // Using transaction for atomic import if possible, but here we just iterate
                data.transactions.forEach { transactionDao.insertTransaction(it) }
                data.aliases.forEach { merchantAliasDao.insertAlias(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
