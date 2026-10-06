package com.masum.cipher.core.data.local.pref

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object WidgetKeys {
    val BUDGET_SPENT = doublePreferencesKey("spent")
    val BUDGET_INCOME = doublePreferencesKey("budget_income")
    val BUDGET_ACCOUNT_NAME = stringPreferencesKey("budget_account_name")
    val BUDGET_ACCOUNT_ID = stringPreferencesKey("budget_account_id")
    val BUDGET_ACCOUNTS_JSON = stringPreferencesKey("budget_accounts_json")

    val STATS_SPENT = doublePreferencesKey("stats_spent")
    val STATS_INCOME = doublePreferencesKey("income")
    val STATS_ACCOUNT_NAME = stringPreferencesKey("stats_account_name")
    val STATS_ACCOUNT_ID = stringPreferencesKey("stats_account_id")
    val STATS_ACCOUNTS_JSON = stringPreferencesKey("stats_accounts_json")

    val PASSBOOK_ACCOUNT_NAME = stringPreferencesKey("passbook_account_name")
    val PASSBOOK_ACCOUNT_ID = stringPreferencesKey("passbook_account_id")
    val PASSBOOK_ACCOUNTS_JSON = stringPreferencesKey("passbook_accounts_json")

    val IS_PRO = booleanPreferencesKey("is_pro")
    val PRO_TIER = stringPreferencesKey("pro_tier")
    
    val NET_WORTH = doublePreferencesKey("net_worth")
    val ACCOUNTS_JSON = stringPreferencesKey("accounts_json")
    val RECENT_TX_JSON = stringPreferencesKey("recent_tx_json")
    val DAILY_SPENT = doublePreferencesKey("daily_spent")
    val DAILY_ALLOWANCE = doublePreferencesKey("daily_allowance")
    val QUICK_LOG_PAGE = intPreferencesKey("quick_log_page")
}
