package com.masum.cipher.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.masum.cipher",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5_000)
        device.waitForIdle(3_000)
    }
}
