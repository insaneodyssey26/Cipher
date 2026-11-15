package com.masum.cipher.core.notifications

import android.content.Context
import android.content.Intent
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.usecase.ApproveSubscriptionUseCase
import com.masum.cipher.core.domain.usecase.SkipSubscriptionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationActionReceiverTest {

    private val transactionRepository = mock<TransactionRepository>()
    private val accountRepository = mock<AccountRepository>()
    private val userPreferences = mock<UserPreferences>()
    private val subscriptionDao = mock<SubscriptionDao>()
    private val approveSubscriptionUseCase = mock<ApproveSubscriptionUseCase>()
    private val skipSubscriptionUseCase = mock<SkipSubscriptionUseCase>()
    private val widgetSyncManager = mock<com.masum.cipher.core.domain.usecase.WidgetSyncManager>()
    private val context = mock<Context>()

    private lateinit var receiver: NotificationActionReceiver

    @Before
    fun setup() {
        receiver = NotificationActionReceiver().apply {
            this.transactionRepository = this@NotificationActionReceiverTest.transactionRepository
            this.accountRepository = this@NotificationActionReceiverTest.accountRepository
            this.userPreferences = this@NotificationActionReceiverTest.userPreferences
            this.subscriptionDao = this@NotificationActionReceiverTest.subscriptionDao
            this.approveSubscriptionUseCase = this@NotificationActionReceiverTest.approveSubscriptionUseCase
            this.skipSubscriptionUseCase = this@NotificationActionReceiverTest.skipSubscriptionUseCase
            this.widgetSyncManager = this@NotificationActionReceiverTest.widgetSyncManager
        }
        whenever(context.applicationContext).thenReturn(context)
    }

    @Test
    fun approveSubscriptionIntentInvokesApproveUseCase(): Unit = runBlocking {
        val sub = SubscriptionEntity(
            id = 42L,
            merchant = "Netflix",
            amount = 15.99,
            frequencyDays = 30,
            nextExpectedDate = 1700000000000L,
            category = "Entertainment"
        )
        whenever(subscriptionDao.getById(42L)).thenReturn(sub)

        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(NotificationActionReceiver.ACTION_APPROVE_SUBSCRIPTION)
        whenever(intent.getLongExtra(NotificationActionReceiver.EXTRA_SUBSCRIPTION_ID, -1L)).thenReturn(42L)
        whenever(intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1)).thenReturn(999)

        receiver.handleAction(context, intent)
        Thread.sleep(200)

        verify(approveSubscriptionUseCase).invoke(sub)
        verify(skipSubscriptionUseCase, never()).invoke(any())
    }

    @Test
    fun skipSubscriptionIntentInvokesSkipUseCase(): Unit = runBlocking {
        val sub = SubscriptionEntity(
            id = 84L,
            merchant = "Spotify",
            amount = 9.99,
            frequencyDays = 30,
            nextExpectedDate = 1700000000000L,
            category = "Entertainment"
        )
        whenever(subscriptionDao.getById(84L)).thenReturn(sub)

        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(NotificationActionReceiver.ACTION_SKIP_SUBSCRIPTION)
        whenever(intent.getLongExtra(NotificationActionReceiver.EXTRA_SUBSCRIPTION_ID, -1L)).thenReturn(84L)
        whenever(intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1)).thenReturn(999)

        receiver.handleAction(context, intent)
        Thread.sleep(200)

        verify(skipSubscriptionUseCase).invoke(sub)
        verify(approveSubscriptionUseCase, never()).invoke(any())
    }

    @Test
    fun saveDuplicateIntentInsertsTransactionAndSyncsWidget(): Unit = runBlocking {
        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(NotificationActionReceiver.ACTION_SAVE_DUPLICATE_TRANSACTION)
        whenever(intent.getDoubleExtra(org.mockito.kotlin.eq("amount"), any())).thenReturn(50.0)
        whenever(intent.getStringExtra("merchant")).thenReturn("Starbucks")
        whenever(intent.getStringExtra("currency")).thenReturn("INR")
        whenever(intent.getLongExtra(org.mockito.kotlin.eq("timestamp"), any())).thenReturn(1700000000000L)
        whenever(intent.getStringExtra("category")).thenReturn("Food")
        whenever(intent.getBooleanExtra(org.mockito.kotlin.eq("is_income"), any())).thenReturn(false)
        whenever(intent.getIntExtra(org.mockito.kotlin.eq(NotificationActionReceiver.EXTRA_NOTIFICATION_ID), any())).thenReturn(8001)

        receiver.handleAction(context, intent)
        Thread.sleep(200)

        verify(transactionRepository).insertTransaction(any())
        verify(widgetSyncManager).syncWidget()
    }
}
