package com.masum.cipher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

object VaultMotion {
    val InteractiveSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 400f
    )

    val LayoutSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 300f
    )

    val DataSpring = spring<Float>(
        dampingRatio = 1.0f,
        stiffness = 100f
    )
}

@Composable
fun AnimatedNumberTicker(
    value: Double,
    modifier: Modifier = Modifier,
    prefix: String = "",
    textStyle: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color = textStyle.color
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = VaultMotion.DataSpring,
        label = "NumberTicker"
    )

    androidx.compose.material3.Text(
        text = prefix + String.format(java.util.Locale.getDefault(), "%,.2f", animatedValue),
        style = textStyle,
        color = color,
        modifier = modifier,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
    )
}

@Composable
fun StaggeredEntranceItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(hasAnimated) }
    
    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            kotlinx.coroutines.delay(index * 50L)
            visible = true
            hasAnimated = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "Alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = VaultMotion.LayoutSpring,
        label = "OffsetY"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = offsetY
        }
    ) {
        content()
    }
}

fun Modifier.bounceClick(
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = VaultMotion.InteractiveSpring,
        label = "BounceClick"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

@Composable
private fun Box(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}
