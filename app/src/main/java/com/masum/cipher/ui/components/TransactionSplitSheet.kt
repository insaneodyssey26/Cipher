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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.SplitMode
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.util.SplitCalculator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Share2
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.X
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun TransactionSplitSheet(
    expenseName: String,
    totalAmount: Double,
    currencySymbol: String,
    initialParticipants: List<SplitParticipant>,
    isHapticsEnabled: Boolean,
    suggestedParticipants: List<String> = emptyList(),
    isStandaloneAdd: Boolean = false,
    onDismiss: () -> Unit,
    onDraftChange: ((List<SplitParticipant>) -> Unit)? = null,
    onDraftStandaloneChange: ((String, String, List<SplitParticipant>) -> Unit)? = null,
    onSaveSplits: ((List<SplitParticipant>) -> Unit)? = null,
    onSaveNewSplitExpense: ((String, Double, List<SplitParticipant>) -> Unit)? = null
) {
    val view = LocalView.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var standaloneExpenseName by remember { mutableStateOf(expenseName) }
    var standaloneTotalStr by remember {
        mutableStateOf(
            if (totalAmount > 0) {
                if (totalAmount % 1.0 == 0.0) totalAmount.toLong().toString()
                else String.format(Locale.US, "%.2f", totalAmount)
            } else ""
        )
    }

    val currentEffectiveTotal = if (isStandaloneAdd) {
        standaloneTotalStr.toDoubleOrNull() ?: 0.0
    } else {
        totalAmount
    }

    var splitMode by remember { mutableStateOf(SplitMode.EQUAL) }
    var participants by remember {
        mutableStateOf(
            initialParticipants.ifEmpty {
                listOf(
                    SplitParticipant(
                        name = "You",
                        isCurrentUser = true,
                        amount = currentEffectiveTotal,
                        percentage = 100.0
                    )
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

    fun recalculateSplits(currentList: List<SplitParticipant>, mode: SplitMode, total: Double): List<SplitParticipant> {
        return when (mode) {
            SplitMode.EQUAL -> SplitCalculator.calculateEqualSplits(total, currentList)
            SplitMode.PERCENTAGE -> SplitCalculator.calculatePercentageSplits(total, currentList)
            SplitMode.EXACT -> currentList
        }
    }

    fun notifyDraftChanged(updatedList: List<SplitParticipant>) {
        onDraftChange?.invoke(updatedList)
        if (isStandaloneAdd) {
            onDraftStandaloneChange?.invoke(standaloneExpenseName, standaloneTotalStr, updatedList)
        }
    }

    fun addParticipantWithName(rawName: String) {
        val trimmed = rawName.trim()
        if (trimmed.isNotBlank()) {
            view.performVibrate(isHapticsEnabled)
            val updated = participants + SplitParticipant(name = trimmed)
            val recalculated = recalculateSplits(updated, splitMode, currentEffectiveTotal)
            participants = recalculated
            syncInputMaps(recalculated)
            newPersonName = ""
            notifyDraftChanged(recalculated)
        }
    }

    val availableSuggestions = remember(suggestedParticipants, participants) {
        val currentNamesLower = participants.map { it.name.trim().lowercase(Locale.ROOT) }.toSet()
        suggestedParticipants.filter { suggestion ->
            val sLower = suggestion.trim().lowercase(Locale.ROOT)
            sLower.isNotBlank() && sLower != "you" && !currentNamesLower.contains(sLower)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onDismiss()
                }
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .heightIn(max = 680.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, White10),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
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
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = stringResource(R.string.split_title),
                                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isStandaloneAdd) {
                                        Text(
                                            text = "${currencySymbol}${String.format(Locale.US, "%.2f", totalAmount)} • ${expenseName.ifBlank { "Expense" }}",
                                            style = Typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (participants.size > 1 && currentEffectiveTotal > 0) {
                                    IconButton(
                                        onClick = {
                                            view.performVibrate(isHapticsEnabled)
                                            val currentExpenseTitle = if (isStandaloneAdd) standaloneExpenseName.ifBlank { "Expense" } else expenseName.ifBlank { "Expense" }
                                            val message = SplitCalculator.formatShareBreakdownMessage(
                                                expenseName = currentExpenseTitle,
                                                totalAmount = currentEffectiveTotal,
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

                                IconButton(
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled)
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.X,
                                        contentDescription = stringResource(R.string.action_close),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (isStandaloneAdd) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BasicTextField(
                                    value = standaloneExpenseName,
                                    onValueChange = {
                                        standaloneExpenseName = it
                                        onDraftStandaloneChange?.invoke(it, standaloneTotalStr, participants)
                                    },
                                    textStyle = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                                .border(1.dp, White10, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (standaloneExpenseName.isBlank()) {
                                                Text(
                                                    text = "Expense name",
                                                    style = Typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.weight(1.3f)
                                )

                                BasicTextField(
                                    value = standaloneTotalStr,
                                    onValueChange = { inputStr ->
                                        val sanitized = inputStr.filter { it.isDigit() || it == '.' }
                                        standaloneTotalStr = sanitized
                                        val parsed = sanitized.toDoubleOrNull() ?: 0.0
                                        val recalculated = recalculateSplits(participants, splitMode, parsed)
                                        participants = recalculated
                                        syncInputMaps(recalculated)
                                        onDraftStandaloneChange?.invoke(standaloneExpenseName, sanitized, recalculated)
                                    },
                                    textStyle = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                                .border(1.dp, White10, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = currencySymbol,
                                                style = Typography.bodyMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (standaloneTotalStr.isBlank()) {
                                                    Text(
                                                        text = "0.00",
                                                        style = Typography.bodyMedium.copy(fontFamily = Lato),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
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
                                .height(40.dp)
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
                                modes.forEachIndexed { _, (mode, label) ->
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
                                                val updated = recalculateSplits(participants, mode, currentEffectiveTotal)
                                                participants = updated
                                                syncInputMaps(updated)
                                                notifyDraftChanged(updated)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = Typography.labelMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            participants.forEachIndexed { index, participant ->
                                androidx.compose.runtime.key(participant.id) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                            .border(1.dp, White10, RoundedCornerShape(14.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                    .size(34.dp)
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
                                                                notifyDraftChanged(updated)
                                                            }
                                                            .padding(vertical = 2.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(15.dp)
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
                                                                    modifier = Modifier.size(9.dp)
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
                                                                percentage = if (currentEffectiveTotal > 0) (parsed / currentEffectiveTotal) * 100.0 else 0.0
                                                            )
                                                            participants = updated
                                                            percentageInputs = percentageInputs + (participant.id to String.format(Locale.US, "%.1f", updated[index].percentage))
                                                            notifyDraftChanged(updated)
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
                                                                    modifier = Modifier.widthIn(min = 55.dp, max = 80.dp),
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
                                                            val calculatedAmount = (currentEffectiveTotal * parsedPct) / 100.0
                                                            val updated = participants.toMutableList()
                                                            updated[index] = participant.copy(
                                                                amount = calculatedAmount,
                                                                percentage = parsedPct
                                                            )
                                                            participants = updated
                                                            exactInputs = exactInputs + (participant.id to String.format(Locale.US, "%.2f", calculatedAmount))
                                                            notifyDraftChanged(updated)
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
                                                                    modifier = Modifier.widthIn(min = 40.dp, max = 60.dp),
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
                                                        val recalculated = recalculateSplits(updated, splitMode, currentEffectiveTotal)
                                                        participants = recalculated
                                                        syncInputMaps(recalculated)
                                                        notifyDraftChanged(recalculated)
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
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
                                            addParticipantWithName(newPersonName)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
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
                                        addParticipantWithName(newPersonName)
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

                            if (availableSuggestions.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(availableSuggestions, key = { it }) { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .border(1.dp, White10, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    addParticipantWithName(suggestion)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.Plus,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = suggestion,
                                                style = Typography.labelMedium.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.TopCenter)
                                .graphicsLayer { alpha = if (scrollState.canScrollBackward) 1f else 0f }
                                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Color.Transparent)))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.BottomCenter)
                                .graphicsLayer { alpha = if (scrollState.canScrollForward) 1f else 0f }
                                .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (splitMode == SplitMode.EXACT) {
                            val currentExactSum = participants.sumOf { it.amount }
                            val exactRemaining = currentEffectiveTotal - currentExactSum
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
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isExactBalanced) "Balanced"
                                        else if (exactRemaining > 0) "Remaining: ${currencySymbol}${String.format(Locale.US, "%.2f", exactRemaining)}"
                                        else "Exceeded by: ${currencySymbol}${String.format(Locale.US, "%.2f", abs(exactRemaining))}",
                                        style = Typography.titleSmall.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                                        color = if (isExactBalanced) EmeraldIncome else if (exactRemaining > 0) MaterialTheme.colorScheme.onSurface else RoseExpense
                                    )
                                    Text(
                                        text = "Total: ${currencySymbol}${String.format(Locale.US, "%.2f", currentExactSum)} of ${currencySymbol}${String.format(Locale.US, "%.2f", currentEffectiveTotal)}",
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (!isExactBalanced && participants.any { it.isCurrentUser }) {
                                    TextButton(
                                        onClick = {
                                            view.performVibrate(isHapticsEnabled)
                                            val otherSum = participants.filter { !it.isCurrentUser }.sumOf { it.amount }
                                            val myShare = (currentEffectiveTotal - otherSum).coerceAtLeast(0.0)
                                            val updated = participants.map {
                                                if (it.isCurrentUser) it.copy(
                                                    amount = myShare,
                                                    percentage = if (currentEffectiveTotal > 0) (myShare / currentEffectiveTotal) * 100.0 else 0.0
                                                ) else it
                                            }
                                            participants = updated
                                            syncInputMaps(updated)
                                            notifyDraftChanged(updated)
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
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isPctBalanced) "Balanced (100%)"
                                        else if (pctRemaining > 0) "Remaining: ${String.format(Locale.US, "%.1f", pctRemaining)}%"
                                        else "Exceeded by: ${String.format(Locale.US, "%.1f", abs(pctRemaining))}%",
                                        style = Typography.titleSmall.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
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
                                                    val share = ((currentEffectiveTotal * myPct) / 100.0 * 100.0).roundToLong() / 100.0
                                                    it.copy(percentage = myPct, amount = share)
                                                } else it
                                            }
                                            participants = updated
                                            syncInputMaps(updated)
                                            notifyDraftChanged(updated)
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
                            if (!isStandaloneAdd && (participants.size > 1 || initialParticipants.isNotEmpty())) {
                                Button(
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled)
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onSaveSplits?.invoke(emptyList())
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = RoseExpense
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
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
                                        currentList = recalculateSplits(updated, splitMode, currentEffectiveTotal)
                                        participants = currentList
                                        newPersonName = ""
                                    }

                                    fun saveAndDismiss(finalSplits: List<SplitParticipant>) {
                                        if (isStandaloneAdd) {
                                            val finalExpenseName = standaloneExpenseName.trim().ifBlank { "Expense" }
                                            onSaveNewSplitExpense?.invoke(finalExpenseName, currentEffectiveTotal, finalSplits)
                                        } else {
                                            onSaveSplits?.invoke(finalSplits)
                                        }
                                        onDismiss()
                                    }

                                    when (splitMode) {
                                        SplitMode.EQUAL -> {
                                            val toSave = if (currentList.size > 1) currentList else emptyList()
                                            saveAndDismiss(toSave)
                                        }
                                        SplitMode.EXACT -> {
                                            val sum = currentList.sumOf { it.amount }
                                            val diff = currentEffectiveTotal - sum
                                            if (abs(diff) > 0.05) {
                                                if (currentList.any { it.isCurrentUser }) {
                                                    val otherSum = currentList.filter { !it.isCurrentUser }.sumOf { it.amount }
                                                    val myShare = (currentEffectiveTotal - otherSum).coerceAtLeast(0.0)
                                                    val adjusted = currentList.map {
                                                        if (it.isCurrentUser) it.copy(
                                                            amount = myShare,
                                                            percentage = if (currentEffectiveTotal > 0) (myShare / currentEffectiveTotal) * 100.0 else 0.0
                                                        ) else it
                                                    }
                                                    val toSave = if (adjusted.size > 1) adjusted else emptyList()
                                                    saveAndDismiss(toSave)
                                                } else {
                                                    errorMessage = "Total split (${currencySymbol}${String.format(Locale.US, "%.2f", sum)}) must equal transaction amount (${currencySymbol}${String.format(Locale.US, "%.2f", currentEffectiveTotal)})"
                                                }
                                            } else {
                                                val toSave = if (currentList.size > 1) currentList else emptyList()
                                                saveAndDismiss(toSave)
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
                                                            val share = ((currentEffectiveTotal * myPct) / 100.0 * 100.0).roundToLong() / 100.0
                                                            it.copy(percentage = myPct, amount = share)
                                                        } else it
                                                    }
                                                    val toSave = if (adjusted.size > 1) adjusted else emptyList()
                                                    saveAndDismiss(toSave)
                                                } else {
                                                    errorMessage = "Total percentages must equal 100%"
                                                }
                                            } else {
                                                val toSave = if (currentList.size > 1) currentList else emptyList()
                                                saveAndDismiss(toSave)
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
                                    .height(48.dp)
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
        }
    }
}

