package com.masum.cipher.ui.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.masum.cipher.R
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.CategoryDetailSheet
import com.masum.cipher.ui.components.EditCategoryBudgetDialog
import com.masum.cipher.ui.components.TimeSelectorDropdown
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import com.masum.cipher.ui.insights.InsightsViewModel
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.ChevronRight
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val view = LocalView.current
    val isHapticsEnabled = settings?.isHapticsEnabled ?: true

    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var showBudgetDialogFor by remember { mutableStateOf<TransactionCategory?>(null) }
    var selectedCategoryForDetail by remember { mutableStateOf<DashboardContract.CategoryData?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val categoryBudgets = settings?.categoryBudgets ?: emptyMap()

    val filteredTransactions = remember(state.allTransactions, state.selectedTimeRange) {
        state.allTransactions.filter { tx ->
            tx.timestamp in state.selectedTimeRange.startTime..state.selectedTimeRange.endTime
        }
    }

    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }

    val allCategoryItems = remember(filteredTransactions, totalExpense) {
        val expenses = filteredTransactions.filter { !it.isIncome }
        val categoryTxMap = expenses.groupBy { 
            TransactionCategory.fromString(it.category)
        }

        TransactionCategory.entries.filter { it != TransactionCategory.INCOME }.map { cat ->
            val txList = categoryTxMap[cat] ?: emptyList()
            val spent = txList.sumOf { it.amount }
            val percentage = if (totalExpense > 0) (spent / totalExpense).toFloat() else 0f
            DashboardContract.CategoryData(
                category = cat.displayName,
                amount = spent,
                percentage = percentage,
                color = cat.color.value.toLong()
            )
        }.sortedWith(
            compareByDescending<DashboardContract.CategoryData> { it.amount > 0 }
                .thenByDescending { it.amount }
        )
    }

    val activeCount = allCategoryItems.count { it.amount > 0 }
    val budgetedCount = categoryBudgets.count { it.value > 0 }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_categories),
                        style = Typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TimeSelectorDropdown(
                        selectedPeriod = state.selectedTimePeriod,
                        selectedTimeRange = state.selectedTimeRange,
                        onPeriodSelected = { period, start, end ->
                            viewModel.handleIntent(InsightsContract.Intent.SetTimePeriod(period, start, end))
                        },
                        isHapticsEnabled = isHapticsEnabled,
                        iconOnly = true,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                VaultCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentPadding = 18.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = stringResource(R.string.dashboard_total_spent).uppercase(),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppFormatters.formatCompactCurrency(totalExpense, currencySymbol = state.currencySymbol),
                                    style = Typography.headlineLarge.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.8).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$activeCount ${stringResource(R.string.active_suffix)}",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (budgetedCount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$budgetedCount budgeted",
                                        style = Typography.bodySmall.copy(
                                            fontFamily = Manrope,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            items(
                items = allCategoryItems,
                key = { it.category }
            ) { categoryData ->
                val categoryEnum = TransactionCategory.fromString(categoryData.category)
                val categoryColor = categoryEnum.color
                val budget = categoryBudgets[categoryEnum.displayName] ?: categoryBudgets[categoryEnum.name] ?: 0.0
                val spent = categoryData.amount
                val hasBudget = budget > 0
                val isOverBudget = hasBudget && spent > budget
                val percentUsed = if (hasBudget) ((spent / budget) * 100).toInt() else 0
                val isExpanded = expandedCategory == categoryData.category

                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "chevron_${categoryData.category}"
                )

                val progress = if (hasBudget) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "catProg_${categoryData.category}"
                )

                val now = Calendar.getInstance()
                val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
                val currentDay = now.get(Calendar.DAY_OF_MONTH)
                val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
                val remainingBudget = budget - spent
                val safeDaily = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0

                VaultCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        expandedCategory = if (isExpanded) null else categoryData.category
                    },
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 14.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryEnum.icon,
                                        contentDescription = null,
                                        tint = categoryColor,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(categoryEnum.titleRes),
                                        style = Typography.titleMedium.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (totalExpense > 0 && spent > 0) {
                                            "${String.format(Locale.getDefault(), "%.1f", categoryData.percentage * 100)}% of total"
                                        } else {
                                            stringResource(R.string.no_spend_this_period)
                                        },
                                        style = Typography.bodySmall.copy(
                                            fontFamily = Manrope,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = AppFormatters.formatCompactCurrency(spent, currencySymbol = state.currencySymbol),
                                    style = Typography.titleLarge.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (isOverBudget) RoseExpense else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Icon(
                                    imageVector = LucideIcons.ChevronDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(chevronRotation)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
                            exit = shrinkVertically(tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (hasBudget) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isOverBudget) RoseExpense else if (percentUsed >= 85) Color(0xFFF59E0B) else EmeraldIncome)
                                            )
                                            Text(
                                                text = if (isOverBudget) "OVER LIMIT" else "$percentUsed% OF LIMIT USED",
                                                style = Typography.labelSmall.copy(
                                                    fontFamily = Manrope,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    letterSpacing = 0.8.sp
                                                ),
                                                color = if (isOverBudget) RoseExpense else if (percentUsed >= 85) Color(0xFFF59E0B) else EmeraldIncome
                                            )
                                        }

                                        val currencySymbol = settings?.currencySymbol ?: state.currencySymbol
                                        val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
                                        Text(
                                            text = "${stringResource(R.string.limit_suffix)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(budget, currencySymbol, locale)}",
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Manrope,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(animatedProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (isOverBudget) RoseExpense else categoryColor)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val currencySymbol = settings?.currencySymbol ?: state.currencySymbol
                                        val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
                                        Text(
                                            text = if (isOverBudget) {
                                                "Exceeded by ${com.masum.cipher.core.util.AppFormatters.formatCurrency(spent - budget, currencySymbol, locale)}"
                                            } else {
                                                "${com.masum.cipher.core.util.AppFormatters.formatCurrency(remainingBudget, currencySymbol, locale)} left"
                                            },
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Manrope,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = if (isOverBudget) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!isOverBudget) {
                                            Text(
                                                text = "${stringResource(R.string.safe_daily_spend)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(safeDaily, currencySymbol, locale)}/${stringResource(R.string.day_unit)}",
                                                style = Typography.labelSmall.copy(
                                                    fontFamily = Manrope,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.no_limit_set),
                                            style = Typography.bodySmall.copy(
                                                fontFamily = Manrope,
                                                fontSize = 11.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.set_limit),
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Manrope,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = categoryColor,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(categoryColor.copy(alpha = 0.12f))
                                                .clickable {
                                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                    showBudgetDialogFor = categoryEnum
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedCategoryForDetail = categoryData
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.view_breakdown_transactions),
                                        style = Typography.labelMedium.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = LucideIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showBudgetDialogFor?.let { cat ->
        EditCategoryBudgetDialog(
            categoryName = stringResource(cat.titleRes),
            currentBudget = categoryBudgets[cat.displayName] ?: categoryBudgets[cat.name] ?: 0.0,
            currencySymbol = settings?.currencySymbol ?: state.currencySymbol,
            onDismiss = { showBudgetDialogFor = null },
            onConfirm = { newLimit ->
                viewModel.handleIntent(InsightsContract.Intent.SetCategoryBudget(cat.displayName, newLimit))
                showBudgetDialogFor = null
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    selectedCategoryForDetail?.let { catData ->
        val categoryEnum = TransactionCategory.fromString(catData.category)
        CategoryDetailSheet(
            categoryData = catData,
            categoryBudget = categoryBudgets[categoryEnum.displayName] ?: categoryBudgets[categoryEnum.name] ?: 0.0,
            transactions = filteredTransactions,
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
        TransactionDetailsSheet(
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
