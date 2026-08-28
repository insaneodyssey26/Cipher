package com.masum.cipher.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.CalendarHeatmap
import com.masum.cipher.ui.components.CategoryAllocationDonut
import com.masum.cipher.ui.components.PeakHoursChart
import com.masum.cipher.ui.components.SpendingTrendChart
import com.masum.cipher.ui.components.TimeSelectorDropdown
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Target
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onNavigateToDayDetail: (Long) -> Unit,
    onNavigateToCategories: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val view = androidx.compose.ui.platform.LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddSubDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedSubscription by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<SubscriptionDetector.Subscription?>(null) }
    var showBudgetDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedCategoryForDetail by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<DashboardContract.CategoryData?>(null) }
    var editingTransaction by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.masum.cipher.core.data.local.entity.TransactionEntity?>(null) }
    var budgetInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val monthlyBudget = settings?.monthlyBudget ?: 0.0
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true

    if (showAddSubDialog || selectedSubscription != null) {
        com.masum.cipher.ui.components.EditSubscriptionSheet(
            subscription = selectedSubscription,
            onDismiss = { 
                showAddSubDialog = false 
                selectedSubscription = null
            },
            onConfirm = { merchant, amount, category, frequencyDays, nextExpectedDate ->
                viewModel.handleIntent(InsightsContract.Intent.SaveSubscription(merchant, amount, category, frequencyDays, nextExpectedDate))
                showAddSubDialog = false
                selectedSubscription = null
            },
            onDelete = {
                if (selectedSubscription?.confidence == 1.0f) {
                    selectedSubscription?.merchant?.let { 
                        viewModel.handleIntent(InsightsContract.Intent.DeleteSubscription(it)) 
                    }
                } else {
                    selectedSubscription?.merchant?.let {
                        viewModel.handleIntent(InsightsContract.Intent.IgnoreSubscription(it))
                    }
                }
                showAddSubDialog = false
                selectedSubscription = null
            }
        )
    }

    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InsightsContract.Effect.ShowUndoSubscriptionDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Subscription deleted",
                        actionLabel = "UNDO",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(InsightsContract.Intent.RestoreSubscription(effect.subscription))
                    }
                }
                else -> {}
            }
        }
    }
    
    if (showBudgetDialog) {
        com.masum.cipher.ui.components.EditBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { amount ->
                coroutineScope.launch {
                    userPreferences.setMonthlyBudget(amount)
                }
                showBudgetDialog = false
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { 
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 100.dp)
            ) 
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TimeSelectorDropdown(
                        selectedPeriod = state.selectedTimePeriod,
                        selectedTimeRange = state.selectedTimeRange,
                        onPeriodSelected = { period, start, end ->
                            viewModel.handleIntent(InsightsContract.Intent.SetTimePeriod(period, start, end))
                        },
                        isHapticsEnabled = isHapticsEnabled
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Narrative Hero
            item {
                InsightHero(state = state)
            }
            
            item {
                SectionLabel("MONTHLY BUDGET")
                com.masum.cipher.ui.components.BudgetHealthCard(
                    spent = state.monthlySummary.expense,
                    budget = monthlyBudget,
                    onEditBudgetClick = { showBudgetDialog = true },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    isHapticsEnabled = isHapticsEnabled
                )
            }

            item {
                SectionLabel("FINANCIAL FLOW")
                SpendingTrendChart(
                    expensePoints = state.expenseTrendHistory,
                    incomePoints = state.incomeTrendHistory,
                    netFlowPoints = state.netFlowTrendHistory,
                    isHapticsEnabled = isHapticsEnabled
                )
            }

            // 3. Category Allocation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("CATEGORY ALLOCATION")
                    androidx.compose.material3.TextButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onNavigateToCategories()
                        },
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text("View Hub")
                    }
                }
                VaultCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    CategoryAllocationDonut(
                        categories = state.categoryBreakdown,
                        categoryBudgets = settings?.categoryBudgets ?: emptyMap(),
                        onCategoryClick = { catData ->
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            selectedCategoryForDetail = catData
                        }
                    )
                }
            }

            // 4. Heatmap/Peak Hours
            item {
                SectionLabel("PEAK SPENDING HOURS")
                PeakHoursChart(hours = state.peakHours)
            }

            // 5. Subscriptions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("SUBSCRIPTIONS")
                    androidx.compose.material3.TextButton(
                        onClick = { showAddSubDialog = true },
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text("+ Add")
                    }
                }
                
                if (state.detectedSubscriptions.isNotEmpty()) {
                    val totalMonthly = state.detectedSubscriptions.sumOf { 
                        it.amount * (30.0 / it.frequencyDays.coerceAtLeast(1)) 
                    }
                    Text(
                        text = "Approx. monthly burden: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalMonthly)}",
                        style = Typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp).padding(bottom = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.detectedSubscriptions.forEach { sub ->
                            SubscriptionItem(
                                sub = sub, 
                                isHapticsEnabled = isHapticsEnabled,
                                onClick = { selectedSubscription = sub }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No subscriptions tracked yet.",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            // 6. Calendar History
            item {
                SectionLabel("ACTIVITY CALENDAR")
                VaultCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 16.dp
                ) {
                    CalendarHeatmap(
                        data = state.calendarHeatmap,
                        selectedTimestamp = state.selectedDayTimestamp,
                        onDayClick = { timestamp ->
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            onNavigateToDayDetail(timestamp)
                        }
                    )
                }
            }
        }
    }

    selectedCategoryForDetail?.let { catData ->
        val categoryEnum = com.masum.cipher.core.domain.model.TransactionCategory.fromString(catData.category)
        val filteredTxs = state.allTransactions.filter { tx ->
            tx.timestamp in state.selectedTimeRange.startTime..state.selectedTimeRange.endTime
        }
        com.masum.cipher.ui.components.CategoryDetailSheet(
            categoryData = catData,
            categoryBudget = settings?.categoryBudgets?.get(categoryEnum.displayName) ?: settings?.categoryBudgets?.get(categoryEnum.name) ?: 0.0,
            transactions = filteredTxs,
            onSetCategoryBudget = { newLimit ->
                viewModel.handleIntent(InsightsContract.Intent.SetCategoryBudget(categoryEnum.displayName, newLimit))
            },
            onDismiss = { selectedCategoryForDetail = null },
            onTransactionClick = { tx ->
                editingTransaction = tx
                selectedCategoryForDetail = null
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    editingTransaction?.let { tx ->
        com.masum.cipher.ui.components.TransactionDetailsSheet(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                viewModel.handleIntent(InsightsContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            },
            onDelete = {
                viewModel.handleIntent(InsightsContract.Intent.DeleteTransaction(tx))
                editingTransaction = null
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }
}

@Composable
private fun InsightHero(state: InsightsContract.State) {
    val mostExpensiveCategory = state.categoryBreakdown.maxByOrNull { it.amount }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(LucideIcons.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (mostExpensiveCategory != null) {
                val periodSuffix = when (state.selectedTimePeriod) {
                    com.masum.cipher.core.domain.model.TimePeriod.THIS_WEEK -> "this week"
                    com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK -> "last week"
                    com.masum.cipher.core.domain.model.TimePeriod.THIS_MONTH -> "this month"
                    com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH -> "last month"
                    com.masum.cipher.core.domain.model.TimePeriod.THIS_YEAR -> "this year"
                    com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME,
                    com.masum.cipher.core.domain.model.TimePeriod.CUSTOM -> "in this timeframe"
                }
                "You've spent the most on ${mostExpensiveCategory.category.lowercase().replaceFirstChar { it.uppercase() }} $periodSuffix."
            } else {
                "Your financial story is just beginning."
            },
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (state.selectedTimePeriod) {
                com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME -> {
                    val rangeLabel = AppFormatters.getPeriodLabel(state.selectedTimePeriod, state.allTransactions)
                    if (rangeLabel != "All Time") {
                        "All-time financial activity overview ($rangeLabel)."
                    } else {
                        "All-time financial activity overview."
                    }
                }
                else -> "Activity overview for ${AppFormatters.getPeriodLabel(state.selectedTimePeriod)}."
            },
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InsightMetric(label = "NO-SPEND STREAK", value = "${state.noSpendStreak} Days", icon = LucideIcons.Calendar)
            InsightMetric(label = "AVG SPEND / TXN", value = "₹${state.avgTransactionSize.toInt()}", icon = LucideIcons.Clock)
        }
    }
}

@Composable
private fun InsightMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = Typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        modifier = Modifier.padding(start = 24.dp, top = 28.dp, bottom = 10.dp)
    )
}

@Composable
fun SubscriptionItem(
    sub: SubscriptionDetector.Subscription,
    isHapticsEnabled: Boolean,
    onClick: () -> Unit = {}
) {
    val view = androidx.compose.ui.platform.LocalView.current

    VaultCard(
        onClick = { 
            view.performVibrate(isHapticsEnabled)
            onClick() 
        },
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(sub.category.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sub.category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = sub.category.color
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.merchant,
                    style = Typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Every ${sub.frequencyDays} days · Next: ${AppFormatters.getDay().format(Date(sub.nextExpectedDate))}",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AppFormatters.getCurrencyNoDecimals().format(sub.amount),
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
