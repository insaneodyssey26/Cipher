package com.masum.cipher.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.AccountItem
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.domain.model.CategoryItem
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.MathEvaluator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Bell
import compose.icons.lucideicons.Calculator
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.Copy
import compose.icons.lucideicons.Info
import compose.icons.lucideicons.MessageSquareText
import compose.icons.lucideicons.PenLine
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.X
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailsSheet(
    transaction: TransactionEntity,
    accounts: List<AccountItem> = emptyList(),
    customCategories: List<CustomCategoryEntity> = emptyList(),
    currencySymbol: String = "₹",
    existingSplits: List<SplitParticipant> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit,
    onConfirmWithSplits: ((TransactionEntity, List<SplitParticipant>) -> Unit)? = null,
    onSaveSplits: ((List<SplitParticipant>) -> Unit)? = null,
    onOpenSplitSheet: ((TransactionEntity, List<SplitParticipant>) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDraftChange: ((TransactionEntity) -> Unit)? = null,
    onCreateCustomCategory: ((name: String, iconName: String, colorHex: Long) -> Unit)? = null,
    onUpdateCustomCategory: ((id: Long, oldName: String, newName: String, iconName: String, colorHex: Long) -> Unit)? = null,
    onDeleteCustomCategory: ((CustomCategoryEntity) -> Unit)? = null,
    isHapticsEnabled: Boolean = true,
    isPro: Boolean = true
) {
    val isEditing = transaction.id != 0L
    var merchant by remember { mutableStateOf(transaction.merchant) }
    var amount by remember {
        mutableStateOf(
            if (transaction.amount == 0.0) "" else String.format(Locale.US, "%.2f", transaction.amount)
        )
    }
    var isIncome by remember { mutableStateOf(transaction.isIncome) }
    var selectedCategory by remember {
        mutableStateOf(CategoryHelper.resolveCategory(transaction.category, customCategories))
    }
    var categoryPickerVisible by remember { mutableStateOf(false) }
    var showCreateCategorySheet by remember { mutableStateOf(false) }
    var editingCustomCategoryEntity by remember { mutableStateOf<CustomCategoryEntity?>(null) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var isNoteExpanded by remember { mutableStateOf(transaction.note?.isNotBlank() == true) }
    var selectedTimestamp by remember { mutableLongStateOf(transaction.timestamp) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSplitSheet by remember { mutableStateOf(false) }
    var showRawMessageDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var currentSplits by remember { mutableStateOf(existingSplits) }

    val defaultSelectedAccountId = transaction.accountId
        ?: accounts.firstOrNull { it.isDefault }?.id
        ?: accounts.firstOrNull()?.id
    var selectedAccountId by remember(transaction.id, transaction.accountId) {
        mutableStateOf(defaultSelectedAccountId)
    }

    LaunchedEffect(accounts, transaction.accountId) {
        if (selectedAccountId == null) {
            selectedAccountId = transaction.accountId
                ?: accounts.firstOrNull { it.isDefault }?.id
                ?: accounts.firstOrNull()?.id
        }
    }

    LaunchedEffect(merchant, amount, isIncome, selectedCategory, note, selectedTimestamp, selectedAccountId) {
        if (onDraftChange != null) {
            val finalAmount = MathEvaluator.evaluate(amount) ?: 0.0
            val updated = transaction.copy(
                merchant = merchant,
                amount = finalAmount,
                category = selectedCategory.name,
                isIncome = isIncome,
                note = note.ifBlank { null },
                timestamp = selectedTimestamp,
                accountId = selectedAccountId
            )
            if (updated != transaction) {
                onDraftChange.invoke(updated)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val merchantFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val activeThemeColor by animateColorAsState(
        targetValue = if (isIncome) EmeraldIncome else RoseExpense,
        animationSpec = spring(stiffness = 400f),
        label = "activeThemeColor"
    )

    val consumeOverscrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < 0f) {
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y < 0f) {
                    return Velocity(0f, available.y)
                }
                return Velocity.Zero
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 38.dp, height = 4.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .imeNestedScroll()
                .nestedScroll(consumeOverscrollConnection)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.tx_details_title) else stringResource(R.string.tx_new_title),
                    style = Typography.titleLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!transaction.rawSms.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    showRawMessageDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Info,
                                contentDescription = stringResource(R.string.original_message_title),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isEditing && onDelete != null) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RoseExpense.copy(alpha = 0.12f))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                                    onDelete()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Trash2,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = RoseExpense,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(3.dp)
            ) {
                val tabWidth = maxWidth / 2
                val selectedIndex = if (isIncome) 1 else 0
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f),
                    label = "typeToggleOffset"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (isIncome) EmeraldIncome.copy(alpha = 0.2f) else RoseExpense.copy(alpha = 0.2f))
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(17.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isIncome) {
                                    view.performVibrate(isHapticsEnabled)
                                    isIncome = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.expense),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (!isIncome) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(17.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isIncome) {
                                    view.performVibrate(isHapticsEnabled)
                                    isIncome = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.income),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isIncome) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FintechHeroAmount(
                amount = amount,
                currencySymbol = currencySymbol,
                color = activeThemeColor,
                onAmountChange = { if (it.length <= 15) amount = it },
                onDone = { merchantFocusRequester.requestFocus() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            focusManager.clearFocus()
                            categoryPickerVisible = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = selectedCategory.color.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedCategory.color.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = selectedCategory.icon,
                                contentDescription = null,
                                tint = selectedCategory.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedCategory.titleRes?.let { stringResource(it) } ?: selectedCategory.displayName,
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = LucideIcons.ChevronDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        textStyle = Typography.bodyLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = if (isNoteExpanded) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (!isNoteExpanded) {
                                    isNoteExpanded = true
                                }
                                noteFocusRequester.requestFocus()
                            },
                            onDone = { focusManager.clearFocus() }
                        ),
                        cursorBrush = SolidColor(activeThemeColor),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(merchantFocusRequester),
                        decorationBox = { inner ->
                            if (merchant.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.merchant),
                                    style = Typography.bodyLarge.copy(
                                        fontFamily = Lato,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            inner()
                        }
                    )

                    if (merchant.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                merchant = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (accounts.isNotEmpty()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )

                    val selectedAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    focusManager.clearFocus()
                                    showAccountMenu = true
                                }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val accColor = selectedAccount?.colorHex?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(accColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getAccountIconVector(selectedAccount?.iconName ?: "Wallet"),
                                        contentDescription = null,
                                        tint = accColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.transaction_account_label),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = selectedAccount?.let {
                                            "${it.name}${if (!it.accountNumberLast4.isNullOrBlank()) " •••• ${it.accountNumberLast4}" else ""}"
                                        } ?: "Select Account",
                                        style = Typography.bodyMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Icon(
                                imageVector = LucideIcons.ChevronDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = acc.id == selectedAccountId
                                val accColor = Color(acc.colorHex)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(accColor.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getAccountIconVector(acc.iconName),
                                                    contentDescription = null,
                                                    tint = accColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = "${acc.name}${if (!acc.accountNumberLast4.isNullOrBlank()) " •••• ${acc.accountNumberLast4}" else ""}",
                                                style = Typography.bodyMedium.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled)
                                        selectedAccountId = acc.id
                                        showAccountMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val now = Calendar.getInstance()
                    val target = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                    val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
                    val todayStr = stringResource(R.string.today)
                    val locale = LocalLocale.current.platformLocale
                    val dateLabel = if (isToday) {
                        "$todayStr, " + AppFormatters.getDay(locale).format(Date(selectedTimestamp))
                    } else {
                        AppFormatters.getShortDate(locale).format(Date(selectedTimestamp))
                    }

                    ActionPillChip(
                        icon = LucideIcons.Calendar,
                        label = dateLabel,
                        isActive = !isToday,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            focusManager.clearFocus()
                            showDatePicker = true
                        }
                    )

                    ActionPillChip(
                        icon = LucideIcons.PenLine,
                        label = if (note.isBlank()) stringResource(R.string.note) else stringResource(R.string.note) + " •",
                        isActive = isNoteExpanded || note.isNotBlank(),
                        activeColor = activeThemeColor,
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            isNoteExpanded = !isNoteExpanded
                            if (isNoteExpanded) {
                                noteFocusRequester.requestFocus()
                            }
                        }
                    )

                    if (!isIncome) {
                        val hasSplits = currentSplits.size > 1
                        ActionPillChip(
                            icon = LucideIcons.Users,
                            label = if (hasSplits) "${stringResource(R.string.split_title)} (${currentSplits.size})" else stringResource(R.string.split_title),
                            isActive = hasSplits,
                            activeColor = activeThemeColor,
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                focusManager.clearFocus()
                                val evaluatedTotal = MathEvaluator.evaluate(amount) ?: transaction.amount
                                val currentDraft = transaction.copy(
                                    merchant = merchant.trim().ifBlank { "Miscellaneous" },
                                    amount = evaluatedTotal,
                                    category = selectedCategory.name,
                                    isIncome = isIncome,
                                    note = note.ifBlank { null },
                                    timestamp = selectedTimestamp,
                                    accountId = selectedAccountId
                                )
                                if (onOpenSplitSheet != null) {
                                    onOpenSplitSheet(currentDraft, currentSplits)
                                } else {
                                    showSplitSheet = true
                                }
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isNoteExpanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    BasicTextField(
                        value = note,
                        onValueChange = { if (it.length <= 150) note = it },
                        textStyle = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        cursorBrush = SolidColor(activeThemeColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(noteFocusRequester),
                        decorationBox = { inner ->
                            if (note.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.note) + "...",
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            Button(
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val finalAmount = MathEvaluator.evaluate(amount) ?: 0.0
                    if (finalAmount > 0) {
                        val updatedTx = transaction.copy(
                            merchant = merchant.trim().ifBlank { "Miscellaneous" },
                            amount = finalAmount,
                            category = selectedCategory.name,
                            isIncome = isIncome,
                            note = note.ifBlank { null },
                            timestamp = selectedTimestamp,
                            accountId = selectedAccountId
                        )
                        if (onConfirmWithSplits != null) {
                            onConfirmWithSplits(updatedTx, currentSplits)
                        } else {
                            onConfirm(updatedTx)
                            if (currentSplits.isNotEmpty()) {
                                onSaveSplits?.invoke(currentSplits)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.update_transaction) else stringResource(R.string.action_save),
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = pickedUtcMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            }
                            selectedTimestamp = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_select), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (categoryPickerVisible) {
        CategoryPickerSheet(
            selectedCategory = selectedCategory,
            customCategories = customCategories,
            isIncome = isIncome,
            isHapticsEnabled = isHapticsEnabled,
            onCategorySelected = { category ->
                selectedCategory = category
                categoryPickerVisible = false
            },
            onCreateNewCategory = if (onCreateCustomCategory != null) {
                {
                    editingCustomCategoryEntity = null
                    showCreateCategorySheet = true
                }
            } else null,
            onEditCustomCategory = if (onUpdateCustomCategory != null || onDeleteCustomCategory != null) {
                { entity ->
                    editingCustomCategoryEntity = entity
                    showCreateCategorySheet = true
                }
            } else null,
            onDismiss = { categoryPickerVisible = false }
        )
    }

    if (showCreateCategorySheet && (onCreateCustomCategory != null || onUpdateCustomCategory != null)) {
        CreateCustomCategorySheet(
            existingCategory = editingCustomCategoryEntity,
            existingCustomCategories = customCategories,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = {
                showCreateCategorySheet = false
                editingCustomCategoryEntity = null
            },
            onSaveCategory = { name, iconName, colorHex ->
                val editing = editingCustomCategoryEntity
                if (editing != null && onUpdateCustomCategory != null) {
                    onUpdateCustomCategory(editing.id, editing.name, name, iconName, colorHex)
                } else {
                    onCreateCustomCategory?.invoke(name, iconName, colorHex)
                }
                val newCat = CategoryItem(
                    name = name,
                    displayName = name,
                    icon = CategoryIconRegistry.getIcon(iconName),
                    iconName = iconName,
                    color = Color(colorHex.toInt()),
                    colorHex = colorHex,
                    isCustom = true
                )
                selectedCategory = newCat
                categoryPickerVisible = false
                showCreateCategorySheet = false
                editingCustomCategoryEntity = null
            },
            onDeleteCategory = if (onDeleteCustomCategory != null) {
                { entity ->
                    onDeleteCustomCategory(entity)
                    if (selectedCategory.name.equals(entity.name, ignoreCase = true)) {
                        selectedCategory = CategoryHelper.resolveCategory(TransactionCategory.OTHERS.name, customCategories)
                    }
                    categoryPickerVisible = false
                    showCreateCategorySheet = false
                    editingCustomCategoryEntity = null
                }
            } else null
        )
    }

    if (showSplitSheet) {
        val evaluatedTotal = MathEvaluator.evaluate(amount) ?: transaction.amount
        TransactionSplitSheet(
            expenseName = merchant.ifBlank { "Expense" },
            totalAmount = evaluatedTotal,
            currencySymbol = currencySymbol,
            initialParticipants = currentSplits,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = { showSplitSheet = false },
            onDraftChange = { updatedSplits -> currentSplits = updatedSplits },
            onSaveSplits = { updatedSplits ->
                currentSplits = updatedSplits
                onSaveSplits?.invoke(updatedSplits)
            }
        )
    }

    if (showRawMessageDialog && !transaction.rawSms.isNullOrBlank()) {
        OriginalMessageDialog(
            rawMessage = transaction.rawSms,
            isHapticsEnabled = isHapticsEnabled,
            onDismiss = { showRawMessageDialog = false }
        )
    }
}

@Composable
private fun ActionPillChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) activeColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isActive) activeColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = Typography.labelSmall.copy(
                    fontFamily = Lato,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FintechHeroAmount(
    amount: String,
    currencySymbol: String,
    color: Color,
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = amount, selection = TextRange(amount.length)))
    }
    var showNumpad by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale

    LaunchedEffect(amount) {
        if (amount != textFieldValue.text) {
            val safe = textFieldValue.selection.start.coerceIn(0, amount.length)
            textFieldValue = TextFieldValue(text = amount, selection = TextRange(safe))
        }
    }

    val dynamicFontSize = when {
        textFieldValue.text.length > 9 -> 36.sp
        textFieldValue.text.length > 6 -> 44.sp
        else -> 52.sp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currencySymbol,
                style = Typography.displayLarge.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (dynamicFontSize.value * 0.72f).sp,
                    color = color.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(end = 4.dp)
            )

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.all { it.isDigit() || it in "+-*/. " }) {
                        textFieldValue = newValue
                        onAmountChange(newValue.text)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { onDone() }),
                textStyle = Typography.displayLarge.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = dynamicFontSize,
                    color = color,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                cursorBrush = SolidColor(color),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "0",
                                style = Typography.displayLarge.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dynamicFontSize,
                                    color = color.copy(alpha = 0.25f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        inner()
                    }
                }
            )
        }

        val computed = MathEvaluator.evaluate(textFieldValue.text)
        if (computed != null && textFieldValue.text.any { it in "+-*/" }) {
            Text(
                text = "= ${AppFormatters.formatCurrency(computed, currencySymbol, locale, decimals = 2)}",
                style = Typography.labelMedium.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = color.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Surface(
            onClick = {
                view.performVibrate(true)
                showNumpad = !showNumpad
                if (showNumpad) keyboardController?.hide() else keyboardController?.show()
            },
            shape = CircleShape,
            color = if (showNumpad) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Calculator,
                    contentDescription = null,
                    tint = if (showNumpad) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (showNumpad) stringResource(R.string.use_keyboard) else stringResource(R.string.use_calculator),
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (showNumpad) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = showNumpad,
            enter = expandVertically(tween(250)) + fadeIn(),
            exit = shrinkVertically(tween(200)) + fadeOut()
        ) {
            CalculatorNumpad(
                input = textFieldValue.text,
                cursorPosition = textFieldValue.selection.start,
                onInputChange = { newInput, newCursor ->
                    textFieldValue = TextFieldValue(
                        text = newInput,
                        selection = TextRange(newCursor.coerceIn(0, newInput.length))
                    )
                    onAmountChange(newInput)
                },
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

private data class MessageSourceInfo(
    val type: String,
    val sourceName: String,
    val cleanText: String,
    val isAppNotification: Boolean
)

private fun parseMessageSource(rawMessage: String): MessageSourceInfo {
    val bracketMatch = Regex("^\\[([^\\]]+)\\]\\s*(.*)$", RegexOption.DOT_MATCHES_ALL).find(rawMessage)
    if (bracketMatch != null) {
        val appName = bracketMatch.groupValues[1].trim()
        val text = bracketMatch.groupValues[2].trim()
        return MessageSourceInfo(
            type = "Notification",
            sourceName = appName,
            cleanText = text.ifBlank { rawMessage },
            isAppNotification = true
        )
    }

    val lower = rawMessage.lowercase()

    val knownApps = listOf(
        Regex("(?i)\\b(?:google\\s*pay|gpay)\\b") to "Google Pay",
        Regex("(?i)\\bphonepe\\b") to "PhonePe",
        Regex("(?i)\\bpaytm\\b") to "Paytm",
        Regex("(?i)\\bcred\\b") to "CRED",
        Regex("(?i)\\bamazon\\s*pay\\b") to "Amazon Pay",
        Regex("(?i)\\bbhim\\b") to "BHIM",
        Regex("(?i)\\bwhatsapp\\b") to "WhatsApp",
        Regex("(?i)\\bjupiter\\b") to "Jupiter",
        Regex("(?i)\\bfi\\s*money\\b") to "Fi Money",
        Regex("(?i)\\bnavi\\b") to "Navi",
        Regex("(?i)\\bslice\\b") to "Slice",
        Regex("(?i)\\bsuper\\.?money\\b") to "Super.money",
        Regex("(?i)\\bmobikwik\\b") to "MobiKwik",
        Regex("(?i)\\bfreecharge\\b") to "Freecharge",
        Regex("(?i)\\bfampay\\b") to "FamPay"
    )

    for ((regex, app) in knownApps) {
        if (regex.containsMatchIn(rawMessage)) {
            val isSms = lower.contains("spent on your credit card") || lower.contains("not you? call") ||
                    lower.contains("a/c") || lower.contains("debited from") || lower.contains("credited to")
            return MessageSourceInfo(
                type = if (isSms) "SMS" else "Notification",
                sourceName = app,
                cleanText = rawMessage,
                isAppNotification = !isSms
            )
        }
    }

    val knownBanks = listOf(
        Regex("(?i)\\bhdfc(?:\\s*bank)?\\b") to "HDFC Bank",
        Regex("(?i)\\b(?:state\\s*bank\\s*of\\s*india|sbi)\\b") to "State Bank of India",
        Regex("(?i)\\bicici(?:\\s*bank)?\\b") to "ICICI Bank",
        Regex("(?i)\\baxis(?:\\s*bank)?\\b") to "Axis Bank",
        Regex("(?i)\\bkotak(?:\\s*mahindra)?(?:\\s*bank)?\\b") to "Kotak Mahindra Bank",
        Regex("(?i)\\b(?:punjab\\s*national\\s*bank|pnb)\\b") to "PNB",
        Regex("(?i)\\b(?:bank\\s*of\\s*baroda|bob)\\b") to "Bank of Baroda",
        Regex("(?i)\\bcanara(?:\\s*bank)?\\b") to "Canara Bank",
        Regex("(?i)\\bunion\\s*bank\\b") to "Union Bank",
        Regex("(?i)\\bidfc(?:\\s*first)?(?:\\s*bank)?\\b") to "IDFC FIRST Bank",
        Regex("(?i)\\bindusind(?:\\s*bank)?\\b") to "IndusInd Bank",
        Regex("(?i)\\byes\\s*bank\\b") to "Yes Bank",
        Regex("(?i)\\bfederal\\s*bank\\b") to "Federal Bank",
        Regex("(?i)\\brbl(?:\\s*bank)?\\b") to "RBL Bank",
        Regex("(?i)\\bau\\s*(?:small\\s*finance)?\\s*bank\\b") to "AU Small Finance Bank",
        Regex("(?i)\\bstandard\\s*chartered\\b") to "Standard Chartered",
        Regex("(?i)\\bcitibank\\b") to "Citibank"
    )

    for ((regex, bank) in knownBanks) {
        if (regex.containsMatchIn(rawMessage)) {
            return MessageSourceInfo(
                type = "SMS",
                sourceName = bank,
                cleanText = rawMessage,
                isAppNotification = false
            )
        }
    }

    val isLikelySms = lower.contains("a/c") || lower.contains("acct") || lower.contains("debited") ||
            lower.contains("credited") || lower.contains("bal:") || lower.contains("avl lmt") ||
            lower.contains("spent on your") || lower.contains("not you? call")

    val isUpi = lower.contains("upi payment") || lower.contains("payment received") ||
            lower.contains("money sent") || lower.contains("paid ₹") || lower.contains("received ₹")

    val sourceLabel = when {
        isLikelySms -> "Bank SMS"
        isUpi -> "UPI Notification"
        else -> "App Notification"
    }

    return MessageSourceInfo(
        type = if (isLikelySms) "SMS" else "Notification",
        sourceName = sourceLabel,
        cleanText = rawMessage,
        isAppNotification = !isLikelySms
    )
}

@Composable
private fun OriginalMessageDialog(
    rawMessage: String,
    isHapticsEnabled: Boolean,
    onDismiss: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val view = LocalView.current
    val sourceInfo = remember(rawMessage) { parseMessageSource(rawMessage) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, White10, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.MessageSquareText,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.original_message_title),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val badgeText = if (sourceInfo.sourceName == "App Notification" || sourceInfo.sourceName == "Bank SMS" || sourceInfo.sourceName == "UPI Notification") {
                    sourceInfo.sourceName
                } else {
                    "${sourceInfo.type} • ${sourceInfo.sourceName}"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sourceInfo.isAppNotification) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else EmeraldIncome.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (sourceInfo.isAppNotification) LucideIcons.Bell else LucideIcons.MessageSquareText,
                        contentDescription = null,
                        tint = if (sourceInfo.isAppNotification) MaterialTheme.colorScheme.primary else EmeraldIncome,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = badgeText,
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (sourceInfo.isAppNotification) MaterialTheme.colorScheme.primary else EmeraldIncome
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, White10, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = sourceInfo.cleanText,
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            clipboardManager.setText(AnnotatedString(sourceInfo.cleanText))
                            isCopied = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCopied) EmeraldIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = if (isCopied) EmeraldIncome else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) LucideIcons.Check else LucideIcons.Copy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isCopied) stringResource(R.string.original_message_copied) else stringResource(R.string.action_copy),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}