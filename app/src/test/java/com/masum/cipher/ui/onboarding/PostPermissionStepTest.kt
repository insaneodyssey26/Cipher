package com.masum.cipher.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class PostPermissionStepTest {

    @Test
    fun aRestoreThatBroughtTrackedAppsFinishesSetup() {
        assertEquals(
            PostPermissionStep.FINISH,
            stepAfterPermissions(restoredFromBackup = true, hasTrackedApps = true)
        )
    }

    @Test
    fun aRestoreWithoutTrackedAppsStillAsksWhichAppsToTrack() {
        assertEquals(
            PostPermissionStep.SELECT_APPS,
            stepAfterPermissions(restoredFromBackup = true, hasTrackedApps = false)
        )
    }

    @Test
    fun aNormalSetupAlwaysAsksWhichAppsToTrack() {
        assertEquals(
            PostPermissionStep.SELECT_APPS,
            stepAfterPermissions(restoredFromBackup = false, hasTrackedApps = true)
        )
        assertEquals(
            PostPermissionStep.SELECT_APPS,
            stepAfterPermissions(restoredFromBackup = false, hasTrackedApps = false)
        )
    }
}
