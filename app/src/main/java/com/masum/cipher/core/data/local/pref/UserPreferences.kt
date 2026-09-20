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
    private val syncPrefs = context.getSharedPreferences("sync_currency_cache", Context.MODE_PRIVATE)

    fun getCachedCurrencyCode(): String {
        val default = com.masum.cipher.core.domain.model.AppCurrency.detectDefault()
        return syncPrefs.getString("cached_currency_code", default.code) ?: default.code
    }

    fun getCachedCurrencySymbol(): String {
        val default = com.masum.cipher.core.domain.model.AppCurrency.detectDefault()
        return syncPrefs.getString("cached_currency_symbol", default.symbol) ?: default.symbol
    }

    fun isCachedCurrencySuffix(): Boolean {
        return syncPrefs.getBoolean("cached_currency_is_suffix", false)
    }

    fun isCachedCurrencyHasSpace(): Boolean {
        return syncPrefs.getBoolean("cached_currency_has_space", false)
    }

    fun getCachedLanguageCode(): String {
        return syncPrefs.getString("cached_app_language", "system") ?: "system"
    }

    fun getCachedAccentColor(): AccentColor {
        val name = syncPrefs.getString("cached_accent_color", AccentColor.INDIGO.name) ?: AccentColor.INDIGO.name
        return try {
            AccentColor.valueOf(name)
        } catch (_: Exception) {
            AccentColor.INDIGO
        }
    }

    fun isCachedNavBarCompressed(): Boolean {
        return syncPrefs.getBoolean("cached_navbar_compressed", false)
    }

    fun isCachedOnboardingCompleted(): Boolean {
        return syncPrefs.getBoolean("cached_onboarding_completed", false)
    }

    fun isCachedPro(): Boolean {
        val isFlagged = syncPrefs.getBoolean("cached_is_pro", false)
        if (!isFlagged) return false
        val token = syncPrefs.getString("cached_license_token", null) ?: return false
        val validation = com.masum.cipher.core.security.LicenseEngine().validateLicense(token)
        if (!validation.isValid) {
            syncPrefs.edit().putBoolean("cached_is_pro", false).putString("cached_pro_tier", "FREE").apply()
            return false
        }
        val expiresAt = syncPrefs.getLong("cached_pro_expiry", 0L)
        if (expiresAt > 0L && System.currentTimeMillis() > expiresAt) {
            syncPrefs.edit().putBoolean("cached_is_pro", false).putString("cached_pro_tier", "FREE").apply()
            return false
        }
        return true
    }

    fun getCachedProExpiresAt(): Long {
        if (!isCachedPro()) return 0L
        return syncPrefs.getLong("cached_pro_expiry", 0L)
    }

    fun getCachedProTier(): String {
        if (!isCachedPro()) return "FREE"
        return syncPrefs.getString("cached_pro_tier", "FREE") ?: "FREE"
    }

    fun getCachedLicenseToken(): String? {
        if (!isCachedPro()) return null
        return syncPrefs.getString("cached_license_token", null)
    }

    fun getCachedAppTheme(): AppTheme {
        val name = syncPrefs.getString("cached_app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(name)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun getCachedSettings(): UserSettings {
        val curCode = getCachedCurrencyCode()
        val curSymbol = getCachedCurrencySymbol()
        val isSuffix = isCachedCurrencySuffix()
        val hasSpace = isCachedCurrencyHasSpace()
        val langCode = getCachedLanguageCode()
        val isPro = isCachedPro()
        val proTier = getCachedProTier()
        val appTheme = getCachedAppTheme()
        com.masum.cipher.core.util.AppFormatters.setActiveCurrencyFormatting(isSuffix, hasSpace)
        return UserSettings(
            theme = appTheme,
            isBiometricEnabled = false,
            isPrivacyModeEnabled = false,
            isHapticsEnabled = true,
            currency = curCode,
            currencyCode = curCode,
            currencySymbol = curSymbol,
            isCurrencySuffix = isSuffix,
            hasCurrencySpace = hasSpace,
            appLanguage = langCode,
            autoLockTimeout = 0L,
            lastStopTime = 0L,
            monthlyBudget = 0.0,
            hasCompletedOnboarding = isCachedOnboardingCompleted(),
            accentColor = getCachedAccentColor(),
            isNavBarCompressed = isCachedNavBarCompressed(),
            isPro = isPro,
            proTier = proTier,
            proExpiresAtEpochMs = getCachedProExpiresAt()
        )
    }

    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
        val PREFERRED_CURRENCY_CODE = stringPreferencesKey("preferred_currency_code")
        val PREFERRED_CURRENCY_SYMBOL = stringPreferencesKey("preferred_currency_symbol")
        val PREFERRED_CURRENCY_IS_SUFFIX = booleanPreferencesKey("preferred_currency_is_suffix")
        val PREFERRED_CURRENCY_HAS_SPACE = booleanPreferencesKey("preferred_currency_has_space")
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
        val IS_DYNAMIC_BUDGET_ENABLED = booleanPreferencesKey("is_dynamic_budget_enabled")
        val NAVBAR_COMPRESSED = booleanPreferencesKey("navbar_compressed")
        val CUSTOM_CURRENCIES = stringPreferencesKey("custom_currencies")
        val PRO_ACTIVATED = booleanPreferencesKey("pro_activated")
        val PRO_TIER = stringPreferencesKey("pro_tier")
        val PRO_LICENSE_TOKEN = stringPreferencesKey("pro_license_token")
        val PRO_ORDER_ID = stringPreferencesKey("pro_order_id")
        val PRO_EXPIRES_AT = longPreferencesKey("pro_expires_at")
        val SHOW_PRO_BADGE = booleanPreferencesKey("show_pro_badge")
        val LAST_LICENSE_SYNC_TIME = longPreferencesKey("last_license_sync_time")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val defaultCurrency = com.masum.cipher.core.domain.model.AppCurrency.detectDefault()
        val curCode = preferences[Keys.PREFERRED_CURRENCY_CODE] ?: preferences[Keys.PREFERRED_CURRENCY] ?: defaultCurrency.code
        val curSymbol = preferences[Keys.PREFERRED_CURRENCY_SYMBOL] ?: com.masum.cipher.core.domain.model.AppCurrency.fromCode(curCode).symbol
        val isSuffix = preferences[Keys.PREFERRED_CURRENCY_IS_SUFFIX] ?: isCachedCurrencySuffix()
        val hasSpace = preferences[Keys.PREFERRED_CURRENCY_HAS_SPACE] ?: isCachedCurrencyHasSpace()

        com.masum.cipher.core.util.AppFormatters.setActiveCurrencyFormatting(isSuffix, hasSpace)

        val hasOnboarded = preferences[Keys.ONBOARDING_COMPLETED] ?: false
        val parsedAccentColor = try {
            AccentColor.valueOf(preferences[Keys.ACCENT_COLOR] ?: AccentColor.INDIGO.name)
        } catch (_: Exception) {
            AccentColor.INDIGO
        }
        val isNavCompressed = preferences[Keys.NAVBAR_COMPRESSED] ?: false

        val parsedTheme = try {
            AppTheme.valueOf(preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }

        syncPrefs.edit()
            .putString("cached_currency_code", curCode)
            .putString("cached_currency_symbol", curSymbol)
            .putBoolean("cached_currency_is_suffix", isSuffix)
            .putBoolean("cached_currency_has_space", hasSpace)
            .putBoolean("cached_onboarding_completed", hasOnboarded)
            .putString("cached_accent_color", parsedAccentColor.name)
            .putBoolean("cached_navbar_compressed", isNavCompressed)
            .putString("cached_app_theme", parsedTheme.name)
            .apply()

        UserSettings(
            theme = parsedTheme,
            isBiometricEnabled = preferences[Keys.BIOMETRIC_ENABLED] ?: false,
            isPrivacyModeEnabled = preferences[Keys.PRIVACY_MODE] ?: false,
            isHapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
            currency = curCode,
            currencyCode = curCode,
            currencySymbol = curSymbol,
            isCurrencySuffix = isSuffix,
            hasCurrencySpace = hasSpace,
            appLanguage = preferences[Keys.APP_LANGUAGE] ?: getCachedLanguageCode(),
            autoLockTimeout = preferences[Keys.AUTO_LOCK_TIMEOUT] ?: 0L,
            lastStopTime = preferences[Keys.LAST_STOP_TIME] ?: 0L,
            monthlyBudget = preferences[Keys.MONTHLY_BUDGET] ?: 0.0,
            isDynamicBudgetEnabled = preferences[Keys.IS_DYNAMIC_BUDGET_ENABLED] ?: false,
            hasCompletedOnboarding = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
            trackedApps = preferences[Keys.TRACKED_APPS] ?: emptySet(),
            accentColor = parsedAccentColor,
            hasSeenNotificationFeature = preferences[Keys.HAS_SEEN_NOTIFICATION_FEATURE] ?: false,
            lastSeenWhatsNewVersionCode = preferences[Keys.LAST_SEEN_WHATS_NEW_VERSION_CODE] ?: if (preferences[Keys.HAS_SEEN_NOTIFICATION_FEATURE] == true) 9 else 0,
            autoBackupEnabled = preferences[Keys.AUTO_BACKUP_ENABLED] ?: false,
            autoBackupFrequency = try {
                AutoBackupFrequency.valueOf(preferences[Keys.AUTO_BACKUP_FREQUENCY] ?: AutoBackupFrequency.NEVER.name)
            } catch (_: Exception) {
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
                } catch (_: Exception) {
                    emptyMap()
                }
            } ?: emptyMap(),
            customCurrencies = preferences[Keys.CUSTOM_CURRENCIES]?.let { jsonStr ->
                try {
                    val arr = org.json.JSONArray(jsonStr)
                    val list = mutableListOf<com.masum.cipher.core.domain.model.AppCurrency>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            com.masum.cipher.core.domain.model.AppCurrency(
                                code = obj.getString("code"),
                                symbol = obj.getString("symbol"),
                                name = obj.getString("name"),
                                countryCode = obj.optString("countryCode", "CUSTOM"),
                                isSuffix = obj.optBoolean("isSuffix", false),
                                hasSpace = obj.optBoolean("hasSpace", false)
                            )
                        )
                    }
                    list
                } catch (_: Exception) {
                    emptyList()
                }
            } ?: emptyList(),
            isNavBarCompressed = isNavCompressed,
            isPro = run {
                val flagged = preferences[Keys.PRO_ACTIVATED] ?: false
                if (!flagged) false
                else {
                    val token = preferences[Keys.PRO_LICENSE_TOKEN]
                    if (token.isNullOrBlank()) false
                    else {
                        val validation = com.masum.cipher.core.security.LicenseEngine().validateLicense(token)
                        if (!validation.isValid) false
                        else {
                            val expiresAt = preferences[Keys.PRO_EXPIRES_AT] ?: 0L
                            expiresAt == 0L || System.currentTimeMillis() <= expiresAt
                        }
                    }
                }
            },
            proTier = run {
                val token = preferences[Keys.PRO_LICENSE_TOKEN]
                if (token.isNullOrBlank()) "FREE"
                else {
                    val res = com.masum.cipher.core.security.LicenseEngine().validateLicense(token)
                    val expiresAt = preferences[Keys.PRO_EXPIRES_AT] ?: 0L
                    val isExpired = expiresAt > 0L && System.currentTimeMillis() > expiresAt
                    if (res.isValid && !isExpired) res.tier.identifier else "FREE"
                }
            },
            proLicenseToken = preferences[Keys.PRO_LICENSE_TOKEN],
            proOrderId = preferences[Keys.PRO_ORDER_ID],
            proExpiresAtEpochMs = preferences[Keys.PRO_EXPIRES_AT] ?: 0L,
            showProBadge = preferences[Keys.SHOW_PRO_BADGE] ?: true
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        syncPrefs.edit().putString("cached_app_theme", theme.name).apply()
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

    suspend fun setAutoLockTimeout(timeoutMillis: Long) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT] = timeoutMillis }
    }

    suspend fun setLastStopTime(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_STOP_TIME] = timestamp }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = amount }
    }

    suspend fun setDynamicBudgetEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DYNAMIC_BUDGET_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        syncPrefs.edit().putBoolean("cached_onboarding_completed", completed).apply()
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setTrackedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.TRACKED_APPS] = apps }
    }

    suspend fun setAccentColor(accentColor: AccentColor) {
        syncPrefs.edit().putString("cached_accent_color", accentColor.name).apply()
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = accentColor.name }
    }

    suspend fun setNavBarCompressed(compressed: Boolean) {
        syncPrefs.edit().putBoolean("cached_navbar_compressed", compressed).apply()
        context.dataStore.edit { it[Keys.NAVBAR_COMPRESSED] = compressed }
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
                try { org.json.JSONObject(it) } catch (_: Exception) { org.json.JSONObject() }
            } ?: org.json.JSONObject()
            if (limit > 0) {
                currentJson.put(category, limit)
            } else {
                currentJson.remove(category)
            }
            preferences[Keys.CATEGORY_BUDGETS] = currentJson.toString()
        }
    }

    suspend fun setCategoryBudgets(budgets: Map<String, Double>) {
        context.dataStore.edit { preferences ->
            val json = org.json.JSONObject()
            budgets.forEach { (cat, limit) ->
                if (limit > 0) json.put(cat, limit)
            }
            preferences[Keys.CATEGORY_BUDGETS] = json.toString()
        }
    }

    suspend fun setIgnoredSubscriptions(ignored: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IGNORED_SUBSCRIPTIONS] = ignored
        }
    }

    suspend fun setCurrency(code: String, symbol: String, isSuffix: Boolean = false, hasSpace: Boolean = false) {
        com.masum.cipher.core.util.AppFormatters.setActiveCurrencyFormatting(isSuffix, hasSpace)
        syncPrefs.edit()
            .putString("cached_currency_code", code)
            .putString("cached_currency_symbol", symbol)
            .putBoolean("cached_currency_is_suffix", isSuffix)
            .putBoolean("cached_currency_has_space", hasSpace)
            .apply()
        context.dataStore.edit { preferences ->
            preferences[Keys.PREFERRED_CURRENCY] = code
            preferences[Keys.PREFERRED_CURRENCY_CODE] = code
            preferences[Keys.PREFERRED_CURRENCY_SYMBOL] = symbol
            preferences[Keys.PREFERRED_CURRENCY_IS_SUFFIX] = isSuffix
            preferences[Keys.PREFERRED_CURRENCY_HAS_SPACE] = hasSpace
        }
    }

    suspend fun addCustomCurrency(currency: com.masum.cipher.core.domain.model.AppCurrency) {
        context.dataStore.edit { preferences ->
            val currentList = preferences[Keys.CUSTOM_CURRENCIES]?.let { jsonStr ->
                try {
                    val arr = org.json.JSONArray(jsonStr)
                    val list = mutableListOf<com.masum.cipher.core.domain.model.AppCurrency>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            com.masum.cipher.core.domain.model.AppCurrency(
                                code = obj.getString("code"),
                                symbol = obj.getString("symbol"),
                                name = obj.getString("name"),
                                countryCode = obj.optString("countryCode", "CUSTOM"),
                                isSuffix = obj.optBoolean("isSuffix", false),
                                hasSpace = obj.optBoolean("hasSpace", false)
                            )
                        )
                    }
                    list
                } catch (_: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            val filtered = currentList.filterNot { it.code.equals(currency.code, ignoreCase = true) }
            val updated = filtered + currency
            val jsonArr = org.json.JSONArray()
            updated.forEach { cur ->
                val obj = org.json.JSONObject()
                obj.put("code", cur.code)
                obj.put("symbol", cur.symbol)
                obj.put("name", cur.name)
                obj.put("countryCode", cur.countryCode)
                obj.put("isSuffix", cur.isSuffix)
                obj.put("hasSpace", cur.hasSpace)
                jsonArr.put(obj)
            }
            preferences[Keys.CUSTOM_CURRENCIES] = jsonArr.toString()
        }
    }

    suspend fun updateCustomCurrency(oldCode: String, currency: com.masum.cipher.core.domain.model.AppCurrency) {
        context.dataStore.edit { preferences ->
            val currentList = preferences[Keys.CUSTOM_CURRENCIES]?.let { jsonStr ->
                try {
                    val arr = org.json.JSONArray(jsonStr)
                    val list = mutableListOf<com.masum.cipher.core.domain.model.AppCurrency>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            com.masum.cipher.core.domain.model.AppCurrency(
                                code = obj.getString("code"),
                                symbol = obj.getString("symbol"),
                                name = obj.getString("name"),
                                countryCode = obj.optString("countryCode", "CUSTOM"),
                                isSuffix = obj.optBoolean("isSuffix", false),
                                hasSpace = obj.optBoolean("hasSpace", false)
                            )
                        )
                    }
                    list
                } catch (_: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            val index = currentList.indexOfFirst { it.code.equals(oldCode, ignoreCase = true) }
            val updated = if (index >= 0) {
                currentList.toMutableList().apply { set(index, currency) }
            } else {
                currentList.filterNot { it.code.equals(currency.code, ignoreCase = true) } + currency
            }

            val jsonArr = org.json.JSONArray()
            updated.forEach { cur ->
                val obj = org.json.JSONObject()
                obj.put("code", cur.code)
                obj.put("symbol", cur.symbol)
                obj.put("name", cur.name)
                obj.put("countryCode", cur.countryCode)
                obj.put("isSuffix", cur.isSuffix)
                obj.put("hasSpace", cur.hasSpace)
                jsonArr.put(obj)
            }
            preferences[Keys.CUSTOM_CURRENCIES] = jsonArr.toString()

            val currentActive = preferences[Keys.PREFERRED_CURRENCY_CODE] ?: preferences[Keys.PREFERRED_CURRENCY]
            if (currentActive != null && currentActive.equals(oldCode, ignoreCase = true)) {
                preferences[Keys.PREFERRED_CURRENCY] = currency.code
                preferences[Keys.PREFERRED_CURRENCY_CODE] = currency.code
                preferences[Keys.PREFERRED_CURRENCY_SYMBOL] = currency.symbol
                syncPrefs.edit()
                    .putString("cached_currency_code", currency.code)
                    .putString("cached_currency_symbol", currency.symbol)
                    .apply()
            }
        }
    }

    suspend fun removeCustomCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            val currentList = preferences[Keys.CUSTOM_CURRENCIES]?.let { jsonStr ->
                try {
                    val arr = org.json.JSONArray(jsonStr)
                    val list = mutableListOf<com.masum.cipher.core.domain.model.AppCurrency>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            com.masum.cipher.core.domain.model.AppCurrency(
                                code = obj.getString("code"),
                                symbol = obj.getString("symbol"),
                                name = obj.getString("name"),
                                countryCode = obj.optString("countryCode", "CUSTOM"),
                                isSuffix = obj.optBoolean("isSuffix", false),
                                hasSpace = obj.optBoolean("hasSpace", false)
                            )
                        )
                    }
                    list
                } catch (_: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            val updated = currentList.filterNot { it.code.equals(currencyCode, ignoreCase = true) }
            val jsonArr = org.json.JSONArray()
            updated.forEach { cur ->
                val obj = org.json.JSONObject()
                obj.put("code", cur.code)
                obj.put("symbol", cur.symbol)
                obj.put("name", cur.name)
                obj.put("countryCode", cur.countryCode)
                obj.put("isSuffix", cur.isSuffix)
                obj.put("hasSpace", cur.hasSpace)
                jsonArr.put(obj)
            }
            preferences[Keys.CUSTOM_CURRENCIES] = jsonArr.toString()
        }
    }

    suspend fun setAppLanguage(languageCode: String) {
        syncPrefs.edit()
            .putString("cached_app_language", languageCode)
            .apply()
        com.masum.cipher.core.util.LocaleHelper.setLocale(languageCode)
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_LANGUAGE] = languageCode
        }
    }

    suspend fun setProStatus(isPro: Boolean, tier: String, token: String?, orderId: String?, expiresAt: Long = 0L) {
        syncPrefs.edit()
            .putBoolean("cached_is_pro", isPro)
            .putString("cached_pro_tier", tier)
            .putString("cached_license_token", token)
            .putLong("cached_pro_expiry", expiresAt)
            .apply()
        context.dataStore.edit { preferences ->
            preferences[Keys.PRO_ACTIVATED] = isPro
            preferences[Keys.PRO_TIER] = tier
            preferences[Keys.PRO_EXPIRES_AT] = expiresAt
            if (token != null) {
                preferences[Keys.PRO_LICENSE_TOKEN] = token
            } else {
                preferences.remove(Keys.PRO_LICENSE_TOKEN)
            }
            if (orderId != null) {
                preferences[Keys.PRO_ORDER_ID] = orderId
            } else {
                preferences.remove(Keys.PRO_ORDER_ID)
            }
        }
    }

    suspend fun deactivatePro() {
        setProStatus(isPro = false, tier = "FREE", token = null, orderId = null, expiresAt = 0L)
    }

    suspend fun setShowProBadge(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_PRO_BADGE] = enabled }
    }

    fun getCachedLastLicenseSyncTime(): Long {
        return syncPrefs.getLong("cached_last_license_sync_time", 0L)
    }

    suspend fun setLastLicenseSyncTime(timestamp: Long) {
        syncPrefs.edit().putLong("cached_last_license_sync_time", timestamp).apply()
        context.dataStore.edit { it[Keys.LAST_LICENSE_SYNC_TIME] = timestamp }
    }
}

enum class AccentColor(val colorValue: Long, val colorName: String, val isProOnly: Boolean = false) {
    INDIGO(0xFF6366F1, "Electric Indigo", false),
    MINT(0xFF10B981, "Vibrant Mint", false),
    CHERRY(0xFFF43F5E, "Crimson Cherry", false),
    AMBER(0xFFF59E0B, "Amber Gold", false),
    CYAN(0xFF06B6D4, "Neon Cyan", false),
    VIOLET(0xFF8B5CF6, "Royal Violet", false),
    SAPPHIRE(0xFF2563EB, "Sapphire Blue", true),
    MAGENTA(0xFFBE123C, "Viva Magenta", true),
    EMERALD(0xFF059669, "Emerald Green", true),
    CORAL(0xFFFF6B6B, "Sunset Coral", true),
    LAVENDER(0xFFA855F7, "Neon Purple", true),
    TEAL(0xFF0D9488, "Deep Teal", true),
    PEACH_FUZZ(0xFFFF7A59, "Peach Fuzz", true),
    LIME(0xFF84CC16, "Electric Lime", true),
    OCEAN(0xFF0EA5E9, "Pacific Blue", true),
    FLAME(0xFFFF5722, "Solar Flame", true),
    AURORA(0xFF00B4D8, "Aurora Turquoise", true),
    GOLD(0xFFEAB308, "Imperial Gold", true)
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
    val currencyCode: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().code,
    val currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
    val isCurrencySuffix: Boolean = false,
    val hasCurrencySpace: Boolean = false,
    val appLanguage: String = "system",
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
    val categoryBudgets: Map<String, Double> = emptyMap(),
    val customCurrencies: List<com.masum.cipher.core.domain.model.AppCurrency> = emptyList(),
    val isDynamicBudgetEnabled: Boolean = false,
    val isNavBarCompressed: Boolean = false,
    val isPro: Boolean = false,
    val proTier: String = "FREE",
    val proLicenseToken: String? = null,
    val proOrderId: String? = null,
    val proExpiresAtEpochMs: Long = 0L,
    val showProBadge: Boolean = true
)
