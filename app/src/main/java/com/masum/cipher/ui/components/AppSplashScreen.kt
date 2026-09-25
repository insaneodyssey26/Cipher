package com.masum.cipher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.ui.theme.DMSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppSplashScreen(
    isReady: Boolean,
    onAnimationComplete: () -> Unit
) {
    var shouldDismiss by remember { mutableStateOf(false) }

    val coreRotation = remember { Animatable(-75f) }
    val lockBurst = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val emblemScale = remember { Animatable(0.78f) }

    LaunchedEffect(Unit) {
        launch {
            emblemScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.62f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        delay(120)
        launch {
            coreRotation.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 240f
                )
            )
            launch {
                lockBurst.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    LaunchedEffect(isReady) {
        if (isReady) {
            delay(520)
            shouldDismiss = true
            delay(320)
            onAnimationComplete()
        }
    }

    val exitAlpha by animateFloatAsState(
        targetValue = if (shouldDismiss) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "exitAlpha"
    )

    val exitScale by animateFloatAsState(
        targetValue = if (shouldDismiss) 1.06f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "exitScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(exitAlpha)
            .scale(exitScale),
        contentAlignment = Alignment.Center
    ) {
        if (lockBurst.value > 0f) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(1f + (lockBurst.value * 0.45f))
                    .alpha((1f - lockBurst.value) * 0.45f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(28.dp)
            )
        }

        Column(
            modifier = Modifier.scale(emblemScale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(
                        color = surfaceElevated,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    val centerOffset = Offset(size.width * 0.48f, size.height / 2f)
                    val outerRadius = size.minDimension * 0.44f
                    val strokeW = size.minDimension * 0.13f

                    drawArc(
                        color = primaryColor,
                        startAngle = 38f,
                        sweepAngle = 284f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - outerRadius, centerOffset.y - outerRadius),
                        size = Size(outerRadius * 2f, outerRadius * 2f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    rotate(degrees = coreRotation.value, pivot = centerOffset) {
                        val innerRadius = outerRadius * 0.52f
                        val slotWidth = strokeW * 0.85f
                        val slotLength = outerRadius * 0.92f

                        val keyPath = Path().apply {
                            moveTo(centerOffset.x, centerOffset.y - (slotWidth / 2f))
                            lineTo(centerOffset.x + slotLength, centerOffset.y - (slotWidth / 2f))
                            lineTo(centerOffset.x + slotLength, centerOffset.y + (slotWidth / 2f))
                            lineTo(centerOffset.x, centerOffset.y + (slotWidth / 2f))
                            close()
                        }
                        drawPath(path = keyPath, color = primaryColor, style = Fill)

                        drawCircle(
                            color = primaryColor,
                            radius = innerRadius,
                            center = centerOffset,
                            style = Fill
                        )
                    }

                    drawCircle(
                        color = surfaceElevated,
                        radius = outerRadius * 0.22f,
                        center = centerOffset,
                        style = Fill
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = outerRadius * 0.12f,
                        center = centerOffset,
                        style = Fill
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "cipher",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2).sp
                ),
                color = onSurfaceColor,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "private • offline • conscious",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.4.sp
                ),
                color = onSurfaceVariantColor.copy(alpha = 0.75f),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}
