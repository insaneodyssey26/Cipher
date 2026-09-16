package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class SubscriptionUseCasesTest {

    private lateinit var subscriptionDao: SubscriptionDao
    private lateinit var transactionRepository: TransactionRepository

    private fun subscription(
        id: Long = 1L,
        merchant: String = "NETFLIX",
        amount: Double = 649.0,
        frequencyDays: Int = 30,
        nextExpectedDate: Long = 1_000_000L
    ) = SubscriptionEntity(
        id = id,
        merchant = merchant,
        amount = amount,
        category = "ENTERTAINMENT",
        frequencyDays = frequencyDays,
        nextExpectedDate = nextExpectedDate
    )

    @Before
    fun setup() {
        subscriptionDao = mock()
        transactionRepository = mock()
    }

    // ─── ApproveSubscriptionUseCase ──────────────────────────────────────

    @Test
    fun `approving a subscription inserts a new expense transaction`() = runBlocking {
        val useCase = ApproveSubscriptionUseCase(subscriptionDao, transactionRepository)
        val sub = subscription()

        useCase(sub)

        verify(transactionRepository).insertTransaction(argThat { merchant == "NETFLIX" && amount == 649.0 && !isIncome })
        Unit
    }

    @Test
    fun `approving a subscription advances the next expected date by one frequency interval`() = runBlocking {
        val useCase = ApproveSubscriptionUseCase(subscriptionDao, transactionRepository)
        val sub = subscription(nextExpectedDate = 1_000_000L, frequencyDays = 30)

        useCase(sub)

        val expectedNextDate = 1_000_000L + TimeUnit.DAYS.toMillis(30)
        verify(subscriptionDao).update(argThat { nextExpectedDate == expectedNextDate })
        Unit
    }

    // ─── SkipSubscriptionUseCase ─────────────────────────────────────────

    @Test
    fun `skipping a subscription advances the next expected date without creating a transaction`() = runBlocking {
        val useCase = SkipSubscriptionUseCase(subscriptionDao)
        val sub = subscription(nextExpectedDate = 1_000_000L, frequencyDays = 7)

        useCase(sub)

        val expectedNextDate = 1_000_000L + TimeUnit.DAYS.toMillis(7)
        verify(subscriptionDao).update(argThat { nextExpectedDate == expectedNextDate })
        verify(transactionRepository, never()).insertTransaction(any())
        Unit
    }

    // ─── SaveSubscriptionUseCase ─────────────────────────────────────────

    @Test
    fun `saving a new subscription inserts it with id zero`() = runBlocking {
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(emptyList()))
        val useCase = SaveSubscriptionUseCase(subscriptionDao)

        useCase("SPOTIFY", 119.0, "ENTERTAINMENT", 30, 2_000_000L)

        verify(subscriptionDao).insert(argThat { id == 0L && merchant == "SPOTIFY" && amount == 119.0 })
        Unit
    }

    @Test
    fun `saving over an existing subscription with the same merchant reuses its id`() = runBlocking {
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(listOf(subscription(id = 42L, merchant = "NETFLIX"))))
        val useCase = SaveSubscriptionUseCase(subscriptionDao)

        useCase("netflix", 699.0, "ENTERTAINMENT", 30, 3_000_000L)

        verify(subscriptionDao).insert(argThat { id == 42L && amount == 699.0 })
        Unit
    }

    // ─── DeleteSubscriptionUseCase ───────────────────────────────────────

    @Test
    fun `deleting an existing subscription removes it and returns the deleted entity`() = runBlocking {
        val existing = subscription(merchant = "NETFLIX")
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(listOf(existing)))
        val useCase = DeleteSubscriptionUseCase(subscriptionDao)

        val result = useCase("NETFLIX")

        verify(subscriptionDao).delete(existing)
        assertEquals(existing, result)
    }

    @Test
    fun `deleting a subscription matches merchant name case insensitively`() = runBlocking {
        val existing = subscription(merchant = "Netflix")
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(listOf(existing)))
        val useCase = DeleteSubscriptionUseCase(subscriptionDao)

        val result = useCase("NETFLIX")

        assertTrue(result != null)
    }

    @Test
    fun `deleting a subscription that does not exist does nothing and returns null`() = runBlocking {
        whenever(subscriptionDao.getAllSubscriptions()).thenReturn(flowOf(emptyList()))
        val useCase = DeleteSubscriptionUseCase(subscriptionDao)

        val result = useCase("UNKNOWN")

        verify(subscriptionDao, never()).delete(any())
        assertNull(result)
    }

    // ─── RestoreSubscriptionUseCase ──────────────────────────────────────

    @Test
    fun `restoring a subscription reinserts it unchanged`() = runBlocking {
        val useCase = RestoreSubscriptionUseCase(subscriptionDao)
        val sub = subscription()

        useCase(sub)

        verify(subscriptionDao).insert(sub)
        Unit
    }
}
