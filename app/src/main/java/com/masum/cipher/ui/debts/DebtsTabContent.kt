package com.masum.cipher.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
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
import com.masum.cipher.ui.components.FitText
import androidx.compose.ui.draw.drawBehind
import com.masum.cipher.core.domain.model.DebtItem
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.FormEyebrow
import com.masum.cipher.ui.components.FormHairline
import com.masum.cipher.ui.components.FormMonogram
import com.masum.cipher.ui.components.FormSelectableChip
import com.masum.cipher.ui.theme.DMSans
import androidx.compose.ui.text.TextStyle
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

    val amber = Color(0xFFF59E0B)
    val netColor = if (state.netBalance >= 0) EmeraldIncome else RoseExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormEyebrow(text = stringResource(R.string.debts_net_position))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onAddDebtClick()
                            }
                            .padding(start = 10.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Plus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_add_debt),
                            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val netPrefix = if (state.netBalance > 0) "+" else ""
                FitText(
                    text = "$netPrefix${AppFormatters.formatCurrency(state.netBalance, state.currencySymbol, locale)}",
                    style = TextStyle(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 40.sp,
                        letterSpacing = (-1.2).sp
                    ),
                    color = netColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                val lentWeight = state.totalLent.toFloat().coerceAtLeast(0f)
                val borrowedWeight = state.totalBorrowed.toFloat().coerceAtLeast(0f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (lentWeight > 0f) {
                        Box(modifier = Modifier.weight(lentWeight).height(4.dp).background(EmeraldIncome))
                    }
                    if (borrowedWeight > 0f) {
                        Box(modifier = Modifier.weight(borrowedWeight).height(4.dp).background(RoseExpense))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(EmeraldIncome))
                            Text(
                                text = stringResource(R.string.debts_lent_total),
                                style = TextStyle(fontFamily = Lato, fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = AppFormatters.formatCurrency(state.totalLent, state.currencySymbol, locale),
                            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.debts_borrowed_total),
                                style = TextStyle(fontFamily = Lato, fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(Modifier.size(6.dp).clip(CircleShape).background(RoseExpense))
                        }
                        Text(
                            text = AppFormatters.formatCurrency(state.totalBorrowed, state.currencySymbol, locale),
                            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val neutral = MaterialTheme.colorScheme.onSurface
                listOf(
                    DebtFilterTab.ACTIVE to "${stringResource(R.string.debt_active_badge)} $activeCount",
                    DebtFilterTab.SETTLED to "${stringResource(R.string.debt_settled_badge)} $settledCount",
                    DebtFilterTab.ALL to "${stringResource(R.string.all_records)} $allCount"
                ).forEach { (tab, label) ->
                    FormSelectableChip(
                        label = label,
                        selected = state.filterTab == tab,
                        accent = neutral,
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onIntent(DebtsContract.Intent.SetFilterTab(tab))
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(0.5.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                )
                listOf(
                    null to "All Types",
                    DebtType.LENT to stringResource(R.string.debt_type_lent_short),
                    DebtType.BORROWED to stringResource(R.string.debt_type_borrowed_short)
                ).forEach { (type, label) ->
                    FormSelectableChip(
                        label = label,
                        selected = state.typeFilter == type,
                        accent = when (type) {
                            DebtType.LENT -> EmeraldIncome
                            DebtType.BORROWED -> RoseExpense
                            null -> neutral
                        },
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onIntent(DebtsContract.Intent.SetTypeFilter(type))
                        }
                    )
                }
            }
        }

        if (filteredDebts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Users,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.debts_empty_title),
                        style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.debts_empty_desc),
                        style = TextStyle(fontFamily = Lato, fontSize = 13.sp, lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(
                items = filteredDebts,
                key = { it.id },
                contentType = { "debt_item" }
            ) { debtItem ->
                val typeColor = if (debtItem.type == DebtType.LENT) EmeraldIncome else RoseExpense
                val typeLabel = if (debtItem.type == DebtType.LENT) stringResource(R.string.debt_type_lent_short) else stringResource(R.string.debt_type_borrowed_short)
                val isOverdue = debtItem.dueDate != null && !debtItem.isSettled && debtItem.dueDate!! < System.currentTimeMillis()
                val daysUntilDue = debtItem.dueDate?.let { ((it - System.currentTimeMillis()) / 86_400_000L).toInt() }

                Column(modifier = Modifier.animateItem()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                selectedDebtForDetails = debtItem
                            }
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormMonogram(
                                letter = debtItem.personName.firstOrNull()?.uppercase() ?: "?",
                                accent = typeColor,
                                fallback = LucideIcons.Users,
                                size = 42
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = debtItem.personName,
                                    style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val sub = when {
                                    isOverdue -> stringResource(R.string.debt_overdue)
                                    daysUntilDue != null && daysUntilDue in 0..7 && !debtItem.isSettled ->
                                        if (daysUntilDue == 0) "Due today" else stringResource(R.string.debt_due_in_days, daysUntilDue)
                                    else -> debtItem.accountName?.let { "$typeLabel · $it" } ?: typeLabel
                                }
                                Text(
                                    text = sub,
                                    style = TextStyle(fontFamily = Lato, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                                    color = when {
                                        isOverdue -> RoseExpense
                                        daysUntilDue != null && daysUntilDue in 0..7 && !debtItem.isSettled -> amber
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = AppFormatters.formatCurrency(if (debtItem.isSettled) debtItem.totalAmount else debtItem.remainingAmount, state.currencySymbol, locale),
                                    style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = (-0.3).sp),
                                    color = if (debtItem.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else typeColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (debtItem.isSettled) stringResource(R.string.debt_settled_badge) else stringResource(R.string.debt_remaining_label, ""),
                                    style = TextStyle(fontFamily = Lato, fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (!debtItem.isSettled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(debtItem.progress.coerceIn(0f, 1f))
                                            .height(3.dp)
                                            .background(typeColor)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.action_record_repayment),
                                    style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(50))
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedDebtForRepayment = debtItem
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    FormHairline(modifier = Modifier.padding(start = 54.dp))
                }
            }
            item(key = "add_debt_tile") {
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                Spacer(Modifier.height(18.dp))
                Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .drawBehind {
                                drawRoundRect(
                                    color = outlineColor,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
                                    )
                                )
                            }
                            .clickable { view.performVibrate(isHapticsEnabled, isLongPress = false); onAddDebtClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Plus,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.action_add_debt),
                                style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            }
        }
    }
}
