package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CanadaParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseCad(message: String) = parser.parse(message, preferredCurrency = "CAD")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseCad("You spent \$25.99 at TIM HORTONS using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
        assertEquals("TIM HORTONS", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseCad("You received \$150.00 from ALEX CHEN sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseCad("123456 is your one-time passcode. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseCad("Available balance \$900.00. You spent \$25.99 at LOBLAWS using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseCad("You paid \$15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseCad("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses CAD as default currency`() {
        val result = parseCad("You spent \$25.99 at CANADIAN TIRE using card ending 1234.")
        assertNotNull(result)
        assertEquals("CAD", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseCad("You spent \$1,234.56 at BEST BUY using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseCad("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `interac e-transfer received is marked as income`() {
        val result = parseCad("Interac e-transfer of \$75.00 received from SAM PATEL.")
        assertNotNull(result)
        assertEquals(75.0, result!!.amount, 0.001)
        assertTrue(result.isIncome)
    }
}
