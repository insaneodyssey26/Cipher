package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Pencil
import compose.icons.lucideicons.TrendingDown
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.X
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceSheet(
    currentBalance: Double,
    currencySymbol: String,
    locale: Locale,
    isHapticsEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirmAdjustment: (merchant: String, amount: Double, isIncome: Boolean, timestamp: Long, note: String?) -> Unit
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultOpeningName = stringResource(R.string.adjust_balance_opening_name)
    val defaultEntryName = stringResource(R.string.adjust_balance_entry_name)

    var newBalanceInput by remember { mutableStateOf("") }
    var noteText by remember {
        mutableStateOf(
            if (currentBalance == 0.0) {
                defaultOpeningName
            } else {
                defaultEntryName
            }
        )
    }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedNewBalance = newBalanceInput.replace(",", ".").toDoubleOrNull()
    val difference = parsedNewBalance?.let { it - currentBalance }
    val isSuffix = AppFormatters.isSuffixCurrency(currencySymbol, locale)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Pencil,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.adjust_balance_title),
                            style = Typography.titleLarge.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.adjust_balance_subtitle),
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = stringResource(R.string.action_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            VaultCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.adjust_balance_current).uppercase(),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppFormatters.formatCurrency(currentBalance, currencySymbol, locale, decimals = 2),
                            style = Typography.titleLarge.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    difference?.let { diff ->
                        if (kotlin.math.abs(diff) >= 0.01) {
                            val isIncome = diff > 0
                            val diffColor = if (isIncome) EmeraldIncome else RoseExpense
                            val diffIcon = if (isIncome) LucideIcons.TrendingUp else LucideIcons.TrendingDown
                            val formattedDiff = AppFormatters.formatCurrency(kotlin.math.abs(diff), currencySymbol, locale, decimals = 2)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(diffColor.copy(alpha = 0.12f))
                                    .border(1.dp, diffColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = diffIcon,
                                    contentDescription = null,
                                    tint = diffColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isIncome) "+$formattedDiff" else "-$formattedDiff",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = diffColor
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.adjust_balance_new_label),
                    style = Typography.labelMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isSuffix) {
                            Text(
                                text = currencySymbol,
                                style = Typography.headlineSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        BasicTextField(
                            value = newBalanceInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() || it == '.' || it == ',' }
                                if (clean.count { it == '.' || it == ',' } <= 1) {
                                    newBalanceInput = clean
                                }
                            },
                            textStyle = Typography.headlineSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (newBalanceInput.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = Typography.headlineSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (isSuffix) {
                            Text(
                                text = currencySymbol,
                                style = Typography.headlineSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            difference?.let { diff ->
                val formattedDiff = AppFormatters.formatCurrency(kotlin.math.abs(diff), currencySymbol, locale, decimals = 2)
                val (feedbackText, feedbackColor) = when {
                    diff > 0.009 -> Pair(stringResource(R.string.adjust_balance_income_added, formattedDiff), EmeraldIncome)
                    diff < -0.009 -> Pair(stringResource(R.string.adjust_balance_expense_logged, formattedDiff), RoseExpense)
                    else -> Pair(stringResource(R.string.adjust_balance_no_change), MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    text = feedbackText,
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    ),
                    color = feedbackColor
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.adjust_balance_note_label),
                    style = Typography.labelMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        textStyle = Typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        showDatePicker = true
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        text = SimpleDateFormat("dd MMMM yyyy", locale).format(Date(selectedTimestamp)),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.custom_date_title),
                    style = Typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
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
                                    val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
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
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }

            val canSubmit = difference != null && kotlin.math.abs(difference) >= 0.01

            Button(
                onClick = {
                    val diff = difference ?: return@Button
                    if (kotlin.math.abs(diff) < 0.01) return@Button

                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val isIncome = diff > 0
                    val absAmount = kotlin.math.abs(diff)
                    val merchant = noteText.ifBlank {
                        if (isIncome) {
                            defaultOpeningName
                        } else {
                            defaultEntryName
                        }
                    }

                    onConfirmAdjustment(
                        merchant,
                        absAmount,
                        isIncome,
                        selectedTimestamp,
                        noteText.ifBlank { null }
                    )
                    onDismiss()
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.adjust_balance_action_save),
                    style = Typography.labelLarge.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}
