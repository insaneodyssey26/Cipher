package com.masum.cipher.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
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
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.MessageSquare
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Smartphone
import compose.icons.lucideicons.WifiOff
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSaveApps: (Set<String>) -> Unit
) {
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
                1 -> AppSelectionScreen(
                    initialSelectedApps = emptySet(),
                    onComplete = { apps ->
                        onSaveApps(apps)
                        page = 2
                    }
                )
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
                    colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), Transparent)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp)
                .padding(bottom = 32.dp)
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
                color = MaterialTheme.colorScheme.primary
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
            FeatureItem(LucideIcons.Smartphone, "Universal SMS & Notification Parsing")
            FeatureItem(LucideIcons.WifiOff, "Zero Network Access")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Get Started", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun PermissionPage(onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var hasSmsPermission by remember { 
        mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) 
    }
    var hasPostNotificationPermission by remember { 
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) 
                androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            else true
        ) 
    }
    var hasNotificationAccess by remember { 
        mutableStateOf(androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) 
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasSmsPermission = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    hasPostNotificationPermission = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                hasNotificationAccess = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.containsKey(Manifest.permission.RECEIVE_SMS)) {
            hasSmsPermission = result[Manifest.permission.RECEIVE_SMS] == true
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && result.containsKey(Manifest.permission.POST_NOTIFICATIONS)) {
            hasPostNotificationPermission = result[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(text = "Final Setup", style = Typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cipher needs these permissions to automatically secure your transaction history locally.",
            style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // SMS Permission Card
        VaultCard(backgroundColor = MaterialTheme.colorScheme.surface, contentPadding = 20.dp) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(LucideIcons.Smartphone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SMS Alerts", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Required to parse bank SMS.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        permissionLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
                    },
                    enabled = !hasSmsPermission,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.3f))
                ) {
                    Text(if (hasSmsPermission) "Granted" else "Grant SMS Access", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(16.dp))

            // Post Notifications Card
            VaultCard(backgroundColor = MaterialTheme.colorScheme.surface, contentPadding = 20.dp) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(LucideIcons.MessageSquare, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("App Notifications", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Required to send you budget alerts and goal updates.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                        },
                        enabled = !hasPostNotificationPermission,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.3f))
                    ) {
                        Text(if (hasPostNotificationPermission) "Granted" else "Grant App Notifications", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Permission Card
        VaultCard(backgroundColor = MaterialTheme.colorScheme.surface, contentPadding = 20.dp) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(LucideIcons.BellRing, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Notification Access", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Required to read push notifications from your selected payment apps.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    enabled = !hasNotificationAccess,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.3f))
                ) {
                    Text(if (hasNotificationAccess) "Granted" else "Grant Notification Access", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(text = if (hasSmsPermission && hasNotificationAccess) "Finish Setup" else "Skip for now", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
