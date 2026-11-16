package com.masum.cipher.ui.debts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.DebtEntity
import com.masum.cipher.core.domain.model.DebtType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.FormEyebrow
import com.masum.cipher.ui.components.FormGroup
import com.masum.cipher.ui.components.FormHairline
import com.masum.cipher.ui.components.FormHeroAmount
import com.masum.cipher.ui.components.FormLabelValueRow
import com.masum.cipher.ui.components.FormMonogram
import com.masum.cipher.ui.components.FormPrimaryButton
import com.masum.cipher.ui.components.FormQuickChip
import com.masum.cipher.ui.components.FormRow
import com.masum.cipher.ui.components.FormSelectableChip
import com.masum.cipher.ui.components.FormTextRow
import com.masum.cipher.ui.components.FormTopBar
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowDownLeft
import compose.icons.lucideicons.ArrowUpRight
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.FileText
import compose.icons.lucideicons.User
import compose.icons.lucideicons.X
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditDebtScreen(
    debtToEdit: DebtEntity? = null,
    accounts: List<AccountEntity>,
    currencySymbol: String = "₹",
    isHapticsEnabled: Boolean = true,
    suggestedPersons: List<String> = emptyList(),
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
    val quickChips = listOf(100.0, 500.0, 1000.0, 2000.0, 5000.0)

    val filteredSuggestions = remember(suggestedPersons, personName) {
        val currentLower = personName.trim().lowercase(Locale.ROOT)
        suggestedPersons
            .filter { it.isNotBlank() && it.trim().lowercase(Locale.ROOT) != currentLower }
            .distinctBy { it.trim().lowercase(Locale.ROOT) }
            .take(6)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        dueDate = null
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_clear_input), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                TextButton(onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); showDeleteConfirm = false }) {
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
    val formattedAmountDisplay = AppFormatters.formatCurrency(parsedAmount, currencySymbol, locale = locale, decimals = 0)
    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .background(Brush.verticalGradient(listOf(typeColor.copy(alpha = 0.14f), Color.Transparent), endY = 1100f)),
        containerColor = Color.Transparent,
        topBar = {
            FormTopBar(
                title = if (isEditing) stringResource(R.string.debt_edit_title) else stringResource(R.string.debt_create_title),
                onBack = {
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    hideKeyboard()
                    onNavigateBack()
                },
                onDelete = if (isEditing && onDeleteDebt != null) {
                    {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        showDeleteConfirm = true
                    }
                } else null,
                deleteDescription = stringResource(R.string.action_delete)
            )
        },
        bottomBar = {
            FormPrimaryButton(
                text = if (isEditing) {
                    stringResource(R.string.goals_save_changes)
                } else if (selectedType == DebtType.LENT) {
                    stringResource(R.string.debt_record_lent_action, formattedAmountDisplay)
                } else {
                    stringResource(R.string.debt_record_borrowed_action, formattedAmountDisplay)
                },
                enabled = isFormValid,
                accent = typeColor,
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    hideKeyboard()
                    onSaveDebt(
                        personName.trim(),
                        parsedAmount,
                        selectedType,
                        dueDate,
                        noteText.trim().ifBlank { null },
                        selectedAccountId,
                        syncLedger
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .width(236.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        .padding(3.dp)
                ) {
                    val pillWidth = maxWidth / 2
                    val indicatorOffset by animateDpAsState(
                        targetValue = if (selectedType == DebtType.LENT) 0.dp else pillWidth,
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f),
                        label = "pillIndicator"
                    )
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                            .width(pillWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, typeColor.copy(alpha = 0.55f), RoundedCornerShape(50))
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        listOf(DebtType.LENT, DebtType.BORROWED).forEach { type ->
                            val active = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        view.performVibrate(isHapticsEnabled)
                                        selectedType = type
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (type == DebtType.LENT) LucideIcons.ArrowDownLeft else LucideIcons.ArrowUpRight,
                                        contentDescription = null,
                                        tint = if (active) typeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stringResource(
                                            if (type == DebtType.LENT) R.string.debt_type_lent_short else R.string.debt_type_borrowed_short
                                        ),
                                        style = TextStyle(
                                            fontFamily = DMSans,
                                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (selectedType == DebtType.LENT) stringResource(R.string.debt_create_lent_subtitle) else stringResource(R.string.debt_create_borrowed_subtitle),
                    style = TextStyle(fontFamily = Lato, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(22.dp))

                FormHeroAmount(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1 && input.length <= 10) {
                            amountText = input
                        }
                    },
                    currencySymbol = currencySymbol,
                    accent = typeColor,
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    quickChips.forEach { chipAmount ->
                        FormQuickChip(
                            label = "+${AppFormatters.formatCurrency(chipAmount, currencySymbol, locale = locale, decimals = 0)}",
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val newVal = (amountText.toDoubleOrNull() ?: 0.0) + chipAmount
                                amountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else String.format(Locale.US, "%.2f", newVal)
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                FormGroup {
                    FormTextRow(
                        icon = LucideIcons.User,
                        label = stringResource(R.string.debt_person_name_label),
                        value = personName,
                        onValueChange = { personName = it },
                        placeholder = stringResource(R.string.debt_person_name_placeholder),
                        accent = typeColor,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        leading = {
                            FormMonogram(
                                letter = personName.trim().take(1).uppercase(Locale.ROOT).ifEmpty { null },
                                accent = typeColor,
                                fallback = LucideIcons.User
                            )
                        }
                    )

                    if (filteredSuggestions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredSuggestions.forEach { suggestion ->
                                FormQuickChip(
                                    label = suggestion,
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled)
                                        personName = suggestion
                                    }
                                )
                            }
                        }
                    }

                    FormHairline()

                    FormLabelValueRow(
                        icon = LucideIcons.Calendar,
                        label = stringResource(R.string.debt_due_date_label),
                        value = dueDate?.let { AppFormatters.formatFullDate(it) },
                        placeholder = stringResource(R.string.debt_select_due_date),
                        accent = typeColor,
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            showDatePicker = true
                        },
                        trailing = if (dueDate != null) {
                            {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = stringResource(R.string.action_clear_input),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled)
                                            dueDate = null
                                        }
                                        .padding(7.dp)
                                )
                            }
                        } else null
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7, 14, 30, 60).forEach { days ->
                            FormQuickChip(
                                label = "+${days}d",
                                onClick = {
                                    view.performVibrate(isHapticsEnabled)
                                    dueDate = System.currentTimeMillis() + (days * 86_400_000L)
                                }
                            )
                        }
                    }
                }

                if (accounts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormEyebrow(
                            text = stringResource(R.string.debt_linked_account_label),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        FormGroup {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val accColor = Color(acc.colorHex)
                                    FormSelectableChip(
                                        label = acc.name,
                                        selected = selectedAccountId == acc.id,
                                        accent = accColor,
                                        onClick = {
                                            view.performVibrate(isHapticsEnabled)
                                            selectedAccountId = acc.id
                                        },
                                        leading = {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(accColor)
                                            )
                                        }
                                    )
                                }
                            }

                            FormHairline()

                            FormRow(
                                onClick = {
                                    view.performVibrate(isHapticsEnabled)
                                    syncLedger = !syncLedger
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.debt_sync_ledger_label),
                                            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.debt_sync_ledger_desc),
                                            style = TextStyle(fontFamily = Lato, fontSize = 11.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                    Switch(
                                        checked = syncLedger,
                                        onCheckedChange = {
                                            view.performVibrate(isHapticsEnabled)
                                            syncLedger = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = typeColor,
                                            checkedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                FormGroup {
                    FormTextRow(
                        icon = LucideIcons.FileText,
                        label = stringResource(R.string.adjust_balance_note_label),
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = "e.g. Dinner split, Rent loan, UPI",
                        accent = typeColor,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { hideKeyboard() })
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
