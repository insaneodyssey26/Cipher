package com.masum.cipher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.Trash2
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun BudgetHealthCard(
    spent: Double,
    budget: Double,
    income: Double = 0.0,
    isDynamicBudget: Boolean = false,
    currencySymbol: String = "₹",
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleDynamicMode: ((Boolean) -> Unit)? = null,
    isHapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale

    VaultCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            view.performVibrate(isHapticsEnabled, isLongPress = false)
            onEditBudgetClick()
        },
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 18.dp
    ) {
        if (budget > 0) {
            val effectiveBudget = if (isDynamicBudget) budget + income else budget
            val now = Calendar.getInstance()
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = now.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)

            val remainingBudget = effectiveBudget - spent
            val progress = (spent / effectiveBudget).toFloat().coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "budgetProgress"
            )

            val rawPercent = (spent / effectiveBudget) * 100.0
            val percentUsed = when {
                rawPercent.isInfinite() || rawPercent.isNaN() -> 0L
                rawPercent > 99999.0 -> 99999L
                else -> rawPercent.toLong()
            }
            val percentDisplay = if (percentUsed >= 99999) ">99999%" else "$percentUsed%"

            val safeSpendPerDay = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0
            val currentDailyPace = spent / currentDay

            val isOverBudget = spent > effectiveBudget
            val isNearLimit = percentUsed >= 85

            val accentColor = when {
                isOverBudget -> RoseExpense
                isNearLimit -> Color(0xFFF59E0B)
                else -> EmeraldIncome
            }

            val statusText = when {
                isOverBudget -> stringResource(R.string.over_budget)
                isNearLimit -> "$percentDisplay ${stringResource(R.string.used)}"
                else -> stringResource(R.string.on_track)
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = statusText,
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = accentColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onToggleDynamicMode != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                    .padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (!isDynamicBudget) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            if (isDynamicBudget) {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                onToggleDynamicMode(false)
                                            }
                                        }
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.fixed),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = if (!isDynamicBudget) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (!isDynamicBudget) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isDynamicBudget) EmeraldIncome.copy(alpha = 0.18f) else Color.Transparent)
                                        .clickable {
                                            if (!isDynamicBudget) {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                onToggleDynamicMode(true)
                                            }
                                        }
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.dynamic),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = if (isDynamicBudget) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isDynamicBudget) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (isDynamicBudget) "${stringResource(R.string.dynamic)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(effectiveBudget, currencySymbol, locale)}" else "${stringResource(R.string.fixed)}: ${com.masum.cipher.core.util.AppFormatters.formatCurrency(budget, currencySymbol, locale)}",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onEditBudgetClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Settings,
                                contentDescription = "Edit Budget",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val amountText = if (isOverBudget) {
                    com.masum.cipher.core.util.AppFormatters.formatCurrency(spent - effectiveBudget, currencySymbol, locale)
                } else {
                    com.masum.cipher.core.util.AppFormatters.formatCurrency(remainingBudget, currencySymbol, locale)
                }
                val amountFontSize = when {
                    amountText.length > 18 -> 17.sp
                    amountText.length > 14 -> 20.sp
                    amountText.length > 10 -> 24.sp
                    else -> 28.sp
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = amountText,
                        style = Typography.headlineLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = amountFontSize,
                            letterSpacing = (-0.6).sp
                        ),
                        color = if (isOverBudget) RoseExpense else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = if (isOverBudget) stringResource(R.string.exceeded) else stringResource(R.string.remaining),
                        style = Typography.titleSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .drawBehind {
                            val fillWidth = (size.width * animatedProgress).coerceAtLeast(0f)
                            if (fillWidth > 0f) {
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.8f),
                                            accentColor
                                        )
                                    ),
                                    size = Size(fillWidth, size.height),
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                )
                            }
                        }
                ) {
                    if (isDynamicBudget && income > 0 && effectiveBudget > 0) {
                        val baseRatio = (budget / effectiveBudget).toFloat().coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(1f - baseRatio)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                                    .background(EmeraldIncome.copy(alpha = 0.18f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(baseRatio)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterStart)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterEnd)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.spent_of,
                            com.masum.cipher.core.util.AppFormatters.formatCurrency(spent, currencySymbol, locale),
                            com.masum.cipher.core.util.AppFormatters.formatCurrency(effectiveBudget, currencySymbol, locale)
                        ),
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stringResource(R.string.days_left, daysRemaining)} ($percentDisplay)",
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isDynamicBudget && income > 0) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(
                            R.string.dynamic_budget_badge_desc,
                            com.masum.cipher.core.util.AppFormatters.formatCurrency(budget, currencySymbol, locale),
                            com.masum.cipher.core.util.AppFormatters.formatCurrency(income, currencySymbol, locale)
                        ),
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = EmeraldIncome
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.safe_daily_spend).uppercase(),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.7.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (isOverBudget) "${com.masum.cipher.core.util.AppFormatters.formatCurrency(0.0, currencySymbol, locale)} / ${stringResource(R.string.day_unit)}" else "${com.masum.cipher.core.util.AppFormatters.formatCurrency(safeSpendPerDay, currencySymbol, locale)} / ${stringResource(R.string.day_unit)}",
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isOverBudget) RoseExpense else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.daily_average).uppercase(),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.7.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "${com.masum.cipher.core.util.AppFormatters.formatCurrency(currentDailyPace, currencySymbol, locale)} / ${stringResource(R.string.day_unit)}",
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Target,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.monthly_spending_target),
                    style = Typography.titleMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = stringResource(R.string.budget_empty_desc),
                    style = Typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Plus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stringResource(R.string.set_budget_target),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    isDynamicBudget: Boolean = false,
    currentMonthIncome: Double = 0.0,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var budgetInput by remember {
        mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "")
    }
    var selectedIsDynamic by remember {
        mutableStateOf(isDynamicBudget)
    }
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val locale = LocalLocale.current.platformLocale

    val currentAmount = budgetInput.toDoubleOrNull() ?: 0.0
    val effectiveAmount = if (selectedIsDynamic) currentAmount + currentMonthIncome else currentAmount

    val closeWithAnimation: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.monthly_spending_target),
                        style = Typography.titleLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.set_baseline_budget_subtitle),
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (currentBudget > 0) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onConfirm(0.0, false)
                            closeWithAnimation()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = stringResource(R.string.action_remove_budget),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 9) {
                            budgetInput = input
                        }
                    },
                    label = { Text(stringResource(R.string.monthly_base_limit_label, currencySymbol)) },
                    placeholder = { Text(stringResource(R.string.monthly_base_limit_placeholder)) },
                    textStyle = Typography.headlineSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(1000, 5000, 10000, 25000)
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    val current = budgetInput.toIntOrNull() ?: 0
                                    budgetInput = (current + preset).toString()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${preset / 1000}k",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (budgetInput.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    budgetInput = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_clear_input),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.budget_behavior_header),
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (!selectedIsDynamic) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = if (!selectedIsDynamic) 1.5.dp else 1.dp,
                                color = if (!selectedIsDynamic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                selectedIsDynamic = false
                            }
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.fixed_limit_title),
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (!selectedIsDynamic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (!selectedIsDynamic) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.fixed_limit_desc),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedIsDynamic) EmeraldIncome.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = if (selectedIsDynamic) 1.5.dp else 1.dp,
                                color = if (selectedIsDynamic) EmeraldIncome else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                selectedIsDynamic = true
                            }
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.dynamic_budget_title),
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (selectedIsDynamic) EmeraldIncome else MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedIsDynamic) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = EmeraldIncome,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.dynamic_budget_desc),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedIsDynamic) stringResource(R.string.total_monthly_budget) else stringResource(R.string.fixed_monthly_limit),
                        style = Typography.labelMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.masum.cipher.core.util.AppFormatters.formatCurrency(effectiveAmount, currencySymbol, locale),
                        style = Typography.titleLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (selectedIsDynamic) EmeraldIncome else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (selectedIsDynamic) {
                        if (currentMonthIncome > 0) {
                            stringResource(
                                R.string.dynamic_budget_income_desc,
                                com.masum.cipher.core.util.AppFormatters.formatCurrency(currentAmount, currencySymbol, locale),
                                com.masum.cipher.core.util.AppFormatters.formatCurrency(currentMonthIncome, currencySymbol, locale)
                            )
                        } else {
                            stringResource(
                                R.string.dynamic_budget_no_income_desc,
                                com.masum.cipher.core.util.AppFormatters.formatCurrency(currentAmount, currencySymbol, locale)
                            )
                        }
                    } else {
                        stringResource(
                            R.string.fixed_budget_limit_desc,
                            com.masum.cipher.core.util.AppFormatters.formatCurrency(currentAmount, currencySymbol, locale)
                        )
                    },
                    style = Typography.bodySmall.copy(
                        fontFamily = Lato,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val amount = budgetInput.toDoubleOrNull() ?: 0.0
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    onConfirm(amount, selectedIsDynamic)
                    closeWithAnimation()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_target),
                    style = Typography.labelLarge.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
