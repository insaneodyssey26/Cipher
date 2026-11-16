package com.masum.cipher.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import com.masum.cipher.ui.components.FormTextRow
import com.masum.cipher.ui.components.FormSelectableChip
import com.masum.cipher.ui.components.FormPrimaryButton
import com.masum.cipher.ui.components.FormGroup
import com.masum.cipher.ui.components.FormEyebrow
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check

data class DashboardFilter(
    val type: DashboardContract.FilterType = DashboardContract.FilterType.ALL,
    val selectedCategories: Set<String> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    val isActive: Boolean
        get() = type != DashboardContract.FilterType.ALL || selectedCategories.isNotEmpty() || minAmount != null || maxAmount != null

}

private data class QuickAmountRange(
    val label: String,
    val min: Double?,
    val max: Double?
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardFilterSheet(
    currentFilter: DashboardFilter,
    customCategories: List<CustomCategoryEntity> = emptyList(),
    currencySymbol: String = "₹",
    onApplyFilter: (DashboardFilter) -> Unit,
    onDismiss: () -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeWithAnimation: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    var draftType by remember { mutableStateOf(currentFilter.type) }
    var draftCategories by remember { mutableStateOf(currentFilter.selectedCategories) }
    var minInput by remember { mutableStateOf(currentFilter.minAmount?.toInt()?.toString() ?: "") }
    var maxInput by remember { mutableStateOf(currentFilter.maxAmount?.toInt()?.toString() ?: "") }

    val anyLabel = stringResource(R.string.any)
    val quickRanges = remember(currencySymbol, anyLabel) {
        listOf(
            QuickAmountRange(anyLabel, null, null),
            QuickAmountRange("< ${currencySymbol}500", null, 500.0),
            QuickAmountRange("${currencySymbol}500 - ${currencySymbol}2k", 500.0, 2000.0),
            QuickAmountRange("${currencySymbol}2k - ${currencySymbol}10k", 2000.0, 10000.0),
            QuickAmountRange("> ${currencySymbol}10k", 10000.0, null)
        )
    }

    val draftIsActive = remember(draftType, draftCategories, minInput, maxInput) {
        draftType != DashboardContract.FilterType.ALL ||
                draftCategories.isNotEmpty() ||
                minInput.toDoubleOrNull() != null ||
                maxInput.toDoubleOrNull() != null
    }

    val dm = com.masum.cipher.ui.theme.DMSans
    val accent = MaterialTheme.colorScheme.primary
    val allCategories = remember(customCategories) {
        CategoryHelper.getAllCategories(customCategories, includeIncome = true)
    }

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.filter_title),
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.5).sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (draftIsActive) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    draftType = DashboardContract.FilterType.ALL
                                    draftCategories = emptySet()
                                    minInput = ""
                                    maxInput = ""
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormEyebrow(text = stringResource(R.string.tx_type))
                    val typeOptions = listOf(
                        DashboardContract.FilterType.ALL to stringResource(R.string.all),
                        DashboardContract.FilterType.EXPENSE to stringResource(R.string.expenses),
                        DashboardContract.FilterType.INCOME to stringResource(R.string.income)
                    )
                    val selectedIdx = typeOptions.indexOfFirst { it.first == draftType }.coerceAtLeast(0)
                    val typeAccent = when (draftType) {
                        DashboardContract.FilterType.EXPENSE -> com.masum.cipher.ui.theme.RoseExpense
                        DashboardContract.FilterType.INCOME -> com.masum.cipher.ui.theme.EmeraldIncome
                        else -> accent
                    }
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .padding(3.dp)
                    ) {
                        val segment = maxWidth / typeOptions.size
                        val offsetX by animateDpAsState(
                            targetValue = segment * selectedIdx,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                            label = "type_segment"
                        )
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToPx(), 0) }
                                .width(segment)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, typeAccent.copy(alpha = 0.6f), RoundedCornerShape(50))
                        )
                        Row(modifier = Modifier.fillMaxSize()) {
                            typeOptions.forEach { (type, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            draftType = type
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = dm,
                                            fontWeight = if (draftType == type) FontWeight.SemiBold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = if (draftType == type) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormEyebrow(text = "${stringResource(R.string.categories_header)} · ${draftCategories.size}")
                        if (draftCategories.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.action_cancel),
                                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                                color = accent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        draftCategories = emptySet()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allCategories.chunked(4).forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    val isSelected = draftCategories.contains(cat.name) || draftCategories.contains(cat.displayName)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (isSelected) cat.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .then(if (isSelected) Modifier.border(1.5.dp, cat.color.copy(alpha = 0.85f), RoundedCornerShape(18.dp)) else Modifier)
                                            .clickable {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                draftCategories = if (isSelected) draftCategories - cat.name - cat.displayName else draftCategories + cat.name
                                            }
                                            .padding(horizontal = 4.dp, vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = if (cat.titleRes != null) stringResource(cat.titleRes) else cat.displayName,
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontFamily = dm,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                fontSize = 10.5.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                repeat(4 - rowCats.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormEyebrow(text = stringResource(R.string.amount_range))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (range in quickRanges) {
                            FormSelectableChip(
                                label = range.label,
                                selected = minInput.toDoubleOrNull() == range.min && maxInput.toDoubleOrNull() == range.max,
                                accent = accent,
                                onClick = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    minInput = range.min?.toInt()?.toString() ?: ""
                                    maxInput = range.max?.toInt()?.toString() ?: ""
                                }
                            )
                        }
                    }
                    FormGroup {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FormTextRow(
                                icon = LucideIcons.Check,
                                label = stringResource(R.string.min),
                                value = minInput,
                                onValueChange = { input -> if (input.all { it.isDigit() } && input.length <= 8) minInput = input },
                                placeholder = "0",
                                accent = accent,
                                prefix = currencySymbol,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f)
                            )
                            FormTextRow(
                                icon = LucideIcons.Check,
                                label = stringResource(R.string.max),
                                value = maxInput,
                                onValueChange = { input -> if (input.all { it.isDigit() } && input.length <= 8) maxInput = input },
                                placeholder = "∞",
                                accent = accent,
                                prefix = currencySymbol,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                FormPrimaryButton(
                    background = androidx.compose.ui.graphics.Color.Transparent,
                    text = if (draftIsActive) stringResource(R.string.action_done) else stringResource(R.string.action_show_all),
                    enabled = true,
                    accent = accent,
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onApplyFilter(
                            DashboardFilter(
                                type = draftType,
                                selectedCategories = draftCategories,
                                minAmount = minInput.toDoubleOrNull(),
                                maxAmount = maxInput.toDoubleOrNull()
                            )
                        )
                        closeWithAnimation()
                    }
                )
            }
        }
    }
}
