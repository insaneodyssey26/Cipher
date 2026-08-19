package com.masum.cipher.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.*
import com.masum.cipher.core.util.performVibrate
import compose.icons.LucideIcons
import compose.icons.lucideicons.*
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Bug
import compose.icons.lucideicons.RefreshCw
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.X
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.core.notifications.LocalNotificationManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricAuthenticator: BiometricAuthenticator,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToManageApps: () -> Unit,
    onNavigateToSmartRules: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCrashLogDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    var showPermissionsHealthSheet by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    
    var showBackupPasswordDialog by remember { mutableStateOf<BackupAction?>(null) }
    var backupPassword by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var isColorPickerExpanded by remember { mutableStateOf(false) }
    var showAutoBackupPasswordSetupDialog by remember { mutableStateOf(false) }
    var autoBackupSetupPassword by remember { mutableStateOf("") }

    val timeoutOptions = listOf(
        "Immediately" to 0L,
        "30 Seconds" to 30_000L,
        "1 Minute" to 60_000L,
        "5 Minutes" to 300_000L,
        "Never" to Long.MAX_VALUE
    )

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { pendingUri = it; showBackupPasswordDialog = BackupAction.EXPORT }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { pendingUri = it; showBackupPasswordDialog = BackupAction.IMPORT }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { viewModel.handleIntent(SettingsContract.Intent.ExportCsv(it)) }
    }
    
    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.handleIntent(SettingsContract.Intent.ExportPdf(it)) }
    }

    val autoBackupFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.handleIntent(SettingsContract.Intent.SetAutoBackupUri(it.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is SettingsContract.Effect.ShowToast) {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "SETTINGS", style = Typography.labelSmall.copy(letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp)
        ) {
            SettingsSection("APPEARANCE", icon = LucideIcons.Palette, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "APPEARANCE", onToggle = { expandedSection = if (expandedSection == "APPEARANCE") null else "APPEARANCE" }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Light",
                        icon = LucideIcons.Sun,
                        isSelected = state.theme == AppTheme.LIGHT,
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.LIGHT))
                        }
                    )
                    ThemeOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Dark",
                        icon = LucideIcons.Moon,
                        isSelected = state.theme == AppTheme.DARK,
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.DARK))
                        }
                    )
                    ThemeOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "System",
                        icon = LucideIcons.Laptop,
                        isSelected = state.theme == AppTheme.SYSTEM,
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.SYSTEM))
                        }
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    val view = androidx.compose.ui.platform.LocalView.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.performVibrate(state.isHapticsEnabled)
                                isColorPickerExpanded = !isColorPickerExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Accent Color",
                                    style = Typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.accentColor.colorName,
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        val chevronRotation by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isColorPickerExpanded) 180f else 0f,
                            label = "chevron_rotation"
                        )
                        Icon(
                            imageVector = LucideIcons.ChevronDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronRotation)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isColorPickerExpanded,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            com.masum.cipher.core.data.local.pref.AccentColor.values().toList().chunked(5).forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowColors.forEach { color ->
                                        val isSelected = state.accentColor == color
                                        val scale by androidx.compose.animation.core.animateFloatAsState(
                                            targetValue = if (isSelected) 1.1f else 1f,
                                            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                            label = "color_scale"
                                        )
                                        val borderColor by androidx.compose.animation.animateColorAsState(
                                            targetValue = if (isSelected) Color(color.colorValue) else Color.Transparent,
                                            animationSpec = androidx.compose.animation.core.tween(300),
                                            label = "border_color"
                                        )
                                        val view = androidx.compose.ui.platform.LocalView.current
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (!isSelected) {
                                                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                                                        viewModel.handleIntent(SettingsContract.Intent.UpdateAccentColor(color))
                                                    }
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .scale(scale)
                                                    .border(2.dp, borderColor, androidx.compose.foundation.shape.CircleShape)
                                                    .padding(4.dp)
                                                    .background(Color(color.colorValue), androidx.compose.foundation.shape.CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = LucideIcons.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = color.colorName.substringAfter(" "),
                                                style = Typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (isSelected) Color(color.colorValue) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                modifier = Modifier.height(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SettingsSection("SECURITY & PRIVACY", icon = LucideIcons.Lock, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "SECURITY & PRIVACY", onToggle = { expandedSection = if (expandedSection == "SECURITY & PRIVACY") null else "SECURITY & PRIVACY" }) {
                VaultSettingsSwitch(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.ShieldCheck,
                    title = "Biometric Lock",
                    description = "Require authentication to open the app",
                    checked = state.isBiometricEnabled,
                    onCheckedChange = { isEnabling ->
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        if (isEnabling && biometricAuthenticator.isBiometricAvailable()) {
                            biometricAuthenticator.authenticate(
                                activity = context as FragmentActivity,
                                onSuccess = { viewModel.handleIntent(SettingsContract.Intent.SetBiometricEnabled(true)) },
                                onError = { }
                            )
                        } else {
                            viewModel.handleIntent(SettingsContract.Intent.SetBiometricEnabled(isEnabling))
                        }
                    }
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Timer,
                    title = "Auto-Lock Timer",
                    value = when (state.autoLockTimeout) {
                        0L -> "Immediately"
                        30_000L -> "30 Seconds"
                        60_000L -> "1 Minute"
                        300_000L -> "5 Minutes"
                        else -> "Never"
                    },
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        showTimeoutDialog = true
                    }
                )
                VaultSettingsSwitch(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Zap,
                    title = "Haptic Feedback",
                    description = "Physical response to touch",
                    checked = state.isHapticsEnabled,
                    onCheckedChange = { 
                        view.performVibrate(it, isLongPress = true)
                        viewModel.handleIntent(SettingsContract.Intent.SetHapticsEnabled(it)) 
                    }
                )
                VaultSettingsSwitch(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.EyeOff,
                    title = "Privacy Mode",
                    description = "Hide balances on dashboard",
                    checked = state.isPrivacyModeEnabled,
                    onCheckedChange = { 
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        viewModel.handleIntent(SettingsContract.Intent.SetPrivacyModeEnabled(it)) 
                    }
                )
            }

            SettingsSection("AUTOMATION & TRACKING", icon = LucideIcons.Activity, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "AUTOMATION & TRACKING", onToggle = { expandedSection = if (expandedSection == "AUTOMATION & TRACKING") null else "AUTOMATION & TRACKING" }) {
                VaultSettingsSwitch(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.BellRing,
                    title = "Interactive Transaction Alerts",
                    description = "Alert on every transaction",
                    checked = state.notifyAllTransactions,
                    onCheckedChange = { 
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        viewModel.handleIntent(SettingsContract.Intent.SetNotifyAllTransactions(it)) 
                    }
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Smartphone,
                    title = "Manage Tracked Apps",
                    subtitle = "Select which apps to monitor for transactions",
                    onClick = onNavigateToManageApps
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Activity,
                    title = "Permissions Health",
                    subtitle = "Check if Cipher is working at its best",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        showPermissionsHealthSheet = true
                    }
                )
            
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.BookOpen,
                    title = "Manage Category Rules",
                    subtitle = "View and edit custom merchant categories",
                    onClick = onNavigateToSmartRules
                )
            }

            SettingsSection("FINANCIAL GOALS", icon = LucideIcons.Target, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "FINANCIAL GOALS", onToggle = { expandedSection = if (expandedSection == "FINANCIAL GOALS") null else "FINANCIAL GOALS" }) {
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Wallet,
                    title = "Monthly Budget",
                    value = if (state.monthlyBudget > 0) "₹${state.monthlyBudget.toInt()}" else "No limit set",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        budgetInput = if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else ""
                        showBudgetDialog = true
                    }
                )
            }

            SettingsSection("DATA & BACKUP", icon = LucideIcons.Database, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "DATA & BACKUP", onToggle = { expandedSection = if (expandedSection == "DATA & BACKUP") null else "DATA & BACKUP" }) {
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.FileSpreadsheet,
                    title = "Export CSV Report",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        csvExportLauncher.launch("Cipher_Report_${System.currentTimeMillis()}.csv")
                    },
                    loading = state.isExportingCsv
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.FileText,
                    title = "Export PDF Statement",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        pdfExportLauncher.launch("Cipher_Statement_${System.currentTimeMillis()}.pdf")
                    },
                    loading = state.isExportingPdf
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.CloudUpload,
                    title = "Backup Vault",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        exportLauncher.launch("Cipher_Backup_${System.currentTimeMillis()}.cipher")
                    },
                    loading = state.isExporting
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.CloudDownload,
                    title = "Restore Vault",
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        importLauncher.launch(arrayOf("application/octet-stream"))
                    },
                    loading = state.isImporting
                )
                VaultSettingsItem(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.Trash2,
                    title = "Clear All Data",
                    titleColor = RoseExpense,
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        showDeleteDialog = true
                    }
                )
            
                VaultSettingsSwitch(
                    isHapticsEnabled = state.isHapticsEnabled,
                    icon = LucideIcons.FolderSync,
                    title = "Enable Auto-Backup",
                    description = "Silently backup your vault locally",
                    checked = state.autoBackupEnabled,
                    onCheckedChange = { 
                        if (it) {
                            showAutoBackupPasswordSetupDialog = true
                        } else {
                            viewModel.handleIntent(SettingsContract.Intent.SetAutoBackupEnabled(false))
                        }
                    }
                )
                if (state.autoBackupEnabled) {
                    VaultSettingsItem(
                        isHapticsEnabled = state.isHapticsEnabled,
                        icon = LucideIcons.CalendarClock,
                        title = "Backup Frequency",
                        value = state.autoBackupFrequency.label,
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            showFrequencyDialog = true
                        }
                    )
                    VaultSettingsItem(
                        isHapticsEnabled = state.isHapticsEnabled,
                        icon = LucideIcons.FolderDown,
                        title = "Backup Location",
                        subtitle = if (state.autoBackupUri != null) {
                            try {
                                val decodedPath = android.net.Uri.decode(state.autoBackupUri)
                                val readablePath = decodedPath.substringAfter("tree/", decodedPath)
                                    .replace("primary:", "Internal Storage/")
                                "Selected: $readablePath"
                            } catch (e: Exception) {
                                "Folder Selected"
                            }
                        } else "Tap to select folder",
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            autoBackupFolderLauncher.launch(null)
                        }
                    )
                }
            }

            SettingsSection("ABOUT & SUPPORT", icon = LucideIcons.Info, isHapticsEnabled = state.isHapticsEnabled, isExpanded = expandedSection == "ABOUT & SUPPORT", onToggle = { expandedSection = if (expandedSection == "ABOUT & SUPPORT") null else "ABOUT & SUPPORT" }) {
                VaultSettingsItem(
                    icon = LucideIcons.Star,
                    title = "Rate on Google Play",
                    subtitle = "Enjoying Cipher? Leave a review!",
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.masum.cipher")))
                        } catch (e: android.content.ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.masum.cipher")))
                        }
                    }
                )
                VaultSettingsItem(
                    icon = LucideIcons.MessagesSquare,
                    title = "Feedback & Feature Requests",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tally.so/r/gDz7NK"))
                        context.startActivity(intent)
                    }
                )
                VaultSettingsItem(
                    icon = LucideIcons.RefreshCw,
                    title = "Check for Updates",
                    subtitle = "Look for the latest version on Play Store",
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.masum.cipher")))
                        } catch (e: android.content.ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.masum.cipher")))
                        }
                    }
                )
                VaultSettingsItem(
                    icon = LucideIcons.Bug,
                    title = "App Diagnostics",
                    subtitle = "View and copy crash logs",
                    onClick = { showCrashLogDialog = true }
                )
                VaultSettingsItem(
                    icon = LucideIcons.Mail,
                    title = "Contact Developer",
                    subtitle = "masumali262006@gmail.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:masumali262006@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Hello from Cipher App!")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                )
            
                VaultSettingsItem(
                    icon = LucideIcons.Github,
                    title = "Open Source",
                    subtitle = "github.com/insaneodyssey26/cipher",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/insaneodyssey26/cipher"))
                        context.startActivity(intent)
                    }
                )
                VaultSettingsItem(
                    icon = LucideIcons.Info,
                    title = "Privacy Policy",
                    onClick = onNavigateToPrivacy
                )
                VaultSettingsItem(
                    icon = LucideIcons.Coffee,
                    title = "Support Development",
                    subtitle = "ko-fi.com/insane_odyssey",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/insane_odyssey"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                Text(
                    text = "cipher.",
                    style = Typography.headlineLarge.copy(
                        fontFamily = com.masum.cipher.ui.theme.SpaceGrotesk,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val versionName = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "4.1.0"
                }
                Text(
                    text = "Version $versionName",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showCrashLogDialog) {
        val crashLog = com.masum.cipher.core.util.CrashReporter.getCrashLog(context)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCrashLogDialog = false },
            title = { 
                Text(
                    text = "App Diagnostics",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                if (crashLog == null) {
                    Text(
                        text = "No crashes have been recorded. Cipher is running smoothly!",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column {
                        Text(
                            text = "A crash was recorded. You can copy it below.",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            androidx.compose.foundation.lazy.LazyColumn {
                                item {
                                    Text(
                                        text = crashLog,
                                        style = Typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (crashLog != null) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Crash Log", crashLog)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            showCrashLogDialog = false
                        }
                    ) {
                        Text("COPY LOG", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            dismissButton = {
                Row {
                    if (crashLog != null) {
                        TextButton(
                            onClick = {
                                com.masum.cipher.core.util.CrashReporter.clearCrashLog(context)
                                android.widget.Toast.makeText(context, "Crash log cleared", android.widget.Toast.LENGTH_SHORT).show()
                                showCrashLogDialog = false
                            }
                        ) {
                            Text("CLEAR", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showCrashLogDialog = false }) {
                        Text(if (crashLog == null) "OK" else "CLOSE", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showBudgetDialog) {
        VaultSettingsDialog(
            title = "Monthly Budget",
            onDismiss = { showBudgetDialog = false },
            confirmText = "Save",
            onConfirm = {
                val amount = budgetInput.toDoubleOrNull() ?: 0.0
                viewModel.handleIntent(SettingsContract.Intent.SetMonthlyBudget(amount))
                showBudgetDialog = false
            }
        ) {
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) budgetInput = it },
                label = { Text("Limit (₹)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (showFrequencyDialog) {
        VaultSettingsDialog(
            title = "Auto-Backup Frequency",
            onDismiss = { showFrequencyDialog = false },
            confirmText = "Close",
            showDismissButton = false,
            onConfirm = { showFrequencyDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.masum.cipher.core.data.local.pref.AutoBackupFrequency.values().forEach { freq ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.performVibrate(state.isHapticsEnabled)
                                viewModel.handleIntent(SettingsContract.Intent.SetAutoBackupFrequency(freq))
                                showFrequencyDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = freq.label, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        if (state.autoBackupFrequency == freq) {
                            Icon(LucideIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (showBackupPasswordDialog != null) {
        VaultSettingsDialog(
            title = if (showBackupPasswordDialog == BackupAction.EXPORT) "Set Backup Password" else "Enter Backup Password",
            onDismiss = { showBackupPasswordDialog = null; backupPassword = "" },
            confirmText = if (showBackupPasswordDialog == BackupAction.EXPORT) "Export" else "Import",
            onConfirm = {
                val uri = pendingUri
                if (uri != null && backupPassword.isNotBlank()) {
                    if (showBackupPasswordDialog == BackupAction.EXPORT) {
                        viewModel.handleIntent(SettingsContract.Intent.ExportData(uri, backupPassword.toCharArray()))
                    } else {
                        viewModel.handleIntent(SettingsContract.Intent.ImportData(uri, backupPassword.toCharArray()))
                    }
                }
                showBackupPasswordDialog = null
                backupPassword = ""
            }
        ) {
            Column {
                Text(
                    text = if (showBackupPasswordDialog == BackupAction.EXPORT) 
                        "This password will be used to encrypt your backup. You will need it to restore your data." 
                        else "Enter the password used to encrypt this backup file.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = backupPassword,
                    onValueChange = { backupPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showTimeoutDialog) {
        VaultSettingsDialog(
            title = "Auto-Lock",
            onDismiss = { showTimeoutDialog = false },
            confirmText = "Close",
            showDismissButton = false,
            onConfirm = { showTimeoutDialog = false }
        ) {
            Column {
                timeoutOptions.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.handleIntent(SettingsContract.Intent.SetAutoLockTimeout(value))
                            showTimeoutDialog = false
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = state.autoLockTimeout == value, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        VaultSettingsDialog(
            title = "Delete all data?",
            onDismiss = { showDeleteDialog = false },
            confirmText = "Clear Everything",
            confirmColor = RoseExpense,
            onConfirm = {
                viewModel.handleIntent(SettingsContract.Intent.ClearAllData)
                showDeleteDialog = false
            }
        ) {
            Text("This action is permanent and cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showAutoBackupPasswordSetupDialog) {
        VaultSettingsDialog(
            title = "Auto-Backup Password",
            onDismiss = { 
                showAutoBackupPasswordSetupDialog = false 
                autoBackupSetupPassword = ""
            },
            confirmText = "Enable",
            onConfirm = {
                if (autoBackupSetupPassword.length >= 4) {
                    viewModel.handleIntent(SettingsContract.Intent.SetAutoBackupEncryptedPassword(autoBackupSetupPassword))
                    viewModel.handleIntent(SettingsContract.Intent.SetAutoBackupEnabled(true))
                    showAutoBackupPasswordSetupDialog = false
                    autoBackupSetupPassword = ""
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter a password to encrypt your automatic backups. You will need this to restore your data on a new device.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = autoBackupSetupPassword,
                    onValueChange = { autoBackupSetupPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Backup Password") },
                    singleLine = true,
                    textStyle = Typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }

    if (showPermissionsHealthSheet) {
        PermissionsHealthSheet(
            onDismiss = { showPermissionsHealthSheet = false },
            isHapticsEnabled = state.isHapticsEnabled
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isExpanded: Boolean = true,
    isHapticsEnabled: Boolean = true,
    onToggle: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    VaultCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable(enabled = onToggle != null, onClick = { 
                view.performVibrate(isHapticsEnabled)
                onToggle?.invoke() 
            }),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp).padding(end = 16.dp)
                        )
                    }
                    val targetFontSize = if (isExpanded) 16.sp else 14.sp
                    val fontSize by androidx.compose.animation.core.animateFloatAsState(targetValue = targetFontSize.value, label = "fontSize")
                    Text(
                        text = title.lowercase().replaceFirstChar { it.uppercase() },
                        style = Typography.titleMedium.copy(
                            fontFamily = com.masum.cipher.ui.theme.SpaceGrotesk,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            fontSize = fontSize.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (onToggle != null) {
                    val rotation by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f
                    )
                    Icon(
                        imageVector = LucideIcons.ChevronDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }
            

            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun VaultSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    loading: Boolean = false,
    isHapticsEnabled: Boolean = true
) {
    val view = androidx.compose.ui.platform.LocalView.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable {
            view.performVibrate(isHapticsEnabled)
            onClick()
        }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = titleColor.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.titleSmall, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        } else if (value != null) {
            Text(text = value, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(LucideIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp).padding(start = 4.dp))
        }
    }
}

@Composable
private fun VaultSettingsSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val view = androidx.compose.ui.platform.LocalView.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                view.performVibrate(isHapticsEnabled)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun VaultSettingsDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    showDismissButton: Boolean = true,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text(title, style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
        text = { content() },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = confirmColor)) {
                Text(confirmText, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        dismissButton = if (showDismissButton) {
            { TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else null
    )
}

private enum class BackupAction { EXPORT, IMPORT }

@Composable
private fun ThemeOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = Typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionsHealthSheet(
    onDismiss: () -> Unit,
    isHapticsEnabled: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var hasNotificationAccess by remember { 
        mutableStateOf(androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) 
    }
    var hasPostNotifications by remember { 
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) 
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            else true
        ) 
    }
    var hasSmsPermission by remember {
        mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPostNotifications = isGranted
    }
    
    val smsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[android.Manifest.permission.RECEIVE_SMS] == true
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasNotificationAccess = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                hasSmsPermission = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    hasPostNotifications = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Permissions Health",
                style = Typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ensure Cipher has the necessary permissions to provide the best experience.",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            HealthItemCard(
                title = "Notification Access",
                description = "Required to automatically parse transactions from your tracked apps.",
                isGranted = hasNotificationAccess,
                icon = LucideIcons.BellRing,
                onFixClick = {
                    view.performVibrate(isHapticsEnabled)
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            HealthItemCard(
                title = "Read SMS",
                description = "Required to securely capture transactions from banking SMS messages.",
                isGranted = hasSmsPermission,
                icon = LucideIcons.MessageSquare,
                onFixClick = {
                    view.performVibrate(isHapticsEnabled)
                    smsLauncher.launch(arrayOf(android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                HealthItemCard(
                    title = "App Notifications",
                    description = "Required to send you budget alerts and important updates.",
                    isGranted = hasPostNotifications,
                    icon = LucideIcons.MessageSquare,
                    onFixClick = {
                        view.performVibrate(isHapticsEnabled)
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun HealthItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onFixClick: () -> Unit
) {
    val backgroundColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer
    val iconTint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    
    com.masum.cipher.ui.components.VaultCard(backgroundColor = backgroundColor) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = Typography.titleSmall, color = if (isGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                Text(description, style = Typography.bodySmall, color = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (isGranted) {
                Icon(
                    imageVector = LucideIcons.Check,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onFixClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Fix", color = MaterialTheme.colorScheme.onError, style = Typography.labelMedium)
                }
            }
        }
    }
}
