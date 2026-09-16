package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UsParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseUsd(message: String) = parser.parse(message, preferredCurrency = "USD")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseUsd("You spent \$45.99 at WALMART using card ending 1234.")
        assertNotNull(result)
        assertEquals(45.99, result!!.amount, 0.001)
        assertEquals("WALMART", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseUsd("You received \$500.00 from JOHN SMITH sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseUsd("123456 is your one-time passcode. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseUsd("Available balance \$1500.00. You spent \$45.99 at TARGET using card ending 1234.")
        assertNotNull(result)
        assertEquals(45.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseUsd("You paid \$25.00 to Joes Pizza Shop using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseUsd("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses USD as default currency`() {
        val result = parseUsd("You spent \$45.99 at STARBUCKS using card ending 1234.")
        assertNotNull(result)
        assertEquals("USD", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseUsd("You spent \$1,234.56 at BEST BUY using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseUsd("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `charged at brand dictionary merchant is parsed`() {
        val result = parseUsd("Your card was charged \$12.50 at STARBUCKS on 06/01.")
        assertNotNull(result)
        assertEquals(12.50, result!!.amount, 0.001)
        assertEquals("STARBUCKS", result.merchant)
    }
}
