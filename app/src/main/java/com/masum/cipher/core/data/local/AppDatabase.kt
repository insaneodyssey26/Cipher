package com.masum.cipher.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity

import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity

@Database(
    entities = [TransactionEntity::class, MerchantAliasEntity::class, CategoryRuleEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun merchantAliasDao(): MerchantAliasDao
    abstract fun categoryRuleDao(): CategoryRuleDao

    companion object {
        const val DATABASE_NAME = "cipher_spend_db"

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN note TEXT")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `category_rules` (`merchantName` TEXT NOT NULL, `customCategory` TEXT NOT NULL, PRIMARY KEY(`merchantName`))")
            }
        }
    }
}