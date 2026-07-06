package com.masum.cipher.core.data.local.pref

import androidx.datastore.preferences.core.doublePreferencesKey

object WidgetKeys {
    val BUDGET_SPENT = doublePreferencesKey("spent")
    val STATS_SPENT = doublePreferencesKey("spent")
    val STATS_INCOME = doublePreferencesKey("income")
}
