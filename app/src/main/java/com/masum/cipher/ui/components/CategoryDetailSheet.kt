package com.masum.cipher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.groupByMerchant
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.X
import java.util.Calendar
import kotlinx.coroutines.launch

private data class CategoryMerchantSpend(
    val merchant: String,
    val totalAmount: Double,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDetailSheet(
    categoryData: DashboardContract.CategoryData,
    categoryBudget: Double,
    transactions: List<TransactionEntity>,
    customCategories: List<CustomCategoryEntity> = emptyList(),
    onSetCategoryBudget: (Double) -> Unit,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale
    var showBudgetDialog by remember { mutableStateOf(false) }

    val categoryItem = remember(categoryData.category, customCategories) {
        CategoryHelper.resolveCategory(categoryData.category, customCategories)
    }
    val categoryColor = categoryItem.color
    val categoryIcon = categoryItem.icon
    val categoryTitle = if (categoryItem.titleRes != null) stringResource(categoryItem.titleRes) else categoryItem.displayName

    val categoryTransactions = remember(transactions, categoryData.category, customCategories) {
        transactions.filter {
            !it.isIncome && (
                it.category.equals(categoryData.category, ignoreCase = true) ||
                it.category.equals(categoryItem.name, ignoreCase = true) ||
                it.category.equals(categoryItem.displayName, ignoreCase = true)
            )
        }.sortedByDescending { it.timestamp }
    }

    val totalSpent = remember(categoryTransactions) {
        categoryTransactions.sumOf { it.amount }
    }
    val txCount = categoryTransactions.size
    val avgSpend = if (txCount > 0) totalSpent / txCount else 0.0
    val maxSpend = categoryTransactions.maxOfOrNull { it.amount } ?: 0.0

    val topMerchants = remember(categoryTransactions) {
        categoryTransactions
            .groupByMerchant(blankLabel = "Unknown") { it.merchant }
            .map { group ->
                CategoryMerchantSpend(
                    merchant = group.name,
                    totalAmount = group.items.sumOf { it.amount },
                    count = group.items.size
                )
            }
            .sortedByDescending { it.totalAmount }
            .take(5)
    }

    val now = Calendar.getInstance()
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = now.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val remainingBudget = categoryBudget - totalSpent
    val safeDailySpend = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0

    val hasBudget = categoryBudget > 0
    val isOverBudget = hasBudget && totalSpent > categoryBudget
    val budgetPercent = if (hasBudget) (totalSpent / categoryBudget).toFloat().coerceIn(0f, 1f) else 0f
    val budgetPercentInt = if (hasBudget) ((totalSpent / categoryBudget) * 100).toInt() else 0

    val animatedBudgetProgress by animateFloatAsState(
        targetValue = budgetPercent,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "categoryBudgetProgress"
    )

    if (showBudgetDialog) {
        EditCategoryBudgetDialog(
            categoryName = categoryTitle,
            currentBudget = categoryBudget,
            currencySymbol = currencySymbol,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newLimit ->
                onSetCategoryBudget(newLimit)
                showBudgetDialog = false
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeWithAnimation: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    val consumeOverscrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                return available
            }
            override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available
            }
        }
    }

    val dm = DMSans
    val grouped = remember(categoryTransactions) {
        categoryTransactions.groupBy { tx ->
            Calendar.getInstance().apply { timeInMillis = tx.timestamp }.let { it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR) }
        }.values.toList()
    }
    val maxMerchant = topMerchants.maxOfOrNull { it.totalAmount } ?: 1.0
    val budgetColor = when {
        isOverBudget -> RoseExpense
        budgetPercentInt >= 85 -> Color(0xFFF59E0B)
        else -> EmeraldIncome
    }

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 12.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        tint = categoryColor,
        tintAlpha = 0.2f
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .nestedScroll(consumeOverscrollConnection),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 32.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(categoryColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = categoryTitle.uppercase(),
                        style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.6.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FitText(
                        text = AppFormatters.formatCurrency(totalSpent, currencySymbol, locale, decimals = 0),
                        style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, letterSpacing = (-1.0).sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "${String.format(java.util.Locale.getDefault(), "%.1f", categoryData.percentage * 100)}% of spending  ·  $txCount ${if (txCount == 1) "transaction" else "transactions"}",
                        style = TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(Modifier.height(26.dp))
                if (hasBudget) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOverBudget) "Over budget" else "$budgetPercentInt% of budget",
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                color = budgetColor
                            )
                            Text(
                                text = "Edit",
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        showBudgetDialog = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedBudgetProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(budgetColor)
                            )
                        }
                        Text(
                            text = if (isOverBudget) "${AppFormatters.formatCurrency(totalSpent - categoryBudget, currencySymbol, locale, decimals = 0)} over a ${AppFormatters.formatCurrency(categoryBudget, currencySymbol, locale, decimals = 0)} limit"
                            else "${AppFormatters.formatCurrency(remainingBudget, currencySymbol, locale, decimals = 0)} left  ·  safe to spend ${AppFormatters.formatCurrency(safeDailySpend, currencySymbol, locale, decimals = 0)}/day",
                            style = TextStyle(fontFamily = dm, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                showBudgetDialog = true
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(categoryColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = LucideIcons.Target, contentDescription = null, tint = categoryColor, modifier = Modifier.size(19.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Set a monthly budget",
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Get a safe daily spend for this category",
                                style = TextStyle(fontFamily = dm, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(26.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Average" to AppFormatters.formatCompactCurrency(avgSpend, currencySymbol = currencySymbol),
                        "Largest" to AppFormatters.formatCompactCurrency(maxSpend, currencySymbol = currencySymbol),
                        "Count" to txCount.toString()
                    ).forEachIndexed { index, (label, value) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = when (index) { 0 -> Alignment.Start; 1 -> Alignment.CenterHorizontally; else -> Alignment.End },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = label.uppercase(),
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = value,
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
            }

            if (topMerchants.isNotEmpty()) {
                item {
                    Text(
                        text = "TOP MERCHANTS",
                        style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(14.dp))
                }
                items(topMerchants.size) { index ->
                    val m = topMerchants[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.merchant,
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${m.count}×  ${AppFormatters.formatCurrency(m.totalAmount, currencySymbol, locale, decimals = 0)}",
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((m.totalAmount / maxMerchant).toFloat().coerceIn(0.02f, 1f))
                                    .fillMaxHeight()
                                    .background(categoryColor)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(14.dp)) }
            }

            item {
                Text(
                    text = stringResource(R.string.view_breakdown_transactions).uppercase(),
                    style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.4.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
            }

            grouped.forEach { dayTxs ->
                item {
                    Text(
                        text = AppFormatters.formatFullDate(dayTxs.first().timestamp),
                        style = TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                    )
                }
                items(dayTxs.size) { i ->
                    val tx = dayTxs[i]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onTransactionClick(tx)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(categoryColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tx.merchant.trim().take(1).uppercase().ifEmpty { "?" },
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = categoryColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = tx.merchant,
                                style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = AppFormatters.formatDateTime(tx.timestamp, locale = locale),
                                style = TextStyle(fontFamily = dm, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "-${AppFormatters.formatCurrency(tx.amount, currencySymbol, locale, decimals = 0)}",
                            style = TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = (-0.2).sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditCategoryBudgetDialog(
    categoryName: String,
    currentBudget: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    var budgetInput by remember {
        mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = "$categoryName ${stringResource(R.string.limit_suffix)}",
                    style = Typography.titleLarge.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.set_spending_limit_for, categoryName),
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 9) {
                            budgetInput = input
                        }
                    },
                    label = { Text(stringResource(R.string.monthly_limit_label, currencySymbol)) },
                    placeholder = { Text("e.g. 10000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            val amount = budgetInput.toDoubleOrNull() ?: 0.0
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onConfirm(amount)
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentBudget > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onConfirm(0.0)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(R.string.action_remove_limit),
                            style = Typography.labelMedium.copy(fontFamily = Lato),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetInput.toDoubleOrNull() ?: 0.0
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    onConfirm(amount)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    style = Typography.labelLarge.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = Typography.labelLarge.copy(fontFamily = Lato),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
