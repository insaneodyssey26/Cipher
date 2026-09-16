package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UaeParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseAed(message: String) = parser.parse(message, preferredCurrency = "AED")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseAed("You spent AED 45.99 at LULU using card ending 1234.")
        assertNotNull(result)
        assertEquals(45.99, result!!.amount, 0.001)
        assertEquals("LULU", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseAed("You received AED 150.00 from AHMED ALI sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseAed("123456 is your one-time password. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseAed("Available balance AED 900.00. You spent AED 45.99 at CARREFOUR UAE using card ending 1234.")
        assertNotNull(result)
        assertEquals(45.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseAed("You paid AED 15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseAed("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses AED as default currency`() {
        val result = parseAed("You spent AED 45.99 at NOON using card ending 1234.")
        assertNotNull(result)
        assertEquals("AED", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseAed("You spent AED 1,234.56 at SHARAF DG using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseAed("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `dhs notation is also recognized as an amount`() {
        val result = parseAed("You spent Dhs 25.00 at CAREEM using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.0, result!!.amount, 0.001)
    }
}
