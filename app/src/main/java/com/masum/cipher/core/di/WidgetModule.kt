package com.masum.cipher.core.di

import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import com.masum.cipher.core.domain.usecase.WidgetSyncer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindWidgetSyncer(
        widgetSyncManager: WidgetSyncManager
    ): WidgetSyncer
}
