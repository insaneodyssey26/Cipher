package com.masum.cipher.core.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmsParserTest {

    private lateinit var parser: SmsParser

    @Before
    fun setup() {
        parser = SmsParser()
    }

    // ─── REJECTION ────────────────────────────────────────────────────────────

    @Test
    fun `OTP sms is rejected`() {
        assertNull(parser.parse("Your OTP is 123456. Valid for 10 minutes. Do not share with anyone."))
    }

    @Test
    fun `verification code sms is rejected`() {
        assertNull(parser.parse("123456 is your verification code for SBI YONO. Do not share."))
    }

    @Test
    fun `promo win sms is rejected`() {
        assertNull(parser.parse("Congratulations! You have won Rs.10000 in our lucky draw. Claim now."))
    }

    @Test
    fun `validity offer sms is rejected`() {
        assertNull(parser.parse("Special offer: Get 50% off. Validity 24 hours only. Limited seats."))
    }

    @Test
    fun `sms with call in footer is NOT rejected`() {
        assertNotNull(parser.parse("Rs.500.00 debited from a/c XX1234. Avl Bal Rs.15000. Call 18001080 if not done by you."))
    }

    @Test
    fun `sms with customer service is NOT rejected`() {
        assertNotNull(parser.parse("INR 999.00 debited from your account XX1234 at NETFLIX. For customer service visit hdfc.com."))
    }

    @Test
    fun `sms with report fraud is NOT rejected`() {
        assertNotNull(parser.parse("Rs.350 debited from a/c XX1234 at ZOMATO. Report fraud at icici.com/report."))
    }

    // ─── AMOUNT EXTRACTION ────────────────────────────────────────────────────

    @Test
    fun `SBI UPI debit with Rs dot format`() {
        val result = parser.parse("Rs.500.00 debited from a/c XX1234 on 01-06-26. Info: UPI/123456789012/SWIGGY/swiggy@okicici. Avl Bal: Rs.15000.00. Call 18001111 if not done by you.")
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.001)
    }

    @Test
    fun `HDFC credit card spent with commas`() {
        val result = parser.parse("Rs.1,500 spent on HDFC Bank Credit Card XX1234 at AMAZON on 01-Jun-26. Avl limit: Rs.48,500.")
        assertNotNull(result)
        assertEquals(1500.0, result!!.amount, 0.001)
    }

    @Test
    fun `Axis Bank INR format`() {
        val result = parser.parse("INR 2,500.00 debited from Axis Bank a/c XX1234 at FLIPKART on 01/06/2026. Available Balance is INR 12,500.00.")
        assertNotNull(result)
        assertEquals(2500.0, result!!.amount, 0.001)
    }

    @Test
    fun `Kotak UPI txn of format`() {
        val result = parser.parse("You have done a UPI txn of Rs.350.00 from Kotak Bank AC XXXXXXXX to ZOMATO@okicici on 01/06/2026. UPI Ref No:123456789012345.")
        assertNotNull(result)
        assertEquals(350.0, result!!.amount, 0.001)
    }

    @Test
    fun `amount before verb format`() {
        val result = parser.parse("500.00 debited from your account XX1234. Avl Bal 12000.00.")
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.001)
    }

    @Test
    fun `avl bal is not picked up as transaction amount`() {
        val result = parser.parse("Avl Bal Rs.15000. Rs.500 debited from a/c XX1234 at ZOMATO.")
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.001)
    }

    @Test
    fun `account number digits are not picked up as amount`() {
        val result = parser.parse("Rs.250 debited from a/c no. 1234567890 at SWIGGY. Avl Bal Rs.5000.")
        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.001)
    }

    @Test
    fun `large amount with lakhs and commas`() {
        val result = parser.parse("INR 1,25,000.00 credited to your SBI a/c XX1234. NEFT transfer.")
        assertNotNull(result)
        assertEquals(125000.0, result!!.amount, 0.001)
    }

    @Test
    fun `Yes Bank debit with INR`() {
        val result = parser.parse("Dear Customer, INR 999.00 has been debited from your Yes Bank Account XX1234 on 01-Jun-26 for SPOTIFY.")
        assertNotNull(result)
        assertEquals(999.0, result!!.amount, 0.001)
    }

    // ─── MERCHANT — BRAND DICTIONARY ──────────────────────────────────────────

    @Test
    fun `AMAZON is not corrupted to AMAZ`() {
        val result = parser.parse("Rs.1,500 spent on HDFC Bank Credit Card XX1234 at AMAZON on 01-Jun-26.")
        assertNotNull(result)
        assertEquals("AMAZON", result!!.merchant)
    }

    @Test
    fun `PHONEPE is not corrupted to PH`() {
        val result = parser.parse("Rs.100.00 sent to PHONEPE via UPI on 01-Jun-26. UPI Ref 123456789.")
        assertNotNull(result)
        assertEquals("PHONEPE", result!!.merchant)
    }

    @Test
    fun `VODAFONE is not corrupted to VODAF`() {
        val result = parser.parse("Rs.399.00 paid to VODAFONE for prepaid recharge. Txn ID 987654321.")
        assertNotNull(result)
        assertEquals("VODAFONE", result!!.merchant)
    }

    @Test
    fun `ZOMATO detected from brand dictionary`() {
        val result = parser.parse("Rs.350.00 debited from a/c XX1234 at ZOMATO on 01-Jun-26. Avl Bal Rs.5000.")
        assertNotNull(result)
        assertEquals("ZOMATO", result!!.merchant)
    }

    @Test
    fun `SWIGGY detected from brand dictionary`() {
        val result = parser.parse("Your ICICI Bank a/c XX1234 debited for Rs.280.00 at SWIGGY on 01-Jun-26. Call 18001080 if not done by you.")
        assertNotNull(result)
        assertEquals("SWIGGY", result!!.merchant)
    }

    @Test
    fun `NETFLIX detected from brand dictionary`() {
        val result = parser.parse("INR 649.00 debited from your HDFC a/c XX1234 at NETFLIX on 01-Jun-26.")
        assertNotNull(result)
        assertEquals("NETFLIX", result!!.merchant)
    }

    @Test
    fun `IRCTC detected from brand dictionary`() {
        val result = parser.parse("Rs.1250.00 paid to IRCTC for train ticket booking. UPI Ref 123456789012.")
        assertNotNull(result)
        assertEquals("IRCTC", result!!.merchant)
    }

    // ─── MERCHANT — VPA EXTRACTION ────────────────────────────────────────────

    @Test
    fun `ZOMATO extracted from VPA zomato@okicici`() {
        val result = parser.parse("Rs.350.00 debited to zomato@okicici on 01-Jun-26. UPI Ref 123456789012.")
        assertNotNull(result)
        assertEquals("ZOMATO", result!!.merchant)
    }

    @Test
    fun `local merchant extracted from VPA`() {
        val result = parser.parse("Rs.150.00 paid to localcafe@oksbi via UPI. Ref 123456789.")
        assertNotNull(result)
        assertEquals("LOCALCAFE", result!!.merchant)
    }

    @Test
    fun `phone number VPA is not used as merchant`() {
        val result = parser.parse("Rs.500.00 sent to 9876543210@ybl via UPI. Ref 123456789.")
        assertNotNull(result)
        assertTrue(result!!.merchant != "9876543210")
    }

    // ─── MERCHANT — UPI INFO STRING ───────────────────────────────────────────

    @Test
    fun `merchant extracted from SBI UPI info string`() {
        val result = parser.parse("Rs.280.00 debited from SBI a/c XX1234. Info: UPI/123456789012/SWIGGY ORDERS/SWIGGYIT@okaxis. Avl Bal Rs.10000. Call 18001111.")
        assertNotNull(result)
        assertEquals("SWIGGY", result!!.merchant)
    }

    @Test
    fun `merchant extracted from UPI info string when not in brand dict`() {
        val result = parser.parse("Rs.200.00 debited from a/c XX1234. Info: UPI/987654321098/HALDIRAMS STORE/haldirams@okhdfc. Avl Bal Rs.8000.")
        assertNotNull(result)
        assertEquals("HALDIRAMS STORE", result!!.merchant)
    }

    // ─── MERCHANT — STRUCTURAL ────────────────────────────────────────────────

    @Test
    fun `merchant extracted via at keyword`() {
        val result = parser.parse("Rs.500.00 spent at Big Bazar on 01-Jun-26 using HDFC Card XX1234.")
        assertNotNull(result)
        assertEquals("BIG BAZAR", result!!.merchant)
    }

    @Test
    fun `your account is not captured as merchant`() {
        val result = parser.parse("Rs.5000.00 transferred to your account XX5678 from a/c XX1234.")
        assertNotNull(result)
        assertTrue(result!!.merchant != "YOUR ACCOUNT")
    }

    @Test
    fun `fallback to miscellaneous when no merchant found`() {
        val result = parser.parse("Rs.100.00 debited from a/c XX1234.")
        assertNotNull(result)
        assertEquals("MISCELLANEOUS", result!!.merchant)
    }

    // ─── INCOME vs EXPENSE ────────────────────────────────────────────────────

    @Test
    fun `salary credit is income`() {
        val result = parser.parse("Rs.45000.00 credited to your SBI a/c XX1234 on 01-Jun-26. Info: NEFT/Salary/EMPLOYER NAME. Avl Bal Rs.50000.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `cashback is income`() {
        val result = parser.parse("Cashback of Rs.50.00 credited to your HDFC a/c XX1234. UPI Ref 123456.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `refund is income`() {
        val result = parser.parse("Rs.499.00 refunded to your a/c XX1234 by AMAZON. Avl Bal Rs.15000.")
        assertNotNull(result)
        assertTrue(result!!.isIncome)
    }

    @Test
    fun `debit with credited in same sms is expense not income`() {
        val result = parser.parse("Your SBI a/c XX1234 is debited with INR 500.00 on 01/06/2026 and a/c YY5678 is credited. UPI Ref 123456789.")
        assertNotNull(result)
        assertTrue(!result!!.isIncome)
    }

    @Test
    fun `UPI payment expense is not income`() {
        val result = parser.parse("Rs.350.00 debited from Axis Bank a/c XX1234 at ZOMATO on 01/06/2026. Avl Bal Rs.12000.")
        assertNotNull(result)
        assertTrue(!result!!.isIncome)
    }

    @Test
    fun `ICICI transfer debit is expense`() {
        val result = parser.parse("ICICI Bank Acct XX1234 debited for Rs 500.00 on 01-Jun-2026; SWIGGY credited. If not done by you, call 18001080.")
        assertNotNull(result)
        assertTrue(!result!!.isIncome)
    }

    // ─── FULL PARSE — REAL BANK SMS ───────────────────────────────────────────

    @Test
    fun `full SBI UPI parse`() {
        val result = parser.parse("Dear UPI user, Rs.499.00 debited from a/c XX1234 on 01-Jun-26. Info: UPI/123456789012/NETFLIX/netflix@okhdfc. Avl Bal: Rs.12501.00. Call 1800111 to report.")
        assertNotNull(result)
        assertEquals(499.0, result!!.amount, 0.001)
        assertEquals("NETFLIX", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `full HDFC credit card parse`() {
        val result = parser.parse("Rs.2,999 spent on HDFC Bank Credit Card XX1234 at FLIPKART on 01-Jun-26 at 15:30. Avl limit: Rs.47,001. To block card SMS BLOCK to 5676712.")
        assertNotNull(result)
        assertEquals(2999.0, result!!.amount, 0.001)
        assertEquals("FLIPKART", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `full Kotak UPI parse`() {
        val result = parser.parse("You have done a UPI txn of Rs.649.00 from Kotak Bank AC XXXXXXXX1234 to SPOTIFY@okicici on 01/06/2026 20:15:00. UPI Ref No:123456789012345.")
        assertNotNull(result)
        assertEquals(649.0, result!!.amount, 0.001)
        assertEquals("SPOTIFY", result.merchant)
        assertTrue(!result.isIncome)
    }

    @Test
    fun `full ICICI salary credit parse`() {
        val result = parser.parse("INR 55000.00 credited to ICICI Bank Acct XX1234 on 01-Jun-26. Info: NEFT/SALARY JUNE/EMPLOYER INDIA PVT LTD. Avl Bal INR 60000.00.")
        assertNotNull(result)
        assertEquals(55000.0, result!!.amount, 0.001)
        assertTrue(result.isIncome)
    }

    @Test
    fun `full Axis UPI debit parse`() {
        val result = parser.parse("INR 380.00 debited from Axis Bank a/c XX1234 on 01-Jun-26 for UPI payment to SWIGGY@okaxis. Call 18004195555 for queries.")
        assertNotNull(result)
        assertEquals(380.0, result!!.amount, 0.001)
        assertEquals("SWIGGY", result.merchant)
        assertTrue(!result.isIncome)
    }
}
