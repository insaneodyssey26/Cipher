package com.masum.cipher.core.notifications

import com.masum.cipher.core.data.local.entity.TransactionEntity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface TransactionNotifier {
    fun showNewTransactionNotification(transaction: TransactionEntity)
    fun showUncategorizedReminderNotification(count: Int)
    fun showBudgetAlertNotification(isExceeded: Boolean, amount: Double, threshold: Int)
}

@Singleton
class LocalTransactionNotifier @Inject constructor(
    private val localNotificationManager: LocalNotificationManager
) : TransactionNotifier {

    override fun showNewTransactionNotification(transaction: TransactionEntity) {
        localNotificationManager.showNewTransactionNotification(transaction)
    }

    override fun showUncategorizedReminderNotification(count: Int) {
        localNotificationManager.showUncategorizedReminderNotification(count)
    }

    override fun showBudgetAlertNotification(isExceeded: Boolean, amount: Double, threshold: Int) {
        localNotificationManager.showBudgetAlertNotification(isExceeded, amount, threshold)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionNotifierModule {

    @Binds
    @Singleton
    abstract fun bindTransactionNotifier(
        impl: LocalTransactionNotifier
    ): TransactionNotifier
}
