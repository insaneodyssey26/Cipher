package com.masum.cipher.core.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

fun View.performVibrate(isHapticsEnabled: Boolean, isLongPress: Boolean = false) {
    if (!isHapticsEnabled) return

    val feedbackConstant = if (isLongPress) {
        HapticFeedbackConstants.LONG_PRESS
    } else {
        HapticFeedbackConstants.KEYBOARD_TAP
    }

    val handled = this.performHapticFeedback(feedbackConstant)

    if (!handled) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Vibrator::class.java)
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effectId = if (isLongPress) {
                        VibrationEffect.EFFECT_HEAVY_CLICK
                    } else {
                        VibrationEffect.EFFECT_CLICK
                    }
                    vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = if (isLongPress) 40L else 12L
                    val amplitude = if (isLongPress) VibrationEffect.DEFAULT_AMPLITUDE else 120
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                }
            }
        } catch (_: Exception) {}
    }
}
