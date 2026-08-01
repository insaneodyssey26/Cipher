package com.masum.cipher.ui.insights

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.dashboard.TransactionItem
import com.masum.cipher.ui.theme.*
import com.masum.cipher.core.util.performVibrate
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    timestamp: Long,
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val view = androidx.compose.ui.platform.LocalView.current
    
    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    
    val date = remember(timestamp) { Date(timestamp) }
    val dayName = remember(date) { SimpleDateFormat("EEEE", Locale.getDefault()).format(date) }
    val fullDate = remember(date) { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date) }
    
    val dayRange = remember(timestamp) {
        val start = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        start to (start + 24L * 60L * 60L * 1000L)
    }

    val dayTransactions = remember(state.allTransactions, dayRange) {
        val (dayStart, dayEnd) = dayRange
        state.allTransactions.filter { tx -> tx.timestamp in dayStart until dayEnd }
    }

    val totalSpent = remember(dayTransactions) {
        dayTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }
    
    val totalIncome = remember(dayTransactions) {
        dayTransactions.filter { it.isIncome }.sumOf { it.amount }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            if (effect is InsightsContract.Effect.ShowUndoDelete) {
                // Handle Undo if needed
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayName.uppercase(),
                            style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = fullDate,
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        onNavigateBack()
                    }) {
                        Icon(LucideIcons.ArrowLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        label = "SPENT",
                        amount = "₹${totalSpent.toInt()}",
                        color = RoseExpense,
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatCard(
                        label = "INCOME",
                        amount = "₹${totalIncome.toInt()}",
                        color = EmeraldIncome,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "TRANSACTIONS",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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
                            text = "No records for this day",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                itemsIndexed(items = dayTransactions, key = { _, it -> it.id }) { index, transaction ->
                    StaggeredEntranceItem(index = index) {
                        TransactionItem(
                            transaction = transaction,
                            privacyMode = false,
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                editingTransaction = transaction
                            }
                        )
                    }
                }
            }
        }
    }

    editingTransaction?.let { transaction ->
        TransactionDetailsSheet(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                viewModel.handleIntent(InsightsContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            },
            onDelete = {
                viewModel.handleIntent(InsightsContract.Intent.DeleteTransaction(transaction))
                editingTransaction = null
            },
            isHapticsEnabled = isHapticsEnabled
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
    VaultCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Text(
                text = label,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = amount,
                style = Typography.headlineSmall,
                color = color
            )
        }
    }
}
