package com.masum.cipher.core.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SmsParserTest2 {
    @Test
    fun testParseReceivedInformalMessage() {
        val parser = SmsParser()
        val result = parser.parse("received 400 rs from her")
        assertNotNull("Result should not be null", result)
        assertEquals(400.0, result?.amount)
        assertEquals("HER", result?.merchant)
    }
}
