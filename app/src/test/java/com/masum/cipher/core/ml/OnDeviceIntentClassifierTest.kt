package com.masum.cipher.core.ml

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.File
import java.io.FileInputStream

class OnDeviceIntentClassifierTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var classifier: OnDeviceIntentClassifier

    @Before
    fun setup() {
        context = mock(Context::class.java)
        assetManager = mock(AssetManager::class.java)
        `when`(context.assets).thenReturn(assetManager)

        val assetFile = File("src/main/assets/ml/intent_classifier.json")
        val streamFile = if (assetFile.exists()) assetFile else File("app/src/main/assets/ml/intent_classifier.json")
        `when`(assetManager.open("ml/intent_classifier.json")).thenAnswer {
            FileInputStream(streamFile)
        }

        classifier = OnDeviceIntentClassifier(context)
    }

    @Test
    fun `promotional GPay notification is classified as promotional`() {
        val text = "EMIs from ₹1,000 to fund your big purchase 🛍️ Take a personal loan of up to ₹40 lakh & enjoy low EMIs. Tap to learn more!"
        val result = classifier.classify(text)
        assertEquals(MessageIntent.PROMOTIONAL_MARKETING, result.intent)
        assertTrue(result.isPromotional)
    }

    @Test
    fun `credit card expense is classified as expense`() {
        val text = "Rs 500 spent on your credit card xx0032 at slice on 08-Oct"
        val result = classifier.classify(text)
        assertEquals(MessageIntent.TRANSACTION_EXPENSE, result.intent)
        assertTrue(result.isExpense)
    }

    @Test
    fun `salary credited is classified as income`() {
        val text = "INR 1,25,000.00 credited to your SBI a/c XX1234. NEFT transfer from INFOSYS LTD."
        val result = classifier.classify(text)
        assertEquals(MessageIntent.TRANSACTION_INCOME, result.intent)
        assertTrue(result.isIncome)
    }

    @Test
    fun `loan EMI debit is classified as expense`() {
        val text = "Your a/c no. XX1234 is debited for Rs.15000 on 05-Oct towards Loan EMI payment"
        val result = classifier.classify(text)
        assertEquals(MessageIntent.TRANSACTION_EXPENSE, result.intent)
        assertTrue(result.isExpense)
    }

    @Test
    fun `OTP message is classified as informational`() {
        val text = "Your OTP is 123456. Valid for 10 minutes. Do not share with anyone."
        val result = classifier.classify(text)
        assertEquals(MessageIntent.INFORMATIONAL_OTP, result.intent)
        assertTrue(result.isInformational)
    }

    @Test
    fun `pre-approved personal loan offer is classified as promotional`() {
        val text = "Congratulations! You are eligible for a personal loan of up to Rs. 10 Lakh. Tap to apply."
        val result = classifier.classify(text)
        assertEquals(MessageIntent.PROMOTIONAL_MARKETING, result.intent)
        assertTrue(result.isPromotional)
    }
}
