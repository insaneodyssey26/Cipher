package com.masum.cipher.core.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
        val AUTO_LOCK_TIMEOUT = longPreferencesKey("auto_lock_timeout")
        val LAST_STOP_TIME = longPreferencesKey("last_stop_time")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val TRACKED_APPS = stringSetPreferencesKey("tracked_apps")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val HAS_SEEN_NOTIFICATION_FEATURE = booleanPreferencesKey("has_seen_notification_feature")
        val LAST_SEEN_WHATS_NEW_VERSION_CODE = intPreferencesKey("last_seen_whats_new_version_code")
        
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        val AUTO_BACKUP_URI = stringPreferencesKey("auto_backup_uri")
        val AUTO_BACKUP_ENCRYPTED_PASSWORD = stringPreferencesKey("auto_backup_encrypted_password")
        
        val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count_v2")
        val HAS_PROMPTED_REVIEW = booleanPreferencesKey("has_prompted_review_v2")
        val REVIEW_PROMPT_INTERVAL = intPreferencesKey("review_prompt_interval")
        
        val NOTIFY_ALL_TRANSACTIONS = booleanPreferencesKey("notify_all_transactions")
        val NOTIFY_BUDGET_ALERTS = booleanPreferencesKey("notify_budget_alerts")
        val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        val NOTIFY_MONTHLY_WRAPPED = booleanPreferencesKey("notify_monthly_wrapped")
        val NOTIFY_UNCATEGORIZED_REMINDER = booleanPreferencesKey("notify_uncategorized_reminder")
        val NOTIFY_SUBSCRIPTIONS = booleanPreferencesKey("notify_subscriptions")
        val NOTIFY_NEW_APP_DETECTED = booleanPreferencesKey("notify_new_app_detected")
        val IGNORED_SUBSCRIPTIONS = stringSetPreferencesKey("ignored_subscriptions")
        val CATEGORY_BUDGETS = stringPreferencesKey("category_budgets")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            theme = AppTheme.valueOf(preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name),
            isBiometricEnabled = preferences[Keys.BIOMETRIC_ENABLED] ?: false,
            isPrivacyModeEnabled = preferences[Keys.PRIVACY_MODE] ?: false,
            isHapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
            currency = preferences[Keys.PREFERRED_CURRENCY] ?: "INR",
            autoLockTimeout = preferences[Keys.AUTO_LOCK_TIMEOUT] ?: 0L,
            lastStopTime = preferences[Keys.LAST_STOP_TIME] ?: 0L,
            monthlyBudget = preferences[Keys.MONTHLY_BUDGET] ?: 0.0,
            hasCompletedOnboarding = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
            trackedApps = preferences[Keys.TRACKED_APPS] ?: emptySet(),
            accentColor = try {
                AccentColor.valueOf(preferences[Keys.ACCENT_COLOR] ?: AccentColor.INDIGO.name)
            } catch (e: Exception) {
                AccentColor.INDIGO
            },
            hasSeenNotificationFeature = preferences[Keys.HAS_SEEN_NOTIFICATION_FEATURE] ?: false,
            lastSeenWhatsNewVersionCode = preferences[Keys.LAST_SEEN_WHATS_NEW_VERSION_CODE] ?: if (preferences[Keys.HAS_SEEN_NOTIFICATION_FEATURE] == true) 9 else 0,
            autoBackupEnabled = preferences[Keys.AUTO_BACKUP_ENABLED] ?: false,
            autoBackupFrequency = try {
                AutoBackupFrequency.valueOf(preferences[Keys.AUTO_BACKUP_FREQUENCY] ?: AutoBackupFrequency.NEVER.name)
            } catch (e: Exception) {
                AutoBackupFrequency.NEVER
            },
            autoBackupUri = preferences[Keys.AUTO_BACKUP_URI],
            autoBackupEncryptedPassword = preferences[Keys.AUTO_BACKUP_ENCRYPTED_PASSWORD],
            appLaunchCount = preferences[Keys.APP_LAUNCH_COUNT] ?: 0,
            hasPromptedReview = preferences[Keys.HAS_PROMPTED_REVIEW] ?: false,
            reviewPromptInterval = preferences[Keys.REVIEW_PROMPT_INTERVAL] ?: 10,
            notifyAllTransactions = preferences[Keys.NOTIFY_ALL_TRANSACTIONS] ?: true,
            notifyBudgetAlerts = preferences[Keys.NOTIFY_BUDGET_ALERTS] ?: true,
            notifyDailySummary = preferences[Keys.NOTIFY_DAILY_SUMMARY] ?: true,
            notifyMonthlyWrapped = preferences[Keys.NOTIFY_MONTHLY_WRAPPED] ?: true,
            notifyUncategorizedReminder = preferences[Keys.NOTIFY_UNCATEGORIZED_REMINDER] ?: true,
            notifySubscriptions = preferences[Keys.NOTIFY_SUBSCRIPTIONS] ?: true,
            notifyNewAppDetected = preferences[Keys.NOTIFY_NEW_APP_DETECTED] ?: true,
            ignoredSubscriptions = preferences[Keys.IGNORED_SUBSCRIPTIONS] ?: emptySet(),
            categoryBudgets = preferences[Keys.CATEGORY_BUDGETS]?.let { jsonStr ->
                try {
                    val json = org.json.JSONObject(jsonStr)
                    val map = mutableMapOf<String, Double>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        map[k] = json.getDouble(k)
                    }
                    map
                } catch (e: Exception) {
                    emptyMap()
                }
            } ?: emptyMap()
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setPrivacyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_MODE] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[Keys.PREFERRED_CURRENCY] = currency }
    }

    suspend fun setAutoLockTimeout(timeoutMillis: Long) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT] = timeoutMillis }
    }

    suspend fun setLastStopTime(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_STOP_TIME] = timestamp }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = amount }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setTrackedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.TRACKED_APPS] = apps }
    }
    suspend fun setAccentColor(accentColor: AccentColor) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = accentColor.name }
    }

    suspend fun setHasSeenNotificationFeature(seen: Boolean) {
        context.dataStore.edit { it[Keys.HAS_SEEN_NOTIFICATION_FEATURE] = seen }
    }

    suspend fun setLastSeenWhatsNewVersionCode(versionCode: Int) {
        context.dataStore.edit { it[Keys.LAST_SEEN_WHATS_NEW_VERSION_CODE] = versionCode }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = enabled }
    }

    suspend fun setAutoBackupFrequency(frequency: AutoBackupFrequency) {
        context.dataStore.edit { it[Keys.AUTO_BACKUP_FREQUENCY] = frequency.name }
    }

    suspend fun setAutoBackupUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[Keys.AUTO_BACKUP_URI] = uri
            } else {
                preferences.remove(Keys.AUTO_BACKUP_URI)
            }
        }
    }

    suspend fun setAutoBackupEncryptedPassword(password: String?) {
        context.dataStore.edit { preferences ->
            if (password != null) {
                preferences[Keys.AUTO_BACKUP_ENCRYPTED_PASSWORD] = password
            } else {
                preferences.remove(Keys.AUTO_BACKUP_ENCRYPTED_PASSWORD)
            }
        }
    }

    suspend fun incrementAppLaunchCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.APP_LAUNCH_COUNT] ?: 0
            preferences[Keys.APP_LAUNCH_COUNT] = current + 1
        }
    }

    suspend fun resetAppLaunchCount() {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_LAUNCH_COUNT] = 0
        }
    }

    suspend fun increaseReviewPromptInterval() {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.REVIEW_PROMPT_INTERVAL] ?: 10
            preferences[Keys.REVIEW_PROMPT_INTERVAL] = minOf(20, current + 5)
        }
    }

    suspend fun setHasPromptedReview(prompted: Boolean) {
        context.dataStore.edit { it[Keys.HAS_PROMPTED_REVIEW] = prompted }
    }

    suspend fun setNotifyAllTransactions(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_ALL_TRANSACTIONS] = enabled }
    }

    suspend fun setNotifyBudgetAlerts(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_BUDGET_ALERTS] = enabled }
    }

    suspend fun setNotifyDailySummary(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_DAILY_SUMMARY] = enabled }
    }

    suspend fun setNotifyMonthlyWrapped(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_MONTHLY_WRAPPED] = enabled }
    }

    suspend fun setNotifyUncategorizedReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_UNCATEGORIZED_REMINDER] = enabled }
    }

    suspend fun setNotifySubscriptions(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_SUBSCRIPTIONS] = enabled }
    }

    suspend fun setNotifyNewAppDetected(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_NEW_APP_DETECTED] = enabled }
    }

    suspend fun addIgnoredSubscription(merchant: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.IGNORED_SUBSCRIPTIONS] ?: emptySet()
            prefs[Keys.IGNORED_SUBSCRIPTIONS] = current + merchant
        }
    }

    suspend fun setCategoryBudget(category: String, limit: Double) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[Keys.CATEGORY_BUDGETS]?.let {
                try { org.json.JSONObject(it) } catch (e: Exception) { org.json.JSONObject() }
            } ?: org.json.JSONObject()
            if (limit > 0) {
                currentJson.put(category, limit)
            } else {
                currentJson.remove(category)
            }
            preferences[Keys.CATEGORY_BUDGETS] = currentJson.toString()
        }
    }
}

enum class AccentColor(val colorValue: Long, val colorName: String) {
    INDIGO(0xFF6366F1, "Electric Indigo"),
    MINT(0xFF10B981, "Midnight Mint"),
    CHERRY(0xFFF43F5E, "Cherry Blossom"),
    AMBER(0xFFF59E0B, "Amber Vault"),
    CYAN(0xFF06B6D4, "Ocean Cyan"),
    VIOLET(0xFF8B5CF6, "Royal Violet"),
    ROSE(0xFFE11D48, "Crimson Rose"),
    OCEAN(0xFF0EA5E9, "Pacific Blue"),
    SAGE(0xFF84CC16, "Spring Sage"),
    CORAL(0xFFF97316, "Sunset Coral")
}

enum class AutoBackupFrequency(val label: String) {
    NEVER("Never"),
    EVERY_CHANGE("After every change"),
    DAILY("Daily"),
    WEEKLY("Weekly")
}

data class UserSettings(
    val theme: AppTheme,
    val isBiometricEnabled: Boolean,
    val isPrivacyModeEnabled: Boolean,
    val isHapticsEnabled: Boolean,
    val currency: String,
    val autoLockTimeout: Long,
    val lastStopTime: Long,
    val monthlyBudget: Double,
    val hasCompletedOnboarding: Boolean = false,
    val trackedApps: Set<String> = emptySet(),
    val accentColor: AccentColor = AccentColor.INDIGO,
    val hasSeenNotificationFeature: Boolean = false,
    val lastSeenWhatsNewVersionCode: Int = 0,
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: AutoBackupFrequency = AutoBackupFrequency.NEVER,
    val autoBackupUri: String? = null,
    val autoBackupEncryptedPassword: String? = null,
    val appLaunchCount: Int = 0,
    val hasPromptedReview: Boolean = false,
    val reviewPromptInterval: Int = 10,
    val notifyAllTransactions: Boolean = true,
    val notifyBudgetAlerts: Boolean = true,
    val notifyDailySummary: Boolean = true,
    val notifyMonthlyWrapped: Boolean = true,
    val notifyUncategorizedReminder: Boolean = true,
    val notifySubscriptions: Boolean = true,
    val notifyNewAppDetected: Boolean = true,
    val ignoredSubscriptions: Set<String> = emptySet(),
    val categoryBudgets: Map<String, Double> = emptyMap()
)
