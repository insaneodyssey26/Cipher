package com.masum.cipher.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppFormattersTest {

    // ─── isSuffixCurrency ────────────────────────────────────────────────

    @Test
    fun `euro symbol is a suffix currency for non english locales`() {
        assertTrue(AppFormatters.isSuffixCurrency("€", Locale.GERMANY))
    }

    @Test
    fun `euro symbol is a prefix currency for english locale`() {
        assertFalse(AppFormatters.isSuffixCurrency("€", Locale.US))
    }

    @Test
    fun `AED is always a suffix currency`() {
        assertTrue(AppFormatters.isSuffixCurrency("AED", Locale.US))
    }

    @Test
    fun `rupee symbol is a prefix currency`() {
        assertFalse(AppFormatters.isSuffixCurrency("₹", Locale.US))
    }

    @Test
    fun `dollar symbol is a prefix currency`() {
        assertFalse(AppFormatters.isSuffixCurrency("$", Locale.US))
    }

    // ─── formatAmountWithSymbol ──────────────────────────────────────────

    @Test
    fun `prefix currency places symbol before the amount`() {
        assertEquals("$100", AppFormatters.formatAmountWithSymbol("100", "$", Locale.US))
    }

    @Test
    fun `suffix currency places symbol after the amount with a space`() {
        assertEquals("100 AED", AppFormatters.formatAmountWithSymbol("100", "AED", Locale.US))
    }

    @Test
    fun `sign is placed before the symbol for prefix currencies`() {
        assertEquals("-$100", AppFormatters.formatAmountWithSymbol("100", "$", Locale.US, sign = "-"))
    }

    // ─── formatCurrency ──────────────────────────────────────────────────

    @Test
    fun `formatCurrency with zero decimals rounds to whole number`() {
        assertEquals("₹1,234", AppFormatters.formatCurrency(1234.0, "₹", Locale.US, decimals = 0))
    }

    @Test
    fun `formatCurrency with decimals shows the requested precision`() {
        assertEquals("₹1,234.50", AppFormatters.formatCurrency(1234.5, "₹", Locale.US, decimals = 2))
    }

    @Test
    fun `formatCurrency handles negative values with a leading minus`() {
        assertEquals("-₹500", AppFormatters.formatCurrency(-500.0, "₹", Locale.US, decimals = 0))
    }

    @Test
    fun `formatCurrency handles zero`() {
        assertEquals("₹0", AppFormatters.formatCurrency(0.0, "₹", Locale.US, decimals = 0))
    }

    @Test
    fun `formatCurrency adds thousands separators`() {
        assertEquals("₹1,000,000", AppFormatters.formatCurrency(1000000.0, "₹", Locale.US, decimals = 0))
    }

    // ─── formatCompactCurrency ───────────────────────────────────────────

    @Test
    fun `formatCompactCurrency below 100k shows the full number`() {
        assertEquals("₹999", AppFormatters.formatCompactCurrency(999.0, "₹", Locale.US))
    }

    @Test
    fun `formatCompactCurrency in the thousands uses k suffix`() {
        assertEquals("₹150k", AppFormatters.formatCompactCurrency(150_000.0, "₹", Locale.US))
    }

    @Test
    fun `formatCompactCurrency in the millions uses M suffix`() {
        assertEquals("₹1.5M", AppFormatters.formatCompactCurrency(1_500_000.0, "₹", Locale.US))
    }

    @Test
    fun `formatCompactCurrency in the billions uses B suffix`() {
        assertEquals("₹2B", AppFormatters.formatCompactCurrency(2_000_000_000.0, "₹", Locale.US))
    }

    @Test
    fun `formatCompactCurrency strips a trailing point zero`() {
        val result = AppFormatters.formatCompactCurrency(2_000_000.0, "₹", Locale.US)
        assertFalse(result.contains(".0M"))
        assertEquals("₹2M", result)
    }

    @Test
    fun `formatCompactCurrency handles negative values`() {
        val result = AppFormatters.formatCompactCurrency(-1_500_000.0, "₹", Locale.US)
        assertTrue(result.startsWith("-"))
    }

    @Test
    fun `formatCompactCurrency for japanese locale uses man for ten thousands`() {
        val result = AppFormatters.formatCompactCurrency(50_000.0, "¥", Locale.JAPAN)
        assertTrue(result.contains("万"))
    }

    @Test
    fun `formatCompactCurrency for japanese locale uses oku for hundred millions`() {
        val result = AppFormatters.formatCompactCurrency(200_000_000.0, "¥", Locale.JAPAN)
        assertTrue(result.contains("億"))
    }

    @Test
    fun `formatCompactCurrency for japanese locale below ten thousand shows full number`() {
        val result = AppFormatters.formatCompactCurrency(500.0, "¥", Locale.JAPAN)
        assertFalse(result.contains("万"))
    }
}
