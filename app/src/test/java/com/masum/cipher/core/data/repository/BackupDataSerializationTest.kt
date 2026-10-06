package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import com.masum.cipher.core.security.BackupCrypto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupDataSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val crypto = BackupCrypto()

    @Test
    fun fullBackupDataRoundTripSerializationAndEncryption() {
        val testDebt = DebtEntity(
            id = 101L,
            personName = "John Doe",
            amount = 5000.0,
            remainingAmount = 2500.0,
            type = "LENT",
            dueDate = 1750000000000L,
            createdAt = 1740000000000L,
            note = "Trip loan",
            accountId = 1L,
            isSettled = false,
            interestRate = 0.0,
            transactionId = 2001L
        )

        val testRepayment = DebtRepaymentEntity(
            id = 501L,
            debtId = 101L,
            amount = 2500.0,
            timestamp = 1745000000000L,
            accountId = 1L,
            note = "First installment",
            transactionId = 2002L
        )

        val testGoal = GoalEntity(
            id = 77L,
            name = "Japan Trip",
            targetAmount = 150000.0,
            savedAmount = 45000.0,
            colorHex = 0xFF3B82F6,
            iconName = "plane",
            createdAt = 1730000000000L
        )

        val testAccount = AccountEntity(
            id = 1L,
            name = "HDFC Savings",
            type = "BANK",
            initialBalance = 85400.50,
            colorHex = 0xFF10B981,
            iconName = "Landmark",
            isDefault = true,
            accountNumberLast4 = "1234",
            createdAt = 1720000000000L
        )

        val testTransaction = TransactionEntity(
            id = 2001L,
            amount = 5000.0,
            merchant = "John Doe",
            currency = "INR",
            timestamp = 1740000000000L,
            category = "Transfer",
            rawSms = null,
            isIncome = false,
            note = "Trip loan",
            accountId = 1L
        )

        val testSplit = TransactionSplitEntity(
            id = 301L,
            transactionId = 2001L,
            name = "John Doe",
            amount = 2500.0,
            isCurrentUser = false,
            isPaid = false
        )

        val testCategory = CustomCategoryEntity(
            id = 12L,
            name = "Vacation",
            iconName = "palmtree",
            colorHex = 0xFFF59E0B,
            createdAt = 1720000000000L
        )

        val originalData = BackupData(
            transactions = listOf(testTransaction),
            splits = listOf(testSplit),
            aliases = listOf(MerchantAliasEntity("AMZN", "Amazon")),
            rules = listOf(CategoryRuleEntity(merchantName = "uber", customCategory = "Transport")),
            subscriptions = listOf(SubscriptionEntity(id = 1L, merchant = "Spotify", amount = 119.0, category = "Entertainment", frequencyDays = 30, nextExpectedDate = 1750000000000L)),
            customCategories = listOf(testCategory),
            accounts = listOf(testAccount),
            goals = listOf(testGoal),
            debts = listOf(testDebt),
            debtRepayments = listOf(testRepayment),
            monthlyBudget = 50000.0,
            isDynamicBudgetEnabled = true,
            categoryBudgets = mapOf("Food" to 15000.0),
            currencyCode = "INR",
            currencySymbol = "₹",
            theme = "DARK",
            accentColor = "CYAN",
            proLicenseToken = "CIPHER-PRO-LIFETIME-KEY-TEST"
        )

        val encodedJson = json.encodeToString(BackupData.serializer(), originalData)
        val password = "StrongBackupPassword123!".toCharArray()

        val encryptedBytes = crypto.encrypt(encodedJson.toByteArray(Charsets.UTF_8), password)
        assertTrue(encryptedBytes.isNotEmpty())

        val decryptedBytes = crypto.decrypt(encryptedBytes, password)
        val decodedJson = String(decryptedBytes, Charsets.UTF_8)
        val restoredData = json.decodeFromString(BackupData.serializer(), decodedJson)

        assertEquals(1, restoredData.transactions.size)
        assertEquals(testTransaction.id, restoredData.transactions[0].id)
        assertEquals(testTransaction.amount, restoredData.transactions[0].amount, 0.001)

        assertEquals(1, restoredData.splits.size)
        assertEquals(testSplit.name, restoredData.splits[0].name)

        assertEquals(1, restoredData.accounts.size)
        assertEquals(testAccount.name, restoredData.accounts[0].name)
        assertEquals(testAccount.initialBalance, restoredData.accounts[0].initialBalance, 0.001)

        assertEquals(1, restoredData.goals.size)
        assertEquals(testGoal.name, restoredData.goals[0].name)
        assertEquals(testGoal.targetAmount, restoredData.goals[0].targetAmount, 0.001)

        assertEquals(1, restoredData.debts.size)
        val restoredDebt = restoredData.debts[0]
        assertEquals(testDebt.id, restoredDebt.id)
        assertEquals(testDebt.personName, restoredDebt.personName)
        assertEquals(testDebt.amount, restoredDebt.amount, 0.001)
        assertEquals(testDebt.remainingAmount, restoredDebt.remainingAmount, 0.001)
        assertEquals(testDebt.type, restoredDebt.type)
        assertEquals(testDebt.dueDate, restoredDebt.dueDate)
        assertEquals(testDebt.note, restoredDebt.note)
        assertEquals(testDebt.accountId, restoredDebt.accountId)

        assertEquals(1, restoredData.debtRepayments.size)
        val restoredRepayment = restoredData.debtRepayments[0]
        assertEquals(testRepayment.id, restoredRepayment.id)
        assertEquals(testRepayment.debtId, restoredRepayment.debtId)
        assertEquals(testRepayment.amount, restoredRepayment.amount, 0.001)
        assertEquals(testRepayment.note, restoredRepayment.note)

        assertEquals(originalData.monthlyBudget, restoredData.monthlyBudget, 0.001)
        assertEquals("INR", restoredData.currencyCode)
        assertEquals("₹", restoredData.currencySymbol)
        assertEquals("CIPHER-PRO-LIFETIME-KEY-TEST", restoredData.proLicenseToken)
    }

    @Test
    fun legacyBackupWithoutDebtsAndGoalsRestoresSafelyWithDefaults() {
        val legacyJson = """
            {
                "transactions": [
                    {
                        "id": 1,
                        "amount": 100.0,
                        "merchant": "Coffee Shop",
                        "currency": "USD",
                        "timestamp": 1700000000000,
                        "category": "Food",
                        "rawSms": null,
                        "isIncome": false
                    }
                ],
                "monthlyBudget": 20000.0,
                "currencyCode": "USD",
                "currencySymbol": "$",
                "unknownNewFieldFromFuture": "futureValue"
            }
        """.trimIndent()

        val restored = json.decodeFromString(BackupData.serializer(), legacyJson)

        assertEquals(1, restored.transactions.size)
        assertEquals("Coffee Shop", restored.transactions[0].merchant)
        assertEquals(20000.0, restored.monthlyBudget, 0.001)
        assertTrue(restored.debts.isEmpty())
        assertTrue(restored.debtRepayments.isEmpty())
        assertTrue(restored.goals.isEmpty())
        assertTrue(restored.accounts.isEmpty())
        assertTrue(restored.splits.isEmpty())
        assertNotNull(restored.debts)
        assertNotNull(restored.debtRepayments)
    }
}
