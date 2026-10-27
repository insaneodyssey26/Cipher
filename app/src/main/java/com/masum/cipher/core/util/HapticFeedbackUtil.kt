package com.masum.cipher.core.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

enum class HapticIntensity {
    LIGHT,
    MEDIUM,
    HEAVY
}

fun View.performVibrate(isHapticsEnabled: Boolean, isLongPress: Boolean = false) {
    if (!isHapticsEnabled) return
    performHaptic(isHapticsEnabled, if (isLongPress) HapticIntensity.HEAVY else HapticIntensity.LIGHT)
}

fun View.performHaptic(isHapticsEnabled: Boolean, intensity: HapticIntensity = HapticIntensity.LIGHT) {
    if (!isHapticsEnabled) return

    val feedbackConstant = when (intensity) {
        HapticIntensity.LIGHT -> HapticFeedbackConstants.KEYBOARD_TAP
        HapticIntensity.MEDIUM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.CONTEXT_CLICK
            }
        }
        HapticIntensity.HEAVY -> HapticFeedbackConstants.LONG_PRESS
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
                    val effectId = when (intensity) {
                        HapticIntensity.LIGHT -> VibrationEffect.EFFECT_CLICK
                        HapticIntensity.MEDIUM -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                VibrationEffect.EFFECT_CLICK
                            } else {
                                VibrationEffect.EFFECT_HEAVY_CLICK
                            }
                        }
                        HapticIntensity.HEAVY -> VibrationEffect.EFFECT_HEAVY_CLICK
                    }
                    vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                } else {
                    val duration = when (intensity) {
                        HapticIntensity.LIGHT -> 12L
                        HapticIntensity.MEDIUM -> 22L
                        HapticIntensity.HEAVY -> 40L
                    }
                    val amplitude = when (intensity) {
                        HapticIntensity.LIGHT -> 120
                        HapticIntensity.MEDIUM -> 180
                        HapticIntensity.HEAVY -> VibrationEffect.DEFAULT_AMPLITUDE
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                }
            }
        } catch (_: Exception) {}
    }
}
