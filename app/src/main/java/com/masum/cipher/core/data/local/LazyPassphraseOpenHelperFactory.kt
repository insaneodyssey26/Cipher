package com.masum.cipher.core.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

class LazyPassphraseOpenHelperFactory(
    private val passphraseProvider: () -> ByteArray
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper =
        LazyPassphraseOpenHelper(configuration, passphraseProvider)
}

private class LazyPassphraseOpenHelper(
    private val configuration: SupportSQLiteOpenHelper.Configuration,
    private val passphraseProvider: () -> ByteArray
) : SupportSQLiteOpenHelper {

    private val lock = Any()
    private var delegate: SupportSQLiteOpenHelper? = null
    private var writeAheadLoggingEnabled: Boolean? = null

    override val databaseName: String? = configuration.name

    override val writableDatabase: SupportSQLiteDatabase
        get() = resolveDelegate().writableDatabase

    override val readableDatabase: SupportSQLiteDatabase
        get() = resolveDelegate().readableDatabase

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        synchronized(lock) {
            writeAheadLoggingEnabled = enabled
            delegate?.setWriteAheadLoggingEnabled(enabled)
        }
    }

    override fun close() {
        synchronized(lock) { delegate?.close() }
    }

    private fun resolveDelegate(): SupportSQLiteOpenHelper = synchronized(lock) {
        delegate ?: SupportOpenHelperFactory(passphraseProvider()).create(configuration).also { helper ->
            writeAheadLoggingEnabled?.let(helper::setWriteAheadLoggingEnabled)
            delegate = helper
        }
    }
}
