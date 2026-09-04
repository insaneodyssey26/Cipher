package com.masum.cipher.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.SplitMode
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.util.SplitCalculator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Share2
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.X
import java.util.Locale
import kotlin.math.abs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.TextButton
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSplitSheet(
    expenseName: String,
    totalAmount: Double,
    currencySymbol: String,
    initialParticipants: List<SplitParticipant>,
    isHapticsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSaveSplits: (List<SplitParticipant>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    var splitMode by remember { mutableStateOf(SplitMode.EQUAL) }
    var participants by remember {
        mutableStateOf(
            if (initialParticipants.isNotEmpty()) {
                initialParticipants
            } else {
                listOf(
                    SplitParticipant(name = "You", isCurrentUser = true, amount = totalAmount, percentage = 100.0)
                )
            }
        )
    }

    var exactInputs by remember {
        mutableStateOf(
            participants.associate { p ->
                p.id to if (p.amount == 0.0) "" else if (p.amount % 1.0 == 0.0) p.amount.toLong().toString() else String.format(Locale.US, "%.2f", p.amount)
            }
        )
    }

    var percentageInputs by remember {
        mutableStateOf(
            participants.associate { p ->
                p.id to if (p.percentage == 0.0) "" else if (p.percentage % 1.0 == 0.0) p.percentage.toLong().toString() else String.format(Locale.US, "%.1f", p.percentage)
            }
        )
    }

    fun syncInputMaps(currentList: List<SplitParticipant>) {
        exactInputs = currentList.associate { p ->
            p.id to if (p.amount == 0.0) "" else if (p.amount % 1.0 == 0.0) p.amount.toLong().toString() else String.format(Locale.US, "%.2f", p.amount)
        }
        percentageInputs = currentList.associate { p ->
            p.id to if (p.percentage == 0.0) "" else if (p.percentage % 1.0 == 0.0) p.percentage.toLong().toString() else String.format(Locale.US, "%.1f", p.percentage)
        }
    }

    var newPersonName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun recalculateSplits(currentList: List<SplitParticipant>, mode: SplitMode): List<SplitParticipant> {
        return when (mode) {
            SplitMode.EQUAL -> SplitCalculator.calculateEqualSplits(totalAmount, currentList)
            SplitMode.PERCENTAGE -> SplitCalculator.calculatePercentageSplits(totalAmount, currentList)
            SplitMode.EXACT -> currentList
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Users,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.split_title),
                            style = Typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currencySymbol}${String.format(Locale.US, "%.2f", totalAmount)} • ${expenseName.ifBlank { "Expense" }}",
                            style = Typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (participants.size > 1) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            val message = SplitCalculator.formatShareBreakdownMessage(
                                expenseName = expenseName,
                                totalAmount = totalAmount,
                                currencySymbol = currencySymbol,
                                participants = participants
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Split Breakdown"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Share2,
                            contentDescription = stringResource(R.string.split_share_whatsapp),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            val modes = listOf(
                SplitMode.EQUAL to stringResource(R.string.split_mode_equal),
                SplitMode.EXACT to stringResource(R.string.split_mode_exact),
                SplitMode.PERCENTAGE to stringResource(R.string.split_mode_percentage)
            )
            val selectedModeIndex = modes.indexOfFirst { it.first == splitMode }.coerceAtLeast(0)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, White10, RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                val tabWidth = maxWidth / modes.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedModeIndex,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
                    label = "split_mode_offset"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    modes.forEachIndexed { index, (mode, label) ->
                        val isSelected = splitMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    view.performVibrate(isHapticsEnabled)
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    splitMode = mode
                                    errorMessage = null
                                    val updated = recalculateSplits(participants, mode)
                                    participants = updated
                                    syncInputMaps(updated)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = Typography.labelMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                participants.forEachIndexed { index, participant ->
                    androidx.compose.runtime.key(participant.id) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, White10, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (participant.isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = participant.name.take(1).uppercase(Locale.ROOT),
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (participant.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = if (participant.isCurrentUser) stringResource(R.string.split_you) else participant.name,
                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (!participant.isCurrentUser) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    view.performVibrate(isHapticsEnabled)
                                                    val updated = participants.toMutableList()
                                                    updated[index] = participant.copy(isPaid = !participant.isPaid)
                                                    participants = updated
                                                }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (participant.isPaid) EmeraldIncome else Color.Transparent
                                                    )
                                                    .border(
                                                        width = if (participant.isPaid) 0.dp else 1.2.dp,
                                                        color = if (participant.isPaid) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (participant.isPaid) {
                                                    Icon(
                                                        imageVector = LucideIcons.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(2.dp))
                                            Text(
                                                text = if (participant.isPaid) stringResource(R.string.split_settled) else stringResource(R.string.split_pending),
                                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                                color = if (participant.isPaid) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (splitMode) {
                                    SplitMode.EQUAL -> {
                                        Text(
                                            text = "${currencySymbol}${String.format(Locale.US, "%.2f", participant.amount)}",
                                            style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    SplitMode.EXACT -> {
                                        BasicTextField(
                                            value = exactInputs[participant.id] ?: String.format(Locale.US, "%.2f", participant.amount),
                                            onValueChange = { inputStr ->
                                                val sanitized = inputStr.filter { it.isDigit() || it == '.' }
                                                exactInputs = exactInputs + (participant.id to sanitized)
                                                val parsed = sanitized.toDoubleOrNull() ?: 0.0
                                                val updated = participants.toMutableList()
                                                updated[index] = participant.copy(
                                                    amount = parsed,
                                                    percentage = if (totalAmount > 0) (parsed / totalAmount) * 100.0 else 0.0
                                                )
                                                participants = updated
                                                percentageInputs = percentageInputs + (participant.id to String.format(Locale.US, "%.1f", updated[index].percentage))
                                            },
                                            textStyle = Typography.titleMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.End
                                            ),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            decorationBox = { innerTextField ->
                                                Row(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                                        .border(1.dp, White10, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = currencySymbol,
                                                        style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier.widthIn(min = 60.dp, max = 90.dp),
                                                        contentAlignment = Alignment.CenterEnd
                                                    ) {
                                                        innerTextField()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    SplitMode.PERCENTAGE -> {
                                        BasicTextField(
                                            value = percentageInputs[participant.id] ?: String.format(Locale.US, "%.1f", participant.percentage),
                                            onValueChange = { inputStr ->
                                                val sanitized = inputStr.filter { it.isDigit() || it == '.' }
                                                percentageInputs = percentageInputs + (participant.id to sanitized)
                                                val parsedPct = sanitized.toDoubleOrNull() ?: 0.0
                                                val calculatedAmount = (totalAmount * parsedPct) / 100.0
                                                val updated = participants.toMutableList()
                                                updated[index] = participant.copy(
                                                    amount = calculatedAmount,
                                                    percentage = parsedPct
                                                )
                                                participants = updated
                                                exactInputs = exactInputs + (participant.id to String.format(Locale.US, "%.2f", calculatedAmount))
                                            },
                                            textStyle = Typography.titleMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.End
                                            ),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            decorationBox = { innerTextField ->
                                                Row(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                                        .border(1.dp, White10, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier.widthIn(min = 45.dp, max = 65.dp),
                                                        contentAlignment = Alignment.CenterEnd
                                                    ) {
                                                        innerTextField()
                                                    }
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "%",
                                                        style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }

                                if (!participant.isCurrentUser && participants.size > 1) {
                                    IconButton(
                                        onClick = {
                                            view.performVibrate(isHapticsEnabled)
                                            val updated = participants.filter { it.id != participant.id }
                                            val recalculated = recalculateSplits(updated, splitMode)
                                            participants = recalculated
                                            syncInputMaps(recalculated)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.X,
                                            contentDescription = "Remove",
                                            tint = RoseExpense.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newPersonName,
                    onValueChange = { newPersonName = it },
                    textStyle = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newPersonName.isNotBlank()) {
                                view.performVibrate(isHapticsEnabled)
                                val updated = participants + SplitParticipant(name = newPersonName.trim())
                                val recalculated = recalculateSplits(updated, splitMode)
                                participants = recalculated
                                syncInputMaps(recalculated)
                                newPersonName = ""
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (newPersonName.isBlank()) {
                            Text(
                                text = stringResource(R.string.split_person_name_hint),
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            view.performVibrate(isHapticsEnabled)
                            val updated = participants + SplitParticipant(name = newPersonName.trim())
                            val recalculated = recalculateSplits(updated, splitMode)
                            participants = recalculated
                            syncInputMaps(recalculated)
                            newPersonName = ""
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                    enabled = newPersonName.isNotBlank()
                ) {
                    Icon(
                        imageVector = LucideIcons.Plus,
                        contentDescription = stringResource(R.string.split_add_person),
                        tint = if (newPersonName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (splitMode == SplitMode.EXACT) {
                val currentExactSum = participants.sumOf { it.amount }
                val exactRemaining = totalAmount - currentExactSum
                val isExactBalanced = abs(exactRemaining) < 0.02

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isExactBalanced) EmeraldIncome.copy(alpha = 0.35f)
                            else if (exactRemaining > 0) White10
                            else RoseExpense.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isExactBalanced) "Balanced"
                                   else if (exactRemaining > 0) "Remaining: ${currencySymbol}${String.format(Locale.US, "%.2f", exactRemaining)}"
                                   else "Exceeded by: ${currencySymbol}${String.format(Locale.US, "%.2f", abs(exactRemaining))}",
                            style = Typography.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
                            color = if (isExactBalanced) EmeraldIncome else if (exactRemaining > 0) MaterialTheme.colorScheme.onSurface else RoseExpense
                        )
                        Text(
                            text = "Total: ${currencySymbol}${String.format(Locale.US, "%.2f", currentExactSum)} of ${currencySymbol}${String.format(Locale.US, "%.2f", totalAmount)}",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isExactBalanced && participants.any { it.isCurrentUser }) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val otherSum = participants.filter { !it.isCurrentUser }.sumOf { it.amount }
                                val myShare = (totalAmount - otherSum).coerceAtLeast(0.0)
                                val updated = participants.map {
                                    if (it.isCurrentUser) it.copy(
                                        amount = myShare,
                                        percentage = if (totalAmount > 0) (myShare / totalAmount) * 100.0 else 0.0
                                    ) else it
                                }
                                participants = updated
                                syncInputMaps(updated)
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Auto-balance",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else if (splitMode == SplitMode.PERCENTAGE) {
                val currentPctSum = participants.sumOf { it.percentage }
                val pctRemaining = 100.0 - currentPctSum
                val isPctBalanced = abs(pctRemaining) < 0.2

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isPctBalanced) EmeraldIncome.copy(alpha = 0.35f)
                            else if (pctRemaining > 0) White10
                            else RoseExpense.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPctBalanced) "Balanced (100%)"
                                   else if (pctRemaining > 0) "Remaining: ${String.format(Locale.US, "%.1f", pctRemaining)}%"
                                   else "Exceeded by: ${String.format(Locale.US, "%.1f", abs(pctRemaining))}%",
                            style = Typography.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
                            color = if (isPctBalanced) EmeraldIncome else if (pctRemaining > 0) MaterialTheme.colorScheme.onSurface else RoseExpense
                        )
                        Text(
                            text = "Allocated: ${String.format(Locale.US, "%.1f", currentPctSum)}% of 100%",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isPctBalanced && participants.any { it.isCurrentUser }) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val otherPct = participants.filter { !it.isCurrentUser }.sumOf { it.percentage }
                                val myPct = (100.0 - otherPct).coerceAtLeast(0.0)
                                val updated = participants.map {
                                    if (it.isCurrentUser) {
                                        val share = ((totalAmount * myPct) / 100.0 * 100.0).roundToLong() / 100.0
                                        it.copy(percentage = myPct, amount = share)
                                    } else it
                                }
                                participants = updated
                                syncInputMaps(updated)
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Auto-balance",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = Typography.labelMedium,
                    color = RoseExpense,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (participants.size > 1 || initialParticipants.isNotEmpty()) {
                    Button(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSaveSplits(emptyList())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = RoseExpense
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.split_clear),
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        var currentList = participants
                        if (newPersonName.isNotBlank()) {
                            val updated = participants + SplitParticipant(name = newPersonName.trim())
                            currentList = recalculateSplits(updated, splitMode)
                            participants = currentList
                            newPersonName = ""
                        }
                        when (splitMode) {
                            SplitMode.EQUAL -> {
                                val toSave = if (currentList.size > 1) currentList else emptyList()
                                onSaveSplits(toSave)
                                onDismiss()
                            }
                            SplitMode.EXACT -> {
                                val sum = currentList.sumOf { it.amount }
                                val diff = totalAmount - sum
                                if (abs(diff) > 0.05) {
                                    if (currentList.any { it.isCurrentUser }) {
                                        val otherSum = currentList.filter { !it.isCurrentUser }.sumOf { it.amount }
                                        val myShare = (totalAmount - otherSum).coerceAtLeast(0.0)
                                        val adjusted = currentList.map {
                                            if (it.isCurrentUser) it.copy(
                                                amount = myShare,
                                                percentage = if (totalAmount > 0) (myShare / totalAmount) * 100.0 else 0.0
                                            ) else it
                                        }
                                        val toSave = if (adjusted.size > 1) adjusted else emptyList()
                                        onSaveSplits(toSave)
                                        onDismiss()
                                    } else {
                                        errorMessage = "Total split (${currencySymbol}${String.format(Locale.US, "%.2f", sum)}) must equal transaction amount (${currencySymbol}${String.format(Locale.US, "%.2f", totalAmount)})"
                                    }
                                } else {
                                    val toSave = if (currentList.size > 1) currentList else emptyList()
                                    onSaveSplits(toSave)
                                    onDismiss()
                                }
                            }
                            SplitMode.PERCENTAGE -> {
                                val sumPct = currentList.sumOf { it.percentage }
                                val diffPct = 100.0 - sumPct
                                if (abs(diffPct) > 0.5) {
                                    if (currentList.any { it.isCurrentUser }) {
                                        val otherPct = currentList.filter { !it.isCurrentUser }.sumOf { it.percentage }
                                        val myPct = (100.0 - otherPct).coerceAtLeast(0.0)
                                        val adjusted = currentList.map {
                                            if (it.isCurrentUser) {
                                                val share = ((totalAmount * myPct) / 100.0 * 100.0).roundToLong() / 100.0
                                                it.copy(percentage = myPct, amount = share)
                                            } else it
                                        }
                                        val toSave = if (adjusted.size > 1) adjusted else emptyList()
                                        onSaveSplits(toSave)
                                        onDismiss()
                                    } else {
                                        errorMessage = "Total percentages must equal 100%"
                                    }
                                } else {
                                    val toSave = if (currentList.size > 1) currentList else emptyList()
                                    onSaveSplits(toSave)
                                    onDismiss()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.split_save),
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
