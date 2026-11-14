package com.masum.cipher.ui.onboarding

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.ui.theme.CipherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomePageLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showWelcomePage(widthDp: Int, heightDp: Int, fontScale: Float) {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, fontScale)) {
                CipherTheme {
                    Box(modifier = Modifier.size(widthDp.dp, heightDp.dp)) {
                        WelcomeUnderTest()
                    }
                }
            }
        }
    }

    @Composable
    private fun WelcomeUnderTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WelcomePage(
            userPreferences = UserPreferences(context),
            isPro = false,
            proTier = "FREE",
            onProStatusChanged = {},
            onRestoreBackup = {},
            onNext = {}
        )
    }

    @Test
    fun aRegularPhoneShowsTheRestoreActionWithoutScrolling() {
        showWelcomePage(widthDp = 411, heightDp = 900, fontScale = 1f)

        composeRule.onNodeWithTag(WELCOME_RESTORE_BACKUP_TAG).assertIsDisplayed()
    }

    @Test
    fun aSmallPhoneWithTheLargestFontCanScrollToTheRestoreAction() {
        showWelcomePage(widthDp = 320, heightDp = 480, fontScale = 2f)

        composeRule.onNodeWithTag(WELCOME_RESTORE_BACKUP_TAG).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun aNarrowPhoneKeepsTheRestoreActionInsideTheScreenWidth() {
        showWelcomePage(widthDp = 320, heightDp = 900, fontScale = 1f)

        val restore = composeRule.onNodeWithTag(WELCOME_RESTORE_BACKUP_TAG).getUnclippedBoundsInRoot()

        assertTrue(restore.left >= 0.dp)
        assertTrue(restore.right <= 320.dp)
    }
}
