package com.masum.cipher.ui.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Minus
import compose.icons.lucideicons.Pencil
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Trash2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositWithdrawGoalSheet(
    goal: GoalEntity,
    currencySymbol: String = "₹",
    isHapticsEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onEditGoal: () -> Unit = {},
    onDeleteGoal: () -> Unit = {},
    onConfirmAdjust: (delta: Double) -> Unit
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isDepositMode by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val goalColor = Color(goal.colorHex.toInt())
    val icon = CategoryIconRegistry.getIcon(goal.iconName)

    val quickChips = listOf(500.0, 1000.0, 2000.0, 5000.0)

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.goals_delete_confirm_title, goal.name),
                    style = Typography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.goals_delete_confirm_desc),
                    style = Typography.bodyMedium.copy(fontFamily = Lato)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        showDeleteConfirmDialog = false
                        onDeleteGoal()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = RoseExpense,
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = Typography.labelLarge
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .clip(CircleShape)
                            .background(goalColor.copy(alpha = 0.18f))
                            .border(1.dp, goalColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = goalColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = goal.name,
                            style = Typography.titleMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Balance: ${AppFormatters.formatCurrency(goal.savedAmount, currencySymbol, decimals = 0)}",
                            style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                            color = goalColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onEditGoal()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Pencil,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            showDeleteConfirmDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = RoseExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isDepositMode) EmeraldIncome.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            isDepositMode = true
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Plus,
                            contentDescription = null,
                            tint = if (isDepositMode) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.goals_deposit),
                            style = Typography.labelMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (isDepositMode) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (!isDepositMode) RoseExpense.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            isDepositMode = false
                            errorMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Minus,
                            contentDescription = null,
                            tint = if (!isDepositMode) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.goals_withdraw),
                            style = Typography.labelMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (!isDepositMode) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = "0.00",
                        style = Typography.headlineMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                },
                prefix = {
                    Text(
                        text = currencySymbol,
                        style = Typography.headlineMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                        color = if (isDepositMode) EmeraldIncome else RoseExpense
                    )
                },
                textStyle = Typography.headlineMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDepositMode) EmeraldIncome else RoseExpense,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickChips.forEach { chipAmount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                val currentVal = amountText.toDoubleOrNull() ?: 0.0
                                val newVal = currentVal + chipAmount
                                amountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else newVal.toString()
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${AppFormatters.formatCurrency(chipAmount, currencySymbol, decimals = 0)}",
                            style = Typography.labelSmall.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = RoseExpense,
                    style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp)
                )
            }

            val btnInteraction = remember { MutableInteractionSource() }
            val btnPressed by btnInteraction.collectIsPressedAsState()
            val btnScale by animateFloatAsState(targetValue = if (btnPressed) 0.97f else 1f, label = "btn_scale")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(btnScale)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDepositMode) EmeraldIncome else RoseExpense)
                    .clickable(interactionSource = btnInteraction, indication = null) {
                        val parsed = amountText.toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            errorMessage = "Please enter a valid amount"
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            return@clickable
                        }

                        if (!isDepositMode && parsed > goal.savedAmount) {
                            errorMessage = "Cannot withdraw more than current balance (${AppFormatters.formatCurrency(goal.savedAmount, currencySymbol, decimals = 0)})"
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            return@clickable
                        }

                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        val delta = if (isDepositMode) parsed else -parsed
                        onConfirmAdjust(delta)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDepositMode) "Deposit $currencySymbol${amountText.ifBlank { "0" }}" else "Withdraw $currencySymbol${amountText.ifBlank { "0" }}",
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
