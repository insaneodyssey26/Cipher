package com.masum.cipher.core.security

import android.content.Context
import com.masum.cipher.core.data.local.AppDatabase
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

class DatabaseRecoveryTest {

    @Test
    fun databaseIsDeletedBeforeTheKeyIsDiscarded() {
        val context = mock<Context>()
        val securityManager = mock<SecurityManager>()

        DatabaseRecovery(context, securityManager).eraseEncryptedData()

        inOrder(context, securityManager) {
            verify(context).deleteDatabase(AppDatabase.DATABASE_NAME)
            verify(securityManager).discardDatabaseKey()
        }
    }
}
