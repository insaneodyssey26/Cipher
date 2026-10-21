package com.masum.cipher.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.SplitMode
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.util.SplitCalculator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Share2
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.X
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val consumeOverscrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f) return Offset(0f, available.y)
                return Offset.Zero
            }
            override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y < 0f) return Velocity(0f, available.y)
                return Velocity.Zero
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 38.dp, height = 4.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .imeNestedScroll()
                .nestedScroll(consumeOverscrollConnection)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
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
                            style = Typography.titleLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isStandaloneAdd) {
                            Text(
                                text = "${currencySymbol}${String.format(Locale.US, "%.2f", totalAmount)} • ${expenseName.ifBlank { "Expense" }}",
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (participants.size > 1 && currentEffectiveTotal > 0) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
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
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Share2,
                                contentDescription = stringResource(R.string.split_share_whatsapp),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                view.performVibrate(isHapticsEnabled)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
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
                        textStyle = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                if (standaloneExpenseName.isBlank()) {
                                    Text(
                                        text = "Expense description",
                                        style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
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
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currencySymbol,
                                    style = Typography.bodyMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (standaloneTotalStr.isBlank()) {
                                        Text(
                                            text = "0.00",
                                            style = Typography.bodyMedium.copy(fontFamily = Lato),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(3.dp)
            ) {
                val tabWidth = maxWidth / modes.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedModeIndex,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f),
                    label = "modeIndicator"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(17.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    modes.forEachIndexed { _, (mode, label) ->
                        val isSelected = splitMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(17.dp))
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(vertical = 4.dp)
            ) {
                participants.forEachIndexed { index, participant ->
                    key(participant.id) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(220)) + expandVertically(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                            ),
                            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                        thickness = 1.dp
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (participant.isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = participant.name.take(1).uppercase(Locale.ROOT),
                                            style = Typography.labelMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                            color = if (participant.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (participant.isCurrentUser) stringResource(R.string.split_you) else participant.name,
                                            style = Typography.titleSmall.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (!participant.isCurrentUser) {
                                            Surface(
                                                onClick = {
                                                    view.performVibrate(isHapticsEnabled)
                                                    val isNowPaid = !participant.isPaid
                                                    val updated = participants.mapIndexed { i, p ->
                                                        if (i == index) p.copy(isPaid = isNowPaid) else p
                                                    }
                                                    participants = updated
                                                    notifyDraftChanged(updated)
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (participant.isPaid) EmeraldIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (participant.isPaid) EmeraldIncome.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (participant.isPaid) EmeraldIncome
                                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                                            )
                                                    )
                                                    Text(
                                                        text = if (participant.isPaid) stringResource(R.string.split_settled) else stringResource(R.string.split_pending),
                                                        style = Typography.labelSmall.copy(
                                                            fontFamily = Lato,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (participant.isPaid) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        when (splitMode) {
                                            SplitMode.EQUAL -> {
                                                AnimatedContent(
                                                    targetState = participant.amount,
                                                    transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                                                    label = "equalAmountAnim"
                                                ) { animatedAmount ->
                                                    Text(
                                                        text = "${currencySymbol}${String.format(Locale.US, "%.2f", animatedAmount)}",
                                                        style = Typography.titleSmall.copy(
                                                            fontFamily = DMSans,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            SplitMode.EXACT -> {
                                                BasicTextField(
                                                    value = exactInputs[participant.id] ?: "",
                                                    onValueChange = { str ->
                                                        val sanitized = str.filter { it.isDigit() || it == '.' }
                                                        exactInputs = exactInputs + (participant.id to sanitized)
                                                        val parsed = sanitized.toDoubleOrNull() ?: 0.0
                                                        val updated = participants.mapIndexed { i, p ->
                                                            if (i == index) p.copy(
                                                                amount = parsed,
                                                                percentage = if (currentEffectiveTotal > 0) (parsed / currentEffectiveTotal) * 100.0 else 0.0
                                                            ) else p
                                                        }
                                                        participants = updated
                                                        notifyDraftChanged(updated)
                                                    },
                                                    textStyle = Typography.bodyMedium.copy(
                                                        fontFamily = DMSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        textAlign = TextAlign.End,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                                                    decorationBox = { innerTextField ->
                                                        Row(
                                                            modifier = Modifier
                                                                .width(92.dp)
                                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            Text(
                                                                text = currencySymbol,
                                                                style = Typography.labelSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(Modifier.width(2.dp))
                                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                                                if (exactInputs[participant.id].isNullOrBlank()) {
                                                                    Text(
                                                                        text = "0.00",
                                                                        style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp),
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            SplitMode.PERCENTAGE -> {
                                                BasicTextField(
                                                    value = percentageInputs[participant.id] ?: "",
                                                    onValueChange = { str ->
                                                        val sanitized = str.filter { it.isDigit() || it == '.' }
                                                        percentageInputs = percentageInputs + (participant.id to sanitized)
                                                        val parsedPct = sanitized.toDoubleOrNull() ?: 0.0
                                                        val parsedAmt = (currentEffectiveTotal * parsedPct) / 100.0
                                                        val updated = participants.mapIndexed { i, p ->
                                                            if (i == index) p.copy(percentage = parsedPct, amount = parsedAmt)
                                                            else p
                                                        }
                                                        participants = updated
                                                        notifyDraftChanged(updated)
                                                    },
                                                    textStyle = Typography.bodyMedium.copy(
                                                        fontFamily = DMSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        textAlign = TextAlign.End,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                                                    decorationBox = { innerTextField ->
                                                        Row(
                                                            modifier = Modifier
                                                                .width(76.dp)
                                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                                                if (percentageInputs[participant.id].isNullOrBlank()) {
                                                                    Text(
                                                                        text = "0",
                                                                        style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp),
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }
                                                            Spacer(Modifier.width(2.dp))
                                                            Text(
                                                                text = "%",
                                                                style = Typography.labelSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        if (!participant.isCurrentUser) {
                                            IconButton(
                                                onClick = {
                                                    view.performVibrate(isHapticsEnabled)
                                                    val updated = participants.filterIndexed { i, _ -> i != index }
                                                    val recalculated = recalculateSplits(updated, splitMode, currentEffectiveTotal)
                                                    participants = recalculated
                                                    syncInputMaps(recalculated)
                                                    notifyDraftChanged(updated)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.X,
                                                    contentDescription = stringResource(R.string.action_delete),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = newPersonName,
                        onValueChange = { newPersonName = it },
                        textStyle = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            addParticipantWithName(newPersonName)
                            focusManager.clearFocus()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                if (newPersonName.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.split_person_name_hint),
                                        style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        onClick = { addParticipantWithName(newPersonName) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = LucideIcons.Plus,
                                contentDescription = stringResource(R.string.split_add_person),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (availableSuggestions.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSuggestions) { name ->
                            Surface(
                                onClick = { addParticipantWithName(name) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Plus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = name,
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (splitMode == SplitMode.EXACT) {
                val currentSum = participants.sumOf { it.amount }
                val remaining = currentEffectiveTotal - currentSum
                val isBalanced = abs(remaining) < 0.01

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (isBalanced) EmeraldIncome.copy(alpha = 0.35f)
                            else if (remaining > 0) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            else RoseExpense.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBalanced) "Balanced"
                            else if (remaining > 0) "Remaining: ${currencySymbol}${String.format(Locale.US, "%.2f", remaining)}"
                            else "Exceeded by: ${currencySymbol}${String.format(Locale.US, "%.2f", abs(remaining))}",
                            style = Typography.titleSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = if (isBalanced) EmeraldIncome else if (remaining > 0) MaterialTheme.colorScheme.onSurface else RoseExpense
                        )
                        Text(
                            text = "Total: ${currencySymbol}${String.format(Locale.US, "%.2f", currentSum)} of ${currencySymbol}${String.format(Locale.US, "%.2f", currentEffectiveTotal)}",
                            style = Typography.labelSmall.copy(fontFamily = Lato, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isBalanced && remaining > 0 && participants.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val perPersonExtra = remaining / participants.size
                                val updated = participants.map { p ->
                                    val newAmt = p.amount + perPersonExtra
                                    p.copy(amount = newAmt, percentage = if (currentEffectiveTotal > 0) (newAmt / currentEffectiveTotal) * 100.0 else 0.0)
                                }
                                participants = updated
                                syncInputMaps(updated)
                                notifyDraftChanged(updated)
                            }
                        ) {
                            Text(
                                text = "Auto-balance",
                                style = Typography.labelMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else if (splitMode == SplitMode.PERCENTAGE) {
                val currentPctSum = participants.sumOf { it.percentage }
                val pctRemaining = 100.0 - currentPctSum
                val isPctBalanced = abs(pctRemaining) < 0.1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (isPctBalanced) EmeraldIncome.copy(alpha = 0.35f)
                            else if (pctRemaining > 0) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            else RoseExpense.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPctBalanced) "Balanced (100%)"
                            else if (pctRemaining > 0) "Remaining: ${String.format(Locale.US, "%.1f", pctRemaining)}%"
                            else "Exceeded by: ${String.format(Locale.US, "%.1f", abs(pctRemaining))}%",
                            style = Typography.titleSmall.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = if (isPctBalanced) EmeraldIncome else if (pctRemaining > 0) MaterialTheme.colorScheme.onSurface else RoseExpense
                        )
                        Text(
                            text = "Allocated: ${String.format(Locale.US, "%.1f", currentPctSum)}% of 100%",
                            style = Typography.labelSmall.copy(fontFamily = Lato, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isPctBalanced && pctRemaining > 0 && participants.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val perPersonExtraPct = pctRemaining / participants.size
                                val updated = participants.map { p ->
                                    val newPct = p.percentage + perPersonExtraPct
                                    val newAmt = (currentEffectiveTotal * newPct) / 100.0
                                    p.copy(percentage = newPct, amount = newAmt)
                                }
                                participants = updated
                                syncInputMaps(updated)
                                notifyDraftChanged(updated)
                            }
                        ) {
                            Text(
                                text = "Auto-balance",
                                style = Typography.labelMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = Typography.labelMedium.copy(fontFamily = Lato),
                    color = RoseExpense,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isStandaloneAdd && (participants.size > 1 || initialParticipants.isNotEmpty())) {
                    Button(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            val single = listOf(
                                SplitParticipant(
                                    name = "You",
                                    isCurrentUser = true,
                                    amount = currentEffectiveTotal,
                                    percentage = 100.0
                                )
                            )
                            participants = single
                            onSaveSplits?.invoke(emptyList())
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = stringResource(R.string.split_clear),
                            style = Typography.labelLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        if (isStandaloneAdd) {
                            val validTotal = standaloneTotalStr.toDoubleOrNull() ?: 0.0
                            if (validTotal <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }
                            if (standaloneExpenseName.isBlank()) {
                                errorMessage = "Please enter an expense title"
                                return@Button
                            }
                        }

                        when (splitMode) {
                            SplitMode.EXACT -> {
                                val sum = participants.sumOf { it.amount }
                                if (abs(sum - currentEffectiveTotal) > 0.01) {
                                    errorMessage = "Total split amounts must equal ${currencySymbol}${String.format(Locale.US, "%.2f", currentEffectiveTotal)}"
                                    return@Button
                                }
                            }
                            SplitMode.PERCENTAGE -> {
                                val sumPct = participants.sumOf { it.percentage }
                                if (abs(sumPct - 100.0) > 0.1) {
                                    errorMessage = "Total split percentages must equal 100%"
                                    return@Button
                                }
                            }
                            SplitMode.EQUAL -> {
                                participants = recalculateSplits(participants, SplitMode.EQUAL, currentEffectiveTotal)
                            }
                        }

                        if (isStandaloneAdd) {
                            val validTotal = standaloneTotalStr.toDoubleOrNull() ?: 0.0
                            onSaveNewSplitExpense?.invoke(standaloneExpenseName, validTotal, participants)
                        } else {
                            onSaveSplits?.invoke(participants)
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = stringResource(R.string.split_save),
                        style = Typography.labelLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}
