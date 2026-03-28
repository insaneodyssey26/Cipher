package com.example.cipherspend.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.ui.components.*
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.handleIntent(DashboardContract.Intent.RestoreTransaction(effect.transaction))
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = if (scrollBehavior.state.contentOffset < -1f) 3.dp else 0.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (!isSearchActive) {
                                    Text(
                                        text = "Cipher",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-1).sp
                                        ),
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                } else {
                                    TextField(
                                        value = localSearchQuery,
                                        onValueChange = { 
                                            localSearchQuery = it
                                            viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(it)) 
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(searchFocusRequester),
                                        placeholder = { Text("Search vault...") },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                        trailingIcon = {
                                            IconButton(onClick = { 
                                                localSearchQuery = ""
                                                viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(""))
                                                isSearchActive = false 
                                            }) {
                                                Icon(Icons.Rounded.Close, null)
                                            }
                                        }
                                    )
                                    LaunchedEffect(Unit) { searchFocusRequester.requestFocus() }
                                }
                            }
                        },
                        actions = {
                            if (!isSearchActive) {
                                IconButton(onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSearchActive = true 
                                }) {
                                    Icon(Icons.Rounded.Search, "Search")
                                }
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        visible = isSearchActive || localSearchQuery.isNotEmpty(),
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            DashboardContract.FilterType.entries.forEachIndexed { index, filter ->
                                SegmentedButton(
                                    selected = state.activeFilter == filter,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.handleIntent(DashboardContract.Intent.FilterTransactions(filter)) 
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardContract.FilterType.entries.size),
                                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        val privacyMode = settings?.isPrivacyModeEnabled ?: false

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isSearchActive && localSearchQuery.isEmpty()) {
                item {
                    PremiumBalanceHeader(
                        totalBalance = state.totalBalance,
                        income = state.totalIncome,
                        expenses = state.totalExpenses,
                        isPrivacyMode = privacyMode
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToInsights()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Intelligence",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Analyze your spending patterns",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSearchActive || localSearchQuery.isNotEmpty()) "Search Results" else "Timeline",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!isSearchActive && localSearchQuery.isEmpty()) {
                        TextButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAddDialog = true 
                        }) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                item {
                    if (isSearchActive || localSearchQuery.isNotEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text("No matching records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    TransactionCard(
                        transaction = transaction,
                        isPrivacyMode = privacyMode,
                        onDelete = { viewModel.handleIntent(DashboardContract.Intent.DeleteTransaction(transaction)) },
                        onEdit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editingTransaction = transaction
                        }
                    )
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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(DashboardContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            }
        )
    }
}
