package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.AccountDao
import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.MerchantAliasDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.domain.CategorizerEngine
import com.masum.cipher.core.notifications.LocalNotificationManager
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
    private lateinit var fakeAccountDao: FakeAccountDao
    private var syncCount = 0

    private lateinit var useCase: ProcessIncomingTransactionUseCase

    @Before
    fun setup() {
        fakeTransactionDao = FakeTransactionDao()
        fakeMerchantAliasDao = FakeMerchantAliasDao()
        fakeCategoryRuleDao = FakeCategoryRuleDao()
        fakeAccountDao = FakeAccountDao()
        val categorizerEngine = CategorizerEngine()
        syncCount = 0

        useCase = ProcessIncomingTransactionUseCase(
            fakeTransactionDao.asDao(),
            fakeMerchantAliasDao.asDao(),
            fakeCategoryRuleDao.asDao(),
            fakeAccountDao.asDao(),
            categorizerEngine,
            null,
            null,
            null
        ).apply {
            onSyncWidget = { syncCount++ }
            onGetSettings = {
                UserSettings(
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
            }
        }
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
        assertEquals(0, syncCount)
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
        assertEquals(1, syncCount)
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
        assertEquals(1, syncCount)
    }

    @Test
    fun differentMerchantsWithSameAmountWithinWindowAreBothAllowed() = runBlocking {
        val existing = TransactionEntity(
            id = 1L,
            amount = 500.0,
            merchant = "RAMESH",
            currency = "INR",
            category = "OTHERS",
            timestamp = 100_000L,
            rawSms = "Paid to Ramesh",
            isIncome = false
        )
        fakeTransactionDao.duplicateReturn = existing

        val incoming = TransactionEntity(
            amount = 500.0,
            merchant = "SURESH",
            currency = "INR",
            category = "",
            timestamp = 110_000L,
            rawSms = "Paid to Suresh",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(1, fakeTransactionDao.insertedTransactions.size)
        assertEquals(1, syncCount)
    }

    @Test
    fun incomeAndExpenseOfSameAmountWithinWindowAreBothAllowed() = runBlocking {
        val existingExpense = TransactionEntity(
            id = 1L,
            amount = 100.0,
            merchant = "STORE",
            currency = "INR",
            category = "SHOPPING",
            timestamp = 100_000L,
            rawSms = "Paid 100",
            isIncome = false
        )
        fakeTransactionDao.duplicateReturn = existingExpense

        val incomingIncome = TransactionEntity(
            amount = 100.0,
            merchant = "FRIEND",
            currency = "INR",
            category = "",
            timestamp = 105_000L,
            rawSms = "Received 100",
            isIncome = true
        )

        val result = useCase(incomingIncome)
        assertNotNull(result)
        assertEquals(1, fakeTransactionDao.insertedTransactions.size)
        assertEquals(1, syncCount)
    }

    @Test
    fun incomingSmsWithLast4MatchesAccountWithSameDigits() = runBlocking {
        val hdfc = AccountEntity(id = 101L, name = "HDFC Savings", type = "BANK", accountNumberLast4 = "4821", isDefault = false)
        val sbi = AccountEntity(id = 102L, name = "SBI Primary", type = "BANK", accountNumberLast4 = "9012", isDefault = true)
        fakeAccountDao.accounts.addAll(listOf(hdfc, sbi))

        val incoming = TransactionEntity(
            amount = 450.0,
            merchant = "Starbucks",
            currency = "INR",
            category = "",
            timestamp = 400_000L,
            rawSms = "Rs 450 debited from A/c ending with 4821 at STARBUCKS",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(101L, result!!.accountId)
    }

    @Test
    fun incomingSmsWithMaskedCardMatchesCreditCardAccount() = runBlocking {
        val amex = AccountEntity(id = 201L, name = "Amex Platinum", type = "CREDIT_CARD", accountNumberLast4 = "1004", isDefault = false)
        val axis = AccountEntity(id = 202L, name = "Axis Bank", type = "BANK", accountNumberLast4 = "5566", isDefault = true)
        fakeAccountDao.accounts.addAll(listOf(amex, axis))

        val incoming = TransactionEntity(
            amount = 3200.0,
            merchant = "Apple Store",
            currency = "INR",
            category = "",
            timestamp = 500_000L,
            rawSms = "Spent Rs 3200 on Credit Card ending in 1004",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(201L, result!!.accountId)
    }

    @Test
    fun incomingSmsWithBankNameMatchesAccountWithoutLast4() = runBlocking {
        val icici = AccountEntity(id = 301L, name = "ICICI Salary", type = "BANK", accountNumberLast4 = null, isDefault = false)
        val defaultAcc = AccountEntity(id = 302L, name = "Cash Wallet", type = "CASH", accountNumberLast4 = null, isDefault = true)
        fakeAccountDao.accounts.addAll(listOf(icici, defaultAcc))

        val incoming = TransactionEntity(
            amount = 1500.0,
            merchant = "Zomato",
            currency = "INR",
            category = "",
            timestamp = 600_000L,
            rawSms = "ICICI Bank: Payment of Rs 1500 made at Zomato",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(301L, result!!.accountId)
    }

    @Test
    fun incomingSmsWithoutAccountDetailsFallsBackToDefaultAccount() = runBlocking {
        val hdfc = AccountEntity(id = 401L, name = "HDFC", type = "BANK", accountNumberLast4 = "1234", isDefault = false)
        val defaultAcc = AccountEntity(id = 402L, name = "Main Vault", type = "BANK", accountNumberLast4 = "5678", isDefault = true)
        fakeAccountDao.accounts.addAll(listOf(hdfc, defaultAcc))

        val incoming = TransactionEntity(
            amount = 200.0,
            merchant = "Tea Stall",
            currency = "INR",
            category = "",
            timestamp = 700_000L,
            rawSms = "Paid Rs 200 at Tea Stall via UPI",
            isIncome = false
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(402L, result!!.accountId)
    }

    @Test
    fun explicitAccountIdInTransactionIsPreserved() = runBlocking {
        val hdfc = AccountEntity(id = 501L, name = "HDFC", type = "BANK", accountNumberLast4 = "1234", isDefault = true)
        fakeAccountDao.accounts.add(hdfc)

        val incoming = TransactionEntity(
            amount = 99.0,
            merchant = "Manual Entry",
            currency = "INR",
            category = "FOOD",
            timestamp = 800_000L,
            rawSms = null,
            isIncome = false,
            accountId = 999L
        )

        val result = useCase(incoming)
        assertNotNull(result)
        assertEquals(999L, result!!.accountId)
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
                    "findDuplicate" -> {
                        val amount = args[0] as Double
                        val isIncome = args[1] as Boolean
                        val merchant = args[2] as String
                        duplicateReturn?.takeIf {
                            it.amount == amount && it.isIncome == isIncome && it.merchant.equals(merchant, ignoreCase = true)
                        }
                    }
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
                        aliases.entries.firstOrNull { it.key.equals(raw, ignoreCase = true) }?.let {
                            MerchantAliasEntity(it.key, it.value)
                        }
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
                    "getCategoryForMerchant" -> {
                        val merchant = args[0] as String
                        rules.entries.firstOrNull { it.key.equals(merchant, ignoreCase = true) }?.value
                    }
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

    private class FakeAccountDao {
        val accounts = mutableListOf<AccountEntity>()

        fun asDao(): AccountDao {
            return Proxy.newProxyInstance(
                AccountDao::class.java.classLoader,
                arrayOf(AccountDao::class.java)
            ) { _, method, args ->
                when (method.name) {
                    "getAllAccounts" -> accounts.toList()
                    "getDefaultAccount" -> accounts.firstOrNull { it.isDefault } ?: accounts.firstOrNull()
                    "getAccountByLast4" -> {
                        val last4 = args[0] as String
                        accounts.firstOrNull { it.accountNumberLast4 == last4 }
                    }
                    "getAccountById" -> {
                        val id = args[0] as Long
                        accounts.firstOrNull { it.id == id }
                    }
                    else -> null
                }
            } as AccountDao
        }
    }
}
