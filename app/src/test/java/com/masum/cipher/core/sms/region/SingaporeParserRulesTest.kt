package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SingaporeParserRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    private fun parseSgd(message: String) = parser.parse(message, preferredCurrency = "SGD")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseSgd("You spent S\$25.99 at FAIRPRICE using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
        assertEquals("FAIRPRICE", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseSgd("You received S\$150.00 from WEI LIN sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseSgd("123456 is your one-time passcode. Do not share it with anyone."))
    }

    @Test
    fun `available balance is not picked up as transaction amount`() {
        val result = parseSgd("Available balance S\$900.00. You spent S\$25.99 at GIANT using card ending 1234.")
        assertNotNull(result)
        assertEquals(25.99, result!!.amount, 0.001)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseSgd("You paid S\$15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseSgd("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `parsed transaction uses SGD as default currency`() {
        val result = parseSgd("You spent S\$25.99 at LAZADA using card ending 1234.")
        assertNotNull(result)
        assertEquals("SGD", result!!.currency)
    }

    @Test
    fun `amount with thousands separator is parsed correctly`() {
        val result = parseSgd("You spent S\$1,234.56 at SHOPEE using card ending 1234.")
        assertNotNull(result)
        assertEquals(1234.56, result!!.amount, 0.001)
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseSgd("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `paynow transfer is parsed correctly`() {
        val result = parseSgd("You paid S\$40.00 via PayNow to Corner Bakery.")
        assertNotNull(result)
        assertEquals(40.0, result!!.amount, 0.001)
    }
}
