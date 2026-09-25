package com.masum.cipher.ui.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Target

@Composable
fun SavingsGoalsView(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
    onCreateGoalClick: (() -> Unit)? = null,
    showCreateSheetExternal: Boolean = false,
    onDismissCreateSheetExternal: (() -> Unit)? = null
) {
    val view = LocalView.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showCreateSheetInternal by remember { mutableStateOf(false) }
    val showCreateSheet = showCreateSheetExternal || showCreateSheetInternal
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    var activeAdjustGoal by remember { mutableStateOf<GoalEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.goals_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GoalsContract.Effect.ShowUndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = deletedMessage,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                        viewModel.handleIntent(GoalsContract.Intent.RestoreGoal(effect.goal))
                    }
                }
            }
        }
    }

    val overallProgressFraction = if (state.totalTarget > 0.0) {
        (state.totalSaved / state.totalTarget).toFloat().coerceIn(0f, 1f)
    } else 0f

    val overallProgressPercent = if (state.totalTarget > 0.0) {
        ((state.totalSaved / state.totalTarget) * 100).toInt()
    } else 0

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .border(1.dp, White10, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldIncome.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Target,
                                        contentDescription = null,
                                        tint = EmeraldIncome,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.goals_total_saved),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldIncome.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$overallProgressPercent%",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = EmeraldIncome
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = AppFormatters.formatCurrency(state.totalSaved, state.currencySymbol, decimals = 0),
                                style = Typography.headlineMedium.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(R.string.goals_of_target, AppFormatters.formatCurrency(state.totalTarget, state.currencySymbol, decimals = 0), ""),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(overallProgressFraction)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(EmeraldIncome)
                            )
                        }
                    }
                }
            }

            if (state.goals.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Target,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.goals_empty_title),
                            style = Typography.titleMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.goals_empty_desc),
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                view.performVibrate(state.isHapticsEnabled, isLongPress = false)
                                if (onCreateGoalClick != null) {
                                    onCreateGoalClick()
                                } else {
                                    showCreateSheetInternal = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Plus,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.goals_create_goal),
                                    style = Typography.labelLarge.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = state.goals,
                    key = { it.id }
                ) { goal ->
                    GoalCard(
                        goal = goal,
                        currencySymbol = state.currencySymbol,
                        isHapticsEnabled = state.isHapticsEnabled,
                        onCardClick = {
                            activeAdjustGoal = goal
                        }
                    )
                }
            }
        }

        if (showCreateSheet) {
            CreateEditGoalSheet(
                goalToEdit = null,
                currencySymbol = state.currencySymbol,
                isHapticsEnabled = state.isHapticsEnabled,
                onDismiss = {
                    showCreateSheetInternal = false
                    onDismissCreateSheetExternal?.invoke()
                },
                onSaveGoal = { name, targetAmount, initialSaved, colorHex, iconName ->
                    viewModel.handleIntent(
                        GoalsContract.Intent.CreateGoal(
                            name = name,
                            targetAmount = targetAmount,
                            initialSaved = initialSaved,
                            colorHex = colorHex,
                            iconName = iconName
                        )
                    )
                    showCreateSheetInternal = false
                    onDismissCreateSheetExternal?.invoke()
                }
            )
        }

        goalToEdit?.let { goal ->
            CreateEditGoalSheet(
                goalToEdit = goal,
                currencySymbol = state.currencySymbol,
                isHapticsEnabled = state.isHapticsEnabled,
                onDismiss = { goalToEdit = null },
                onSaveGoal = { name, targetAmount, savedAmount, colorHex, iconName ->
                    viewModel.handleIntent(
                        GoalsContract.Intent.UpdateGoal(
                            id = goal.id,
                            name = name,
                            targetAmount = targetAmount,
                            savedAmount = savedAmount,
                            colorHex = colorHex,
                            iconName = iconName
                        )
                    )
                    goalToEdit = null
                },
                onDeleteGoal = {
                    viewModel.handleIntent(GoalsContract.Intent.DeleteGoal(it))
                    goalToEdit = null
                }
            )
        }

        activeAdjustGoal?.let { goal ->
            DepositWithdrawGoalSheet(
                goal = goal,
                currencySymbol = state.currencySymbol,
                isHapticsEnabled = state.isHapticsEnabled,
                onDismiss = { activeAdjustGoal = null },
                onEditGoal = {
                    val target = goal
                    activeAdjustGoal = null
                    goalToEdit = target
                },
                onDeleteGoal = {
                    viewModel.handleIntent(GoalsContract.Intent.DeleteGoal(goal))
                    activeAdjustGoal = null
                },
                onConfirmAdjust = { delta ->
                    viewModel.handleIntent(GoalsContract.Intent.AdjustSavedAmount(goal.id, delta))
                    activeAdjustGoal = null
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}
