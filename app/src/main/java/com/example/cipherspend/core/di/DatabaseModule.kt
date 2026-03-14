package com.example.cipherspend.core.di

import android.content.Context
import androidx.room.Room
import com.example.cipherspend.core.data.local.AppDatabase
import com.example.cipherspend.core.data.local.dao.TransactionDao
import com.example.cipherspend.core.data.local.dao.MerchantAliasDao
import com.example.cipherspend.core.data.repository.BackupRepository
import com.example.cipherspend.core.security.SecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        securityManager: SecurityManager
    ): AppDatabase {
        val passphrase = securityManager.getDatabasePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideMerchantAliasDao(database: AppDatabase): MerchantAliasDao {
        return database.merchantAliasDao()
    }
    
    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        transactionDao: TransactionDao,
        merchantAliasDao: MerchantAliasDao
    ): BackupRepository {
        return BackupRepository(context, transactionDao, merchantAliasDao)
    }
}