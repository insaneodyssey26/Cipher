package com.masum.cipher.ui.debts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowDownLeft
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowUpRight
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.User
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditDebtScreen(
    debtToEdit: DebtEntity? = null,
    accounts: List<AccountEntity>,
    currencySymbol: String = "₹",
    isHapticsEnabled: Boolean = true,
    onNavigateBack: () -> Unit,
    onSaveDebt: (personName: String, amount: Double, type: DebtType, dueDate: Long?, note: String?, accountId: Long?, syncLedger: Boolean) -> Unit,
    onDeleteDebt: ((DebtEntity) -> Unit)? = null
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val locale = LocalLocale.current.platformLocale

    val isEditing = debtToEdit != null

    var personName by remember(debtToEdit) { mutableStateOf(debtToEdit?.personName ?: "") }
    var amountText by remember(debtToEdit) {
        mutableStateOf(
            debtToEdit?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var selectedType by remember(debtToEdit) {
        mutableStateOf(
            debtToEdit?.let {
                if (it.type == "LENT") DebtType.LENT else DebtType.BORROWED
            } ?: DebtType.LENT
        )
    }
    var dueDate by remember(debtToEdit) { mutableStateOf(debtToEdit?.dueDate) }
    var noteText by remember(debtToEdit) { mutableStateOf(debtToEdit?.note ?: "") }
    var selectedAccountId by remember(debtToEdit) {
        mutableStateOf(
            debtToEdit?.accountId ?: accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id
        )
    }
    var syncLedger by remember(debtToEdit) { mutableStateOf(debtToEdit?.transactionId != null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(debtToEdit) {
        if (debtToEdit != null) {
            personName = debtToEdit.personName
            amountText = if (debtToEdit.amount % 1.0 == 0.0) debtToEdit.amount.toLong().toString() else debtToEdit.amount.toString()
            selectedType = if (debtToEdit.type == "LENT") DebtType.LENT else DebtType.BORROWED
            dueDate = debtToEdit.dueDate
            noteText = debtToEdit.note ?: ""
            selectedAccountId = debtToEdit.accountId ?: accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id
            syncLedger = debtToEdit.transactionId != null
        }
    }

    BackHandler {
        focusManager.clearFocus()
        keyboardController?.hide()
        onNavigateBack()
    }

    val typeColor = if (selectedType == DebtType.LENT) EmeraldIncome else RoseExpense
    val quickChips = listOf(500.0, 1000.0, 2000.0, 5000.0)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dueDate = null
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_clear_input))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && debtToEdit != null && onDeleteDebt != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.debt_delete_confirm_title),
                    style = Typography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.debt_delete_confirm_desc),
                    style = Typography.bodyMedium.copy(fontFamily = Lato)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        showDeleteConfirm = false
                        onDeleteDebt(debtToEdit)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = RoseExpense,
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = Typography.labelLarge
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isFormValid = personName.isNotBlank() && parsedAmount > 0.0

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed by btnInteraction.collectIsPressedAsState()
    val btnScale by animateFloatAsState(targetValue = if (btnPressed) 0.97f else 1f, label = "btn_scale")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isEditing) stringResource(R.string.debt_edit_title) else stringResource(R.string.debt_create_title),
                            style = Typography.titleLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedType == DebtType.LENT) stringResource(R.string.debt_create_lent_subtitle) else stringResource(R.string.debt_create_borrowed_subtitle),
                            style = Typography.bodySmall.copy(
                                fontFamily = Lato,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isEditing && onDeleteDebt != null) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            showDeleteConfirm = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(RoseExpense.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = RoseExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val formattedAmountDisplay = AppFormatters.formatCurrency(parsedAmount, currencySymbol, locale = locale, decimals = 0)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(btnScale)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isFormValid) typeColor else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = isFormValid, interactionSource = btnInteraction, indication = null) {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSaveDebt(
                                personName.trim(),
                                parsedAmount,
                                selectedType,
                                dueDate,
                                noteText.trim().ifBlank { null },
                                selectedAccountId,
                                syncLedger
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEditing) {
                            stringResource(R.string.goals_save_changes)
                        } else if (selectedType == DebtType.LENT) {
                            stringResource(R.string.debt_record_lent_action, formattedAmountDisplay)
                        } else {
                            stringResource(R.string.debt_record_borrowed_action, formattedAmountDisplay)
                        },
                        style = Typography.titleMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (isFormValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isLent = selectedType == DebtType.LENT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLent) EmeraldIncome.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(
                            width = if (isLent) 1.5.dp else 1.dp,
                            color = if (isLent) EmeraldIncome.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            selectedType = DebtType.LENT
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isLent) EmeraldIncome.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isLent) EmeraldIncome.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ArrowDownLeft,
                                    contentDescription = null,
                                    tint = if (isLent) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            if (isLent) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldIncome),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.debt_type_lent_short),
                                style = Typography.labelLarge.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isLent) EmeraldIncome else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.debt_lent_desc),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isLent) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isLent) EmeraldIncome.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                val isBorrowed = selectedType == DebtType.BORROWED
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isBorrowed) RoseExpense.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(
                            width = if (isBorrowed) 1.5.dp else 1.dp,
                            color = if (isBorrowed) RoseExpense.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            selectedType = DebtType.BORROWED
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isBorrowed) RoseExpense.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isBorrowed) RoseExpense.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ArrowUpRight,
                                    contentDescription = null,
                                    tint = if (isBorrowed) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            if (isBorrowed) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(RoseExpense),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.debt_type_borrowed_short),
                                style = Typography.labelLarge.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isBorrowed) RoseExpense else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.debt_borrowed_desc),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isBorrowed) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isBorrowed) RoseExpense.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1 && input.length <= 10) {
                            amountText = input
                        }
                    },
                    placeholder = {
                        Text(
                            text = "0.00",
                            style = Typography.headlineMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    },
                    prefix = {
                        Text(
                            text = currencySymbol,
                            style = Typography.headlineMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                            color = typeColor
                        )
                    },
                    textStyle = Typography.headlineMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = typeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickChips.forEach { chipAmount ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    val currentVal = amountText.toDoubleOrNull() ?: 0.0
                                    val newVal = currentVal + chipAmount
                                    amountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else newVal.toString()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${AppFormatters.formatCurrency(chipAmount, currencySymbol, locale = locale, decimals = 0)}",
                                style = Typography.labelSmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.debt_person_name_label),
                    style = Typography.labelMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.debt_person_name_placeholder),
                            style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 4.dp)
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(typeColor.copy(alpha = 0.16f))
                                .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.User,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.debt_due_date_label),
                    style = Typography.labelMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                showDatePicker = true
                            }
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Calendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (dueDate != null) AppFormatters.getFullDate().format(Date(dueDate!!)) else stringResource(R.string.debt_select_due_date),
                                style = Typography.bodyMedium.copy(
                                    fontFamily = Lato,
                                    fontSize = 13.sp,
                                    fontWeight = if (dueDate != null) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (dueDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val quickDays = listOf(7, 14, 30)
                    quickDays.forEach { days ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    dueDate = System.currentTimeMillis() + (days * 86_400_000L)
                                }
                                .padding(horizontal = 10.dp, vertical = 11.dp)
                        ) {
                            Text(
                                text = "+${days}d",
                                style = Typography.labelSmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (accounts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.debt_linked_account_label),
                            style = Typography.labelMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = selectedAccountId == acc.id
                                val accColor = Color(acc.colorHex)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) accColor.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) accColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedAccountId = acc.id
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(accColor)
                                        )
                                        Text(
                                            text = acc.name,
                                            style = Typography.labelMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.5.sp
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = LucideIcons.Check,
                                                contentDescription = null,
                                                tint = accColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    syncLedger = !syncLedger
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.debt_sync_ledger_label),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.debt_sync_ledger_desc),
                                    style = Typography.bodySmall.copy(
                                        fontFamily = Lato,
                                        fontSize = 11.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = syncLedger,
                                onCheckedChange = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    syncLedger = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.adjust_balance_note_label),
                    style = Typography.labelMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = {
                        Text(
                            text = "e.g. Dinner split, Rent loan, UPI",
                            style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
