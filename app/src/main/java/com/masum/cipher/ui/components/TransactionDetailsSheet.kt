package com.masum.cipher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.MathEvaluator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calculator
import compose.icons.lucideicons.ChevronDown
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsSheet(
    transaction: TransactionEntity,
    currencySymbol: String = "₹",
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
                        label = "MERCHANT"
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
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = selectedCategory.icon,
                                        contentDescription = null,
                                        tint = selectedCategory.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "CATEGORY",
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = Typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
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
                                                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
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
                    label = "NOTE (OPTIONAL)",
                    showClearButton = true
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
                text = "= $currencySymbol${String.format(locale, "%,.2f", computed)}",
                style = Typography.titleMedium,
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
                    text = if (showCalculator) "Use Keyboard" else "Use Calculator",
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
