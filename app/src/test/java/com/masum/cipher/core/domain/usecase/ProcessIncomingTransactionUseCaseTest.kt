package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.data.local.pref.UserSettingsProvider
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.core.notifications.TransactionNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

class ProcessIncomingTransactionUseCaseTest {

    private lateinit var fakeTransactionDao: FakeTransactionDao
    private lateinit var fakeMerchantAliasDao: FakeMerchantAliasDao
    private lateinit var fakeCategoryRuleDao: FakeCategoryRuleDao
    private lateinit var categorizerEngine: CategorizerEngine
    private lateinit var fakeTransactionNotifier: FakeTransactionNotifier
    private lateinit var fakeUserSettingsProvider: FakeUserSettingsProvider
    private lateinit var fakeWidgetSyncer: FakeWidgetSyncer

    private lateinit var useCase: ProcessIncomingTransactionUseCase

    @Before
    fun setup() {
        fakeTransactionDao = FakeTransactionDao()
        fakeMerchantAliasDao = FakeMerchantAliasDao()
        fakeCategoryRuleDao = FakeCategoryRuleDao()
        categorizerEngine = CategorizerEngine()
        fakeTransactionNotifier = FakeTransactionNotifier()
        fakeUserSettingsProvider = FakeUserSettingsProvider()
        fakeWidgetSyncer = FakeWidgetSyncer()

        useCase = ProcessIncomingTransactionUseCase(
            fakeTransactionDao.asDao(),
            fakeMerchantAliasDao.asDao(),
            fakeCategoryRuleDao.asDao(),
            categorizerEngine,
            fakeTransactionNotifier,
            fakeUserSettingsProvider,
            fakeWidgetSyncer
        )
    }

    @Test
    fun duplicateTransactionWithinWindowIsDiscarded() = runBlocking {
        val existing = TransactionEntity(
            id = 1L,
            amount = 500.0,
            merchant = "SWIGGY",
            currency = "INR",
            category = "FOOD",
            timestamp = 100_000L,
            rawSms = "Test",
            isIncome = false
        )
        fakeTransactionDao.duplicateReturn = existing

        val incoming = TransactionEntity(
            amount = 500.0,
            merchant = "SWIGGY",
            currency = "INR",
            category = "",
            timestamp = 110_000L,
            rawSms = "Test duplicate",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNull(result)
        assertEquals(0, fakeTransactionDao.insertedTransactions.size)
        assertEquals(0, fakeWidgetSyncer.syncCount)
    }

    @Test
    fun smartRuleOverridesAutoCategorizer() = runBlocking {
        fakeCategoryRuleDao.rules["Amazon"] = "SHOPPING"

        val incoming = TransactionEntity(
            amount = 1200.0,
            merchant = "Amazon",
            currency = "INR",
            category = "",
            timestamp = 200_000L,
            rawSms = "Paid at Amazon",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals("Amazon", result!!.merchant)
        assertEquals("SHOPPING", result.category)
        assertEquals(1, fakeTransactionDao.insertedTransactions.size)
        assertEquals(1, fakeWidgetSyncer.syncCount)
    }

    @Test
    fun merchantAliasIsUtilizedAndPersisted() = runBlocking {
        fakeMerchantAliasDao.aliases["ZOMATO INDIA"] = "Zomato"

        val incoming = TransactionEntity(
            amount = 350.0,
            merchant = "Zomato India",
            currency = "INR",
            category = "",
            timestamp = 300_000L,
            rawSms = "Paid at Zomato India",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals("Zomato", result!!.merchant)
        assertEquals("FOOD", result.category)
        assertEquals(1, fakeWidgetSyncer.syncCount)
    }

    private class FakeWidgetSyncer : WidgetSyncer {
        var syncCount = 0
        override suspend fun syncWidget() {
            syncCount++
        }
    }

    private class FakeTransactionNotifier : TransactionNotifier {
        override fun showNewTransactionNotification(transaction: TransactionEntity) {}
        override fun showUncategorizedReminderNotification(count: Int) {}
        override fun showBudgetAlertNotification(isExceeded: Boolean, amount: Double, threshold: Int) {}
    }

    private class FakeUserSettingsProvider : UserSettingsProvider {
        val userSettings = UserSettings(
            theme = AppTheme.SYSTEM,
            isBiometricEnabled = false,
            isPrivacyModeEnabled = false,
            isHapticsEnabled = true,
            currency = "INR",
            currencyCode = "INR",
            currencySymbol = "₹",
            appLanguage = "en",
            autoLockTimeout = 0L,
            lastStopTime = 0L,
            monthlyBudget = 0.0,
            notifyAllTransactions = false
        )
        override val settingsFlow: Flow<UserSettings> = flowOf(userSettings)
    }

    private class FakeTransactionDao {
        var duplicateReturn: TransactionEntity? = null
        val insertedTransactions = mutableListOf<TransactionEntity>()

        fun asDao(): TransactionDao {
            return Proxy.newProxyInstance(
                TransactionDao::class.java.classLoader,
                arrayOf(TransactionDao::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "findDuplicate" -> duplicateReturn
                    "insertTransaction" -> {
                        val tx = args[0] as TransactionEntity
                        val id = (insertedTransactions.size + 1).toLong()
                        insertedTransactions.add(tx.copy(id = id))
                        id
                    }
                    "sumExpensesSince" -> 0.0
                    "sumIncomeSince" -> 0.0
                    "getUncategorizedCount" -> 0
                    else -> null
                }
            } as TransactionDao
        }
    }

    private class FakeMerchantAliasDao {
        val aliases = mutableMapOf<String, String>()

        fun asDao(): MerchantAliasDao {
            return Proxy.newProxyInstance(
                MerchantAliasDao::class.java.classLoader,
                arrayOf(MerchantAliasDao::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "getAliasForRawName" -> {
                        val raw = args[0] as String
                        aliases[raw]?.let { MerchantAliasEntity(raw, it) }
                    }
                    "insertAlias" -> {
                        val entity = args[0] as MerchantAliasEntity
                        aliases[entity.rawName] = entity.cleanName
                        Unit
                    }
                    else -> null
                }
            } as MerchantAliasDao
        }
    }

    private class FakeCategoryRuleDao {
        val rules = mutableMapOf<String, String>()

        fun asDao(): CategoryRuleDao {
            return Proxy.newProxyInstance(
                CategoryRuleDao::class.java.classLoader,
                arrayOf(CategoryRuleDao::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "getCategoryForMerchant" -> rules[args[0] as String]
                    "insertRule" -> {
                        val entity = args[0] as CategoryRuleEntity
                        rules[entity.merchantName] = entity.customCategory
                        Unit
                    }
                    else -> null
                }
            } as CategoryRuleDao
        }
    }
}
