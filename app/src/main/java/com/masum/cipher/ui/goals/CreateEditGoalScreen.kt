package com.masum.cipher.ui.goals

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.domain.model.CategoryColorRegistry
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Palette
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.X
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditGoalScreen(
    goalToEdit: GoalEntity? = null,
    currencySymbol: String = "₹",
    isHapticsEnabled: Boolean = true,
    onNavigateBack: () -> Unit,
    onSaveGoal: (name: String, targetAmount: Double, initialSaved: Double, colorHex: Long, iconName: String) -> Unit,
    onDeleteGoal: ((GoalEntity) -> Unit)? = null
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val locale = LocalLocale.current.platformLocale

    var goalName by remember(goalToEdit) { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmountText by remember(goalToEdit) {
        mutableStateOf(
            goalToEdit?.targetAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var savedAmountText by remember(goalToEdit) {
        mutableStateOf(
            goalToEdit?.savedAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var selectedIconName by remember(goalToEdit) { mutableStateOf(goalToEdit?.iconName ?: "PiggyBank") }
    var selectedColorHex by remember(goalToEdit) { mutableStateOf(goalToEdit?.colorHex ?: CategoryColorRegistry.COLORS.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showIconPickerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(goalToEdit) {
        if (goalToEdit != null) {
            goalName = goalToEdit.name
            targetAmountText = if (goalToEdit.targetAmount % 1.0 == 0.0) goalToEdit.targetAmount.toLong().toString() else goalToEdit.targetAmount.toString()
            savedAmountText = if (goalToEdit.savedAmount % 1.0 == 0.0) goalToEdit.savedAmount.toLong().toString() else goalToEdit.savedAmount.toString()
            selectedIconName = goalToEdit.iconName
            selectedColorHex = goalToEdit.colorHex
        }
    }

    val isEditing = goalToEdit != null
    val selectedColor = Color(selectedColorHex.toInt())
    val selectedIcon = CategoryIconRegistry.getIcon(selectedIconName)

    val previewTargetAmount = targetAmountText.toDoubleOrNull() ?: 0.0
    val previewSavedAmount = savedAmountText.toDoubleOrNull() ?: 0.0
    val remainingAmount = max(0.0, previewTargetAmount - previewSavedAmount)
    val previewProgress = if (previewTargetAmount > 0.0) {
        (previewSavedAmount / previewTargetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = previewProgress,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f),
        label = "goalProgressAnim"
    )

    val quickTargetIncrements = listOf(5000.0, 10000.0, 25000.0, 50000.0, 100000.0)

    val errNameEmpty = stringResource(R.string.goals_name_error_empty)
    val errAmountEmpty = stringResource(R.string.goals_amount_error_empty)
    val errAmountInvalid = stringResource(R.string.goals_amount_error_invalid)

    val isFormValid = goalName.isNotBlank() && previewTargetAmount > 0.0

    val onSaveAction = {
        val trimmed = goalName.trim()
        if (trimmed.isEmpty()) {
            errorMessage = errNameEmpty
            view.performVibrate(isHapticsEnabled, isLongPress = true)
        } else {
            val targetAmount = targetAmountText.toDoubleOrNull()
            if (targetAmount == null || targetAmount <= 0.0) {
                errorMessage = if (targetAmountText.isBlank()) errAmountEmpty else errAmountInvalid
                view.performVibrate(isHapticsEnabled, isLongPress = true)
            } else {
                val initialSaved = savedAmountText.toDoubleOrNull() ?: 0.0
                view.performVibrate(isHapticsEnabled, isLongPress = false)
                focusManager.clearFocus()
                keyboardController?.hide()
                onSaveGoal(trimmed, targetAmount, initialSaved, selectedColorHex, selectedIconName)
            }
        }
    }

    BackHandler {
        focusManager.clearFocus()
        keyboardController?.hide()
        onNavigateBack()
    }

    if (showIconPickerSheet) {
        val iconSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var iconSearchQuery by remember { mutableStateOf("") }
        val allIcons = remember { CategoryIconRegistry.ICONS }
        val filteredIcons = remember(iconSearchQuery) {
            if (iconSearchQuery.isBlank()) allIcons
            else allIcons.filter { it.first.contains(iconSearchQuery.trim(), ignoreCase = true) }
        }

        ModalBottomSheet(
            onDismissRequest = { showIconPickerSheet = false },
            sheetState = iconSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            tonalElevation = 0.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .size(width = 38.dp, height = 4.5.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .nestedScroll(remember {
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
                    })
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.goals_select_icon),
                        style = Typography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { showIconPickerSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    if (iconSearchQuery.isBlank()) {
                        Text(
                            text = "Search icons (e.g. Car, Home, Flight)",
                            style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = iconSearchQuery,
                        onValueChange = { iconSearchQuery = it },
                        textStyle = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredIcons, key = { it.first }) { (iconKey, iconVector) ->
                        val isSelected = iconKey == selectedIconName
                        Surface(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                selectedIconName = iconKey
                                showIconPickerSheet = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) selectedColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) selectedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.aspectRatio(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && goalToEdit != null && onDeleteGoal != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.goals_delete_confirm_title, goalToEdit.name),
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
                        showDeleteConfirm = false
                        onDeleteGoal(goalToEdit)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.custom_category_delete),
                        style = Typography.labelLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                        color = RoseExpense
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = Typography.labelLarge.copy(fontFamily = DMSans),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed by btnInteraction.collectIsPressedAsState()
    val btnScale by animateFloatAsState(targetValue = if (btnPressed) 0.97f else 1f, label = "btn_scale")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(if (isEditing) R.string.goals_edit_goal else R.string.goals_new_goal),
                            style = Typography.titleLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.account_form_subtitle),
                            style = Typography.bodySmall.copy(
                                fontFamily = Lato,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isEditing && onDeleteGoal != null) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            showDeleteConfirm = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(RoseExpense.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = stringResource(R.string.custom_category_delete),
                            tint = RoseExpense,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(btnScale)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFormValid) selectedColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = isFormValid, interactionSource = btnInteraction, indication = null) {
                            onSaveAction()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                tint = if (isFormValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(if (isEditing) R.string.goals_save_changes else R.string.goals_create_goal),
                                style = Typography.titleMedium.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = if (isFormValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                selectedColor.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(1.2.dp, selectedColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(selectedColor.copy(alpha = 0.2f))
                                    .border(1.dp, selectedColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled)
                                        showIconPickerSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedIcon,
                                    contentDescription = null,
                                    tint = selectedColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = goalName.ifBlank { stringResource(R.string.goals_goal_name_placeholder) },
                                    style = Typography.titleMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = if (goalName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (previewTargetAmount > 0.0) {
                                        "${AppFormatters.formatCurrency(previewSavedAmount, currencySymbol, locale = locale, decimals = 0)} of ${AppFormatters.formatCurrency(previewTargetAmount, currencySymbol, locale = locale, decimals = 0)}"
                                    } else "Set target amount below",
                                    style = Typography.bodySmall.copy(
                                        fontFamily = Lato,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = selectedColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, selectedColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(selectedColor)
                                )
                                Text(
                                    text = "${(previewProgress * 100).toInt()}%",
                                    style = Typography.labelMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = selectedColor
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(RoundedCornerShape(4.5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(4.5.dp))
                                    .background(selectedColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (previewTargetAmount <= 0.0) "Ready to start"
                                else if (remainingAmount <= 0.0 && previewSavedAmount > 0.0) "Goal Achieved! 🎉"
                                else "${AppFormatters.formatCurrency(remainingAmount, currencySymbol, locale = locale, decimals = 0)} left to reach goal",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (remainingAmount <= 0.0 && previewSavedAmount > 0.0) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (previewTargetAmount > 0.0) {
                                Text(
                                    text = "Target: ${AppFormatters.formatCurrency(previewTargetAmount, currencySymbol, locale = locale, decimals = 0)}",
                                    style = Typography.labelSmall.copy(fontFamily = Lato, fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.goals_target_amount),
                    style = Typography.labelMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val amountLength = targetAmountText.length
                val dynamicFontSize = when {
                    amountLength > 9 -> 26.sp
                    amountLength > 7 -> 30.sp
                    amountLength > 5 -> 34.sp
                    else -> 38.sp
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currencySymbol,
                        style = Typography.headlineLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = dynamicFontSize * 0.85f
                        ),
                        color = selectedColor
                    )
                    Spacer(Modifier.width(4.dp))
                    BasicTextField(
                        value = targetAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1 && input.length <= 10) {
                                targetAmountText = input
                                errorMessage = null
                            }
                        },
                        textStyle = Typography.headlineLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = dynamicFontSize,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start
                        ),
                        cursorBrush = SolidColor(selectedColor),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (targetAmountText.isBlank()) {
                                    Text(
                                        text = "0",
                                        style = Typography.headlineLarge.copy(
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = dynamicFontSize
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickTargetIncrements.forEach { increment ->
                        Surface(
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                val currentVal = targetAmountText.toDoubleOrNull() ?: 0.0
                                val newVal = currentVal + increment
                                targetAmountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else String.format(Locale.US, "%.2f", newVal)
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        ) {
                            Text(
                                text = "+${AppFormatters.formatCurrency(increment, currencySymbol, locale = locale, decimals = 0)}",
                                style = Typography.labelSmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.goals_name_label),
                        style = Typography.labelMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(selectedColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Target,
                                contentDescription = null,
                                tint = selectedColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (goalName.isBlank()) {
                                Text(
                                    text = stringResource(R.string.goals_goal_name_placeholder),
                                    style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            }
                            BasicTextField(
                                value = goalName,
                                onValueChange = {
                                    goalName = it
                                    errorMessage = null
                                },
                                textStyle = Typography.bodyMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (goalName.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    view.performVibrate(isHapticsEnabled)
                                    goalName = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                    thickness = 1.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(if (isEditing) R.string.goals_saved_amount else R.string.goals_initial_amount),
                        style = Typography.labelMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Wallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = currencySymbol,
                            style = Typography.bodyMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (savedAmountText.isBlank()) {
                                Text(
                                    text = "0.00 (optional)",
                                    style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            }
                            BasicTextField(
                                value = savedAmountText,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() || ch == '.' } && it.count { ch -> ch == '.' } <= 1 && it.length <= 10) {
                                        savedAmountText = it
                                        errorMessage = null
                                    }
                                },
                                textStyle = Typography.bodyMedium.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.SemiBold,
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
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = RoseExpense,
                    style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.5.sp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
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
                        Icon(
                            imageVector = LucideIcons.Palette,
                            contentDescription = null,
                            tint = selectedColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.goals_select_color),
                            style = Typography.labelMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            showIconPickerSheet = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = selectedIcon,
                                contentDescription = null,
                                tint = selectedColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = selectedIconName,
                                style = Typography.labelSmall.copy(fontFamily = Lato, fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = LucideIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryColorRegistry.COLORS.forEach { colorVal ->
                        val c = Color(colorVal.toInt())
                        val isSelected = colorVal == selectedColorHex

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    selectedColorHex = colorVal
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = LucideIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
