package com.masum.cipher.ui.accounts

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.ProFeatureGateSheet
import com.masum.cipher.ui.components.ProFeaturePerk
import com.masum.cipher.ui.components.TransferFundsSheet
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowLeftRight
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Wallet
import java.util.Locale

@Composable
fun AccountsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPro: () -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    onNavigateToEditAccount: (Long) -> Unit,
    onNavigateToAccountDetails: (Long) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val locale = Locale.getDefault()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountsContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AccountsContract.Effect.NavigateToPro -> {
                    onNavigateToPro()
                }
            }
        }
    }

    var isActionsExpanded by remember { mutableStateOf(false) }

    val activeIds = remember(state.accounts, state.isPro) {
        if (state.isPro || state.accounts.size <= state.freeAccountLimit) {
            state.accounts.map { it.id }.toSet()
        } else {
            val defaultAcc = state.accounts.firstOrNull { it.isDefault } ?: state.accounts.first()
            val secondAcc = state.accounts.firstOrNull { it.id != defaultAcc.id }
            listOfNotNull(defaultAcc.id, secondAcc?.id).toSet()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                            onNavigateBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.accounts_title),
                        style = Typography.titleMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = if (state.isPro) "${stringResource(R.string.pro_title)} · ${stringResource(R.string.pro_badge_lifetime)}" else stringResource(R.string.accounts_header_free_count, activeIds.size, state.freeAccountLimit),
                        style = Typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = if (state.isPro) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isActionsExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(expandFrom = Alignment.End),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally(shrinkTowards = Alignment.End)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable {
                                    view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                    isActionsExpanded = false
                                    if (state.accounts.size < 2) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.transfer_error_need_min_accounts),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.handleIntent(AccountsContract.Intent.OpenTransferSheet)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ArrowLeftRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.transfer_btn_label),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .clickable {
                                    view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                    isActionsExpanded = false
                                    if (!state.isPro && state.accounts.size >= state.freeAccountLimit) {
                                        viewModel.handleIntent(AccountsContract.Intent.OpenCreateAccountSheet)
                                    } else {
                                        onNavigateToCreateAccount()
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Plus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = stringResource(R.string.insights_net_worth_add_account),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .clickable {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                            isActionsExpanded = !isActionsExpanded
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActionsExpanded) LucideIcons.ChevronRight else LucideIcons.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.accounts_combined_net_worth),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.accounts_active_count, state.accounts.size),
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = AppFormatters.formatCurrency(state.totalNetWorth, state.currencySymbol, locale, decimals = 2),
                                    style = Typography.headlineMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.accounts_liquid_cash),
                                            style = Typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = AppFormatters.formatCurrency(state.totalLiquidBalance, state.currencySymbol, locale, decimals = 2),
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = DMSans,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (state.totalLiquidBalance >= 0) EmeraldIncome else RoseExpense
                                        )
                                    }

                                    if (state.totalDebt > 0) {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.accounts_total_debt),
                                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "-${AppFormatters.formatCurrency(state.totalDebt, state.currencySymbol, locale, decimals = 2)}",
                                                style = Typography.bodyMedium.copy(
                                                    fontFamily = DMSans,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = RoseExpense
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!state.isPro && state.accounts.size >= state.freeAccountLimit) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFF59E0B).copy(alpha = 0.12f),
                                                Color(0xFFEC4899).copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                    .clickable {
                                        view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                        onNavigateToPro()
                                    }
                                    .padding(14.dp)
                            ) {
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
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.Crown,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = stringResource(R.string.accounts_free_limit_reached, 2, 2),
                                                style = Typography.labelMedium.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = stringResource(R.string.accounts_free_limit_desc),
                                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = LucideIcons.Sparkles,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.accounts_section_header),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    items(state.accounts, key = { it.id }) { item ->
                        val isFrozen = !state.isPro && item.id !in activeIds
                        LuxuryAccountCard(
                            name = item.name,
                            type = item.type,
                            balance = item.currentBalance,
                            colorHex = item.colorHex,
                            iconName = item.iconName,
                            isDefault = item.isDefault,
                            last4 = item.accountNumberLast4,
                            currencySymbol = state.currencySymbol,
                            locale = locale,
                            transactionCount = item.transactionCount,
                            isFrozen = isFrozen,
                            isHapticsEnabled = state.isHapticsEnabled,
                            onCardClick = if (isFrozen) null else {
                                {
                                    view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                    onNavigateToAccountDetails(item.id)
                                }
                            },
                            onEditClick = if (isFrozen) null else {
                                {
                                    view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                    onNavigateToEditAccount(item.id)
                                }
                            },
                            onSetDefaultClick = {
                                viewModel.handleIntent(AccountsContract.Intent.SetDefaultAccount(item.id))
                            },
                            onDeleteClick = if (isFrozen) null else {
                                {
                                    viewModel.handleIntent(AccountsContract.Intent.RequestDeleteAccount(item.entity))
                                }
                            },
                            onUpgradeProClick = {
                                onNavigateToPro()
                            }
                        )
                    }
                }
            }

    if (state.showTransferSheet) {
        val outflowTpl = stringResource(R.string.transfer_out_merchant)
        val inflowTpl = stringResource(R.string.transfer_in_merchant)
        val transferSuccessTpl = stringResource(R.string.transfer_success_msg)
        TransferFundsSheet(
            accounts = state.accounts,
            currencySymbol = state.currencySymbol,
            locale = locale,
            isHapticsEnabled = state.isHapticsEnabled,
            onDismiss = {
                viewModel.handleIntent(AccountsContract.Intent.DismissTransferSheet)
            },
            onConfirmTransfer = { fromAcc, toAcc, amount, note ->
                val outflowMerchant = String.format(locale, outflowTpl, toAcc.name)
                val inflowMerchant = String.format(locale, inflowTpl, fromAcc.name)
                viewModel.handleIntent(
                    AccountsContract.Intent.TransferFunds(
                        fromAccount = fromAcc,
                        toAccount = toAcc,
                        amount = amount,
                        note = note,
                        outflowMerchantText = outflowMerchant,
                        inflowMerchantText = inflowMerchant
                    )
                )
                val formattedAmount = AppFormatters.formatCurrency(amount, state.currencySymbol, locale, decimals = 2)
                val toastMsg = String.format(locale, transferSuccessTpl, formattedAmount, fromAcc.name, toAcc.name)
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (state.showProGateSheet) {
        ProFeatureGateSheet(
            featureTitle = stringResource(R.string.pro_perk_unlimited_accounts_title),
            featureTagline = stringResource(R.string.pro_perk_unlimited_accounts_desc),
            featureIcon = LucideIcons.Wallet,
            perks = listOf(
                ProFeaturePerk(stringResource(R.string.pro_perk_unlimited_accounts_title), stringResource(R.string.pro_perk_unlimited_accounts_desc)),
                ProFeaturePerk(stringResource(R.string.pro_perk_net_worth_title), stringResource(R.string.pro_perk_net_worth_desc)),
                ProFeaturePerk(stringResource(R.string.pro_perk_smart_rules_title), stringResource(R.string.pro_perk_smart_rules_desc)),
                ProFeaturePerk(stringResource(R.string.pro_perk_card_themes_title), stringResource(R.string.pro_perk_card_themes_desc))
            ),
            isHapticsEnabled = state.isHapticsEnabled,
            primaryButtonText = stringResource(R.string.pro_btn_upgrade),
            onNavigateToPro = {
                viewModel.handleIntent(AccountsContract.Intent.DismissProGate)
                onNavigateToPro()
            },
            onDismiss = {
                viewModel.handleIntent(AccountsContract.Intent.DismissProGate)
            }
        )
    }

    if (state.showDeleteConfirmDialog && state.accountToDelete != null) {
        val target = state.accountToDelete!!
        AlertDialog(
            onDismissRequest = {
                viewModel.handleIntent(AccountsContract.Intent.DismissDeleteDialog)
            },
            title = {
                Text(
                    text = stringResource(R.string.accounts_delete_confirm_title),
                    style = Typography.titleMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.accounts_delete_confirm_desc, target.name),
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        viewModel.handleIntent(AccountsContract.Intent.ConfirmDeleteAccount)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RoseExpense)
                ) {
                    Text(stringResource(R.string.action_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.handleIntent(AccountsContract.Intent.DismissDeleteDialog)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
