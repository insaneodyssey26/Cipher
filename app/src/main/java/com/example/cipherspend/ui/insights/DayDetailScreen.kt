package com.example.cipherspend.ui.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.ui.components.EditTransactionDialog
import com.example.cipherspend.ui.components.TransactionCard
import com.example.cipherspend.ui.theme.IncomeGreen
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    timestamp: Long,
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by userPreferences.settingsFlow.collectAsState(initial = null)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    
    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    
    val date = remember(timestamp) { Date(timestamp) }
    val dayName = remember(date) { SimpleDateFormat("EEEE", Locale.getDefault()).format(date) }
    val fullDate = remember(date) { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date) }
    
    val dayTransactions = remember(state.allTransactions, timestamp) {
        state.allTransactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            txCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
            txCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
        }
    }

    val totalSpent = remember(dayTransactions) {
        dayTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }
    
    val totalIncome = remember(dayTransactions) {
        dayTransactions.filter { it.isIncome }.sumOf { it.amount }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is InsightsContract.Effect.ShowUndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.handleIntent(InsightsContract.Intent.RestoreTransaction(effect.transaction))
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = fullDate,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailStatCard(
                        label = "Total Spent",
                        amount = currencyFormatter.format(totalSpent),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatCard(
                        label = "Total Income",
                        amount = currencyFormatter.format(totalIncome),
                        color = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            if (dayTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions for this day",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(dayTransactions) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        isPrivacyMode = false,
                        onDelete = {
                            viewModel.handleIntent(InsightsContract.Intent.DeleteTransaction(transaction))
                        },
                        onEdit = {
                            if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editingTransaction = transaction
                        },
                        isHapticsEnabled = isHapticsEnabled
                    )
                }
            }
        }
    }

    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(InsightsContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            }
        )
    }
}

@Composable
private fun DetailStatCard(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}
