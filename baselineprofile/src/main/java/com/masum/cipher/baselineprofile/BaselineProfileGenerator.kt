package com.masum.cipher.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

private const val TARGET_PACKAGE = "com.masum.cipher"
private const val UI_TIMEOUT_MS = 4_000L
private const val TRANSITION_PAUSE_MS = 500L
private const val MAX_ONBOARDING_STEPS = 40
private const val SWIPE_STEPS = 30
private const val SLIDE_START_FRACTION = 0.15f
private const val SLIDE_END_FRACTION = 0.92f
private const val SCROLL_LOWER_FRACTION = 0.75f
private const val SCROLL_UPPER_FRACTION = 0.30f
private const val SLIDE_HINT = "Slide to open vault"
private const val DASHBOARD_MARKER = "Search"

private val ONBOARDING_ACTIONS = listOf(
    "Begin",
    "Continue",
    "Next",
    "Got It",
    "Finish Setup",
    "Enter Manually",
    "Proceed Without Alerts"
)

private val INSIGHTS_TABS = listOf("Habits", "Recurring", "Spending")
private val SETTINGS_SECTIONS = listOf("Appearance", "Security & privacy", "Data & backup", "About & support")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupProfile() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        maxIterations = 3,
        stableIterations = 2,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }

    @Test
    fun userJourneyProfile() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        maxIterations = 5,
        stableIterations = 2,
        includeInStartupProfile = false
    ) {
        pressHome()
        startActivityAndWait()
        completeOnboardingIfShown()
        browseMainScreens()
    }
}

private fun MacrobenchmarkScope.pause() {
    Thread.sleep(TRANSITION_PAUSE_MS)
    device.waitForIdle()
}

private fun MacrobenchmarkScope.completeOnboardingIfShown() {
    if (!device.wait(Until.hasObject(By.text("Begin")), UI_TIMEOUT_MS)) return

    repeat(MAX_ONBOARDING_STEPS) {
        if (device.hasObject(By.textContains(SLIDE_HINT)) && slideToOpenVault()) return
        ONBOARDING_ACTIONS.any { label -> clickQuietly(By.text(label)) }
        pause()
    }
}

private fun MacrobenchmarkScope.slideToOpenVault(): Boolean {
    val y = try {
        device.findObject(By.textContains(SLIDE_HINT))?.visibleCenter?.y
    } catch (_: StaleObjectException) {
        null
    } ?: return false
    val width = device.displayWidth
    device.swipe((width * SLIDE_START_FRACTION).toInt(), y, (width * SLIDE_END_FRACTION).toInt(), y, SWIPE_STEPS)
    device.wait(Until.hasObject(By.textContains(DASHBOARD_MARKER)), UI_TIMEOUT_MS)
    pause()
    return true
}

private fun MacrobenchmarkScope.browseMainScreens() {
    device.wait(Until.hasObject(By.textContains(DASHBOARD_MARKER)), UI_TIMEOUT_MS)
    scrollCurrentList()

    openNavItem("Insights")
    INSIGHTS_TABS.forEach { tab ->
        clickIfPresent(By.text(tab))
        scrollCurrentList()
    }

    openNavItem("Splits")
    scrollCurrentList()

    openNavItem("Settings")
    SETTINGS_SECTIONS.forEach { section -> clickIfPresent(By.text(section)) }
    scrollCurrentList()

    openAddTransactionSheet()
}

private fun MacrobenchmarkScope.openNavItem(description: String) {
    clickIfPresent(By.desc(description))
}

private fun MacrobenchmarkScope.scrollCurrentList() {
    val x = device.displayWidth / 2
    val lower = (device.displayHeight * SCROLL_LOWER_FRACTION).toInt()
    val upper = (device.displayHeight * SCROLL_UPPER_FRACTION).toInt()
    device.swipe(x, lower, x, upper, SWIPE_STEPS)
    pause()
    device.swipe(x, upper, x, lower, SWIPE_STEPS)
    pause()
}

private fun MacrobenchmarkScope.clickQuietly(selector: BySelector): Boolean =
    try {
        device.findObject(selector)?.click() != null
    } catch (_: StaleObjectException) {
        true
    }

private fun MacrobenchmarkScope.clickIfPresent(selector: BySelector) {
    clickQuietly(selector)
    pause()
}

private fun MacrobenchmarkScope.openAddTransactionSheet() {
    clickIfPresent(By.desc("Add Transaction"))
    device.pressBack()
    pause()
}
