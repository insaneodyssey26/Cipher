package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import androidx.compose.ui.graphics.graphicsLayer
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Repeat
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.horizontalScroll
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters

import compose.icons.lucideicons.ChevronDown
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriptionSheet(
    subscription: SubscriptionDetector.Subscription?,
    customCategories: List<CustomCategoryEntity> = emptyList(),
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (merchant: String, amount: Double, category: String, frequencyDays: Int, nextExpectedDate: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var merchant by remember { mutableStateOf(subscription?.merchant ?: "") }
    var amountText by remember { mutableStateOf(subscription?.amount?.toString() ?: "") }
    var frequencyDays by remember { mutableStateOf(subscription?.frequencyDays?.toString() ?: "30") }
    var selectedCategory by remember(subscription, customCategories) {
        mutableStateOf(CategoryHelper.resolveCategory(subscription?.category ?: TransactionCategory.OTHERS.name, customCategories))
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    val presetDays = remember { listOf(30, 90, 365) }
    val initialDays = frequencyDays.toIntOrNull() ?: 30
    var customMode by remember { mutableStateOf(initialDays !in presetDays) }
    var customUnit by remember {
        mutableStateOf(
            when {
                initialDays % 365 == 0 -> 365
                initialDays % 30 == 0 -> 30
                initialDays % 7 == 0 -> 7
                else -> 1
            }
        )
    }
    var customCount by remember { mutableStateOf((initialDays / customUnit).coerceAtLeast(1).toString()) }
    
    var nextExpectedDate by remember { androidx.compose.runtime.mutableLongStateOf(subscription?.nextExpectedDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        val isEditing = subscription != null
        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
        val isValid = merchant.isNotBlank() && parsedAmount > 0.0
        val accent = MaterialTheme.colorScheme.primary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .nestedScroll(consumeOverscrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FormEyebrow(text = "Recurring payment")
                    Text(
                        text = if (isEditing) "Subscription details" else "New subscription",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = com.masum.cipher.ui.theme.DMSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (isEditing && onDelete != null) {
                    Text(
                        text = "Delete",
                        style = TextStyle(fontFamily = com.masum.cipher.ui.theme.DMSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            if (subscription != null) {
                val monthlyEst = subscription.amount * (30.0 / subscription.frequencyDays.coerceAtLeast(1).toDouble())
                val trackType = if (subscription.confidence == 1.0f) "Manual entry" else "Auto-detected"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        FormEyebrow(text = "Monthly average")
                        Text(
                            text = "${AppFormatters.formatCurrency(monthlyEst, currencySymbol)}/mo",
                            style = TextStyle(fontFamily = com.masum.cipher.ui.theme.DMSans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.6).sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = trackType,
                        style = TextStyle(fontFamily = com.masum.cipher.ui.theme.DMSans, fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            FormHeroAmount(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1 && input.length <= 10) amountText = input
                },
                currencySymbol = currencySymbol,
                accent = accent,
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                listOf("Monthly" to 30, "Quarterly" to 90, "Yearly" to 365).forEach { (label, days) ->
                    FormSelectableChip(
                        label = label,
                        selected = !customMode && frequencyDays == days.toString(),
                        accent = accent,
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                            customMode = false
                            frequencyDays = days.toString()
                        }
                    )
                }
                FormSelectableChip(
                    label = "Custom",
                    selected = customMode,
                    accent = accent,
                    onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                        customMode = true
                        frequencyDays = ((customCount.toIntOrNull() ?: 1).coerceAtLeast(1) * customUnit).toString()
                    }
                )
            }

            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FormGroup {
                    FormTextRow(
                        icon = LucideIcons.Repeat,
                        label = "Merchant / name",
                        value = merchant,
                        onValueChange = { merchant = it },
                        placeholder = "e.g. Netflix, Rent, Gym",
                        accent = accent,
                        keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words)
                    )
                    androidx.compose.animation.AnimatedVisibility(visible = customMode) {
                        Column {
                            FormHairline()
                            FormTextRow(
                                icon = LucideIcons.Clock,
                                label = "Repeats every",
                                value = customCount,
                                onValueChange = { input ->
                                    if (input.length <= 3 && input.all { c -> c.isDigit() }) {
                                        customCount = input
                                        frequencyDays = ((input.toIntOrNull() ?: 1).coerceAtLeast(1) * customUnit).toString()
                                    }
                                },
                                placeholder = "1",
                                accent = accent,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Days" to 1, "Weeks" to 7, "Months" to 30, "Years" to 365).forEach { (label, unitDays) ->
                                    FormSelectableChip(
                                        label = label,
                                        selected = customUnit == unitDays,
                                        accent = accent,
                                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                                            customUnit = unitDays
                                            frequencyDays = ((customCount.toIntOrNull() ?: 1).coerceAtLeast(1) * unitDays).toString()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    FormHairline()
                    FormLabelValueRow(
                        icon = LucideIcons.Calendar,
                        label = "Starting / next date",
                        value = AppFormatters.getDay().format(Date(nextExpectedDate)),
                        placeholder = "",
                        accent = accent,
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); showDatePicker = true }
                    )
                    FormHairline()
                    FormLabelValueRow(
                        icon = selectedCategory.icon,
                        label = "Category",
                        value = selectedCategory.titleRes?.let { stringResource(it) } ?: selectedCategory.displayName,
                        placeholder = "",
                        accent = selectedCategory.color,
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); categoryExpanded = !categoryExpanded },
                        trailing = {
                            val chevron by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (categoryExpanded) 180f else 0f,
                                label = "category_chevron"
                            )
                            Icon(
                                imageVector = compose.icons.LucideIcons.ChevronDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { rotationZ = chevron }
                            )
                        }
                    )
                    androidx.compose.animation.AnimatedVisibility(visible = categoryExpanded) {
                        val allCategories = remember(customCategories) {
                            CategoryHelper.getAllCategories(customCategories, includeIncome = false)
                        }
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allCategories.forEach { category ->
                                FormSelectableChip(
                                    label = if (category.titleRes != null) stringResource(category.titleRes) else category.displayName,
                                    selected = category.name == selectedCategory.name,
                                    accent = category.color,
                                    onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                                        selectedCategory = category
                                        categoryExpanded = false
                                    },
                                    leading = {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = category.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = nextExpectedDate
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                                datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = pickedUtcMillis
                                    }
                                    val localCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                                    }
                                    nextExpectedDate = localCal.timeInMillis
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text(stringResource(R.string.action_select), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); showDatePicker = false }
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

            Spacer(Modifier.height(4.dp))

            FormPrimaryButton(
                background = androidx.compose.ui.graphics.Color.Transparent,
                text = if (isEditing) "Save changes" else "Add subscription",
                enabled = isValid,
                accent = accent,
                onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                    val freq = frequencyDays.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    if (isValid) onConfirm(merchant, parsedAmount, selectedCategory.name, freq, nextExpectedDate)
                }
            )
        }
    }
}
