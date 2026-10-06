package com.masum.cipher.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.DebtItem
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowDownLeft
import compose.icons.lucideicons.ArrowUpRight
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Users

@Composable
fun DebtsTabContent(
    state: DebtsContract.State,
    onIntent: (DebtsContract.Intent) -> Unit,
    onAddDebtClick: () -> Unit,
    onEditDebtClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val locale = LocalLocale.current.platformLocale
    val isHapticsEnabled = state.isHapticsEnabled

    var selectedDebtForDetails by remember { mutableStateOf<DebtItem?>(null) }
    var selectedDebtForRepayment by remember { mutableStateOf<DebtItem?>(null) }

    val activeCount = remember(state.debts) { state.debts.count { !it.isSettled } }
    val settledCount = remember(state.debts) { state.debts.count { it.isSettled } }
    val allCount = state.debts.size

    val filteredDebts = remember(state.debts, state.filterTab, state.typeFilter) {
        state.debts.filter { item ->
            val matchesTab = when (state.filterTab) {
                DebtFilterTab.ACTIVE -> !item.isSettled
                DebtFilterTab.SETTLED -> item.isSettled
                DebtFilterTab.ALL -> true
            }
            val matchesType = when (state.typeFilter) {
                null -> true
                DebtType.LENT -> item.type == DebtType.LENT
                DebtType.BORROWED -> item.type == DebtType.BORROWED
            }
            matchesTab && matchesType
        }
    }

    if (selectedDebtForDetails != null) {
        val currentItem = state.debts.find { it.id == selectedDebtForDetails!!.id } ?: selectedDebtForDetails!!
        DebtDetailsSheet(
            debtItem = currentItem,
            accounts = state.accounts,
            currencySymbol = state.currencySymbol,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = { selectedDebtForDetails = null },
            onRecordRepaymentClick = {
                selectedDebtForRepayment = currentItem
            },
            onSettleDebtClick = {
                onIntent(DebtsContract.Intent.SettleDebt(currentItem.id))
            },
            onDeleteDebtClick = {
                onIntent(DebtsContract.Intent.DeleteDebt(currentItem.debt))
            },
            onDeleteRepaymentClick = { repayment ->
                onIntent(DebtsContract.Intent.DeleteRepayment(repayment))
            },
            onSyncToLedger = { debtId, accountId ->
                onIntent(DebtsContract.Intent.SyncDebtToLedger(debtId, accountId))
            },
            onUnlogFromLedger = { debtId ->
                onIntent(DebtsContract.Intent.UnlogDebtFromLedger(debtId))
            },
            onEditDebtClick = { debtId ->
                selectedDebtForDetails = null
                onEditDebtClick(debtId)
            }
        )
    }

    if (selectedDebtForRepayment != null) {
        val currentItem = state.debts.find { it.id == selectedDebtForRepayment!!.id } ?: selectedDebtForRepayment!!
        RecordRepaymentSheet(
            debtItem = currentItem,
            accounts = state.accounts,
            currencySymbol = state.currencySymbol,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = { selectedDebtForRepayment = null },
            onConfirm = { amount, timestamp, accountId, note, syncLedger ->
                onIntent(
                    DebtsContract.Intent.RecordRepayment(
                        debtId = currentItem.id,
                        amount = amount,
                        timestamp = timestamp,
                        accountId = accountId,
                        note = note,
                        syncLedger = syncLedger
                    )
                )
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VaultCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                contentPadding = 18.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.debts_net_position),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val netPrefix = if (state.netBalance > 0) "+" else ""
                            Text(
                                text = "$netPrefix${AppFormatters.formatCurrency(state.netBalance, state.currencySymbol, locale)}",
                                style = Typography.headlineMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = if (state.netBalance >= 0) EmeraldIncome else RoseExpense
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onAddDebtClick()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Plus,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = stringResource(R.string.action_add_debt),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldIncome.copy(alpha = 0.1f))
                                .border(1.dp, EmeraldIncome.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ArrowDownLeft,
                                        contentDescription = null,
                                        tint = EmeraldIncome,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.debts_lent_total),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontSize = 11.sp
                                        ),
                                        color = EmeraldIncome
                                    )
                                }
                                Text(
                                    text = AppFormatters.formatCurrency(state.totalLent, state.currencySymbol, locale),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = EmeraldIncome
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RoseExpense.copy(alpha = 0.1f))
                                .border(1.dp, RoseExpense.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ArrowUpRight,
                                        contentDescription = null,
                                        tint = RoseExpense,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.debts_borrowed_total),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontSize = 11.sp
                                        ),
                                        color = RoseExpense
                                    )
                                }
                                Text(
                                    text = AppFormatters.formatCurrency(state.totalBorrowed, state.currencySymbol, locale),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = RoseExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusTabs = listOf(
                    DebtFilterTab.ACTIVE to "${stringResource(R.string.debt_active_badge)} ($activeCount)",
                    DebtFilterTab.SETTLED to "${stringResource(R.string.debt_settled_badge)} ($settledCount)",
                    DebtFilterTab.ALL to "${stringResource(R.string.all_records)} ($allCount)"
                )

                statusTabs.forEach { (tab, label) ->
                    val isSelected = state.filterTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onIntent(DebtsContract.Intent.SetFilterTab(tab))
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                val typeFilters = listOf(
                    null to "All Types",
                    DebtType.LENT to stringResource(R.string.debt_type_lent_short),
                    DebtType.BORROWED to stringResource(R.string.debt_type_borrowed_short)
                )

                typeFilters.forEach { (type, label) ->
                    val isSelected = state.typeFilter == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onIntent(DebtsContract.Intent.SetTypeFilter(type))
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (filteredDebts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Users,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.debts_empty_title),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.debts_empty_desc),
                            style = Typography.bodySmall.copy(
                                fontFamily = Lato,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredDebts, key = { it.id }) { debtItem ->
                val typeColor = if (debtItem.type == DebtType.LENT) EmeraldIncome else RoseExpense
                val typeLabel = if (debtItem.type == DebtType.LENT) stringResource(R.string.debt_type_lent_short) else stringResource(R.string.debt_type_borrowed_short)

                val isOverdue = debtItem.dueDate != null && !debtItem.isSettled && debtItem.dueDate!! < System.currentTimeMillis()
                val daysUntilDue = debtItem.dueDate?.let { ((it - System.currentTimeMillis()) / 86_400_000L).toInt() }

                VaultCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentPadding = 14.dp,
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        selectedDebtForDetails = debtItem
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(typeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = debtItem.personName.firstOrNull()?.uppercase() ?: "?",
                                        style = Typography.titleSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = typeColor
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = debtItem.personName,
                                        style = Typography.titleSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
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
                                            text = typeLabel,
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = typeColor
                                        )
                                        if (debtItem.accountName != null) {
                                            Text(
                                                text = "• ${debtItem.accountName}",
                                                style = Typography.labelSmall.copy(
                                                    fontFamily = Lato,
                                                    fontSize = 11.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = AppFormatters.formatCurrency(if (debtItem.isSettled) debtItem.totalAmount else debtItem.remainingAmount, state.currencySymbol, locale),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (debtItem.isSettled) MaterialTheme.colorScheme.onSurface else typeColor
                                )
                                Text(
                                    text = if (debtItem.isSettled) stringResource(R.string.debt_settled_badge) else stringResource(R.string.debt_remaining_label, ""),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontSize = 10.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(debtItem.progress)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(EmeraldIncome)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isOverdue) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RoseExpense.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.debt_overdue),
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = RoseExpense
                                        )
                                    }
                                } else if (daysUntilDue != null && daysUntilDue in 0..7 && !debtItem.isSettled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (daysUntilDue == 0) "Due today" else stringResource(R.string.debt_due_in_days, daysUntilDue),
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(
                                            R.string.debt_repaid_of_total,
                                            AppFormatters.formatCurrency(debtItem.repaidAmount, state.currencySymbol, locale),
                                            AppFormatters.formatCurrency(debtItem.totalAmount, state.currencySymbol, locale)
                                        ),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!debtItem.isSettled) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            .clickable {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                selectedDebtForRepayment = debtItem
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_record_repayment),
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
