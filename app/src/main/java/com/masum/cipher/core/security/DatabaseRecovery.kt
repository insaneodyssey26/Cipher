package com.masum.cipher.core.security

import android.content.Context
import com.masum.cipher.core.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRecovery @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager
) {

    fun eraseEncryptedData() {
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        securityManager.discardDatabaseKey()
    }
}
