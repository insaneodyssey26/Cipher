package com.example.cipherspend.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "cipher_spend_db"
    }
}