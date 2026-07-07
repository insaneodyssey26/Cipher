package com.masum.cipher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Vault Motion System - Spring Primitives
 * 
 * We avoid linear movement. Everything is organic and responsive.
 */
object VaultMotion {
    // Snappy and bouncy for interactive elements (press/scale)
    val InteractiveSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 400f
    )

    // Smooth and controlled for layout transitions (entering screens)
    val LayoutSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 300f
    )

    // Fluid for data visualizations (charts)
    val DataSpring = spring<Float>(
        dampingRatio = 1.0f,
        stiffness = 100f
    )
}

/**
 * Animates a number from its current value to a new value using a rolling/counting effect.
 */
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
        text = prefix + String.format("%.2f", animatedValue),
        style = textStyle,
        color = color,
        modifier = modifier
    )
}

/**
 * Container that applies a staggered entrance animation to its content.
 * Uses rememberSaveable to ensure the animation only plays once per item life-cycle.
 */
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
        targetValue = if (visible) 0f else 20f,
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

// Inline helper for Box usage in Motion.kt
@Composable
private fun Box(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}
