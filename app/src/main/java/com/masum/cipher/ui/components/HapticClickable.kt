package com.masum.cipher.ui.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable as foundationClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import com.masum.cipher.core.util.performVibrate

object AppHaptics {
    @Volatile
    var enabled: Boolean = true

    @Volatile
    var view: android.view.View? = null

    fun tick() {
        view?.performVibrate(enabled)
    }
}

fun Modifier.clickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    this.foundationClickable(enabled = enabled, onClickLabel = onClickLabel, role = role) {
        view.performVibrate(AppHaptics.enabled)
        onClick()
    }
}

fun Modifier.clickable(
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    this.foundationClickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    ) {
        view.performVibrate(AppHaptics.enabled)
        onClick()
    }
}
