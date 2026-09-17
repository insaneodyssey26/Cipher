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
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = "Back",
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.isPro) "${stringResource(R.string.pro_title)} · ${stringResource(R.string.pro_badge_lifetime)}" else "${state.accounts.size}/${state.freeAccountLimit} Free Accounts",
                            style = Typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = if (state.isPro) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                        if (!state.isPro && state.accounts.size >= state.freeAccountLimit) {
                            viewModel.handleIntent(AccountsContract.Intent.OpenCreateAccountSheet)
                        } else {
                            onNavigateToCreateAccount()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Plus,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.custom_category_add_btn),
                        style = Typography.labelMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
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
                            isHapticsEnabled = state.isHapticsEnabled,
                            onCardClick = {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                onNavigateToAccountDetails(item.id)
                            },
                            onEditClick = {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                onNavigateToEditAccount(item.id)
                            },
                            onSetDefaultClick = {
                                viewModel.handleIntent(AccountsContract.Intent.SetDefaultAccount(item.id))
                            },
                            onDeleteClick = {
                                viewModel.handleIntent(AccountsContract.Intent.RequestDeleteAccount(item.entity))
                            }
                        )
                    }
                }
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
