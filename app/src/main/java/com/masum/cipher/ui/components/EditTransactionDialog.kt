package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
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
import com.masum.cipher.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    var merchant by remember { mutableStateOf(transaction.merchant) }
    var textFieldValue by remember { 
        val initText = if (transaction.amount == 0.0) "" else transaction.amount.toString()
        mutableStateOf(TextFieldValue(text = initText, selection = TextRange(initText.length)))
    }
    val amount = textFieldValue.text
    var isIncome by remember { mutableStateOf(transaction.isIncome) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.fromString(transaction.category)) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            val isEditing = transaction.amount != 0.0 || transaction.merchant.isNotBlank()
            Text(
                text = if (isEditing) "Edit Transaction" else "Add Transaction",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Type toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeToggleButton(
                    label = "Expense",
                    selected = !isIncome,
                    activeColor = CipherExpense,
                    modifier = Modifier.weight(1f),
                    onClick = { isIncome = false }
                )
                TypeToggleButton(
                    label = "Income",
                    selected = isIncome,
                    activeColor = CipherIncome,
                    modifier = Modifier.weight(1f),
                    onClick = { isIncome = true }
                )
            }

            // Amount field
            Column {
                SheetTextFieldValue(
                    value = textFieldValue,
                    onValueChange = { newValue -> 
                        if (newValue.text.all { it.isDigit() || it in "+-*/. " }) {
                            textFieldValue = newValue
                        } 
                    },
                    label = "Amount",
                    prefix = "₹",
                    readOnly = false
                )
                
                val computed = com.masum.cipher.core.util.MathEvaluator.evaluate(amount)
                if (computed != null && amount.any { it in "+-*/" }) {
                    Text(
                        text = "= ₹${String.format(java.util.Locale.getDefault(), "%,.2f", computed)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                CalculatorNumpad(
                    input = amount,
                    cursorPosition = textFieldValue.selection.start,
                    onInputChange = { newInput, newCursor ->
                        textFieldValue = TextFieldValue(
                            text = newInput,
                            selection = TextRange(newCursor.coerceIn(0, newInput.length))
                        )
                    }
                )
            }

            // Merchant field
            SheetTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = "Merchant"
            )

            // Category picker
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = selectedCategory.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (category == selectedCategory) category.color else MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(category.color, RoundedCornerShape(4.dp))
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    val finalAmount = com.masum.cipher.core.util.MathEvaluator.evaluate(amount) ?: transaction.amount
                    if (finalAmount > 0) {
                        onConfirm(
                            transaction.copy(
                                merchant = merchant.trim().ifBlank { "Miscellaneous" },
                                amount = finalAmount,
                                category = selectedCategory.name,
                                isIncome = isIncome
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CipherBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Add Transaction",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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
                color = if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            )
            .clip(RoundedCornerShape(9.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {
    val surface = MaterialTheme.colorScheme.surface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val onBg = MaterialTheme.colorScheme.onBackground
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(12.dp))
            .border(1.dp, outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = onSurfaceVar
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = onSurfaceVar
                )
                Spacer(Modifier.width(4.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = onBg
                ),
                singleLine = true,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(CipherBlue),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = outline
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun SheetTextFieldValue(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    prefix: String? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val surface = MaterialTheme.colorScheme.surface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val onBg = MaterialTheme.colorScheme.onBackground
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(12.dp))
            .border(1.dp, outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = onSurfaceVar
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = onSurfaceVar
                )
                Spacer(Modifier.width(4.dp))
            }
            val keyboardController = LocalSoftwareKeyboardController.current
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                    keyboardController?.hide()
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = onBg
                ),
                singleLine = true,
                readOnly = false,
                keyboardOptions = keyboardOptions.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                cursorBrush = SolidColor(CipherBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            keyboardController?.hide()
                        }
                    },
                decorationBox = { inner ->
                    if (value.text.isEmpty()) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = outline
                        )
                    }
                    inner()
                }
            )
        }
    }
}

