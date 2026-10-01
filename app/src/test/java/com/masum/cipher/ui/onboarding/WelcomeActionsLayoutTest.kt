package com.masum.cipher.ui.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeActionsLayoutTest {

    @Test
    fun narrowPhonesStackTheButtons() {
        assertTrue(shouldStackWelcomeActions(availableWidthDp = 320f, fontScale = 1f))
        assertTrue(shouldStackWelcomeActions(availableWidthDp = 359f, fontScale = 1f))
    }

    @Test
    fun largeFontSizesStackTheButtonsEvenOnWidePhones() {
        assertTrue(shouldStackWelcomeActions(availableWidthDp = 411f, fontScale = 1.3f))
        assertTrue(shouldStackWelcomeActions(availableWidthDp = 600f, fontScale = 2f))
    }

    @Test
    fun regularPhonesKeepTheButtonsSideBySide() {
        assertFalse(shouldStackWelcomeActions(availableWidthDp = 360f, fontScale = 1f))
        assertFalse(shouldStackWelcomeActions(availableWidthDp = 411f, fontScale = 1.15f))
    }
}
