package com.masum.cipher.core.util

import android.view.HapticFeedbackConstants
import android.view.View

fun View.performVibrate(isHapticsEnabled: Boolean, isLongPress: Boolean = false) {
    if (isHapticsEnabled) {
        val feedbackConstant = if (isLongPress) {
            HapticFeedbackConstants.LONG_PRESS
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        
        this.performHapticFeedback(
            feedbackConstant,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}
