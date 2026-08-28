package com.masum.cipher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

object VaultMotion {
    val InteractiveSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 400f
    )

    val LayoutSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 300f
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
    var oldValue by remember { androidx.compose.runtime.mutableDoubleStateOf(value) }
    val isCountingUp = value >= oldValue
    LaunchedEffect(value) { oldValue = value }

    val formattedString = String.format(java.util.Locale.US, "%,.2f", value)
    
    val tabularStyle = textStyle.copy(fontFeatureSettings = "tnum")

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            androidx.compose.material3.Text(
                text = prefix,
                style = tabularStyle,
                color = color
            )
        }
        
        formattedString.forEachIndexed { index, char ->
            val distFromEnd = formattedString.length - index
            androidx.compose.runtime.key(distFromEnd) {
                if (char.isDigit()) {
                    AnimatedDigit(
                        digit = char,
                        isCountingUp = isCountingUp,
                        textStyle = tabularStyle,
                        color = color
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = char.toString(),
                        style = tabularStyle,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedDigit(
    digit: Char,
    isCountingUp: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color
) {
    Box(modifier = Modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                if (isCountingUp) {
                    (slideInVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) { -it } + fadeIn(tween(150)))
                        .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) { it } + fadeOut(tween(150)))
                } else {
                    (slideInVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) { it } + fadeIn(tween(150)))
                        .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) { -it } + fadeOut(tween(150)))
                }.using(SizeTransform(clip = false))
            },
            label = "DigitTumbler"
        ) { targetDigit ->
            androidx.compose.material3.Text(
                text = targetDigit.toString(),
                style = textStyle,
                color = color,
                modifier = Modifier.padding(horizontal = 0.5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun StaggeredEntranceItem(
    index: Int,
    content: @Composable () -> Unit
) {
    if (index >= 5) {
        content()
        return
    }

    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(hasAnimated) }
    
    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            kotlinx.coroutines.delay((index * 25L).milliseconds)
            visible = true
            hasAnimated = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "Alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
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