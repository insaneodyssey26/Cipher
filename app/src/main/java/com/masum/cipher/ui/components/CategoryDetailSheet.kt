package com.masum.cipher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.masum.cipher.R
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.X
import java.util.Calendar
import java.util.Date

private data class CategoryMerchantSpend(
    val merchant: String,
    val totalAmount: Double,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailSheet(
    categoryData: DashboardContract.CategoryData,
    categoryBudget: Double,
    transactions: List<TransactionEntity>,
    onSetCategoryBudget: (Double) -> Unit,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale
    var showBudgetDialog by remember { mutableStateOf(false) }

    val categoryEnum = remember(categoryData.category) {
        TransactionCategory.fromString(categoryData.category)
    }
    val categoryColor = categoryEnum.color

    val categoryTransactions = remember(transactions, categoryData.category) {
        transactions.filter {
            !it.isIncome && (
                it.category.equals(categoryData.category, ignoreCase = true) ||
                it.category.equals(categoryEnum.name, ignoreCase = true) ||
                it.category.equals(categoryEnum.displayName, ignoreCase = true)
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
            .groupBy { it.merchant.trim().ifEmpty { "Unknown" } }
            .map { (merchant, txs) ->
                CategoryMerchantSpend(
                    merchant = merchant,
                    totalAmount = txs.sumOf { it.amount },
                    count = txs.size
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
    val percentUsed = if (categoryBudget > 0) ((totalSpent / categoryBudget) * 100).toInt() else 0
    val safeSpendPerDay = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0
    val isOverBudget = categoryBudget > 0 && totalSpent > categoryBudget

    val progress = if (categoryBudget > 0) (totalSpent / categoryBudget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "categoryBudgetProgress"
    )

    if (showBudgetDialog) {
        EditCategoryBudgetDialog(
            categoryName = stringResource(categoryEnum.titleRes),
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler { onDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryEnum.icon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(categoryEnum.titleRes),
                                style = Typography.titleLarge.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.percent_of_expenses, String.format(locale, "%.1f", categoryData.percentage * 100)),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Manrope,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = AppFormatters.formatCurrency(totalSpent, currencySymbol, locale),
                            style = Typography.headlineMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (totalSpent >= 1_000_000_000) 18.sp else 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
                ) {

            item {
                VaultCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        showBudgetDialog = true
                    },
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentPadding = 14.dp
                ) {
                    if (categoryBudget > 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                            .size(7.dp)
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Settings,
                                        contentDescription = "Edit Category Budget",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${stringResource(R.string.limit_suffix)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(categoryBudget, currencySymbol, locale)}",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
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
                                Text(
                                    text = if (isOverBudget) {
                                        "Exceeded by ${com.masum.cipher.core.util.AppFormatters.formatCurrency(totalSpent - categoryBudget, currencySymbol, locale)}"
                                    } else {
                                        "${com.masum.cipher.core.util.AppFormatters.formatCurrency(remainingBudget, currencySymbol, locale)} remaining"
                                    },
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (isOverBudget) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!isOverBudget) {
                                    Text(
                                        text = "${stringResource(R.string.safe_daily_spend)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(safeSpendPerDay, currencySymbol, locale)}/${stringResource(R.string.day_unit)}",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Target,
                                        contentDescription = null,
                                        tint = categoryColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.set_category_budget),
                                        style = Typography.titleSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.set_spending_limit_for, stringResource(categoryEnum.titleRes)),
                                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(categoryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Plus,
                                        contentDescription = null,
                                        tint = categoryColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.set_limit),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = categoryColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VaultCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentPadding = 12.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.avg_spend_header),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.7.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppFormatters.formatCompactCurrency(avgSpend, currencySymbol = currencySymbol),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    VaultCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentPadding = 12.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.count_header),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.7.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$txCount",
                                style = Typography.titleMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    VaultCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentPadding = 12.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.largest_header),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.7.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppFormatters.formatCompactCurrency(maxSpend, currencySymbol = currencySymbol),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (topMerchants.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.top_merchants),
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VaultCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentPadding = 14.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            topMerchants.forEach { merchant ->
                                val merchantPercent = if (totalSpent > 0) (merchant.totalAmount / totalSpent).toFloat() else 0f
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = merchant.merchant,
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = Manrope,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = com.masum.cipher.core.util.AppFormatters.formatCurrency(merchant.totalAmount, currencySymbol, locale),
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = Manrope,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.5.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(merchantPercent.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.5.dp))
                                                .background(categoryColor.copy(alpha = 0.85f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (categoryTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "${stringResource(R.string.transactions_header)} (${categoryTransactions.size})",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(
                    items = categoryTransactions,
                    key = { it.id }
                ) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onTransactionClick(tx)
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.merchant.ifEmpty { stringResource(R.string.expense) },
                                style = Typography.bodyMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${AppFormatters.getDay().format(Date(tx.timestamp))}${if (!tx.note.isNullOrBlank()) " • ${tx.note}" else ""}",
                                style = Typography.bodySmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "-${com.masum.cipher.core.util.AppFormatters.formatCurrency(tx.amount, currencySymbol, locale)}",
                            style = Typography.titleMedium.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                        fontFamily = Manrope,
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
                            style = Typography.labelMedium.copy(fontFamily = Manrope),
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
                        fontFamily = Manrope,
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
                    style = Typography.labelLarge.copy(fontFamily = Manrope),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
