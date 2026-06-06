package com.masum.cipher.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userPreferences: UserPreferences,
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by userPreferences.settingsFlow.collectAsState(initial = null)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            if (effect is DashboardContract.Effect.ShowUndoDelete) {
                val result = snackbarHostState.showSnackbar(
                    message = "Transaction deleted",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.handleIntent(DashboardContract.Intent.RestoreTransaction(effect.transaction))
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    actionColor = CipherBlue,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Column {
                    TopAppBar(
                        title = {
                            if (!isSearchActive) {
                                Text(
                                    text = "Cipher",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                TextField(
                                    value = localSearchQuery,
                                    onValueChange = {
                                        localSearchQuery = it
                                        viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(it))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Search vault...",
                                            color = MaterialTheme.colorScheme.outline,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        cursorColor = CipherBlue
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            localSearchQuery = ""
                                            viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(""))
                                            isSearchActive = false
                                        }) {
                                            Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                )
                            }
                        },
                        actions = {
                            if (!isSearchActive) {
                                IconButton(onClick = {
                                    if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSearchActive = true
                                }) {
                                    Icon(Icons.Rounded.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToSettings()
                                }) {
                                    Icon(Icons.Rounded.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )

                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(180)) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardContract.FilterType.entries.forEach { filter ->
                                FilterChip(
                                    selected = state.activeFilter == filter,
                                    onClick = {
                                        if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.handleIntent(DashboardContract.Intent.FilterTransactions(filter))
                                    },
                                    label = {
                                        Text(
                                            text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CipherBlueDim,
                                        selectedLabelColor = CipherBlue,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = state.activeFilter == filter,
                                        selectedBorderColor = CipherBlue.copy(alpha = 0.3f),
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            if (!isSearchActive && localSearchQuery.isEmpty()) {
                item {
                    BalanceHeader(
                        totalBalance = state.totalBalance,
                        income = state.totalIncome,
                        expenses = state.totalExpenses,
                        isPrivacyMode = privacyMode,
                        isHapticsEnabled = isHapticsEnabled
                    )
                }

                item {
                    BudgetCard(
                        spent = state.totalExpenses,
                        budget = state.monthlyBudget,
                        onSetBudgetClick = onNavigateToSettings,
                        isHapticsEnabled = isHapticsEnabled
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }

                item {
                    IntelligenceRow(
                        isHapticsEnabled = isHapticsEnabled,
                        onClick = onNavigateToInsights
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSearchActive || localSearchQuery.isNotEmpty()) "RESULTS" else "TIMELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isSearchActive && localSearchQuery.isEmpty()) {
                        AddButton(
                            isHapticsEnabled = isHapticsEnabled,
                            onClick = { showAddDialog = true }
                        )
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                item {
                    if (isSearchActive || localSearchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching records",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        EmptyTransactionsState()
                    }
                }
            } else {
                items(
                    items = state.transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        isPrivacyMode = privacyMode,
                        onDelete = { viewModel.handleIntent(DashboardContract.Intent.DeleteTransaction(transaction)) },
                        onEdit = {
                            if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editingTransaction = transaction
                        },
                        isHapticsEnabled = isHapticsEnabled
                    )
                    RowDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        EditTransactionDialog(
            transaction = TransactionEntity(
                amount = 0.0,
                merchant = "",
                currency = "INR",
                timestamp = System.currentTimeMillis(),
                category = "MISC",
                rawSms = null,
                isIncome = false
            ),
            onDismiss = { showAddDialog = false },
            onConfirm = { newTransaction ->
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(DashboardContract.Intent.AddTransaction(newTransaction))
                showAddDialog = false
            }
        )
    }

    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(DashboardContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            }
        )
    }
}

@Composable
private fun IntelligenceRow(
    isHapticsEnabled: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(CipherBlueDim, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = CipherBlue
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Intelligence",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Patterns · Subscriptions · Trends",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun AddButton(isHapticsEnabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "add"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(CipherBlueDim, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = CipherBlue
            )
            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CipherBlue
            )
        }
    }
}

