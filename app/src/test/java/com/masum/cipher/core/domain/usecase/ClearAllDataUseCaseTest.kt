package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.AppDatabase
import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ClearAllDataUseCaseTest {

    private lateinit var appDatabase: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var widgetSyncManager: WidgetSyncManager
    private lateinit var useCase: ClearAllDataUseCase

    @Before
    fun setup() {
        appDatabase = mock()
        accountDao = mock()
        widgetSyncManager = mock()
        whenever(appDatabase.accountDao()).thenReturn(accountDao)
        useCase = ClearAllDataUseCase(appDatabase, widgetSyncManager)
    }

    @Test
    fun `clearAllData clears all tables and inserts default account`() = runBlocking {
        whenever(accountDao.insertAccount(any())).thenReturn(1L)

        useCase()

        verify(appDatabase).clearAllTables()
        verify(accountDao).insertAccount(any())
        verify(widgetSyncManager).syncWidget()
    }
}
