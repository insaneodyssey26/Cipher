package com.masum.cipher.core.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.masum.cipher.core.security.DatabaseKeyUnavailableException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock

class LazyPassphraseOpenHelperFactoryTest {

    private var passphraseReads = 0

    private val failingProvider: () -> ByteArray = {
        passphraseReads++
        throw DatabaseKeyUnavailableException()
    }

    private val configuration = SupportSQLiteOpenHelper.Configuration.builder(mock<Context>())
        .name("test_db")
        .callback(object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        })
        .build()

    private fun createHelper() = LazyPassphraseOpenHelperFactory(failingProvider).create(configuration)

    @Test
    fun creatingTheHelperDoesNotReadThePassphrase() {
        createHelper()

        assertEquals(0, passphraseReads)
    }

    @Test
    fun helperExposesTheConfiguredDatabaseName() {
        assertEquals("test_db", createHelper().databaseName)
    }

    @Test
    fun configuringWriteAheadLoggingBeforeOpeningDoesNotReadThePassphrase() {
        createHelper().setWriteAheadLoggingEnabled(true)

        assertEquals(0, passphraseReads)
    }

    @Test
    fun closingAnUnopenedHelperDoesNotReadThePassphrase() {
        createHelper().close()

        assertEquals(0, passphraseReads)
    }

    @Test
    fun openingForWritePropagatesAnUnavailableKey() {
        val helper = createHelper()

        assertThrows(DatabaseKeyUnavailableException::class.java) { helper.writableDatabase }
        assertEquals(1, passphraseReads)
    }

    @Test
    fun openingForReadPropagatesAnUnavailableKey() {
        val helper = createHelper()

        assertThrows(DatabaseKeyUnavailableException::class.java) { helper.readableDatabase }
    }
}
