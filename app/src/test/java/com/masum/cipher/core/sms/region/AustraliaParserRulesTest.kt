package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AustraliaParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseAud(message: String) = parser.parse(message, preferredCurrency = "AUD")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseAud("You spent \$25.99 at WOOLWORTHS using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
        assertEquals("WOOLWORTHS", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseAud("You received \$150.00 from SAM WILSON sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseAud("123456 is your one-time passcode. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseAud("Available balance \$900.00. You spent \$25.99 at COLES using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseAud("You paid \$15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseAud("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses AUD as default currency`() {
        val result = parseAud("You spent \$25.99 at BUNNINGS using card ending 1234.")
        assertNotNull(result)
        assertEquals("AUD", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseAud("You spent \$1,234.56 at HARVEY NORMAN using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseAud("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `payid transfer is parsed correctly`() {
        val result = parseAud("You paid \$40.00 via PayID to Corner Bakery.")
        assertNotNull(result)
        assertEquals(40.0, result!!.amount, 0.001)
    }
}
