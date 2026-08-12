package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.Trash2
import com.masum.cipher.core.util.MathEvaluator
import java.util.Locale

/**
 * Vault Transaction Details Sheet
 * 
 * A premium bottom sheet for adding or editing transactions.
 * Focuses on thumb reachability and clean ergonomics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsSheet(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit,
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

    LaunchedEffect(merchant, amount, isIncome, selectedCategory, note) {
        if (onDraftChange != null) {
            val finalAmount = MathEvaluator.evaluate(amount) ?: 0.0
            onDraftChange.invoke(
                transaction.copy(
                    merchant = merchant,
                    amount = finalAmount,
                    category = selectedCategory.name,
                    isIncome = isIncome,
                    note = note.ifBlank { null }
                )
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val view = androidx.compose.ui.platform.LocalView.current

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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Transaction details" else "Add transaction",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isEditing && onDelete != null) {
                    IconButton(onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        onDelete()
                    }) {
                        Icon(LucideIcons.Trash2, "Delete", tint = RoseExpense, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Type toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeToggleButton(
                    label = "EXPENSE",
                    selected = !isIncome,
                    activeColor = RoseExpense,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isIncome) view.performVibrate(isHapticsEnabled)
                        isIncome = false
                    }
                )
                TypeToggleButton(
                    label = "INCOME",
                    selected = isIncome,
                    activeColor = EmeraldIncome,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (!isIncome) view.performVibrate(isHapticsEnabled)
                        isIncome = true
                    }
                )
            }

            // Amount field
            AmountInputField(
                value = amount,
                onValueChange = { amount = it },
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
                        label = "MERCHANT"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = {
                            view.performVibrate(isHapticsEnabled)
                            categoryExpanded = !categoryExpanded
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, White10, RoundedCornerShape(12.dp))
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CATEGORY",
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = selectedCategory.toString().lowercase().replaceFirstChar { it.uppercase() },
                                        style = Typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = LucideIcons.ChevronDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            TransactionCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = category.toString().lowercase().replaceFirstChar { it.uppercase() },
                                            style = Typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
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
                                                .size(12.dp)
                                                .background(category.color, CircleShape)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (!isNoteExpanded) {
                Text(
                    text = "+ Add note",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performVibrate(isHapticsEnabled)
                            isNoteExpanded = true
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
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
                    label = "NOTE (OPTIONAL)"
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val finalAmount = MathEvaluator.evaluate(amount) ?: 0.0
                    if (finalAmount > 0) {
                        onConfirm(
                            transaction.copy(
                                merchant = merchant.trim().ifBlank { "Miscellaneous" },
                                amount = finalAmount,
                                category = selectedCategory.name,
                                isIncome = isIncome,
                                note = note.ifBlank { null }
                            )
                        )
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
                    text = if (isEditing) "Update transaction" else "Save transaction",
                    style = Typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TypeToggleButton(
    label: String,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VaultSheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, White10, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
}

@Composable
private fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    color: Color
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue -> 
                if (newValue.text.all { it.isDigit() || it in "+-*/. " }) {
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                }
                keyboardController?.hide()
            },
            textStyle = Typography.displayLarge.copy(
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 56.sp,
                letterSpacing = (-2).sp
            ),
            singleLine = true,
            readOnly = true,
            cursorBrush = SolidColor(color),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        keyboardController?.hide()
                    }
                },
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹",
                        style = Typography.displayLarge.copy(
                            color = color.copy(alpha = 0.5f),
                            fontSize = 48.sp
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
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
            }
        )
        
        val computed = MathEvaluator.evaluate(textFieldValue.text)
        if (computed != null && textFieldValue.text.any { it in "+-*/" }) {
            Text(
                text = "= ₹${String.format(Locale.getDefault(), "%,.2f", computed)}",
                style = Typography.titleMedium,
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
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
