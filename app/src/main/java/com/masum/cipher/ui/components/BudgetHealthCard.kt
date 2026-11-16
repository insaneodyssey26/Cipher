package com.masum.cipher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.pluralStringResource
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
    targetAccountName: String? = null,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleDynamicMode: ((Boolean) -> Unit)? = null,
    isHapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val locale = LocalLocale.current.platformLocale

    val dm = com.masum.cipher.ui.theme.DMSans
    val money = { v: Double -> com.masum.cipher.core.util.AppFormatters.formatCurrency(v, currencySymbol, locale, decimals = 0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                view.performVibrate(isHapticsEnabled, isLongPress = false)
                onEditBudgetClick()
            }
            .padding(vertical = 4.dp)
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
                isNearLimit -> "$percentUsed% ${stringResource(R.string.used)}"
                else -> stringResource(R.string.on_track)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Text(
                        text = statusText,
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                        color = accentColor
                    )
                }
                Text(
                    text = if (targetAccountName != null) "$targetAccountName · $percentUsed%" else "$percentUsed%",
                    style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            FitText(
                text = if (isOverBudget) "${money(-remainingBudget)} over" else "${money(remainingBudget)} left",
                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, letterSpacing = (-1.2).sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "of ${money(effectiveBudget)} this month",
                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    "Spent" to money(spent),
                    "Safe / day" to money(safeSpendPerDay),
                    "Your pace" to money(currentDailyPace)
                ).forEachIndexed { index, (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = when (index) { 0 -> Alignment.Start; 1 -> Alignment.CenterHorizontally; else -> Alignment.End },
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isDynamicBudget) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Dynamic: ${money(budget)} base + ${money(income)} income",
                    style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                    color = EmeraldIncome,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EmeraldIncome.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Plus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.monthly_spending_target),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.set_baseline_budget_subtitle),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    accounts: List<com.masum.cipher.core.data.local.entity.AccountEntity> = emptyList(),
    selectedAccountId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, Long?) -> Unit,
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
    var chosenAccountId by remember {
        mutableStateOf(selectedAccountId)
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

    val dm = com.masum.cipher.ui.theme.DMSans
    val accent = MaterialTheme.colorScheme.primary
    val money = { v: Double -> com.masum.cipher.core.util.AppFormatters.formatCurrency(v, currencySymbol, locale, decimals = 0) }

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.monthly_spending_target),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.5).sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (currentBudget > 0) {
                    Text(
                        text = stringResource(R.string.action_remove_budget),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onConfirm(0.0, false, null)
                                closeWithAnimation()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            FormHeroAmount(
                value = budgetInput,
                onValueChange = { input -> if (input.all { it.isDigit() } && input.length <= 9) budgetInput = input },
                currencySymbol = currencySymbol,
                accent = accent,
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                listOf(5000, 10000, 25000, 50000).forEach { preset ->
                    FormQuickChip(
                        label = money(preset.toDouble()),
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            budgetInput = preset.toString()
                        }
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (accounts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormEyebrow(text = stringResource(R.string.budget_scope_header), modifier = Modifier.padding(start = 4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FormSelectableChip(
                                label = stringResource(R.string.budget_scope_all_accounts),
                                selected = chosenAccountId == null,
                                accent = accent,
                                onClick = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    chosenAccountId = null
                                }
                            )
                            accounts.forEach { acc ->
                                FormSelectableChip(
                                    label = acc.name,
                                    selected = chosenAccountId == acc.id,
                                    accent = androidx.compose.ui.graphics.Color(acc.colorHex),
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        chosenAccountId = acc.id
                                    }
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormEyebrow(text = stringResource(R.string.budget_behavior_header), modifier = Modifier.padding(start = 4.dp))
                    FormGroup {
                        listOf(
                            Triple(false, stringResource(R.string.fixed_limit_title), stringResource(R.string.fixed_limit_desc)),
                            Triple(true, stringResource(R.string.dynamic_budget_title), stringResource(R.string.dynamic_budget_desc))
                        ).forEachIndexed { index, (dynamic, title, desc) ->
                            if (index == 1) FormHairline()
                            FormRow(onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                selectedIsDynamic = dynamic
                            }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = title,
                                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontSize = 12.sp, lineHeight = 16.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (selectedIsDynamic == dynamic) accent else androidx.compose.ui.graphics.Color.Transparent)
                                            .border(1.5.dp, if (selectedIsDynamic == dynamic) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedIsDynamic == dynamic) {
                                            Icon(
                                                imageVector = LucideIcons.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp)
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
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (selectedIsDynamic) {
                                if (currentMonthIncome > 0) "${money(currentAmount)} + ${money(currentMonthIncome)} income" else "${money(currentAmount)} + any income received"
                            } else {
                                "Strict ceiling, income not added"
                            },
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = money(effectiveAmount),
                            style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                            color = if (selectedIsDynamic) EmeraldIncome else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            FormPrimaryButton(
                text = stringResource(R.string.save_target),
                enabled = true,
                accent = accent,
                background = androidx.compose.ui.graphics.Color.Transparent,
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    onConfirm(budgetInput.toDoubleOrNull() ?: 0.0, selectedIsDynamic, chosenAccountId)
                    closeWithAnimation()
                }
            )
        }
    }
}
