package com.masum.cipher.ui.accounts.details

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.accounts.LuxuryAccountCard
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.components.ProFeatureGateSheet
import com.masum.cipher.ui.components.ProFeaturePerk
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.TransactionSplitSheet
import com.masum.cipher.ui.components.TransferFundsSheet
import com.masum.cipher.ui.dashboard.TransactionItem
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowDownLeft
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowLeftRight
import compose.icons.lucideicons.ArrowUpRight
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Download
import compose.icons.lucideicons.FileSpreadsheet
import compose.icons.lucideicons.FileText
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.SlidersHorizontal
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.X
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditAccount: (Long) -> Unit,
    onNavigateToPro: () -> Unit = {},
    viewModel: AccountDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val locale = Locale.getDefault()

    var activeSplittingTx by remember { mutableStateOf<Pair<TransactionEntity, List<SplitParticipant>>?>(null) }

    val accountNameSafe = (state.account?.name ?: "Account").replace(" ", "_")

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.handleIntent(AccountDetailsContract.Intent.ExportCsv(it)) }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.handleIntent(AccountDetailsContract.Intent.ExportPdf(it)) }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountDetailsContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AccountDetailsContract.Effect.NavigateToPro -> {
                    onNavigateToPro()
                }
            }
        }
    }

    val account = state.account
    val accountType = account?.let { AccountType.fromKey(it.type) } ?: AccountType.BANK

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
                    modifier = Modifier.weight(1f, fill = false),
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
                            text = account?.name ?: stringResource(R.string.account_details_statement),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.account_details_ledger_subtitle, stringResource(accountType.labelRes)),
                            style = Typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0xFF6366F1).copy(alpha = 0.25f),
                                ambientColor = Color.Black.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                viewModel.handleIntent(AccountDetailsContract.Intent.OpenExportSheet)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Download,
                            contentDescription = stringResource(R.string.account_export_statement_title),
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                ambientColor = Color.Black.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                if (state.allAccounts.size < 2) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.transfer_error_need_min_accounts),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.handleIntent(AccountDetailsContract.Intent.OpenTransferSheet)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeftRight,
                            contentDescription = stringResource(R.string.transfer_btn_label),
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }
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
                if (account != null) {
                    LuxuryAccountCard(
                        name = account.name,
                        type = accountType,
                        balance = state.currentBalance,
                        colorHex = account.colorHex,
                        iconName = account.iconName,
                        isDefault = account.isDefault,
                        last4 = account.accountNumberLast4,
                        currencySymbol = state.currencySymbol,
                        locale = locale,
                        transactionCount = state.allTransactions.size,
                        isHapticsEnabled = state.isHapticsEnabled,
                        onEditClick = {
                            view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                            onNavigateToEditAccount(account.id)
                        }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = stringResource(R.string.account_details_flow_overview),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
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
                                        text = stringResource(R.string.account_details_inflow),
                                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = AppFormatters.formatCurrency(state.totalInflow, state.currencySymbol, locale, decimals = 2),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = EmeraldIncome
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
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
                                        text = stringResource(R.string.account_details_outflow),
                                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = AppFormatters.formatCurrency(state.totalOutflow, state.currencySymbol, locale, decimals = 2),
                                    style = Typography.titleMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = RoseExpense
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.account_details_net_flow),
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (state.netFlow >= 0) {
                                        "+${AppFormatters.formatCurrency(state.netFlow, state.currencySymbol, locale, decimals = 2)}"
                                    } else {
                                        AppFormatters.formatCurrency(state.netFlow, state.currencySymbol, locale, decimals = 2)
                                    },
                                    style = Typography.titleMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (state.netFlow >= 0) EmeraldIncome else RoseExpense
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.handleIntent(AccountDetailsContract.Intent.UpdateSearchQuery(it)) },
                        textStyle = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.account_details_search_hint),
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                viewModel.handleIntent(AccountDetailsContract.Intent.UpdateSearchQuery(""))
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountTransactionFilter.entries.forEach { filter ->
                        val isSelected = state.selectedFilter == filter
                        val count = when (filter) {
                            AccountTransactionFilter.ALL -> state.allTransactions.size
                            AccountTransactionFilter.EXPENSE -> state.allTransactions.count { !it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
                            AccountTransactionFilter.INCOME -> state.allTransactions.count { it.isIncome && !it.category.equals("TRANSFER", ignoreCase = true) }
                            AccountTransactionFilter.TRANSFER -> state.allTransactions.count { it.category.equals("TRANSFER", ignoreCase = true) }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                    viewModel.handleIntent(AccountDetailsContract.Intent.SelectFilter(filter))
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(filter.labelRes),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.25f)
                                             else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.FileText,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = if (state.searchQuery.isNotBlank()) stringResource(R.string.account_details_empty_search_title) else stringResource(R.string.account_details_empty_title),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.searchQuery.isNotBlank()) stringResource(R.string.account_details_empty_search_desc) else stringResource(R.string.account_details_empty_desc),
                                style = Typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                state.groupedDays.forEach { dayGroup ->
                    item(key = "header_${dayGroup.title}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayGroup.title.uppercase(),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (dayGroup.netTotal >= 0) "+${AppFormatters.formatCurrency(dayGroup.netTotal, state.currencySymbol, locale, decimals = 2)}"
                                else AppFormatters.formatCurrency(dayGroup.netTotal, state.currencySymbol, locale, decimals = 2),
                                style = Typography.labelSmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (dayGroup.netTotal >= 0) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(dayGroup.transactions, key = { it.id }) { tx ->
                        val txSplits = state.splits.filter { it.transactionId == tx.id }
                        TransactionItem(
                            transaction = tx,
                            customCategories = state.customCategories,
                            privacyMode = state.isPrivacyMode,
                            currencySymbol = state.currencySymbol,
                            splits = txSplits,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                viewModel.handleIntent(AccountDetailsContract.Intent.OpenTransactionDetails(tx))
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (state.transactionToEdit != null) {
        val tx = state.transactionToEdit!!
        TransactionDetailsSheet(
            transaction = tx,
            accounts = state.allAccounts,
            currencySymbol = state.currencySymbol,
            customCategories = state.customCategories,
            onDismiss = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissTransactionDetails)
            },
            onConfirm = { updatedTx ->
                viewModel.handleIntent(AccountDetailsContract.Intent.SaveTransaction(updatedTx))
            },
            onConfirmWithSplits = { updatedTx, splits ->
                viewModel.handleIntent(AccountDetailsContract.Intent.SaveTransaction(updatedTx, splits))
            },
            onDelete = {
                viewModel.handleIntent(AccountDetailsContract.Intent.RequestDeleteTransaction(tx))
            },
            onOpenSplitSheet = { draftTx, splits ->
                activeSplittingTx = Pair(draftTx, splits)
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissTransactionDetails)
            }
        )
    }

    if (activeSplittingTx != null) {
        val (draftTx, currentSplits) = activeSplittingTx!!
        TransactionSplitSheet(
            expenseName = draftTx.merchant,
            totalAmount = draftTx.amount,
            currencySymbol = state.currencySymbol,
            initialParticipants = currentSplits,
            isHapticsEnabled = state.isHapticsEnabled,
            onDismiss = {
                activeSplittingTx = null
            },
            onSaveSplits = { finalizedSplits ->
                viewModel.handleIntent(AccountDetailsContract.Intent.SaveTransaction(draftTx, finalizedSplits))
                activeSplittingTx = null
            }
        )
    }

    if (state.showTransferSheet) {
        val outflowTpl = stringResource(R.string.transfer_out_merchant)
        val inflowTpl = stringResource(R.string.transfer_in_merchant)
        TransferFundsSheet(
            accounts = state.allAccounts,
            currencySymbol = state.currencySymbol,
            locale = locale,
            isHapticsEnabled = state.isHapticsEnabled,
            initialSourceAccountId = accountId,
            onDismiss = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissTransferSheet)
            },
            onConfirmTransfer = { fromAcc, toAcc, amount, note ->
                val outflowMerchant = String.format(locale, outflowTpl, toAcc.name)
                val inflowMerchant = String.format(locale, inflowTpl, fromAcc.name)
                viewModel.handleIntent(
                    AccountDetailsContract.Intent.TransferFunds(
                        fromAccount = fromAcc,
                        toAccount = toAcc,
                        amount = amount,
                        note = note,
                        outflowMerchantText = outflowMerchant,
                        inflowMerchantText = inflowMerchant
                    )
                )
            }
        )
    }

    if (state.showExportSheet) {
        AccountStatementExportSheet(
            accountName = state.account?.name ?: "Account",
            isPro = state.isPro,
            isExportingCsv = state.isExportingCsv,
            isExportingPdf = state.isExportingPdf,
            onExportCsvClick = {
                csvExportLauncher.launch("Cipher_${accountNameSafe}_Report_${System.currentTimeMillis()}.csv")
            },
            onExportPdfClick = {
                if (state.isPro) {
                    pdfExportLauncher.launch("Cipher_${accountNameSafe}_Statement_${System.currentTimeMillis()}.pdf")
                } else {
                    viewModel.handleIntent(AccountDetailsContract.Intent.OpenProGate)
                }
            },
            onDismiss = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissExportSheet)
            },
            isHapticsEnabled = state.isHapticsEnabled
        )
    }

    if (state.showProGateSheet) {
        ProFeatureGateSheet(
            featureTitle = stringResource(R.string.export_pdf_title),
            featureTagline = stringResource(R.string.account_export_pdf_desc),
            featureIcon = LucideIcons.FileText,
            perks = listOf(
                ProFeaturePerk(stringResource(R.string.pro_perk_export_title), stringResource(R.string.pro_perk_export_desc)),
                ProFeaturePerk(stringResource(R.string.pro_perk_unlimited_accounts_title), stringResource(R.string.pro_perk_unlimited_accounts_desc)),
                ProFeaturePerk(stringResource(R.string.pro_perk_net_worth_title), stringResource(R.string.pro_perk_net_worth_desc))
            ),
            isHapticsEnabled = state.isHapticsEnabled,
            primaryButtonText = stringResource(R.string.pro_btn_upgrade),
            onNavigateToPro = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissProGate)
                onNavigateToPro()
            },
            onDismiss = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissProGate)
            }
        )
    }

    if (state.showDeleteDialog && state.transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.handleIntent(AccountDetailsContract.Intent.DismissDeleteDialog)
            },
            title = {
                Text(stringResource(R.string.account_details_delete_tx_title), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Text(stringResource(R.string.account_details_delete_tx_desc), style = Typography.bodyMedium)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        viewModel.handleIntent(AccountDetailsContract.Intent.ConfirmDeleteTransaction)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RoseExpense)
                ) {
                    Text(stringResource(R.string.action_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.handleIntent(AccountDetailsContract.Intent.DismissDeleteDialog)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountStatementExportSheet(
    accountName: String,
    isPro: Boolean,
    isExportingCsv: Boolean,
    isExportingPdf: Boolean,
    onExportCsvClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onDismiss: () -> Unit,
    isHapticsEnabled: Boolean
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            )
        },
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.account_export_statement_title),
                        style = Typography.titleLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.account_export_statement_desc),
                        style = Typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable(enabled = !isExportingCsv) {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onExportCsvClick()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.FileSpreadsheet,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_export_csv),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FREE",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF10B981)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.account_export_csv_desc),
                            style = Typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Icon(
                        imageVector = LucideIcons.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF6366F1).copy(alpha = 0.12f),
                                Color(0xFFA855F7).copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                    .clickable(enabled = !isExportingPdf) {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onExportPdfClick()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.20f))
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.FileText,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_export_pdf),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF6366F1).copy(alpha = 0.20f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF6366F1)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.account_export_pdf_desc),
                            style = Typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Icon(
                        imageVector = if (isPro) LucideIcons.Download else LucideIcons.Sparkles,
                        contentDescription = null,
                        tint = if (isPro) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
