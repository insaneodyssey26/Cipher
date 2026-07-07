package com.masum.cipher.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.ChartBar
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Lock
import com.masum.cipher.core.domain.model.TransactionCategory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

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
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false

    // State for Bottom Sheet
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddSheet = true 
                },
                containerColor = ElectricIndigo,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(LucideIcons.Plus, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Hero Section (Balance + In/Out)
            item {
                DashboardHero(
                    totalBalance = state.totalBalance,
                    income = state.totalIncome,
                    expense = state.totalExpenses,
                    privacyMode = privacyMode,
                    onNavigateToInsights = onNavigateToInsights,
                    onNavigateToSettings = onNavigateToSettings
                )
            }

            // 2. Budget Pulse Card
            item {
                BudgetPulseCard(
                    spent = state.totalExpenses,
                    budget = state.monthlyBudget,
                    onSetBudgetClick = onNavigateToSettings
                )
            }

            // 3. Timeline Label
            item {
                Text(
                    text = "RECENT ACTIVITY",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp)
                )
            }

            // 4. Transaction List
            if (state.transactions.isEmpty()) {
                item {
                    GenesisEmptyState(onAddManual = { showAddSheet = true })
                }
            } else {
                itemsIndexed(
                    items = state.transactions,
                    key = { _, t -> t.id }
                ) { index, transaction ->
                    StaggeredEntranceItem(index = index) {
                        TransactionItem(
                            transaction = transaction,
                            privacyMode = privacyMode,
                            onClick = {
                                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editingTransaction = transaction
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        TransactionDetailsSheet(
            transaction = TransactionEntity(
                amount = 0.0,
                merchant = "",
                currency = "INR",
                timestamp = System.currentTimeMillis(),
                category = "OTHERS",
                rawSms = null,
                isIncome = false
            ),
            onDismiss = { showAddSheet = false },
            onConfirm = { newTransaction ->
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(DashboardContract.Intent.AddTransaction(newTransaction))
                showAddSheet = false
            }
        )
    }

    editingTransaction?.let { transaction ->
        TransactionDetailsSheet(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.handleIntent(DashboardContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            },
            onDelete = {
                viewModel.handleIntent(DashboardContract.Intent.DeleteTransaction(transaction))
                editingTransaction = null
            }
        )
    }
}

@Composable
private fun DashboardHero(
    totalBalance: Double,
    income: Double,
    expense: Double,
    privacyMode: Boolean,
    onNavigateToInsights: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = if (MaterialTheme.colorScheme.surface == LightSurface) {
                        listOf(ElectricIndigo.copy(alpha = 0.04f), Transparent)
                    } else {
                        listOf(ElectricIndigo.copy(alpha = 0.08f), Transparent)
                    },
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Text(
                text = "TOTAL BALANCE",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "₹",
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (privacyMode) {
                    Text(
                        text = "••••••",
                        style = Typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    AnimatedNumberTicker(
                        value = totalBalance,
                        textStyle = Typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "INCOME", amount = income, color = EmeraldIncome, privacyMode = privacyMode)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                StatItem(label = "EXPENSES", amount = expense, color = RoseExpense, privacyMode = privacyMode)
            }
        }
        
        // Top Bar (App Name + Settings/Insights)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "cipher",
                style = Typography.titleMedium.copy(
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Row {
                IconButton(onClick = onNavigateToInsights) {
                    Icon(LucideIcons.ChartBar, "Insights", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(LucideIcons.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, amount: Double, color: Color, privacyMode: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (privacyMode) "••••" else "₹${String.format("%.0f", amount)}",
            style = Typography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun BudgetPulseCard(
    spent: Double,
    budget: Double,
    onSetBudgetClick: () -> Unit
) {
    VaultCard(
        modifier = Modifier.padding(horizontal = 24.dp),
        onClick = onSetBudgetClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = Typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (budget > 0) "₹${String.format("%.0f", spent)} / ₹${String.format("%.0f", budget)}" else "Tap to set a budget",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (budget > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Premium Gradient Progress Bar
                val progress = (spent / budget).toFloat().coerceIn(0f, 1f)
                val progressBrush = Brush.horizontalGradient(
                    colors = listOf(EmeraldIncome, ElectricIndigo, RoseExpense)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(White10, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(progressBrush, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    privacyMode: Boolean,
    onClick: () -> Unit
) {
    VaultCard(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        onClick = onClick,
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Icon
            val category = TransactionCategory.fromString(transaction.category)
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(transaction.timestamp)),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = if (privacyMode) "•••" else (if (transaction.isIncome) "+" else "-") + "₹${String.format("%.0f", transaction.amount)}",
                style = Typography.titleMedium,
                color = if (transaction.isIncome) EmeraldIncome else RoseExpense
            )
        }
    }
}

@Composable
private fun GenesisEmptyState(onAddManual: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            LucideIcons.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your financial vault is ready.",
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Incoming transaction SMS will be automatically parsed and secured here.",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddManual,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Manual Transaction", style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
