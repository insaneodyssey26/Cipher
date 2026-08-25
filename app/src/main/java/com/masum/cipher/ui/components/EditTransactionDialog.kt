package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory

import com.masum.cipher.ui.theme.CipherExpense
import com.masum.cipher.ui.theme.CipherIncome
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calculator
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.ChevronRight
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import com.masum.cipher.core.util.performVibrate

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

    var showCalculator by remember { mutableStateOf(false) }

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

            Column {
                val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                SheetTextFieldValue(
                    value = textFieldValue,
                    onValueChange = { newValue -> 
                        if (newValue.text.all { it.isDigit() || it in "+-*/. " }) {
                            textFieldValue = newValue
                        } 
                    },
                    label = "Amount",
                    prefix = "₹",
                    readOnly = showCalculator,
                    onFocusChanged = { focusState ->
                        if (focusState.isFocused && showCalculator) {
                            keyboardController?.hide()
                        }
                    },
                    trailingIcon = {
                        androidx.compose.material3.IconButton(
                            onClick = { 
                                showCalculator = !showCalculator 
                                if (showCalculator) {
                                    keyboardController?.hide()
                                } else {
                                    keyboardController?.show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = compose.icons.LucideIcons.Calculator, 
                                contentDescription = "Toggle Calculator", 
                                tint = if (showCalculator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                
                val computed = com.masum.cipher.core.util.MathEvaluator.evaluate(amount)
                if (computed != null && amount.any { it in "+-*/" }) {
                    Text(
                        text = "= ₹${String.format(LocalLocale.current.platformLocale, "%,.2f", computed)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCalculator,
                    enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.fadeOut()
                ) {
                    Column {
                        if (amount.length > 1) {
                            val view = androidx.compose.ui.platform.LocalView.current
                            val safeCursor = textFieldValue.selection.start.coerceIn(0, amount.length)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (safeCursor > 0) {
                                            view.performVibrate(true)
                                            textFieldValue = textFieldValue.copy(selection = TextRange(safeCursor - 1))
                                        }
                                    },
                                    modifier = Modifier.size(28.dp),
                                    enabled = safeCursor > 0
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ChevronRight,
                                        contentDescription = "Move Cursor Left",
                                        tint = if (safeCursor > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp).rotate(180f)
                                    )
                                }

                                Text(
                                    text = "${safeCursor}/${amount.length}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (safeCursor < amount.length) {
                                            view.performVibrate(true)
                                            textFieldValue = textFieldValue.copy(selection = TextRange(safeCursor + 1))
                                        }
                                    },
                                    modifier = Modifier.size(28.dp),
                                    enabled = safeCursor < amount.length
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ChevronRight,
                                        contentDescription = "Move Cursor Right",
                                        tint = if (safeCursor < amount.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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
                }
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
                            imageVector = compose.icons.LucideIcons.ChevronDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(category.color.copy(alpha = 0.15f), CircleShape),
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocusChanged: (androidx.compose.ui.focus.FocusState) -> Unit = {}
) {
    val surface = MaterialTheme.colorScheme.surface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val onBg = MaterialTheme.colorScheme.onBackground
    val outline = MaterialTheme.colorScheme.outline
    val view = LocalView.current

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlinkDialog")
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

    val cursorColor = MaterialTheme.colorScheme.primary

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
            Box(modifier = Modifier.weight(1f)) {
                if (readOnly) {
                    val scrollState = rememberScrollState()
                    val currentText = value.text
                    val safeCursor = value.selection.start.coerceIn(0, currentText.length)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .pointerInput(currentText) {
                                detectTapGestures { offset ->
                                    val layout = textLayoutResult ?: return@detectTapGestures
                                    val clicked = layout.getOffsetForPosition(offset).coerceIn(0, currentText.length)
                                    view.performVibrate(true)
                                    onValueChange(value.copy(selection = TextRange(clicked)))
                                }
                            }
                            .pointerInput(currentText) {
                                var accumulated = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        val layout = textLayoutResult ?: return@detectHorizontalDragGestures
                                        val dragIdx = layout.getOffsetForPosition(offset).coerceIn(0, currentText.length)
                                        view.performVibrate(true)
                                        onValueChange(value.copy(selection = TextRange(dragIdx)))
                                        accumulated = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        accumulated += dragAmount
                                        if (kotlin.math.abs(accumulated) >= 18f) {
                                            val delta = (accumulated / 18f).toInt()
                                            if (delta != 0) {
                                                val newPos = (value.selection.start + delta).coerceIn(0, currentText.length)
                                                if (newPos != value.selection.start) {
                                                    view.performVibrate(true)
                                                    onValueChange(value.copy(selection = TextRange(newPos)))
                                                }
                                                accumulated %= 18f
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
                                        color = cursorColor.copy(alpha = cursorAlpha),
                                        start = Offset(cursorRect.left, cursorRect.top + 2.dp.toPx()),
                                        end = Offset(cursorRect.left, cursorRect.bottom - 2.dp.toPx()),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                } else if (currentText.isEmpty()) {
                                    drawLine(
                                        color = cursorColor.copy(alpha = cursorAlpha),
                                        start = Offset(0f, 2.dp.toPx()),
                                        end = Offset(0f, size.height - 2.dp.toPx()),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                    ) {
                        if (currentText.isEmpty()) {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyLarge,
                                color = outline
                            )
                        } else {
                            Text(
                                text = currentText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = onBg
                                ),
                                onTextLayout = { textLayoutResult = it }
                            )
                        }
                    }
                } else {
                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            onValueChange(newValue)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = onBg
                        ),
                        singleLine = true,
                        readOnly = false,
                        keyboardOptions = keyboardOptions.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged(onFocusChanged),
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
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

