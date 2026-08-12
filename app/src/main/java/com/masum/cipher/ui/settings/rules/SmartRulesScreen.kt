package com.masum.cipher.ui.settings.rules

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Trash2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRulesScreen(
    viewModel: SmartRulesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editRule by remember { mutableStateOf<CategoryRuleEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SmartRulesContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SmartRulesContract.Effect.ShowUndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Rule deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(SmartRulesContract.Intent.RestoreRule(effect.rule))
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Smart Rules", 
                        style = Typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performVibrate(state.isHapticsEnabled)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    view.performVibrate(state.isHapticsEnabled)
                    editRule = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(LucideIcons.Plus, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "No custom rules yet.\nRules are created when you edit a transaction's category.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.rules, key = { it.merchantName }) { rule ->
                    RuleItem(
                        rule = rule,
                        isHapticsEnabled = state.isHapticsEnabled,
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled)
                            editRule = rule
                            showAddDialog = true
                        },
                        onDelete = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                            viewModel.handleIntent(SmartRulesContract.Intent.DeleteRule(rule))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        RuleEditDialog(
            initialMerchant = editRule?.merchantName ?: "",
            initialCategory = editRule?.customCategory ?: "OTHERS",
            onDismiss = { showAddDialog = false },
            onSave = { merchant, category ->
                viewModel.handleIntent(SmartRulesContract.Intent.AddOrUpdateRule(merchant, category))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun RuleItem(
    rule: CategoryRuleEntity,
    isHapticsEnabled: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val category = TransactionCategory.fromString(rule.customCategory)
    
    VaultCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        onClick = onClick,
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(category.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.merchantName,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Always as ${category.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Trash2,
                    contentDescription = "Delete Rule",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleEditDialog(
    initialMerchant: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var merchantName by remember { mutableStateOf(initialMerchant) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.fromString(initialCategory)) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMerchant.isEmpty()) "New Rule" else "Edit Rule",
                style = Typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Merchant Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = initialMerchant.isEmpty() // Only editable if new
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { 
                        focusManager.clearFocus()
                        expanded = !expanded 
                    }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        leadingIcon = {
                            Icon(
                                imageVector = selectedCategory.icon,
                                contentDescription = null,
                                tint = selectedCategory.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(
                            surface = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(4.dp)
                        ) {
                            TransactionCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(cat.color.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = cat.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (merchantName.isNotBlank()) {
                        onSave(merchantName, selectedCategory.name)
                    }
                },
                enabled = merchantName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
