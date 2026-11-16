package com.masum.cipher.ui.insights

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.ui.components.FitText
import com.masum.cipher.ui.components.ambientBackdrop
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
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.RoseExpense
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Plus
import kotlinx.coroutines.launch
import java.util.Date

private data class InsightBudgetPage(
    val accountId: Long?,
    val name: String,
    val spent: Double,
    val income: Double,
    val colorHex: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateToDayDetail: (Long) -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = userPreferences.getCachedSettings())
    val view = androidx.compose.ui.platform.LocalView.current
    var showAddSubDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedSubscription by remember { androidx.compose.runtime.mutableStateOf<SubscriptionDetector.Subscription?>(null) }
    var showBudgetDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var dialogTargetAccountId by remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }
    var selectedCategoryForDetail by remember { androidx.compose.runtime.mutableStateOf<DashboardContract.CategoryData?>(null) }
    var editingTransaction by remember { androidx.compose.runtime.mutableStateOf<com.masum.cipher.core.data.local.entity.TransactionEntity?>(null) }
    val monthlyBudget = settings?.monthlyBudget ?: 0.0
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true

    if (showAddSubDialog || selectedSubscription != null) {
        com.masum.cipher.ui.components.EditSubscriptionSheet(
            subscription = selectedSubscription,
            customCategories = state.customCategories,
            currencySymbol = state.currencySymbol,
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
        val accountEntities = state.accounts.map { it.entity }
        com.masum.cipher.ui.components.EditBudgetDialog(
            currentBudget = monthlyBudget,
            isDynamicBudget = settings?.isDynamicBudgetEnabled ?: false,
            currentMonthIncome = state.monthlySummary.income,
            currencySymbol = state.currencySymbol,
            accounts = accountEntities,
            selectedAccountId = dialogTargetAccountId ?: settings?.budgetAccountId,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { amount, isDynamic, accountId ->
                coroutineScope.launch {
                    userPreferences.setMonthlyBudget(amount)
                    userPreferences.setDynamicBudgetEnabled(isDynamic)
                    userPreferences.setBudgetAccountId(accountId)
                    try {
                        dagger.hilt.android.EntryPointAccessors.fromApplication(context, com.masum.cipher.core.di.WidgetEntryPoint::class.java)
                            .widgetSyncManager()
                            .syncWidget()
                    } catch (_: Exception) {}
                }
                showBudgetDialog = false
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabs = listOf(
        stringResource(R.string.tab_spending),
        stringResource(R.string.tab_habits),
        stringResource(R.string.tab_recurring)
    )
    val selectedTabIndex = pagerState.currentPage

    Scaffold(
        modifier = Modifier.ambientBackdrop(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                                    fontFamily = Lato,
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
                            contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp)
                        ) {
                            item {
                                InsightHero(state = state)
                            }

                            item {
                                val monthStart = remember { com.masum.cipher.core.util.DateTimeUtils.currentMonthStart() }
                                val rootAccountId = remember(state.accounts) {
                                    state.accounts.minByOrNull { it.entity.createdAt }?.id ?: state.accounts.firstOrNull()?.id ?: 1L
                                }

                                val budgetPages = remember(state.accounts, state.allTransactions, state.monthlySummary, monthlyBudget) {
                                    val allPage = InsightBudgetPage(
                                        accountId = null,
                                        name = "All Accounts",
                                        spent = state.monthlySummary.expense,
                                        income = state.monthlySummary.income,
                                        colorHex = 0xFF4F46E5L
                                    )
                                    val accountPages = state.accounts.map { acc ->
                                        val accTxs = state.allTransactions.filter {
                                            it.timestamp >= monthStart && (it.accountId == acc.id || (it.accountId == null && acc.id == rootAccountId))
                                        }
                                        val accSpent = accTxs.filter { !it.isIncome }.sumOf { it.amount }
                                        val accIncome = accTxs.filter { it.isIncome }.sumOf { it.amount }
                                        InsightBudgetPage(
                                            accountId = acc.id,
                                            name = acc.name,
                                            spent = accSpent,
                                            income = accIncome,
                                            colorHex = acc.colorHex
                                        )
                                    }
                                    listOf(allPage) + accountPages
                                }

                                val budgetPagerState = rememberPagerState(pageCount = { budgetPages.size })

                                SectionLabel(stringResource(R.string.dashboard_monthly_budget).uppercase())

                                if (budgetPages.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .padding(bottom = 14.dp)
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        budgetPages.forEachIndexed { index, pageData ->
                                            val isSelected = budgetPagerState.currentPage == index
                                            val pageColor = Color(pageData.colorHex)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(
                                                        if (isSelected) pageColor.copy(alpha = 0.18f)
                                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                                    )
                                                    .clickable {
                                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                        coroutineScope.launch {
                                                            budgetPagerState.animateScrollToPage(index)
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(pageColor)
                                                    )
                                                    Text(
                                                        text = pageData.name,
                                                        style = Typography.labelMedium.copy(
                                                            fontFamily = Lato,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 12.sp
                                                        ),
                                                        color = if (isSelected) pageColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalPager(
                                    state = budgetPagerState,
                                    modifier = Modifier.fillMaxWidth()
                                ) { pageIdx ->
                                    val pageData = budgetPages.getOrNull(pageIdx) ?: budgetPages.first()
                                    com.masum.cipher.ui.components.BudgetHealthCard(
                                        spent = pageData.spent,
                                        budget = monthlyBudget,
                                        income = pageData.income,
                                        isDynamicBudget = settings?.isDynamicBudgetEnabled ?: false,
                                        currencySymbol = state.currencySymbol,
                                        targetAccountName = pageData.name,
                                        onEditBudgetClick = {
                                            dialogTargetAccountId = pageData.accountId
                                            showBudgetDialog = true
                                        },
                                        onToggleDynamicMode = { enabled ->
                                            viewModel.handleIntent(InsightsContract.Intent.SetDynamicBudget(enabled))
                                        },
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        isHapticsEnabled = isHapticsEnabled
                                    )
                                }
                            }

                            item {
                                SectionLabel(stringResource(R.string.cash_flow_trend).uppercase())
                                SpendingTrendChart(
                                    expensePoints = state.expenseTrendHistory,
                                    incomePoints = state.incomeTrendHistory,
                                    netFlowPoints = state.netFlowTrendHistory,
                                    currencySymbol = state.currencySymbol,
                                    isHapticsEnabled = isHapticsEnabled
                                )
                            }

                            item {
                                SectionLabel(stringResource(R.string.insights_net_worth_title).uppercase())
                                NetWorthRow(
                                    state = state,
                                    onNavigateToAccounts = onNavigateToAccounts,
                                    isHapticsEnabled = isHapticsEnabled
                                )
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionTitle(stringResource(R.string.spending_by_category))
                                    Text(
                                        text = stringResource(R.string.view_hub),
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = com.masum.cipher.ui.theme.DMSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .clickable {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                onNavigateToCategories()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                                VaultCard(
                                    flat = true,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentPadding = 0.dp
                                ) {
                                    CategoryAllocationDonut(
                                        categories = state.categoryBreakdown,
                                        categoryBudgets = settings?.categoryBudgets ?: emptyMap(),
                                        customCategories = state.customCategories,
                                        currencySymbol = state.currencySymbol,
                                        onCategoryClick = { catData ->
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedCategoryForDetail = catData
                                        }
                                    )
                                }
                            }

                            item {
                                SectionLabel(stringResource(R.string.peak_spending_hours).uppercase())
                                PeakHoursChart(hours = state.peakHours, currencySymbol = state.currencySymbol)
                            }

                            item {
                                SectionLabel(stringResource(R.string.activity_calendar).uppercase())
                                VaultCard(
                                    flat = true,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentPadding = 0.dp
                                ) {
                                    CalendarHeatmap(
                                        data = state.calendarHeatmap,
                                        selectedTimestamp = state.selectedDayTimestamp,
                                        currencySymbol = state.currencySymbol,
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
                            contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp)
                        ) {
                            item {
                                SubscriptionsCard(
                                    subscriptions = state.detectedSubscriptions,
                                    customCategories = state.customCategories,
                                    currencySymbol = state.currencySymbol,
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
        val categoryItem = com.masum.cipher.core.domain.model.CategoryHelper.resolveCategory(catData.category, state.customCategories)
        val filteredTxs = state.allTransactions.filter { tx ->
            tx.timestamp in state.selectedTimeRange.startTime..state.selectedTimeRange.endTime
        }
        com.masum.cipher.ui.components.CategoryDetailSheet(
            categoryData = catData,
            categoryBudget = settings?.categoryBudgets?.get(categoryItem.displayName) ?: settings?.categoryBudgets?.get(categoryItem.name) ?: settings?.categoryBudgets?.get(catData.category) ?: 0.0,
            transactions = filteredTxs,
            customCategories = state.customCategories,
            currencySymbol = state.currencySymbol,
            onSetCategoryBudget = { newLimit ->
                viewModel.handleIntent(InsightsContract.Intent.SetCategoryBudget(categoryItem.name, newLimit))
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
            accounts = state.accounts,
            customCategories = state.customCategories,
            currencySymbol = state.currencySymbol,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                viewModel.handleIntent(InsightsContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            },
            onSaveSplits = { splits ->
                viewModel.handleIntent(InsightsContract.Intent.SaveTransactionSplits(tx.id, splits))
            },
            onDelete = {
                viewModel.handleIntent(InsightsContract.Intent.DeleteTransaction(tx))
                editingTransaction = null
            },
            onCreateCustomCategory = { name, iconName, colorHex ->
                viewModel.handleIntent(InsightsContract.Intent.CreateCustomCategory(name, iconName, colorHex))
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    state.promptMerchantRuleFor?.let { prompt ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(InsightsContract.Intent.DismissMerchantRulePrompt) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text(
                    text = stringResource(R.string.smart_rules_merchant_dialog_title),
                    style = com.masum.cipher.ui.theme.Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.smart_rules_merchant_dialog_message, prompt.rawMerchant, prompt.newMerchant),
                    style = com.masum.cipher.ui.theme.Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                        viewModel.handleIntent(InsightsContract.Intent.SaveMerchantRule(prompt.rawMerchant, prompt.newMerchant))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.smart_rules_dialog_confirm), color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                    viewModel.handleIntent(InsightsContract.Intent.DismissMerchantRulePrompt)
                }) {
                    Text(stringResource(R.string.smart_rules_dialog_dismiss), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    state.promptCategoryRuleFor?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(InsightsContract.Intent.DismissCategoryRulePrompt) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text(
                    text = stringResource(R.string.smart_rules_category_dialog_title),
                    style = com.masum.cipher.ui.theme.Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                val catItem = com.masum.cipher.core.domain.model.CategoryHelper.resolveCategory(tx.category, state.customCategories)
                val categoryDisplayName = catItem.titleRes?.let { stringResource(it) } ?: catItem.displayName
                Text(
                    text = stringResource(R.string.smart_rules_category_dialog_message, tx.merchant, categoryDisplayName),
                    style = com.masum.cipher.ui.theme.Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                        viewModel.handleIntent(InsightsContract.Intent.SaveCategoryRule(tx.merchant, tx.category))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.smart_rules_dialog_confirm), color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                    viewModel.handleIntent(InsightsContract.Intent.DismissCategoryRulePrompt)
                }) {
                    Text(stringResource(R.string.smart_rules_dialog_dismiss), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun InsightHero(state: InsightsContract.State) {
    val totalSpent = remember(state.categoryBreakdown) { state.categoryBreakdown.sumOf { it.amount } }
    val mostExpensiveCategory = state.categoryBreakdown.maxByOrNull { it.amount }
    val context = androidx.compose.ui.platform.LocalContext.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
    val periodLabel = AppFormatters.getPeriodLabel(state.selectedTimePeriod, state.allTransactions, context = context, locale = locale)
    val dm = com.masum.cipher.ui.theme.DMSans

    val daysInRange = remember(state.selectedTimeRange) {
        val now = System.currentTimeMillis()
        val end = state.selectedTimeRange.endTime.coerceAtMost(now)
        val start = state.selectedTimeRange.startTime
        ((end - start) / (1000L * 60 * 60 * 24)).coerceIn(1L, 3650L)
    }
    val dailyRunRate = if (daysInRange > 0) totalSpent / daysInRange.toDouble() else state.avgTransactionSize
    val segments = remember(state.categoryBreakdown, state.customCategories) {
        state.categoryBreakdown
            .sortedByDescending { it.amount }
            .take(6)
            .map { it.amount to com.masum.cipher.core.domain.model.CategoryHelper.resolveCategory(it.category, state.customCategories).color }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(stringResource(R.string.overview))
            Text(
                text = periodLabel,
                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        FitText(
            text = AppFormatters.formatCurrency(totalSpent, state.currencySymbol, locale, decimals = 0),
            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, letterSpacing = (-1.4).sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        if (segments.isNotEmpty() && totalSpent > 0.0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                segments.forEach { (amount, color) ->
                    Box(
                        modifier = Modifier
                            .weight(amount.toFloat().coerceAtLeast(0.0001f))
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }

        if (mostExpensiveCategory != null && totalSpent > 0.0) {
            val categoryItem = com.masum.cipher.core.domain.model.CategoryHelper.resolveCategory(mostExpensiveCategory.category, state.customCategories)
            val categoryName = categoryItem.titleRes?.let { stringResource(it) } ?: categoryItem.displayName
            val catPercent = ((mostExpensiveCategory.amount / totalSpent) * 100.0).toInt().coerceIn(0, 100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(categoryItem.color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = categoryItem.icon, contentDescription = null, tint = categoryItem.color, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryName,
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${AppFormatters.formatCompactCurrency(mostExpensiveCategory.amount, currencySymbol = state.currencySymbol)} · $catPercent%",
                        style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.no_spending_desc),
                style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            listOf(
                Triple(stringResource(R.string.no_spend_streak), pluralStringResource(R.plurals.no_spend_streak_days, state.noSpendStreak, state.noSpendStreak), Alignment.Start),
                Triple(stringResource(R.string.daily_average), "${AppFormatters.formatCompactCurrency(dailyRunRate, currencySymbol = state.currencySymbol)}/${stringResource(R.string.day_unit)}", Alignment.CenterHorizontally),
                Triple(stringResource(R.string.avg_txn), AppFormatters.formatCompactCurrency(state.avgTransactionSize, currencySymbol = state.currencySymbol), Alignment.End)
            ).forEach { (label, value, align) ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = align) {
                    Text(
                        text = label.uppercase(),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = value,
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text.lowercase().replaceFirstChar { it.uppercase() },
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = com.masum.cipher.ui.theme.DMSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionLabel(text: String, withDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (withDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp)
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
        }
        Box(modifier = Modifier.padding(start = 24.dp, top = if (withDivider) 18.dp else 2.dp, bottom = 10.dp)) {
            SectionTitle(text)
        }
    }
}

@Composable
fun SubscriptionsCard(
    subscriptions: List<SubscriptionDetector.Subscription>,
    customCategories: List<com.masum.cipher.core.data.local.entity.CustomCategoryEntity> = emptyList(),
    currencySymbol: String = "₹",
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
        flat = true,
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
                        text = stringResource(R.string.recurring_bills_title).uppercase(),
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
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
                                text = "${subscriptions.size} ${stringResource(R.string.active_suffix)}",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
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
                        contentDescription = stringResource(R.string.action_add_transaction),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_add_transaction).substringBefore(" "),
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
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
                            text = stringResource(R.string.monthly_commitment).uppercase(),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${AppFormatters.formatCurrency(totalMonthly, currencySymbol, locale)} / mo",
                            style = Typography.titleLarge.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "≈ ${AppFormatters.formatCurrency(totalAnnual, currencySymbol, locale)} / yr projected",
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
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
                                        fontFamily = Lato,
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
                            val catItem = com.masum.cipher.core.domain.model.CategoryHelper.resolveCategory(sub.category, customCategories)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(catItem.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = catItem.icon,
                                    contentDescription = null,
                                    tint = catItem.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.merchant,
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Lato,
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
                                            fontFamily = Lato,
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
                                                    fontFamily = Lato,
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
                                    text = AppFormatters.formatCurrency(sub.amount, currencySymbol, locale),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = Lato,
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
                        text = stringResource(R.string.no_recurring_bills),
                        style = Typography.titleSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.no_recurring_bills_desc),
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
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

@Composable
private fun NetWorthRow(
    state: InsightsContract.State,
    onNavigateToAccounts: () -> Unit,
    isHapticsEnabled: Boolean
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
    val dm = com.masum.cipher.ui.theme.DMSans
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                view.performVibrate(isHapticsEnabled, isLongPress = false)
                onNavigateToAccounts()
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = LucideIcons.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = AppFormatters.formatCurrency(state.totalNetWorth, state.currencySymbol, locale, decimals = 0),
                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.6).sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = buildString {
                    append(stringResource(R.string.insights_net_worth_accounts_count, state.accounts.size))
                    append("  ·  ")
                    append(AppFormatters.formatCompactCurrency(state.totalLiquidBalance, currencySymbol = state.currencySymbol))
                    append(" ")
                    append(stringResource(R.string.insights_net_worth_liquid))
                    if (state.totalDebt > 0) {
                        append("  ·  -")
                        append(AppFormatters.formatCompactCurrency(state.totalDebt, currencySymbol = state.currencySymbol))
                        append(" ")
                        append(stringResource(R.string.insights_net_worth_debt))
                    }
                },
                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Icon(imageVector = LucideIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}

