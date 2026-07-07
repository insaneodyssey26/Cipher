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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricAuthenticator: BiometricAuthenticator,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    
    var showBackupPasswordDialog by remember { mutableStateOf<BackupAction?>(null) }
    var backupPassword by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            SettingsSection("APPEARANCE") {
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
                            if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.LIGHT))
                        }
                    )
                    ThemeOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Dark",
                        icon = LucideIcons.Moon,
                        isSelected = state.theme == AppTheme.DARK,
                        onClick = {
                            if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.DARK))
                        }
                    )
                    ThemeOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "System",
                        icon = LucideIcons.Laptop,
                        isSelected = state.theme == AppTheme.SYSTEM,
                        onClick = {
                            if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.SYSTEM))
                        }
                    )
                }
            }

            SettingsSection("SECURITY & PRIVACY") {
                VaultSettingsSwitch(
                    icon = LucideIcons.ShieldCheck,
                    title = "Biometric Lock",
                    description = "Require authentication to open the app",
                    checked = state.isBiometricEnabled,
                    onCheckedChange = { isEnabling ->
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTimeoutDialog = true
                    }
                )
                VaultSettingsSwitch(
                    icon = LucideIcons.Zap,
                    title = "Haptic Feedback",
                    description = "Physical response to touch",
                    checked = state.isHapticsEnabled,
                    onCheckedChange = { 
                        if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.handleIntent(SettingsContract.Intent.SetHapticsEnabled(it)) 
                    }
                )
                VaultSettingsSwitch(
                    icon = LucideIcons.EyeOff,
                    title = "Privacy Mode",
                    description = "Hide balances on dashboard",
                    checked = state.isPrivacyModeEnabled,
                    onCheckedChange = { 
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.handleIntent(SettingsContract.Intent.SetPrivacyModeEnabled(it)) 
                    }
                )
            }

            SettingsSection("FINANCIAL GOALS") {
                VaultSettingsItem(
                    icon = LucideIcons.Wallet,
                    title = "Monthly Budget",
                    value = if (state.monthlyBudget > 0) "₹${state.monthlyBudget.toInt()}" else "No limit set",
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        budgetInput = if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else ""
                        showBudgetDialog = true
                    }
                )
            }

            SettingsSection("DATA MANAGEMENT") {
                VaultSettingsItem(
                    icon = LucideIcons.FileSpreadsheet,
                    title = "Export CSV Report",
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        csvExportLauncher.launch("Cipher_Report_${System.currentTimeMillis()}.csv")
                    },
                    loading = state.isExportingCsv
                )
                VaultSettingsItem(
                    icon = LucideIcons.CloudUpload,
                    title = "Backup Vault",
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        exportLauncher.launch("Cipher_Backup_${System.currentTimeMillis()}.cipher")
                    },
                    loading = state.isExporting
                )
                VaultSettingsItem(
                    icon = LucideIcons.CloudDownload,
                    title = "Restore Vault",
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        importLauncher.launch(arrayOf("application/octet-stream"))
                    },
                    loading = state.isImporting
                )
                VaultSettingsItem(
                    icon = LucideIcons.Trash2,
                    title = "Clear All Data",
                    titleColor = RoseExpense,
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true
                    }
                )
            }

            SettingsSection("ABOUT") {
                VaultSettingsItem(
                    icon = LucideIcons.Info,
                    title = "Privacy Policy",
                    onClick = onNavigateToPrivacy
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
                    icon = LucideIcons.Github,
                    title = "Open Source",
                    subtitle = "github.com/insaneodyssey26/cipher",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/insaneodyssey26/cipher"))
                        context.startActivity(intent)
                    }
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

            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Cipher 4.0.0",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
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
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(text = title, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 12.dp))
        VaultCard(contentPadding = 0.dp) {
            Column(content = content)
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
    loading: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = titleColor.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.titleSmall, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricIndigo)
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
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                checkedTrackColor = ElectricIndigo,
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
    confirmColor: Color = ElectricIndigo,
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
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
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
