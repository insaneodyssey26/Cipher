package com.example.cipherspend.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.security.BiometricAuthenticator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricAuthenticator: BiometricAuthenticator,
    onNavigateBack: () -> Unit
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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val timeoutOptions = listOf(
        "Immediately" to 0L,
        "30 Seconds" to 30_000L,
        "1 Minute" to 60_000L,
        "5 Minutes" to 300_000L,
        "Never" to Long.MAX_VALUE
    )

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { 
            pendingUri = it
            showBackupPasswordDialog = BackupAction.EXPORT
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            pendingUri = it
            showBackupPasswordDialog = BackupAction.IMPORT
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.handleIntent(SettingsContract.Intent.ExportCsv(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(title = "Appearance")
            
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { 
                    Text(when (state.theme) {
                        AppTheme.LIGHT -> "Light"
                        AppTheme.DARK -> "Dark"
                        AppTheme.SYSTEM -> "System default"
                    })
                },
                leadingContent = {
                    Icon(
                        imageVector = when (state.theme) {
                            AppTheme.LIGHT -> Icons.Default.LightMode
                            AppTheme.DARK -> Icons.Default.DarkMode
                            AppTheme.SYSTEM -> Icons.Default.SettingsSuggest
                        },
                        contentDescription = null
                    )
                },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { 
                            if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expanded = true 
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Select Theme")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Light") },
                                onClick = {
                                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.LIGHT))
                                    expanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.LightMode, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Dark") },
                                onClick = {
                                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.DARK))
                                    expanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.DarkMode, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("System default") },
                                onClick = {
                                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.handleIntent(SettingsContract.Intent.UpdateTheme(AppTheme.SYSTEM))
                                    expanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.SettingsSuggest, null) }
                            )
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

            SettingsSectionHeader(title = "Security & Privacy")
            
            ListItem(
                headlineContent = { Text("Biometric Lock") },
                supportingContent = { Text("Require authentication to open the app") },
                leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                trailingContent = {
                    Switch(
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
                }
            )

            ListItem(
                headlineContent = { Text("Auto-Lock Timer") },
                supportingContent = { 
                    Text(when (state.autoLockTimeout) {
                        0L -> "Immediately"
                        30_000L -> "30 Seconds"
                        60_000L -> "1 Minute"
                        300_000L -> "5 Minutes"
                        else -> "Never"
                    })
                },
                leadingContent = { Icon(Icons.Default.Timer, null) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTimeoutDialog = true 
                }
            )

            ListItem(
                headlineContent = { Text("Haptic Feedback") },
                supportingContent = { Text("Physical response to touch and actions") },
                leadingContent = { Icon(Icons.Default.TouchApp, null) },
                trailingContent = {
                    Switch(
                        checked = state.isHapticsEnabled,
                        onCheckedChange = { 
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(SettingsContract.Intent.SetHapticsEnabled(it)) 
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Privacy Mode") },
                supportingContent = { Text("Hide sensitive balances on the dashboard") },
                leadingContent = { Icon(Icons.Default.VisibilityOff, null) },
                trailingContent = {
                    Switch(
                        checked = state.isPrivacyModeEnabled,
                        onCheckedChange = { 
                            if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(SettingsContract.Intent.SetPrivacyModeEnabled(it)) 
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

            SettingsSectionHeader(title = "Financial Goals")

            ListItem(
                headlineContent = { Text("Monthly Spending Limit") },
                supportingContent = { 
                    Text(if (state.monthlyBudget > 0) "Currently: ₹${state.monthlyBudget.toInt()}" else "No limit set")
                },
                leadingContent = { Icon(Icons.Default.AccountBalanceWallet, null) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    budgetInput = if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else ""
                    showBudgetDialog = true 
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

            SettingsSectionHeader(title = "Data Management")

            ListItem(
                headlineContent = { Text("Export CSV Report") },
                supportingContent = { Text("Download a spreadsheet of all transactions") },
                leadingContent = { Icon(Icons.Rounded.FileDownload, null) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    csvExportLauncher.launch("Cipher_Report_${System.currentTimeMillis()}.csv")
                },
                trailingContent = {
                    if (state.isExportingCsv) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            )

            ListItem(
                headlineContent = { Text("Export Encrypted Backup") },
                supportingContent = { Text("Save your data to a secure file") },
                leadingContent = { Icon(Icons.Default.CloudUpload, null) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    exportLauncher.launch("Cipher_Backup_${System.currentTimeMillis()}.cipher")
                },
                trailingContent = {
                    if (state.isExporting) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            )

            ListItem(
                headlineContent = { Text("Import Encrypted Backup") },
                supportingContent = { Text("Restore data from a previously exported file") },
                leadingContent = { Icon(Icons.Default.CloudDownload, null) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    importLauncher.launch(arrayOf("application/octet-stream"))
                },
                trailingContent = {
                    if (state.isImporting) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

            ListItem(
                headlineContent = { Text("Clear All Data", color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text("Permanently delete all transaction records") },
                leadingContent = { Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { 
                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteDialog = true 
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Cipher Version 2.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Monthly Spending Limit") },
            text = {
                Column {
                    Text(
                        "Set a total amount you'd like to stay under each month.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) budgetInput = it },
                        label = { Text("Limit (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        viewModel.handleIntent(SettingsContract.Intent.SetMonthlyBudget(amount))
                        showBudgetDialog = false
                    }
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackupPasswordDialog != null) {
        AlertDialog(
            onDismissRequest = { 
                showBackupPasswordDialog = null
                backupPassword = ""
            },
            title = { 
                Text(if (showBackupPasswordDialog == BackupAction.EXPORT) "Set Backup Password" else "Enter Backup Password")
            },
            text = {
                Column {
                    Text(
                        text = if (showBackupPasswordDialog == BackupAction.EXPORT) 
                            "This password will be used to encrypt your backup. You will need it to restore your data." 
                            else "Enter the password used to encrypt this backup file.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    },
                    enabled = backupPassword.isNotBlank()
                ) {
                    Text(if (showBackupPasswordDialog == BackupAction.EXPORT) "Export" else "Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBackupPasswordDialog = null
                    backupPassword = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("Auto-Lock Timeout") },
            text = {
                Column {
                    timeoutOptions.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.handleIntent(SettingsContract.Intent.SetAutoLockTimeout(value))
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.autoLockTimeout == value,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all data?") },
            text = { Text("This action will permanently erase your entire transaction history. It cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.handleIntent(SettingsContract.Intent.ClearAllData)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private enum class BackupAction { EXPORT, IMPORT }

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}
