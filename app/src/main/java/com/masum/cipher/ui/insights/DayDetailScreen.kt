package com.masum.cipher.ui.insights

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.StaggeredEntranceItem
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.dashboard.TransactionItem
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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
    val locale = LocalLocale.current.platformLocale
    
    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    
    val date = remember(timestamp) { Date(timestamp) }
    val dayName = remember(date, locale) { SimpleDateFormat("EEEE", locale).format(date) }
    val fullDate = remember(date, locale) { SimpleDateFormat("MMMM dd, yyyy", locale).format(date) }
    
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
                        amount = "${state.currencySymbol}${String.format(locale, "%,.0f", totalSpent)}",
                        color = RoseExpense,
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatCard(
                        label = "INCOME",
                        amount = "${state.currencySymbol}${String.format(locale, "%,.0f", totalIncome)}",
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
                            currencySymbol = state.currencySymbol,
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
            currencySymbol = state.currencySymbol,
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

    state.promptCategoryRuleFor?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(InsightsContract.Intent.DismissCategoryRulePrompt) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text(
                    text = "Save Category Rule?",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Do you want to always categorize future transactions from '${tx.merchant}' as '${tx.category}'?",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleIntent(InsightsContract.Intent.SaveCategoryRule(tx.merchant, tx.category))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yes, always", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.handleIntent(InsightsContract.Intent.DismissCategoryRulePrompt)
                }) {
                    Text("No, just this once", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
    val fontSize = when {
        amount.length > 14 -> 14.sp
        amount.length > 10 -> 17.sp
        amount.length > 7 -> 20.sp
        else -> 24.sp
    }
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
                style = Typography.headlineSmall.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                ),
                color = color,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
