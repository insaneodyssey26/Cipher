package com.masum.cipher.ui.onboarding

enum class PostPermissionStep { SELECT_APPS, FINISH }

fun stepAfterPermissions(restoredFromBackup: Boolean, hasTrackedApps: Boolean): PostPermissionStep =
    if (restoredFromBackup && hasTrackedApps) PostPermissionStep.FINISH else PostPermissionStep.SELECT_APPS
