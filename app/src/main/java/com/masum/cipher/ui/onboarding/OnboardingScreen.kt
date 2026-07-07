package com.masum.cipher.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Smartphone
import compose.icons.lucideicons.WifiOff
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(300))
            },
            label = "onboarding_page"
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
    LaunchedEffect(Unit) { delay(100); visible = true }

    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")
    val offsetY by animateFloatAsState(if (visible) 0f else 40f, spring(stiffness = 200f), label = "offset")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ElectricIndigo.copy(alpha = 0.05f), Transparent)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(bottom = 64.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.translationY = offsetY
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "cipher",
                style = Typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your financial vault.",
                style = Typography.headlineMedium,
                color = ElectricIndigo
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Secure, offline-first transaction tracking that puts your privacy above everything else.",
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            FeatureItem(LucideIcons.ShieldCheck, "AES-256 Encrypted")
            FeatureItem(LucideIcons.Smartphone, "Local SMS Parsing")
            FeatureItem(LucideIcons.WifiOff, "Zero Network Access")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text(text = "Get Started", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PermissionPage(onComplete: () -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onComplete() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "Data Access",
            style = Typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cipher needs permission to read banking SMS alerts to automatically secure your transaction history.",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        VaultCard(
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "WHY THIS MATTERS",
                    style = Typography.labelSmall,
                    color = ElectricIndigo
                )
                Text(
                    text = "By parsing SMS locally, we can provide real-time insights without ever asking for your bank credentials or syncing data to the cloud.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { permissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
        ) {
            Text(text = "Allow Access", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Skip for now", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(icon, null, tint = ElectricIndigo, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
