package com.masum.cipher.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.CustomCategoryDao
import com.masum.cipher.core.data.local.dao.GoalDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.TransactionSplitDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity

@Database(
    entities = [
        TransactionEntity::class,
        MerchantAliasEntity::class,
        CategoryRuleEntity::class,
        SubscriptionEntity::class,
        TransactionSplitEntity::class,
        CustomCategoryEntity::class,
        AccountEntity::class,
        GoalEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun merchantAliasDao(): MerchantAliasDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun transactionSplitDao(): TransactionSplitDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao

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

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `colorHex` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_custom_categories_name` ON `custom_categories` (`name`)")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `accountId` INTEGER DEFAULT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `initialBalance` REAL NOT NULL, `colorHex` INTEGER NOT NULL, `iconName` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `accountNumberLast4` TEXT)")
                db.execSQL("INSERT OR IGNORE INTO `accounts` (`id`, `name`, `type`, `initialBalance`, `colorHex`, `iconName`, `isDefault`, `createdAt`) VALUES (1, 'Main Account', 'BANK', 0.0, 4283332009, 'Landmark', 1, 1700000000000)")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `savedAmount` REAL NOT NULL, `colorHex` INTEGER NOT NULL, `iconName` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
            }
        }
    }
}