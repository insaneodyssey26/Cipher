package com.masum.cipher.core.data.local.pref

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object WidgetKeys {
    val BUDGET_SPENT = doublePreferencesKey("spent")
    val STATS_SPENT = doublePreferencesKey("spent")
    val STATS_INCOME = doublePreferencesKey("income")
    
    val IS_PRO = booleanPreferencesKey("is_pro")
    val PRO_TIER = stringPreferencesKey("pro_tier")
    
    val NET_WORTH = doublePreferencesKey("net_worth")
    val ACCOUNTS_JSON = stringPreferencesKey("accounts_json")
    val RECENT_TX_JSON = stringPreferencesKey("recent_tx_json")
    val DAILY_SPENT = doublePreferencesKey("daily_spent")
    val DAILY_ALLOWANCE = doublePreferencesKey("daily_allowance")
    val QUICK_LOG_PAGE = intPreferencesKey("quick_log_page")
}
