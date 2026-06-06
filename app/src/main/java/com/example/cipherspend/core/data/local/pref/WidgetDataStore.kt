package com.masum.cipher.core.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_data")

object WidgetDataStore {
    private val KEY_SPENT = doublePreferencesKey("monthly_spent")

    suspend fun update(context: Context, spent: Double) {
        context.widgetDataStore.edit { it[KEY_SPENT] = spent }
    }

    fun spentFlow(context: Context): Flow<Double> =
        context.widgetDataStore.data.map { it[KEY_SPENT] ?: 0.0 }
}
