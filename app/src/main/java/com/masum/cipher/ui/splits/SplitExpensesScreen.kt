package com.masum.cipher.ui.splits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.entity.TransactionSplitEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.SplitCalculator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.TransactionSplitSheet
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.dashboard.DashboardViewModel
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Share2
import compose.icons.lucideicons.Users
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SplitFilterTab {
    ALL, PENDING, SETTLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpensesScreen(
    viewModel: DashboardViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val view = LocalView.current
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false

    var selectedTab by remember { mutableStateOf(SplitFilterTab.ALL) }
    var editingSplitTx by remember { mutableStateOf<TransactionEntity?>(null) }

    val splitTransactions = remember(state.transactions, state.splitsByTransactionId) {
        state.transactions.filter { tx ->
            val splits = state.splitsByTransactionId[tx.id] ?: emptyList()
            splits.size > 1
        }
    }

    val totalSharedAmount = remember(splitTransactions, state.splitsByTransactionId) {
        splitTransactions.sumOf { tx ->
            val splits = state.splitsByTransactionId[tx.id] ?: emptyList()
            splits.filter { !it.isCurrentUser }.sumOf { it.amount }
        }
    }

    val totalSettledAmount = remember(splitTransactions, state.splitsByTransactionId) {
        splitTransactions.sumOf { tx ->
            val splits = state.splitsByTransactionId[tx.id] ?: emptyList()
            splits.filter { !it.isCurrentUser && it.isPaid }.sumOf { it.amount }
        }
    }

    val totalPendingAmount = (totalSharedAmount - totalSettledAmount).coerceAtLeast(0.0)

    val displayedTransactions = remember(splitTransactions, state.splitsByTransactionId, selectedTab) {
        splitTransactions.filter { tx ->
            val splits = state.splitsByTransactionId[tx.id] ?: emptyList()
            val pendingSplits = splits.filter { !it.isCurrentUser && !it.isPaid }
            when (selectedTab) {
                SplitFilterTab.ALL -> true
                SplitFilterTab.PENDING -> pendingSplits.isNotEmpty()
                SplitFilterTab.SETTLED -> pendingSplits.isEmpty()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.split_hub_title),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                VaultCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentPadding = 18.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.split_hub_total_lent).uppercase(),
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
                                    text = if (privacyMode) "•••" else AppFormatters.formatCurrency(totalSharedAmount, state.currencySymbol, locale, decimals = 0),
                                    style = Typography.headlineLarge.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.8).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Users,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.split_hub_total_settled),
                                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (privacyMode) "•••" else AppFormatters.formatCurrency(totalSettledAmount, state.currencySymbol, locale, decimals = 0),
                                    style = Typography.bodyMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                    color = EmeraldIncome
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(White10)
                            )

                            Column {
                                Text(
                                    text = stringResource(R.string.split_hub_total_pending),
                                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (privacyMode) "•••" else AppFormatters.formatCurrency(totalPendingAmount, state.currencySymbol, locale, decimals = 0),
                                    style = Typography.bodyMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                    color = if (totalPendingAmount > 0) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                val tabs = listOf(
                    SplitFilterTab.ALL to stringResource(R.string.split_hub_filter_all),
                    SplitFilterTab.PENDING to stringResource(R.string.split_hub_filter_pending),
                    SplitFilterTab.SETTLED to stringResource(R.string.split_hub_filter_settled)
                )
                val selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp)
                ) {
                    val tabWidth = maxWidth / tabs.size
                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * selectedTabIndex,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                        label = "split_tab_offset"
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
                        tabs.forEachIndexed { index, (tab, title) ->
                            val isSelected = selectedTab == tab
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
                                        selectedTab = tab
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
            }

            if (displayedTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Users,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.split_hub_empty_title),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.split_hub_empty_desc),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(displayedTransactions, key = { it.id }) { transaction ->
                    val splits = state.splitsByTransactionId[transaction.id] ?: emptyList()
                    SplitTransactionCard(
                        transaction = transaction,
                        splits = splits,
                        currencySymbol = state.currencySymbol,
                        privacyMode = privacyMode,
                        locale = locale,
                        isHapticsEnabled = isHapticsEnabled,
                        onTogglePaid = { splitId, isPaid ->
                            view.performVibrate(isHapticsEnabled)
                            viewModel.handleIntent(DashboardContract.Intent.UpdateSplitPaidStatus(splitId, isPaid))
                        },
                        onShare = {
                            view.performVibrate(isHapticsEnabled)
                            val mapped = splits.map {
                                SplitParticipant(
                                    id = it.id.toString(),
                                    name = it.name,
                                    amount = it.amount,
                                    percentage = if (transaction.amount > 0) (it.amount / transaction.amount) * 100.0 else 0.0,
                                    isPaid = it.isPaid,
                                    isCurrentUser = it.isCurrentUser
                                )
                            }
                            val message = SplitCalculator.formatShareBreakdownMessage(
                                expenseName = transaction.merchant,
                                totalAmount = transaction.amount,
                                currencySymbol = state.currencySymbol,
                                participants = mapped
                            )
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, message)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Split Breakdown"))
                        },
                        onEdit = {
                            view.performVibrate(isHapticsEnabled)
                            editingSplitTx = transaction
                        }
                    )
                }
            }
        }
    }

    editingSplitTx?.let { transaction ->
        val splitsForTx = state.splitsByTransactionId[transaction.id] ?: emptyList()
        val mappedParticipants = splitsForTx.map {
            SplitParticipant(
                id = it.id.toString(),
                name = it.name,
                amount = it.amount,
                percentage = if (transaction.amount > 0) (it.amount / transaction.amount) * 100.0 else 0.0,
                isPaid = it.isPaid,
                isCurrentUser = it.isCurrentUser
            )
        }
        TransactionSplitSheet(
            expenseName = transaction.merchant,
            totalAmount = transaction.amount,
            currencySymbol = state.currencySymbol,
            initialParticipants = mappedParticipants,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = { editingSplitTx = null },
            onSaveSplits = { updatedSplits ->
                viewModel.handleIntent(DashboardContract.Intent.SaveTransactionSplits(transaction.id, updatedSplits))
                editingSplitTx = null
            }
        )
    }
}

@Composable
private fun SplitTransactionCard(
    transaction: TransactionEntity,
    splits: List<TransactionSplitEntity>,
    currencySymbol: String,
    privacyMode: Boolean,
    locale: Locale,
    isHapticsEnabled: Boolean,
    onTogglePaid: (Long, Boolean) -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit
) {
    val category = TransactionCategory.fromString(transaction.category)
    val myShare = splits.find { it.isCurrentUser }?.amount ?: (transaction.amount / splits.size)
    val otherSplits = splits.filter { !it.isCurrentUser }

    VaultCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 16.dp,
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(40.dp)
                            .background(category.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = transaction.merchant,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = SimpleDateFormat("d MMM, HH:mm", locale).format(Date(transaction.timestamp)),
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (privacyMode) "•••" else AppFormatters.formatCurrency(transaction.amount, currencySymbol, locale, decimals = 0),
                        style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stringResource(R.string.split_hub_my_share)}: ${if (privacyMode) "•••" else AppFormatters.formatCurrency(myShare, currencySymbol, locale, decimals = 0)}",
                        style = Typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White10)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                otherSplits.forEach { participant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (participant.isPaid) EmeraldIncome.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = participant.name.take(1).uppercase(Locale.ROOT),
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (participant.isPaid) EmeraldIncome else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = participant.name,
                                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (participant.isPaid) stringResource(R.string.split_settled) else stringResource(R.string.split_pending),
                                    style = Typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = if (participant.isPaid) EmeraldIncome else RoseExpense
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (privacyMode) "•••" else AppFormatters.formatCurrency(participant.amount, currencySymbol, locale, decimals = 2),
                                style = Typography.bodyMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                         if (participant.isPaid) EmeraldIncome else Color.Transparent
                                     )
                                    .border(
                                         width = if (participant.isPaid) 0.dp else 1.5.dp,
                                         color = if (participant.isPaid) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                         shape = CircleShape
                                     )
                                    .clickable {
                                         onTogglePaid(participant.id, !participant.isPaid)
                                     },
                                contentAlignment = Alignment.Center
                            ) {
                                if (participant.isPaid) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = stringResource(R.string.split_settled),
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White10)
            )

            val pendingCount = otherSplits.count { !it.isPaid }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pendingCount == 0) stringResource(R.string.split_settled) else "$pendingCount ${stringResource(R.string.split_pending)}",
                    style = Typography.labelSmall.copy(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    ),
                    color = if (pendingCount == 0) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { onShare() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Share2,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Share",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
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
