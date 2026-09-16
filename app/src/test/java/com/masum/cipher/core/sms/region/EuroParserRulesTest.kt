package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EuroParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseEur(message: String) = parser.parse(message, preferredCurrency = "EUR")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseEur("You spent €25.99 at CARREFOUR using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
        assertEquals("CARREFOUR", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseEur("You received €150.00 from ANNA MUELLER sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseEur("123456 is your one-time passcode. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseEur("Available balance €900.00. You spent €25.99 at LIDL using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseEur("You paid €15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseEur("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses EUR as default currency`() {
        val result = parseEur("You spent €25.99 at IKEA using card ending 1234.")
        assertNotNull(result)
        assertEquals("EUR", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseEur("You spent €1,234.56 at DECATHLON using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseEur("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `charged at brand dictionary merchant is parsed`() {
        val result = parseEur("Your card was charged €4.50 at ALDI on 06/01.")
        assertNotNull(result)
        assertEquals(4.50, result!!.amount, 0.001)
        assertEquals("ALDI", result.merchant)
    }
}
