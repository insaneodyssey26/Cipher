package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.model.TransactionCategory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CategorizerEngineTest {

    private lateinit var engine: CategorizerEngine

    @Before
    fun setup() {
        engine = CategorizerEngine()
    }

    @Test
    fun `AMAZON is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("AMAZON"))
    }

    @Test
    fun `ZOMATO is categorized as FOOD`() {
        assertEquals(TransactionCategory.FOOD, engine.categorize("ZOMATO"))
    }

    @Test
    fun `UBER is categorized as TRANSPORT`() {
        assertEquals(TransactionCategory.TRANSPORT, engine.categorize("UBER"))
    }

    @Test
    fun `NETFLIX is categorized as ENTERTAINMENT`() {
        assertEquals(TransactionCategory.ENTERTAINMENT, engine.categorize("NETFLIX"))
    }

    @Test
    fun `JIO is categorized as BILLS`() {
        assertEquals(TransactionCategory.BILLS, engine.categorize("JIO"))
    }

    @Test
    fun `unknown merchant is categorized as OTHERS`() {
        val result = engine.categorize("UNKNOWN_MERCHANT_XYZ")
        assertEquals(TransactionCategory.OTHERS, result)
    }

    @Test
    fun `merchant name with CAFE in it is categorized as FOOD`() {
        assertEquals(TransactionCategory.FOOD, engine.categorize("LOCAL_CITY_CAFE"))
    }

    @Test
    fun `merchant name with MART in it is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("NEW_GROCERY_MART"))
    }

    @Test
    fun `merchant cleaning logic works correctly`() {
        assertEquals("Swiggy", engine.cleanMerchantName("SWIGGY*IT"))
        assertEquals("Amazon", engine.cleanMerchantName("AMAZON-STORE"))
        assertEquals("Zomato", engine.cleanMerchantName("  ZOMATO  "))
    }
}
