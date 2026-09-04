package com.masum.cipher.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.TransactionSplitDao
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity

@Database(
    entities = [
        TransactionEntity::class,
        MerchantAliasEntity::class,
        CategoryRuleEntity::class,
        SubscriptionEntity::class,
        TransactionSplitEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun merchantAliasDao(): MerchantAliasDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun transactionSplitDao(): TransactionSplitDao

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

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `subscriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `merchant` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `frequencyDays` INTEGER NOT NULL, `nextExpectedDate` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `transaction_splits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `transactionId` INTEGER NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `isPaid` INTEGER NOT NULL, `isCurrentUser` INTEGER NOT NULL, FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_splits_transactionId` ON `transaction_splits` (`transactionId`)")
            }
        }
    }
}