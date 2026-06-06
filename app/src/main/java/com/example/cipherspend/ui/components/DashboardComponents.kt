package com.masum.cipher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BalanceHeader(
    totalBalance: Double,
    income: Double,
    expenses: Double,
    isPrivacyMode: Boolean = false,
    isHapticsEnabled: Boolean = true
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 28.dp)
    ) {
        Text(
            text = "BALANCE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = onSurfaceVar
        )

        Spacer(Modifier.height(8.dp))

        AnimatedBalance(value = totalBalance, isPrivacyMode = isPrivacyMode, color = onBg)

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill(
                label = "IN",
                amount = income,
                color = CipherIncome,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                isPrivacyMode = isPrivacyMode
            )
            StatPill(
                label = "OUT",
                amount = expenses,
                color = CipherExpense,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                isPrivacyMode = isPrivacyMode
            )
        }
    }
}

@Composable
private fun AnimatedBalance(value: Double, isPrivacyMode: Boolean, color: Color) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "balance"
    )

    Text(
        text = if (isPrivacyMode) "₹ ••••••" else AppFormatters.getCurrencyNoDecimals().format(animatedValue.toDouble()),
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.5).sp,
            fontSize = 38.sp
        ),
        color = color
    )
}

@Composable
private fun StatPill(
    label: String,
    amount: Double,
    color: Color,
    containerColor: Color,
    isPrivacyMode: Boolean
) {
    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(modifier = Modifier.size(5.dp).background(color, CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = if (isPrivacyMode) "₹•••" else AppFormatters.getCurrencyNoDecimals().format(amount),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    isPrivacyMode: Boolean = false,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val category = remember(transaction.category) {
        TransactionCategory.fromString(transaction.category)
    }
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "scale"
    )
    val initial = transaction.merchant.firstOrNull()?.uppercaseChar() ?: '?'
    val dateLabel = remember(transaction.timestamp) {
        SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp))
    }
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) {
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit(transaction)
            }
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(category.color.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = category.color
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = onBg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${category.displayName} · $dateLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = onSurfaceVar
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isPrivacyMode) {
                    "${if (transaction.isIncome) "+" else "−"} ₹••"
                } else {
                    "${if (transaction.isIncome) "+" else "−"} ${
                        AppFormatters.getCurrencyNoDecimals().format(transaction.amount).replace("₹", "")
                    }"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (transaction.isIncome) CipherIncome else onBg
            )
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Delete",
                modifier = Modifier
                    .size(14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete(transaction)
                    },
                tint = outline
            )
        }
    }
}

@Composable
fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 74.dp, end = 20.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Composable
fun BudgetCard(
    spent: Double,
    budget: Double,
    onSetBudgetClick: () -> Unit,
    isHapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "scale"
    )
    val surface = MaterialTheme.colorScheme.surface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale)
            .background(surface, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSetBudgetClick()
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(outlineVariant, RoundedCornerShape(18.dp))
        )

        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MONTHLY BUDGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = onSurfaceVar
                )
                if (budget > 0) {
                    val remaining = (budget - spent).coerceAtLeast(0.0)
                    val overBudget = spent > budget
                    Text(
                        text = if (overBudget) "OVER LIMIT" else "${AppFormatters.getCurrencyNoDecimals().format(remaining)} left",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (overBudget) CipherExpense else CipherIncome
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (budget <= 0) {
                Text(
                    text = "Tap to set a monthly limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = CipherBlue
                )
            } else {
                val progress = (spent / budget).toFloat().coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                    label = "budget"
                )
                val barColor by animateColorAsState(
                    targetValue = when {
                        spent > budget -> CipherExpense
                        progress > 0.8f -> Color(0xFFFFAB40)
                        else -> CipherBlue
                    },
                    label = "barColor"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(outline.copy(alpha = 0.25f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(barColor, CircleShape)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = AppFormatters.getCurrencyNoDecimals().format(spent),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = onBg
                    )
                    Text(
                        text = "of ${AppFormatters.getCurrencyNoDecimals().format(budget)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = onSurfaceVar
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsState() {
    val outline = MaterialTheme.colorScheme.outline
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(outline.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = outline
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Vault is empty",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = onBg,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Send an SMS from your bank account or add a transaction manually.",
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVar,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun PremiumBalanceHeader(
    totalBalance: Double,
    income: Double,
    expenses: Double,
    isPrivacyMode: Boolean = false,
    isHapticsEnabled: Boolean = true
) = BalanceHeader(totalBalance, income, expenses, isPrivacyMode, isHapticsEnabled)

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    isPrivacyMode: Boolean = false,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    isHapticsEnabled: Boolean = true
) = TransactionRow(transaction, isPrivacyMode, onDelete, onEdit, isHapticsEnabled)

@Composable
fun SimplifiedStat(
    label: String,
    amount: Double,
    color: Color,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isPrivacyMode) "₹•••" else AppFormatters.getCurrencyNoDecimals().format(amount),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
