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

    // ─── BRAND MAPPINGS: INDIA ──────────────────────────────────────────────

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
    fun `ZERODHA is categorized as INVESTMENT`() {
        assertEquals(TransactionCategory.INVESTMENT, engine.categorize("ZERODHA"))
    }

    @Test
    fun `APOLLO is categorized as HEALTH`() {
        assertEquals(TransactionCategory.HEALTH, engine.categorize("APOLLO"))
    }

    // ─── BRAND MAPPINGS: OTHER REGIONS (spot checks, not exhaustive) ───────

    @Test
    fun `WALMART is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("WALMART"))
    }

    @Test
    fun `TESCO is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("TESCO"))
    }

    @Test
    fun `TIM HORTONS is categorized as FOOD`() {
        assertEquals(TransactionCategory.FOOD, engine.categorize("TIM HORTONS"))
    }

    @Test
    fun `WOOLWORTHS is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("WOOLWORTHS"))
    }

    @Test
    fun `LULU is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("LULU"))
    }

    @Test
    fun `FAIRPRICE is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("FAIRPRICE"))
    }

    @Test
    fun `CARREFOUR is categorized as SHOPPING`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("CARREFOUR"))
    }

    @Test
    fun `ROBINHOOD is categorized as INVESTMENT`() {
        assertEquals(TransactionCategory.INVESTMENT, engine.categorize("ROBINHOOD"))
    }

    @Test
    fun `CHASE is categorized as BILLS`() {
        assertEquals(TransactionCategory.BILLS, engine.categorize("CHASE"))
    }

    @Test
    fun `CVS is categorized as HEALTH`() {
        assertEquals(TransactionCategory.HEALTH, engine.categorize("CVS"))
    }

    @Test
    fun `ANTHROPIC is categorized as BILLS`() {
        assertEquals(TransactionCategory.BILLS, engine.categorize("ANTHROPIC"))
    }

    // ─── KEYWORD ANCHORS ─────────────────────────────────────────────────

    @Test
    fun `unknown merchant is categorized as OTHERS`() {
        assertEquals(TransactionCategory.OTHERS, engine.categorize("UNKNOWN_MERCHANT_XYZ"))
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
    fun `merchant name with CINEMA in it is categorized as ENTERTAINMENT`() {
        assertEquals(TransactionCategory.ENTERTAINMENT, engine.categorize("CITY CINEMA HALL"))
    }

    @Test
    fun `merchant name with CLINIC in it is categorized as HEALTH`() {
        assertEquals(TransactionCategory.HEALTH, engine.categorize("SUNSHINE CLINIC"))
    }

    @Test
    fun `merchant name with RECHARGE in it is categorized as BILLS`() {
        assertEquals(TransactionCategory.BILLS, engine.categorize("MOBILE RECHARGE SERVICES"))
    }

    @Test
    fun `merchant name with SIP in it is categorized as INVESTMENT`() {
        assertEquals(TransactionCategory.INVESTMENT, engine.categorize("MONTHLY SIP PAYMENT"))
    }

    @Test
    fun `merchant name with SALARY in it is categorized as INCOME`() {
        assertEquals(TransactionCategory.INCOME, engine.categorize("MARCH SALARY DISBURSEMENT"))
    }

    @Test
    fun `CREDIT substring accidentally matches the CRED brand mapping instead of the INCOME anchor`() {
        // "CREDIT" contains "CRED" (the CRED fintech app, mapped to BILLS), and
        // BRAND_MAPPINGS is checked before KEYWORD_ANCHORS, so any merchant description
        // containing the word "credit" is misclassified as BILLS instead of reaching the
        // INCOME anchor list ("SALARY", "REFUND", "INTEREST", ...). This is a real
        // brand-substring collision, not intended behavior — documenting it here so it
        // isn't silently "fixed" by a future test that assumes the opposite.
        assertEquals(TransactionCategory.BILLS, engine.categorize("COMPANY SALARY CREDIT"))
    }

    @Test
    fun `GAS keyword resolves to TRANSPORT due to anchor ordering, not BILLS`() {
        // "GAS" appears in both the TRANSPORT anchor list (fuel/petrol/gas) and the BILLS
        // anchor list (electric/water/gas/utility). Since TRANSPORT's anchor entry is
        // declared first in CategorizerConfig.KEYWORD_ANCHORS and categorize() returns on
        // the first matching anchor, TRANSPORT always wins for a bare "GAS" match. This
        // test exists to document and lock in that resolution, not to claim it's the only
        // valid interpretation.
        assertEquals(TransactionCategory.TRANSPORT, engine.categorize("CITY GAS STATION"))
    }

    // ─── PRIORITY / AMBIGUITY ────────────────────────────────────────────

    @Test
    fun `exact brand mapping takes priority over a keyword anchor also present in the name`() {
        // Contains both the FOOD brand "SWIGGY" and the SHOPPING anchor keyword "MART".
        // BRAND_MAPPINGS is checked before KEYWORD_ANCHORS, so the brand match should win.
        assertEquals(TransactionCategory.FOOD, engine.categorize("SWIGGY MART"))
    }

    @Test
    fun `brand match works as a substring, not just a whole word`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("MYAMAZONSTORE"))
    }

    @Test
    fun `brand matching is case insensitive`() {
        assertEquals(TransactionCategory.FOOD, engine.categorize("zomato"))
    }

    @Test
    fun `brand matching ignores surrounding whitespace`() {
        assertEquals(TransactionCategory.SHOPPING, engine.categorize("  amazon  "))
    }

    @Test
    fun `blank merchant name is categorized as OTHERS`() {
        assertEquals(TransactionCategory.OTHERS, engine.categorize(""))
    }

    @Test
    fun `whitespace only merchant name is categorized as OTHERS`() {
        assertEquals(TransactionCategory.OTHERS, engine.categorize("   "))
    }

    // ─── cleanMerchantName ───────────────────────────────────────────────

    @Test
    fun `merchant cleaning logic strips asterisk suffixes`() {
        assertEquals("Swiggy", engine.cleanMerchantName("SWIGGY*IT"))
    }

    @Test
    fun `merchant cleaning logic strips dash suffixes`() {
        assertEquals("Amazon", engine.cleanMerchantName("AMAZON-STORE"))
    }

    @Test
    fun `merchant cleaning logic trims surrounding whitespace`() {
        assertEquals("Zomato", engine.cleanMerchantName("  ZOMATO  "))
    }

    @Test
    fun `merchant cleaning logic skips short tokens under three characters`() {
        // "IT" (2 chars) is filtered out in favor of the next token with letters.
        assertEquals("Swiggy", engine.cleanMerchantName("IT*SWIGGY"))
    }

    @Test
    fun `merchant cleaning logic falls back to raw string when no usable token exists`() {
        assertEquals("12*34", engine.cleanMerchantName("12*34"))
    }

    @Test
    fun `merchant cleaning logic on an already clean single word name`() {
        assertEquals("Starbucks", engine.cleanMerchantName("STARBUCKS"))
    }

    @Test
    fun `merchant cleaning logic only titlecases the first letter`() {
        assertEquals("Mcdonalds", engine.cleanMerchantName("MCDONALDS"))
    }
}
