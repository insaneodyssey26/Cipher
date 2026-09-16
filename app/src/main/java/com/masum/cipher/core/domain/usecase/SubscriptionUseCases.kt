package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private fun nextDueDate(subscription: SubscriptionEntity): Long {
    return subscription.nextExpectedDate + TimeUnit.DAYS.toMillis(subscription.frequencyDays.toLong())
}

class ApproveSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        val newTransaction = TransactionEntity(
            merchant = subscription.merchant,
            amount = subscription.amount,
            currency = "INR",
            rawSms = null,
            category = subscription.category,
            timestamp = System.currentTimeMillis(),
            isIncome = false,
            note = "Approved subscription"
        )
        transactionRepository.insertTransaction(newTransaction)
        subscriptionDao.update(subscription.copy(nextExpectedDate = nextDueDate(subscription)))
    }
}

class SkipSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription.copy(nextExpectedDate = nextDueDate(subscription)))
    }
}

class SaveSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(
        merchant: String,
        amount: Double,
        category: String,
        frequencyDays: Int,
        nextExpectedDate: Long
    ) {
        val existing = subscriptionDao.getAllSubscriptions().firstOrNull()
            ?.find { it.merchant.equals(merchant, ignoreCase = true) }
        val entity = SubscriptionEntity(
            id = existing?.id ?: 0,
            merchant = merchant,
            amount = amount,
            category = category,
            frequencyDays = frequencyDays,
            nextExpectedDate = nextExpectedDate
        )
        subscriptionDao.insert(entity)
    }
}

class DeleteSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(merchant: String): SubscriptionEntity? {
        val existing = subscriptionDao.getAllSubscriptions().firstOrNull()
            ?.find { it.merchant.equals(merchant, ignoreCase = true) }
        if (existing != null) {
            subscriptionDao.delete(existing)
        }
        return existing
    }
}

class RestoreSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        subscriptionDao.insert(subscription)
    }
}
