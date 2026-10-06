package com.masum.cipher.core.di

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun transactionRepository(): TransactionRepository
    fun accountRepository(): AccountRepository
    fun widgetSyncManager(): WidgetSyncManager
    fun transactionDao(): TransactionDao
    fun accountDao(): AccountDao
    fun userPreferences(): UserPreferences
}
