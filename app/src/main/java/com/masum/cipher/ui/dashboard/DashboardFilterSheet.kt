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
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.Manrope
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

    var draftType by remember(currentFilter) { mutableStateOf(currentFilter.type) }
    var draftCategories by remember(currentFilter) { mutableStateOf(currentFilter.selectedCategories) }
    var minInput by remember(currentFilter) {
        mutableStateOf(currentFilter.minAmount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var maxInput by remember(currentFilter) {
        mutableStateOf(currentFilter.maxAmount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    val quickRanges = remember {
        listOf(
            QuickAmountRange("Any", null, null),
            QuickAmountRange("< ₹500", null, 500.0),
            QuickAmountRange("₹500 - ₹2k", 500.0, 2000.0),
            QuickAmountRange("₹2k - ₹10k", 2000.0, 10000.0),
            QuickAmountRange("> ₹10k", 10000.0, null)
        )
    }

    val draftIsActive = remember(draftType, draftCategories, minInput, maxInput) {
        draftType != DashboardContract.FilterType.ALL ||
                draftCategories.isNotEmpty() ||
                minInput.toDoubleOrNull() != null ||
                maxInput.toDoubleOrNull() != null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Filter Ledger",
                            style = Typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Refine by transaction type, categories & amount",
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (draftIsActive) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                draftType = DashboardContract.FilterType.ALL
                                draftCategories = emptySet()
                                minInput = ""
                                maxInput = ""
                            }
                        ) {
                            Text(
                                text = "Reset All",
                                style = Typography.labelMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TRANSACTION TYPE",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val types = listOf(
                        DashboardContract.FilterType.ALL to "All",
                        DashboardContract.FilterType.EXPENSE to "Expenses",
                        DashboardContract.FilterType.INCOME to "Income"
                    )
                    val selectedIndex = types.indexOfFirst { it.first == draftType }.coerceAtLeast(0)

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp)
                    ) {
                        val tabWidth = maxWidth / types.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * selectedIndex,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                            label = "type_indicator_offset"
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
                            types.forEach { (type, label) ->
                                val isSelected = draftType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(9.dp))
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
                                        style = Typography.labelMedium.copy(
                                            fontFamily = Manrope,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORIES (${draftCategories.size} selected)",
                            style = Typography.labelSmall.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (draftCategories.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    draftCategories = emptySet()
                                }
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in TransactionCategory.entries) {
                            val isSelected = draftCategories.contains(cat.displayName) || draftCategories.contains(cat.name)
                            val catColor = cat.color

                            val animatedBgColor by animateColorAsState(
                                targetValue = if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                animationSpec = tween(200),
                                label = "chip_bg"
                            )
                            val animatedBorderColor by animateColorAsState(
                                targetValue = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                animationSpec = tween(200),
                                label = "chip_border"
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(animatedBgColor)
                                    .border(
                                        width = 1.dp,
                                        color = animatedBorderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        draftCategories = if (isSelected) {
                                            draftCategories - cat.displayName - cat.name
                                        } else {
                                            draftCategories + cat.displayName
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AnimatedContent(
                                    targetState = isSelected,
                                    transitionSpec = {
                                        (fadeIn(tween(180)) + scaleIn(initialScale = 0.7f)) togetherWith
                                        (fadeOut(tween(180)) + scaleOut(targetScale = 0.7f))
                                    },
                                    label = "cat_icon"
                                ) { selected ->
                                    Icon(
                                        imageVector = if (selected) LucideIcons.Check else cat.icon,
                                        contentDescription = null,
                                        tint = if (selected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = cat.displayName,
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Manrope,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "AMOUNT RANGE",
                        style = Typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (range in quickRanges) {
                            val currentMin = minInput.toDoubleOrNull()
                            val currentMax = maxInput.toDoubleOrNull()
                            val isSelected = currentMin == range.min && currentMax == range.max

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        minInput = range.min?.toInt()?.toString() ?: ""
                                        maxInput = range.max?.toInt()?.toString() ?: ""
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = range.label,
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.5.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 8) {
                                    minInput = input
                                }
                            },
                            label = { Text("Min (₹)", fontSize = 12.sp) },
                            placeholder = { Text("0", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = maxInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 8) {
                                    maxInput = input
                                }
                            },
                            label = { Text("Max (₹)", fontSize = 12.sp) },
                            placeholder = { Text("No limit", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            closeWithAnimation()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = Typography.labelLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            val filter = DashboardFilter(
                                type = draftType,
                                selectedCategories = draftCategories,
                                minAmount = minInput.toDoubleOrNull(),
                                maxAmount = maxInput.toDoubleOrNull()
                            )
                            onApplyFilter(filter)
                            closeWithAnimation()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (draftIsActive) "Apply Filters" else "Show All",
                            style = Typography.labelLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}