package com.masum.cipher.core.util

import com.masum.cipher.core.domain.model.SplitParticipant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitCalculatorTest {

    private fun participant(name: String) = SplitParticipant(name = name)

    @Test
    fun `equal split divides amount evenly with no remainder`() {
        val result = SplitCalculator.calculateEqualSplits(300.0, listOf(participant("A"), participant("B"), participant("C")))

        assertTrue(result.all { it.amount == 100.0 })
    }

    @Test
    fun `equal split distributes remainder cents to the first participants`() {
        // 100 / 3 = 33.33... ; the leftover cent should go to the first participant only.
        val result = SplitCalculator.calculateEqualSplits(100.0, listOf(participant("A"), participant("B"), participant("C")))

        val total = result.sumOf { it.amount }
        assertEquals(100.0, total, 0.001)
        assertTrue(result[0].amount >= result[1].amount)
        assertTrue(result[0].amount >= result[2].amount)
    }

    @Test
    fun `equal split sum always equals the original total regardless of remainder`() {
        val result = SplitCalculator.calculateEqualSplits(10.0, (1..7).map { participant("P$it") })

        assertEquals(10.0, result.sumOf { it.amount }, 0.0001)
    }

    @Test
    fun `equal split with a single participant gives them the whole amount`() {
        val result = SplitCalculator.calculateEqualSplits(250.0, listOf(participant("Solo")))
        assertEquals(250.0, result[0].amount, 0.001)
        assertEquals(100.0, result[0].percentage, 0.001)
    }

    @Test
    fun `equal split with no participants returns an empty list`() {
        val result = SplitCalculator.calculateEqualSplits(100.0, emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `equal split percentage reflects each participants share`() {
        val result = SplitCalculator.calculateEqualSplits(200.0, listOf(participant("A"), participant("B")))
        assertEquals(50.0, result[0].percentage, 0.001)
        assertEquals(50.0, result[1].percentage, 0.001)
    }

    @Test
    fun `equal split of a zero total gives everyone a zero amount`() {
        val result = SplitCalculator.calculateEqualSplits(0.0, listOf(participant("A"), participant("B")))
        assertTrue(result.all { it.amount == 0.0 })
    }

    @Test
    fun `percentage split computes amount from each participants percentage`() {
        val result = SplitCalculator.calculatePercentageSplits(
            1000.0,
            listOf(
                SplitParticipant(name = "A", percentage = 30.0),
                SplitParticipant(name = "B", percentage = 70.0)
            )
        )

        assertEquals(300.0, result[0].amount, 0.001)
        assertEquals(700.0, result[1].amount, 0.001)
    }

    @Test
    fun `percentage split rounds to the nearest cent`() {
        val result = SplitCalculator.calculatePercentageSplits(
            100.0,
            listOf(SplitParticipant(name = "A", percentage = 33.333))
        )

        assertEquals(33.33, result[0].amount, 0.001)
    }

    @Test
    fun `percentage split preserves participant identity fields`() {
        val result = SplitCalculator.calculatePercentageSplits(
            100.0,
            listOf(SplitParticipant(name = "A", percentage = 100.0, isCurrentUser = true))
        )

        assertEquals("A", result[0].name)
        assertTrue(result[0].isCurrentUser)
    }

    @Test
    fun `share breakdown message includes expense name and total`() {
        val message = SplitCalculator.formatShareBreakdownMessage(
            "Dinner", 500.0, "₹",
            listOf(SplitParticipant(name = "Alice", amount = 250.0, isCurrentUser = true))
        )

        assertTrue(message.contains("Dinner"))
        assertTrue(message.contains("500.00"))
        assertTrue(message.contains("Alice"))
        assertTrue(message.contains("(Host)"))
    }

    @Test
    fun `share breakdown message falls back to a default name when expense name is blank`() {
        val message = SplitCalculator.formatShareBreakdownMessage("", 100.0, "$", emptyList())
        assertTrue(message.contains("Expense:"))
    }

    @Test
    fun `share breakdown message shows pending status for unpaid non host participants`() {
        val message = SplitCalculator.formatShareBreakdownMessage(
            "Trip", 100.0, "$",
            listOf(SplitParticipant(name = "Bob", amount = 50.0, isPaid = false, isCurrentUser = false))
        )

        assertTrue(message.contains("(Pending)"))
    }

    @Test
    fun `share breakdown message shows settled status for paid non host participants`() {
        val message = SplitCalculator.formatShareBreakdownMessage(
            "Trip", 100.0, "$",
            listOf(SplitParticipant(name = "Bob", amount = 50.0, isPaid = true, isCurrentUser = false))
        )

        assertTrue(message.contains("(Settled)"))
    }
}
