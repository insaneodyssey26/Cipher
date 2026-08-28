package com.masum.cipher.ui.insights

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.TrendingUp
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateToDayDetail: (Long) -> Unit,
    onNavigateToCategories: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val view = androidx.compose.ui.platform.LocalView.current
    var showAddSubDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedSubscription by remember { androidx.compose.runtime.mutableStateOf<SubscriptionDetector.Subscription?>(null) }
    var showBudgetDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedCategoryForDetail by remember { androidx.compose.runtime.mutableStateOf<DashboardContract.CategoryData?>(null) }
    var editingTransaction by remember { androidx.compose.runtime.mutableStateOf<com.masum.cipher.core.data.local.entity.TransactionEntity?>(null) }
    val monthlyBudget = settings?.monthlyBudget ?: 0.0
    val coroutineScope = rememberCoroutineScope()

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

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

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

    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabs = listOf("Spending", "Habits", "Recurring")
    val selectedTabIndex = pagerState.currentPage

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp)
            ) {
                val tabWidth = maxWidth / tabs.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedTabIndex,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                    label = "insights_tab_offset"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = Typography.labelMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
                        ) {
                            item {
                                InsightHero(state = state)
                            }
                            
                            item {
                                SectionLabel("MONTHLY BUDGET")
                                com.masum.cipher.ui.components.BudgetHealthCard(
                                    spent = state.monthlySummary.expense,
                                    budget = monthlyBudget,
                                    onEditBudgetClick = { showBudgetDialog = true },
                                    modifier = Modifier.padding(horizontal = 16.dp),
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
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
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
                                    modifier = Modifier.padding(horizontal = 16.dp),
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

                            item {
                                SectionLabel("PEAK SPENDING HOURS")
                                PeakHoursChart(hours = state.peakHours)
                            }

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
                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
                        ) {
                            item {
                                SubscriptionsCard(
                                    subscriptions = state.detectedSubscriptions,
                                    isHapticsEnabled = isHapticsEnabled,
                                    onAddClick = { showAddSubDialog = true },
                                    onSubscriptionClick = { sub -> selectedSubscription = sub },
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
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
    val locale = LocalLocale.current.platformLocale
    val totalSpent = remember(state.categoryBreakdown) { state.categoryBreakdown.sumOf { it.amount } }
    val mostExpensiveCategory = state.categoryBreakdown.maxByOrNull { it.amount }
    val periodLabel = AppFormatters.getPeriodLabel(state.selectedTimePeriod, state.allTransactions)
    
    val daysInRange = remember(state.selectedTimeRange) {
        val now = System.currentTimeMillis()
        val end = state.selectedTimeRange.endTime.coerceAtMost(now)
        val start = state.selectedTimeRange.startTime
        ((end - start) / (1000L * 60 * 60 * 24)).coerceIn(1L, 3650L)
    }
    val dailyRunRate = if (daysInRange > 0) totalSpent / daysInRange.toDouble() else state.avgTransactionSize

    VaultCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 0.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "OVERVIEW",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = periodLabel,
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (mostExpensiveCategory != null && totalSpent > 0.0) {
                val categoryEnum = com.masum.cipher.core.domain.model.TransactionCategory.fromString(mostExpensiveCategory.category)
                val catPercent = ((mostExpensiveCategory.amount / totalSpent) * 100.0).toInt().coerceIn(0, 100)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Most spent on ${categoryEnum.displayName}",
                            style = Typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format(locale, "%,.0f", mostExpensiveCategory.amount)} spent · $catPercent% of total spending",
                            style = Typography.bodySmall.copy(
                                fontFamily = Manrope,
                                fontSize = 12.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(categoryEnum.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryEnum.icon,
                            contentDescription = null,
                            tint = categoryEnum.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "No spending recorded",
                        style = Typography.titleLarge.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Transactions in this period will appear here.",
                        style = Typography.bodySmall.copy(
                            fontFamily = Manrope,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "NO-SPEND STREAK",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.noSpendStreak} Days",
                        style = Typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                Column(
                    modifier = Modifier.weight(1.1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DAILY AVERAGE",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${String.format(locale, "%,.0f", dailyRunRate)} / day",
                        style = Typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "AVG / TXN",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${String.format(locale, "%,.0f", state.avgTransactionSize)}",
                        style = Typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = Typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        modifier = Modifier.padding(start = 24.dp, top = 28.dp, bottom = 10.dp)
    )
}

@Composable
fun SubscriptionsCard(
    subscriptions: List<SubscriptionDetector.Subscription>,
    isHapticsEnabled: Boolean,
    onAddClick: () -> Unit,
    onSubscriptionClick: (SubscriptionDetector.Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val locale = LocalLocale.current.platformLocale
    val totalMonthly = remember(subscriptions) {
        subscriptions.sumOf { it.amount * (30.0 / it.frequencyDays.coerceAtLeast(1)) }
    }
    val totalAnnual = remember(totalMonthly) { totalMonthly * 12.0 }

    val nextSub = remember(subscriptions) {
        subscriptions.minByOrNull { it.nextExpectedDate }
    }

    VaultCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Calendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "RECURRING BILLS",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (subscriptions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${subscriptions.size} Active",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onAddClick()
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Plus,
                        contentDescription = "Add Subscription",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Add",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (subscriptions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "MONTHLY COMMITMENT",
                            style = Typography.labelSmall.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${String.format(locale, "%,.0f", totalMonthly)} / mo",
                            style = Typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "≈ ₹${String.format(locale, "%,.0f", totalAnnual)} / yr projected",
                            style = Typography.labelSmall.copy(
                                fontFamily = Manrope,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (nextSub != null) {
                        val now = System.currentTimeMillis()
                        val diffDays = ((nextSub.nextExpectedDate - now) / (1000L * 60 * 60 * 24)).toInt()
                        val (badgeText, badgeColor) = when {
                            diffDays <= 0 -> Pair("Due today", MaterialTheme.colorScheme.error)
                            diffDays == 1 -> Pair("Due tomorrow", MaterialTheme.colorScheme.primary)
                            diffDays <= 7 -> Pair("Due in ${diffDays}d", MaterialTheme.colorScheme.primary)
                            else -> Pair("Next: ${AppFormatters.getDay().format(Date(nextSub.nextExpectedDate))}", MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeColor.copy(alpha = 0.12f))
                                .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Clock,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = badgeText,
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    subscriptions.forEachIndexed { index, sub ->
                        val diffDays = ((sub.nextExpectedDate - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
                        val freqLabel = when (sub.frequencyDays) {
                            7 -> "Weekly"
                            14 -> "Bi-weekly"
                            30, 31 -> "Monthly"
                            90 -> "Quarterly"
                            365 -> "Annual"
                            else -> "Every ${sub.frequencyDays}d"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    onSubscriptionClick(sub)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(sub.category.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = sub.category.icon,
                                    contentDescription = null,
                                    tint = sub.category.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.merchant,
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$freqLabel • Next: ${AppFormatters.getDay().format(Date(sub.nextExpectedDate))}",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (diffDays in 0..3) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (diffDays == 0) "Today" else "${diffDays}d left",
                                                style = Typography.labelSmall.copy(
                                                    fontFamily = Manrope,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "₹${String.format(locale, "%,.0f", sub.amount)}",
                                    style = Typography.titleMedium.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = LucideIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (index < subscriptions.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 50.dp)
                                    .height(0.5.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Calendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "No Recurring Bills Tracked",
                        style = Typography.titleSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cipher automatically detects repeat payments, or tap + Add to track one manually.",
                        style = Typography.bodySmall.copy(
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
