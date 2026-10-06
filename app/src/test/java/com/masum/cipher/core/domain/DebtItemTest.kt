package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.data.local.entity.DebtRepaymentEntity
import com.masum.cipher.core.domain.model.DebtItem
import com.masum.cipher.core.domain.model.DebtType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtItemTest {

    @Test
    fun testDebtTypeEnumMapping() {
        assertEquals(DebtType.LENT, DebtType.fromKey("LENT"))
        assertEquals(DebtType.BORROWED, DebtType.fromKey("BORROWED"))
        assertEquals(DebtType.LENT, DebtType.fromKey("UNKNOWN"))
    }

    @Test
    fun testDebtItemCalculations() {
        val debt = DebtEntity(
            id = 1L,
            personName = "Alice",
            amount = 1000.0,
            remainingAmount = 400.0,
            type = "LENT",
            dueDate = 1700000000L,
            createdAt = 1600000000L,
            isSettled = false,
            accountId = 2L,
            interestRate = 0.0,
            note = "Trip expenses"
        )
        val repayments = listOf(
            DebtRepaymentEntity(id = 10L, debtId = 1L, amount = 600.0, timestamp = 1650000000L, accountId = 2L, note = "UPI transfer")
        )

        val item = DebtItem(debt = debt, repayments = repayments, accountName = "Main Bank")

        assertEquals(1L, item.id)
        assertEquals("Alice", item.personName)
        assertEquals(1000.0, item.totalAmount, 0.001)
        assertEquals(400.0, item.remainingAmount, 0.001)
        assertEquals(600.0, item.repaidAmount, 0.001)
        assertEquals(0.6f, item.progress, 0.001f)
        assertEquals(DebtType.LENT, item.type)
        assertFalse(item.isSettled)
        assertEquals("Main Bank", item.accountName)
    }

    @Test
    fun testDebtItemSettledWhenRemainingZero() {
        val debt = DebtEntity(
            id = 2L,
            personName = "Bob",
            amount = 500.0,
            remainingAmount = 0.0,
            type = "BORROWED",
            createdAt = 1600000000L,
            isSettled = false
        )

        val item = DebtItem(debt = debt)

        assertTrue(item.isSettled)
        assertEquals(500.0, item.repaidAmount, 0.001)
        assertEquals(1.0f, item.progress, 0.001f)
    }

    @Test
    fun testDebtItemSettledWhenFlagTrue() {
        val debt = DebtEntity(
            id = 3L,
            personName = "Charlie",
            amount = 300.0,
            remainingAmount = 100.0,
            type = "LENT",
            createdAt = 1600000000L,
            isSettled = true
        )

        val item = DebtItem(debt = debt)

        assertTrue(item.isSettled)
    }
}
