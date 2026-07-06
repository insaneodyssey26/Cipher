package com.masum.cipher.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity

@Database(
    entities = [TransactionEntity::class, MerchantAliasEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun merchantAliasDao(): MerchantAliasDao

    companion object {
        const val DATABASE_NAME = "cipher_spend_db"
    }
}