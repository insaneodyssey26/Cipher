package com.masum.cipher.core.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_data")

object WidgetDataStore {
    private val KEY_SPENT = doublePreferencesKey("monthly_spent")
    private val KEY_INCOME = doublePreferencesKey("monthly_income")

    suspend fun update(context: Context, spent: Double, income: Double) {
        context.widgetDataStore.edit {
            it[KEY_SPENT] = spent
            it[KEY_INCOME] = income
        }
    }

    fun spentFlow(context: Context): Flow<Double> =
        context.widgetDataStore.data.map { it[KEY_SPENT] ?: 0.0 }

    fun incomeFlow(context: Context): Flow<Double> =
        context.widgetDataStore.data.map { it[KEY_INCOME] ?: 0.0 }
}
