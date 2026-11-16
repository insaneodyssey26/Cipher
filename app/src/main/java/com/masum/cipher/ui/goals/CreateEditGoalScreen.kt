package com.masum.cipher.ui.goals

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
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
import com.masum.cipher.ui.components.CipherSheet
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.masum.cipher.ui.components.FormEyebrow
import com.masum.cipher.ui.components.FormGroup
import com.masum.cipher.ui.components.FormHairline
import com.masum.cipher.ui.components.FormHeroAmount
import com.masum.cipher.ui.components.FormLabelValueRow
import com.masum.cipher.ui.components.FormPrimaryButton
import com.masum.cipher.ui.components.FormQuickChip
import com.masum.cipher.ui.components.FormTextRow
import com.masum.cipher.ui.components.FormTopBar
import androidx.compose.ui.text.TextStyle
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

        CipherSheet(
            onDismissRequest = { showIconPickerSheet = false },
            sheetState = iconSheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); showIconPickerSheet = false },
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
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
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
                TextButton(onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); showDeleteConfirm = false }) {
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

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .background(Brush.verticalGradient(listOf(selectedColor.copy(alpha = 0.14f), Color.Transparent), endY = 1100f)),
        containerColor = Color.Transparent,
        topBar = {
            FormTopBar(
                title = stringResource(if (isEditing) R.string.goals_edit_goal else R.string.goals_new_goal),
                onBack = {
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onNavigateBack()
                },
                onDelete = if (isEditing && onDeleteGoal != null) {
                    {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        showDeleteConfirm = true
                    }
                } else null,
                deleteDescription = stringResource(R.string.custom_category_delete)
            )
        },
        bottomBar = {
            FormPrimaryButton(
                text = stringResource(if (isEditing) R.string.goals_save_changes else R.string.goals_create_goal),
                enabled = isFormValid,
                accent = selectedColor,
                onClick = { com.masum.cipher.ui.components.AppHaptics.tick(); onSaveAction() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performVibrate(isHapticsEnabled)
                            showIconPickerSheet = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 7.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )
                        if (animatedProgress > 0f) {
                            drawArc(
                                color = selectedColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedIcon,
                            contentDescription = stringResource(R.string.goals_select_icon),
                            tint = selectedColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (goalName.isBlank()) {
                        Text(
                            text = stringResource(R.string.goals_goal_name_placeholder),
                            style = TextStyle(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = goalName,
                        onValueChange = {
                            goalName = it
                            errorMessage = null
                        },
                        textStyle = TextStyle(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(selectedColor),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (previewTargetAmount <= 0.0) {
                        stringResource(R.string.goals_target_amount)
                    } else if (remainingAmount <= 0.0 && previewSavedAmount > 0.0) {
                        "${(previewProgress * 100).toInt()}%  ·  ${AppFormatters.formatCurrency(previewTargetAmount, currencySymbol, locale = locale, decimals = 0)}"
                    } else {
                        "${(previewProgress * 100).toInt()}%  ·  ${AppFormatters.formatCurrency(remainingAmount, currencySymbol, locale = locale, decimals = 0)} to go"
                    },
                    style = TextStyle(fontFamily = Lato, fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                    color = if (remainingAmount <= 0.0 && previewSavedAmount > 0.0) EmeraldIncome else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormEyebrow(text = stringResource(R.string.goals_target_amount))
                    FormHeroAmount(
                        value = targetAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1 && input.length <= 10) {
                                targetAmountText = input
                                errorMessage = null
                            }
                        },
                        currencySymbol = currencySymbol,
                        accent = selectedColor,
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        quickTargetIncrements.forEach { increment ->
                            FormQuickChip(
                                label = "+${AppFormatters.formatCurrency(increment, currencySymbol, locale = locale, decimals = 0)}",
                                onClick = {
                                    view.performVibrate(isHapticsEnabled)
                                    val newVal = (targetAmountText.toDoubleOrNull() ?: 0.0) + increment
                                    targetAmountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else String.format(Locale.US, "%.2f", newVal)
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                FormGroup {
                    FormTextRow(
                        icon = LucideIcons.Wallet,
                        label = stringResource(if (isEditing) R.string.goals_saved_amount else R.string.goals_initial_amount),
                        value = savedAmountText,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() || ch == '.' } && it.count { ch -> ch == '.' } <= 1 && it.length <= 10) {
                                savedAmountText = it
                                errorMessage = null
                            }
                        },
                        placeholder = "0",
                        accent = selectedColor,
                        prefix = currencySymbol,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = RoseExpense,
                        style = TextStyle(fontFamily = Lato, fontSize = 12.5.sp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormEyebrow(
                        text = stringResource(R.string.goals_select_color),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    FormGroup {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryColorRegistry.COLORS.forEach { colorVal ->
                                val c = Color(colorVal.toInt())
                                val isSelected = colorVal == selectedColorHex
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) Modifier.border(1.5.dp, c, CircleShape).padding(4.dp) else Modifier
                                        )
                                        .clip(CircleShape)
                                        .background(c)
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled)
                                            selectedColorHex = colorVal
                                        }
                                )
                            }
                        }
                        FormHairline()
                        FormLabelValueRow(
                            icon = selectedIcon,
                            label = stringResource(R.string.goals_select_icon),
                            value = selectedIconName,
                            placeholder = selectedIconName,
                            accent = selectedColor,
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                showIconPickerSheet = true
                            },
                            trailing = {
                                Icon(
                                    imageVector = LucideIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
