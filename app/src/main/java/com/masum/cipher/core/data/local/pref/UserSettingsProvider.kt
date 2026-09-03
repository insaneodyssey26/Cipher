package com.masum.cipher.core.data.local.pref

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface UserSettingsProvider {
    val settingsFlow: Flow<UserSettings>
}

@Singleton
class AppUserSettingsProvider @Inject constructor(
    private val userPreferences: UserPreferences
) : UserSettingsProvider {
    override val settingsFlow: Flow<UserSettings>
        get() = userPreferences.settingsFlow
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSettingsProviderModule {

    @Binds
    @Singleton
    abstract fun bindUserSettingsProvider(
        impl: AppUserSettingsProvider
    ): UserSettingsProvider
}
