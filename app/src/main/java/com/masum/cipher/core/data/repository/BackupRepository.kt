package com.masum.cipher.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.masum.cipher.core.data.local.AppDatabase
import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.CustomCategoryDao
import com.masum.cipher.core.data.local.dao.GoalDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.dao.TransactionSplitDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.security.BackupCrypto
import com.masum.cipher.core.security.BackupCryptoException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton

class BackupRestoreException(message: String) : Exception(message)

@Serializable
data class BackupData(
    val transactions: List<TransactionEntity>,
    val splits: List<TransactionSplitEntity> = emptyList(),
    val aliases: List<MerchantAliasEntity> = emptyList(),
    val rules: List<CategoryRuleEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val customCategories: List<CustomCategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val monthlyBudget: Double = 0.0,
    val isDynamicBudgetEnabled: Boolean? = null,
    val categoryBudgets: Map<String, Double> = emptyMap(),
    val trackedApps: Set<String> = emptySet(),
    val ignoredSubscriptions: Set<String> = emptySet(),
    val currencyCode: String? = null,
    val currencySymbol: String? = null,
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
    val notifyNewAppDetected: Boolean? = null,
    val appLanguage: String? = null,
    val proLicenseToken: String? = null,
    val proTier: String? = null,
    val proOrderId: String? = null
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val transactionDao: TransactionDao,
    private val transactionSplitDao: TransactionSplitDao,
    private val merchantAliasDao: MerchantAliasDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: SubscriptionDao,
    private val customCategoryDao: CustomCategoryDao,
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val userPreferences: UserPreferences,
    private val backupCrypto: BackupCrypto
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
                splits = transactionSplitDao.getAllSplits(),
                aliases = merchantAliasDao.getAllAliases().first(),
                rules = categoryRuleDao.getAllRules().first(),
                subscriptions = subscriptionDao.getAllSubscriptions().first(),
                customCategories = customCategoryDao.getAllCustomCategories(),
                accounts = accountDao.getAllAccounts(),
                goals = goalDao.getAllGoalsList(),
                monthlyBudget = settings.monthlyBudget,
                isDynamicBudgetEnabled = settings.isDynamicBudgetEnabled,
                categoryBudgets = settings.categoryBudgets,
                trackedApps = settings.trackedApps,
                ignoredSubscriptions = settings.ignoredSubscriptions,
                currencyCode = settings.currencyCode,
                currencySymbol = settings.currencySymbol,
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
                notifyNewAppDetected = settings.notifyNewAppDetected,
                appLanguage = settings.appLanguage,
                proLicenseToken = settings.proLicenseToken,
                proTier = settings.proTier,
                proOrderId = settings.proOrderId
            )
            val jsonString = json.encodeToString(data)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val payload = backupCrypto.encrypt(jsonBytes, password)

            outputStream.use { out ->
                out.write(payload)
                out.flush()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreData(inputStream: InputStream, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            inputStream.use { stream ->
                val bytes = stream.readBytes()
                val jsonBytes = try {
                    backupCrypto.decrypt(bytes, password)
                } catch (e: BackupCryptoException) {
                    return@withContext Result.failure(BackupRestoreException(e.message.orEmpty()))
                }
                val jsonString = String(jsonBytes, Charsets.UTF_8)
                val data = json.decodeFromString<BackupData>(jsonString)

                appDatabase.withTransaction {
                    data.transactions.forEach { transactionDao.insertTransaction(it) }
                    if (data.splits.isNotEmpty()) {
                        transactionSplitDao.insertSplits(data.splits)
                    }
                    data.aliases.forEach { merchantAliasDao.insertAlias(it) }
                    data.rules.forEach { categoryRuleDao.insertRule(it) }
                    data.subscriptions.forEach { subscriptionDao.insert(it) }
                    data.customCategories.forEach { customCategoryDao.insertCustomCategory(it) }
                    data.accounts.forEach { accountDao.insertAccount(it) }
                    data.goals.forEach { goalDao.insertGoal(it) }
                }

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
                if (!data.currencyCode.isNullOrBlank() && !data.currencySymbol.isNullOrBlank()) {
                    userPreferences.setCurrency(data.currencyCode, data.currencySymbol)
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
                data.appLanguage?.let { userPreferences.setAppLanguage(it) }
                if (!data.proLicenseToken.isNullOrBlank()) {
                    userPreferences.setProStatus(
                        isPro = true,
                        tier = data.proTier ?: "LIFETIME",
                        token = data.proLicenseToken,
                        orderId = data.proOrderId
                    )
                }
            }
            Result.success(Unit)
        } catch (e: BackupRestoreException) {
            Result.failure(e)
        } catch (e: AEADBadTagException) {
            Result.failure(BackupRestoreException("Incorrect password. Please try again."))
        } catch (e: SerializationException) {
            Result.failure(BackupRestoreException("This doesn't look like a valid Cipher backup file."))
        } catch (e: Exception) {
            Result.failure(BackupRestoreException("Restore failed. Please try again."))
        }
    }
}
