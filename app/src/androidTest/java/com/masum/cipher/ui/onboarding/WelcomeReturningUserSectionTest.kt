package com.masum.cipher.ui.onboarding

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.masum.cipher.ui.theme.CipherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeReturningUserSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var restoreClicks = 0

    private fun show() {
        composeRule.setContent {
            CipherTheme {
                WelcomeReturningUserSection(onRestoreBackup = { restoreClicks++ })
            }
        }
    }

    @Test
    fun restoreActionIsVisibleAndRespondsToTaps() {
        show()

        composeRule.onNodeWithTag(WELCOME_RESTORE_BACKUP_TAG).assertIsDisplayed().assertHasClickAction().performClick()

        assertEquals(1, restoreClicks)
    }

    @Test
    fun restoreActionMeetsTheMinimumTouchTargetHeight() {
        show()

        composeRule.onNodeWithTag(WELCOME_RESTORE_BACKUP_TAG).assertHeightIsAtLeast(48.dp)
    }
}
