package com.masum.cipher.core.sms.region

import com.masum.cipher.core.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GlobalFallbackRulesTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setup() {
        parser = TransactionParser()
    }

    // JPY is not one of the explicitly mapped currencies, so this always routes through
    // the fallback rule chain, with GlobalFallbackRules tried first.
    private fun parseUnmapped(message: String) = parser.parse(message, preferredCurrency = "JPY")

    @Test
    fun `debit with brand dictionary merchant is parsed correctly`() {
        val result = parseUnmapped("You spent \$45.99 at NETFLIX using card ending 1234.")
        assertNotNull(result)
        assertEquals(45.99, result!!.amount, 0.001)
        assertEquals("NETFLIX", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `credit received from someone is marked as income`() {
        val result = parseUnmapped("You received \$150.00 from A FRIEND sent to your account.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parseUnmapped("123456 is your security code. Do not share it with anyone."))
    }

    @Test
    fun `non transactional message returns null`() {
        assertNull(parseUnmapped("Your appointment is confirmed for 3pm tomorrow. See you soon."))
    }

    @Test
    fun `resolved transaction uses fallback default currency`() {
        val result = parseUnmapped("You spent \$45.99 at STARBUCKS using card ending 1234.")
        assertNotNull(result)
        assertEquals("USD", result!!.currency)
    }

    @Test
    fun `structural merchant extraction works for non dictionary merchant`() {
        val result = parseUnmapped("You paid \$15.00 to Corner Bakery using card ending 1234.")
        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertTrue(result.merchant.isNotBlank())
    }

    @Test
    fun `promotional sms with reward points is rejected`() {
        assertNull(parseUnmapped("You earned reward points on your last purchase! Claim your special offer now."))
    }

    @Test
    fun `unmapped currency still parses a recognizable indian format debit as a fallback candidate`() {
        // Confirms the chain-of-regions fallback finds a match further down the chain
        // (India's rules) for an unmapped currency when the global rules alone don't match
        // amount+merchant with high confidence, since evidence still requires a debit keyword.
        val result = parser.parse("Rs.500.00 debited from a/c XX1234 at ZOMATO.", preferredCurrency = "JPY")
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.001)
    }
}
