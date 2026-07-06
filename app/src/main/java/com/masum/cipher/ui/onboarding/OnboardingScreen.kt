package com.masum.cipher.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false).also { it.consume() }
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                fadeIn(tween(320)) togetherWith fadeOut(tween(220))
            },
            label = "page"
        ) { currentPage ->
            when (currentPage) {
                0 -> WelcomePage(onNext = { page = 1 })
                else -> PermissionPage(onComplete = onComplete)
            }
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "a")
    val sc by animateFloatAsState(if (visible) 1f else 0.91f, tween(500, easing = FastOutSlowInEasing), label = "s")

    val breathe by rememberInfiniteTransition(label = "b").animateFloat(
        initialValue = 1f, targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bs"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp)
                .padding(bottom = 52.dp)
                .graphicsLayer(alpha = alpha, scaleX = sc, scaleY = sc),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "cipher",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-3).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.scale(breathe)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Every rupee tracked.\nNothing leaves your phone.",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Local · Encrypted · Private",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))

            InvertedButton(text = "Get Started", onClick = onNext)
        }
    }
}

@Composable
private fun PermissionPage(onComplete: () -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onComplete() }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "a")
    val sc by animateFloatAsState(if (visible) 1f else 0.91f, tween(500, easing = FastOutSlowInEasing), label = "s")

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp)
                .padding(bottom = 52.dp)
                .graphicsLayer(alpha = alpha, scaleX = sc, scaleY = sc),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "One permission.\nThat's all it needs.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 42.sp,
                    letterSpacing = (-1.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Cipher reads bank SMS alerts to log your transactions automatically. It runs entirely on-device — no servers, no accounts, no sync.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(
                    "Parsed on your device only",
                    "No account or sign-in needed",
                    "Works fully offline"
                ).forEach { line ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            InvertedButton(
                text = "Allow SMS Access",
                onClick = { permissionLauncher.launch(Manifest.permission.RECEIVE_SMS) }
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onComplete
                    )
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InvertedButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "btn"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.background
        )
    }
}
