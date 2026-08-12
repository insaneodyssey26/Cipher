package com.masum.cipher.core.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        
        // Auto-Backup Keys
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        val AUTO_BACKUP_URI = stringPreferencesKey("auto_backup_uri")
        val AUTO_BACKUP_ENCRYPTED_PASSWORD = stringPreferencesKey("auto_backup_encrypted_password")
        
        // Review Keys
        val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
        val HAS_PROMPTED_REVIEW = booleanPreferencesKey("has_prompted_review")
        
        // Notifications
        val NOTIFY_ALL_TRANSACTIONS = booleanPreferencesKey("notify_all_transactions")
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
            notifyAllTransactions = preferences[Keys.NOTIFY_ALL_TRANSACTIONS] ?: false
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

    suspend fun setHasPromptedReview(prompted: Boolean) {
        context.dataStore.edit { it[Keys.HAS_PROMPTED_REVIEW] = prompted }
    }

    suspend fun setNotifyAllTransactions(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_ALL_TRANSACTIONS] = enabled }
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
    val notifyAllTransactions: Boolean = false
)
