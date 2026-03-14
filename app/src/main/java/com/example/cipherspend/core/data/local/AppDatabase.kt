package com.example.cipherspend.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity

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