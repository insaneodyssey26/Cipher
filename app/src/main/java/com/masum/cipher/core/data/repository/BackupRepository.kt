package com.masum.cipher.core.data.repository

import android.content.Context
import android.net.Uri
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
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
    val aliases: List<MerchantAliasEntity> = emptyList(),
    val rules: List<CategoryRuleEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val monthlyBudget: Double = 0.0,
    val isDynamicBudgetEnabled: Boolean? = null,
    val categoryBudgets: Map<String, Double> = emptyMap(),
    val trackedApps: Set<String> = emptySet(),
    val ignoredSubscriptions: Set<String> = emptySet(),
    val theme: String? = null,
    val accentColor: String? = null,
    val isBiometricEnabled: Boolean? = null,
    val isPrivacyModeEnabled: Boolean? = null,
    val isHapticsEnabled: Boolean? = null,
    val autoLockTimeout: Long? = null,
    val notifyAllTransactions: Boolean? = null,
    val notifyBudgetAlerts: Boolean? = null,
    val notifyDailySummary: Boolean? = null,
    val notifyMonthlyWrapped: Boolean? = null,
    val notifyUncategorizedReminder: Boolean? = null,
    val notifySubscriptions: Boolean? = null,
    val notifyNewAppDetected: Boolean? = null
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: SubscriptionDao,
    private val userPreferences: UserPreferences
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
            val settings = userPreferences.settingsFlow.first()
            val data = BackupData(
                transactions = transactionDao.getAllTransactions().first(),
                aliases = merchantAliasDao.getAllAliases().first(),
                rules = categoryRuleDao.getAllRules().first(),
                subscriptions = subscriptionDao.getAllSubscriptions().first(),
                monthlyBudget = settings.monthlyBudget,
                isDynamicBudgetEnabled = settings.isDynamicBudgetEnabled,
                categoryBudgets = settings.categoryBudgets,
                trackedApps = settings.trackedApps,
                ignoredSubscriptions = settings.ignoredSubscriptions,
                theme = settings.theme.name,
                accentColor = settings.accentColor.name,
                isBiometricEnabled = settings.isBiometricEnabled,
                isPrivacyModeEnabled = settings.isPrivacyModeEnabled,
                isHapticsEnabled = settings.isHapticsEnabled,
                autoLockTimeout = settings.autoLockTimeout,
                notifyAllTransactions = settings.notifyAllTransactions,
                notifyBudgetAlerts = settings.notifyBudgetAlerts,
                notifyDailySummary = settings.notifyDailySummary,
                notifyMonthlyWrapped = settings.notifyMonthlyWrapped,
                notifyUncategorizedReminder = settings.notifyUncategorizedReminder,
                notifySubscriptions = settings.notifySubscriptions,
                notifyNewAppDetected = settings.notifyNewAppDetected
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
                
                data.transactions.forEach { transactionDao.insertTransaction(it) }
                data.aliases.forEach { merchantAliasDao.insertAlias(it) }
                data.rules.forEach { categoryRuleDao.insertRule(it) }
                data.subscriptions.forEach { subscriptionDao.insert(it) }

                if (data.monthlyBudget > 0) {
                    userPreferences.setMonthlyBudget(data.monthlyBudget)
                }
                data.isDynamicBudgetEnabled?.let {
                    userPreferences.setDynamicBudgetEnabled(it)
                }
                if (data.categoryBudgets.isNotEmpty()) {
                    userPreferences.setCategoryBudgets(data.categoryBudgets)
                }
                if (data.trackedApps.isNotEmpty()) {
                    userPreferences.setTrackedApps(data.trackedApps)
                }
                if (data.ignoredSubscriptions.isNotEmpty()) {
                    userPreferences.setIgnoredSubscriptions(data.ignoredSubscriptions)
                }

                data.theme?.let {
                    try { userPreferences.setTheme(com.masum.cipher.core.data.local.pref.AppTheme.valueOf(it)) } catch (_: Exception) {}
                }
                data.accentColor?.let {
                    try { userPreferences.setAccentColor(com.masum.cipher.core.data.local.pref.AccentColor.valueOf(it)) } catch (_: Exception) {}
                }
                data.isBiometricEnabled?.let { userPreferences.setBiometricEnabled(it) }
                data.isPrivacyModeEnabled?.let { userPreferences.setPrivacyModeEnabled(it) }
                data.isHapticsEnabled?.let { userPreferences.setHapticsEnabled(it) }
                data.autoLockTimeout?.let { userPreferences.setAutoLockTimeout(it) }
                data.notifyAllTransactions?.let { userPreferences.setNotifyAllTransactions(it) }
                data.notifyBudgetAlerts?.let { userPreferences.setNotifyBudgetAlerts(it) }
                data.notifyDailySummary?.let { userPreferences.setNotifyDailySummary(it) }
                data.notifyMonthlyWrapped?.let { userPreferences.setNotifyMonthlyWrapped(it) }
                data.notifyUncategorizedReminder?.let { userPreferences.setNotifyUncategorizedReminder(it) }
                data.notifySubscriptions?.let { userPreferences.setNotifySubscriptions(it) }
                data.notifyNewAppDetected?.let { userPreferences.setNotifyNewAppDetected(it) }
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
