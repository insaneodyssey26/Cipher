package com.masum.cipher.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun `simple addition`() {
        assertEquals(15.0, MathEvaluator.evaluate("10+5")!!, 0.0001)
    }

    @Test
    fun `simple subtraction`() {
        assertEquals(5.0, MathEvaluator.evaluate("10-5")!!, 0.0001)
    }

    @Test
    fun `simple multiplication`() {
        assertEquals(50.0, MathEvaluator.evaluate("10*5")!!, 0.0001)
    }

    @Test
    fun `simple division`() {
        assertEquals(2.0, MathEvaluator.evaluate("10/5")!!, 0.0001)
    }

    @Test
    fun `multiplication has higher precedence than addition`() {
        assertEquals(20.0, MathEvaluator.evaluate("10+2*5")!!, 0.0001)
    }

    @Test
    fun `parentheses override precedence`() {
        assertEquals(60.0, MathEvaluator.evaluate("(10+2)*5")!!, 0.0001)
    }

    @Test
    fun `nested parentheses are evaluated correctly`() {
        assertEquals(14.0, MathEvaluator.evaluate("2*((1+2)*2+1)")!!, 0.0001)
    }

    @Test
    fun `decimal numbers are supported`() {
        assertEquals(3.5, MathEvaluator.evaluate("1.5+2")!!, 0.0001)
    }

    @Test
    fun `leading unary minus is supported`() {
        // Result is clamped to a non-negative range, so a net-negative expression clamps to 0.
        assertEquals(0.0, MathEvaluator.evaluate("-5+2")!!, 0.0001)
    }

    @Test
    fun `whitespace inside the expression is ignored`() {
        assertEquals(15.0, MathEvaluator.evaluate(" 10 + 5 ")!!, 0.0001)
    }

    @Test
    fun `division by zero returns null instead of infinity`() {
        assertNull(MathEvaluator.evaluate("10/0"))
    }

    @Test
    fun `empty expression returns null`() {
        assertNull(MathEvaluator.evaluate(""))
    }

    @Test
    fun `malformed expression returns null instead of throwing`() {
        assertNull(MathEvaluator.evaluate("10+*5"))
    }

    @Test
    fun `unbalanced parentheses does not crash and returns a best-effort result or null`() {
        // Should not throw regardless of the outcome.
        MathEvaluator.evaluate("(10+5")
    }

    @Test
    fun `non numeric characters are rejected`() {
        assertNull(MathEvaluator.evaluate("10+abc"))
    }

    @Test
    fun `injection style input with letters is rejected`() {
        assertNull(MathEvaluator.evaluate("System.exit(0)"))
    }

    @Test
    fun `result is clamped to a non negative value`() {
        assertEquals(0.0, MathEvaluator.evaluate("5-10")!!, 0.0001)
    }

    @Test
    fun `result above the clamp ceiling is coerced down`() {
        val result = MathEvaluator.evaluate("999999999999999*2")
        assertEquals(999_999_999_999.0, result!!, 0.0001)
    }

    @Test
    fun `plain integer passes through unchanged`() {
        assertEquals(42.0, MathEvaluator.evaluate("42")!!, 0.0001)
    }
}
