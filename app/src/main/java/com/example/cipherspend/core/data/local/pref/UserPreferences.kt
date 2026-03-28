package com.example.cipherspend.core.data.local.pref

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
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            theme = AppTheme.valueOf(preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name),
            isBiometricEnabled = preferences[Keys.BIOMETRIC_ENABLED] ?: true,
            isPrivacyModeEnabled = preferences[Keys.PRIVACY_MODE] ?: false,
            isHapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
            currency = preferences[Keys.PREFERRED_CURRENCY] ?: "INR",
            autoLockTimeout = preferences[Keys.AUTO_LOCK_TIMEOUT] ?: 0L,
            lastStopTime = preferences[Keys.LAST_STOP_TIME] ?: 0L,
            monthlyBudget = preferences[Keys.MONTHLY_BUDGET] ?: 0.0
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
}

data class UserSettings(
    val theme: AppTheme,
    val isBiometricEnabled: Boolean,
    val isPrivacyModeEnabled: Boolean,
    val isHapticsEnabled: Boolean,
    val currency: String,
    val autoLockTimeout: Long,
    val lastStopTime: Long,
    val monthlyBudget: Double
)
