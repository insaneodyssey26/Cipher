package com.masum.cipher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.MathEvaluator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Calculator
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.X
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.ui.theme.Lato
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import compose.icons.lucideicons.Users
import com.masum.cipher.core.domain.model.SplitParticipant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsSheet(
    transaction: TransactionEntity,
    currencySymbol: String = "₹",
    existingSplits: List<SplitParticipant> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit,
    onConfirmWithSplits: ((TransactionEntity, List<SplitParticipant>) -> Unit)? = null,
    onSaveSplits: ((List<SplitParticipant>) -> Unit)? = null,
    onOpenSplitSheet: ((TransactionEntity, List<SplitParticipant>) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDraftChange: ((TransactionEntity) -> Unit)? = null,
    isHapticsEnabled: Boolean = true
) {
    var merchant by remember { mutableStateOf(transaction.merchant) }
    var amount by remember { mutableStateOf(if (transaction.amount == 0.0) "" else String.format(Locale.US, "%.2f", transaction.amount)) }
    var isIncome by remember { mutableStateOf(transaction.isIncome) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.fromString(transaction.category)) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var isNoteExpanded by remember { mutableStateOf(transaction.note?.isNotBlank() == true) }
    var selectedTimestamp by remember { mutableLongStateOf(transaction.timestamp) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSplitSheet by remember { mutableStateOf(false) }
    var currentSplits by remember { mutableStateOf(existingSplits) }

    LaunchedEffect(merchant, amount, isIncome, selectedCategory, note, selectedTimestamp) {
        if (onDraftChange != null) {
            val finalAmount = MathEvaluator.evaluate(amount) ?: 0.0
            onDraftChange.invoke(
                transaction.copy(
                    merchant = merchant,
                    amount = finalAmount,
                    category = selectedCategory.name,
                    isIncome = isIncome,
                    note = note.ifBlank { null },
                    timestamp = selectedTimestamp
                )
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val view = LocalView.current

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val isEditing = transaction.id != 0L
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
            val todayStr = stringResource(R.string.today)
            val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
            val dateLabel = if (isToday) {
                "$todayStr, " + AppFormatters.getDay(locale).format(Date(selectedTimestamp))
            } else {
                AppFormatters.getShortDate(locale).format(Date(selectedTimestamp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.tx_details_title) else stringResource(R.string.tx_new_title),
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isEditing && onDelete != null) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            onDelete()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = RoseExpense,
                            modifier = Modifier.size(20.dp)
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
                        TextButton(
                            onClick = { showDatePicker = false }
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                val tabWidth = maxWidth / 2
                val selectedTypeIndex = if (isIncome) 1 else 0
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedTypeIndex,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
                    label = "tx_type_offset"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) EmeraldIncome.copy(alpha = 0.15f) else RoseExpense.copy(alpha = 0.15f))
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isIncome) view.performVibrate(isHapticsEnabled)
                                isIncome = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.expense).uppercase(),
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (!isIncome) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isIncome) view.performVibrate(isHapticsEnabled)
                                isIncome = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.income).uppercase(),
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isIncome) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AmountInputField(
                value = amount,
                onValueChange = { if (it.length <= 15) amount = it },
                currencySymbol = currencySymbol,
                color = if (isIncome) EmeraldIncome else RoseExpense
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    VaultSheetTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = stringResource(R.string.merchant).uppercase()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = {
                            view.performVibrate(isHapticsEnabled)
                            focusManager.clearFocus()
                            categoryExpanded = !categoryExpanded
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, White10, RoundedCornerShape(12.dp))
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = selectedCategory.icon,
                                        contentDescription = null,
                                        tint = selectedCategory.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = stringResource(R.string.category).uppercase(),
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(selectedCategory.titleRes),
                                            style = Typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = LucideIcons.ChevronDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(
                                surface = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                TransactionCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(category.titleRes),
                                                style = Typography.bodyMedium.copy(
                                                    fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (category == selectedCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            view.performVibrate(isHapticsEnabled)
                                            selectedCategory = category
                                            categoryExpanded = false
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(
                                                        category.color.copy(alpha = 0.12f),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = category.icon,
                                                    contentDescription = null,
                                                    tint = category.color,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val showSplitOption = !isIncome
            val showNoteOption = !isNoteExpanded

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, White10, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showNoteOption) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled)
                                isNoteExpanded = true
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Plus,
                            contentDescription = stringResource(R.string.action_add_note),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.note),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                if (showNoteOption) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp)
                            .background(White10)
                    )
                }

                if (showSplitOption) {
                    val hasSplits = currentSplits.size > 1
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled)
                                focusManager.clearFocus()
                                val evaluatedTotal = MathEvaluator.evaluate(amount) ?: transaction.amount
                                val currentDraft = transaction.copy(
                                    merchant = merchant.trim().ifBlank { "Miscellaneous" },
                                    amount = evaluatedTotal,
                                    category = selectedCategory.name,
                                    isIncome = isIncome,
                                    note = note.ifBlank { null },
                                    timestamp = selectedTimestamp
                                )
                                if (onOpenSplitSheet != null) {
                                    onOpenSplitSheet(currentDraft, currentSplits)
                                } else {
                                    showSplitSheet = true
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Users,
                            contentDescription = stringResource(R.string.split_expense),
                            tint = if (hasSplits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (hasSplits) "${stringResource(R.string.split_title)} (${currentSplits.size})" else stringResource(R.string.split_title),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = if (hasSplits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp)
                            .background(White10)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            view.performVibrate(isHapticsEnabled)
                            focusManager.clearFocus()
                            showDatePicker = true
                        }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Calendar,
                        contentDescription = "Pick Date",
                        tint = if (isToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = dateLabel,
                        style = Typography.labelMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = if (isToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
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
                    onDraftChange = { updatedSplits ->
                        currentSplits = updatedSplits
                    },
                    onSaveSplits = { updatedSplits ->
                        currentSplits = updatedSplits
                        onSaveSplits?.invoke(updatedSplits)
                    }
                )
            }

            AnimatedVisibility(
                visible = isNoteExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                VaultSheetTextField(
                    value = note,
                    onValueChange = { if (it.length <= 150) note = it },
                    label = stringResource(R.string.note).uppercase(),
                    showClearButton = true
                )
            }

            Spacer(Modifier.height(8.dp))

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
                            timestamp = selectedTimestamp
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.update_transaction) else stringResource(R.string.action_save),
                    style = Typography.titleMedium
                )
            }
        }
    }
}


@Composable
private fun VaultSheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    showClearButton: Boolean = false
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, White10, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = if (showClearButton && value.isNotEmpty()) 36.dp else 0.dp)
        ) {
            Text(
                text = label,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prefix != null) {
                    Text(
                        text = prefix,
                        style = Typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = Typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = "—",
                                style = Typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        inner()
                    }
                )
            }
        }

        if (showClearButton && value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .clickable {
                        view.performVibrate(true)
                        onValueChange("")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.X,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String = "₹",
    color: Color
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    var showCalculator by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale
    val hapticsEnabled = true

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursorAlpha"
    )

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            val safeCursor = textFieldValue.selection.start.coerceIn(0, value.length)
            textFieldValue = TextFieldValue(text = value, selection = TextRange(safeCursor))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currencySymbol,
                style = Typography.displayLarge.copy(
                    color = color.copy(alpha = 0.5f),
                    fontSize = 48.sp
                ),
                modifier = Modifier.padding(end = 8.dp)
            )

            if (showCalculator) {
                val textScrollState = rememberScrollState()
                val currentText = textFieldValue.text
                val safeCursor = textFieldValue.selection.start.coerceIn(0, currentText.length)

                LaunchedEffect(safeCursor, currentText, textLayoutResult) {
                    val layout = textLayoutResult ?: return@LaunchedEffect
                    if (currentText.isNotEmpty()) {
                        val cursorRect = layout.getCursorRect(safeCursor)
                        textScrollState.animateScrollTo(cursorRect.left.toInt())
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .horizontalScroll(textScrollState)
                        .pointerInput(currentText) {
                            detectTapGestures { offset ->
                                val layout = textLayoutResult ?: return@detectTapGestures
                                val clickedOffset = layout.getOffsetForPosition(offset).coerceIn(0, currentText.length)
                                view.performVibrate(hapticsEnabled)
                                textFieldValue = textFieldValue.copy(selection = TextRange(clickedOffset))
                            }
                        }
                        .pointerInput(currentText) {
                            var accumulatedDrag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val layout = textLayoutResult ?: return@detectHorizontalDragGestures
                                    val dragIndex = layout.getOffsetForPosition(offset).coerceIn(0, currentText.length)
                                    view.performVibrate(hapticsEnabled)
                                    textFieldValue = textFieldValue.copy(selection = TextRange(dragIndex))
                                    accumulatedDrag = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    accumulatedDrag += dragAmount
                                    if (kotlin.math.abs(accumulatedDrag) >= 18f) {
                                        val deltaChars = (accumulatedDrag / 18f).toInt()
                                        if (deltaChars != 0) {
                                            val newCursor = (textFieldValue.selection.start + deltaChars).coerceIn(0, currentText.length)
                                            if (newCursor != textFieldValue.selection.start) {
                                                view.performVibrate(hapticsEnabled)
                                                textFieldValue = textFieldValue.copy(selection = TextRange(newCursor))
                                            }
                                            accumulatedDrag %= 18f
                                        }
                                    }
                                    change.consume()
                                }
                            )
                        }
                        .drawWithContent {
                            drawContent()
                            val layout = textLayoutResult
                            if (layout != null && currentText.isNotEmpty()) {
                                val cursorRect = layout.getCursorRect(safeCursor)
                                drawLine(
                                    color = color.copy(alpha = cursorAlpha),
                                    start = Offset(cursorRect.left, cursorRect.top + 6.dp.toPx()),
                                    end = Offset(cursorRect.left, cursorRect.bottom - 6.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            } else if (currentText.isEmpty()) {
                                drawLine(
                                    color = color.copy(alpha = cursorAlpha),
                                    start = Offset(0f, 6.dp.toPx()),
                                    end = Offset(0f, size.height - 6.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                ) {
                    if (currentText.isEmpty()) {
                        Text(
                            text = "0",
                            style = Typography.displayLarge.copy(
                                color = color.copy(alpha = 0.3f),
                                fontSize = 56.sp,
                                letterSpacing = (-2).sp
                            )
                        )
                    } else {
                        Text(
                            text = currentText,
                            style = Typography.displayLarge.copy(
                                color = color,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 56.sp,
                                letterSpacing = (-2).sp
                            ),
                            onTextLayout = { textLayoutResult = it }
                        )
                    }
                }
            } else {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue -> 
                        if (newValue.text.all { it.isDigit() || it in "+-*/. " }) {
                            if (newValue.selection != textFieldValue.selection) {
                                view.performVibrate(hapticsEnabled)
                            }
                            textFieldValue = newValue
                            onValueChange(newValue.text)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    textStyle = Typography.displayLarge.copy(
                        color = color,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 56.sp,
                        letterSpacing = (-2).sp
                    ),
                    singleLine = true,
                    readOnly = false,
                    cursorBrush = SolidColor(color),
                    modifier = Modifier.weight(1f, fill = false),
                    decorationBox = { inner ->
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "0",
                                style = Typography.displayLarge.copy(
                                    color = color.copy(alpha = 0.3f),
                                    fontSize = 56.sp,
                                    letterSpacing = (-2).sp
                                )
                            )
                        } else {
                            inner()
                        }
                    }
                )
            }
        }
        
        val computed = MathEvaluator.evaluate(textFieldValue.text)
        if (computed != null && textFieldValue.text.any { it in "+-*/" }) {
            Text(
                text = "= ${com.masum.cipher.core.util.AppFormatters.formatCurrency(computed, currencySymbol, locale, decimals = 2)}",
                style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        androidx.compose.material3.Surface(
            onClick = { 
                showCalculator = !showCalculator
                if (showCalculator) {
                    keyboardController?.hide()
                } else {
                    keyboardController?.show()
                }
            },
            shape = CircleShape,
            color = if (showCalculator) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Calculator,
                    contentDescription = "Toggle Input",
                    tint = if (showCalculator) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showCalculator) stringResource(R.string.use_keyboard) else stringResource(R.string.use_calculator),
                    style = Typography.labelMedium,
                    color = if (showCalculator) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        AnimatedVisibility(
            visible = showCalculator,
            enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeIn(),
            exit = shrinkVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeOut()
        ) {
            CalculatorNumpad(
                input = textFieldValue.text,
                cursorPosition = textFieldValue.selection.start,
                onInputChange = { newInput, newCursor ->
                    textFieldValue = TextFieldValue(
                        text = newInput,
                        selection = TextRange(newCursor.coerceIn(0, newInput.length))
                    )
                    onValueChange(newInput)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
