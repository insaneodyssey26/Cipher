package com.masum.cipher.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.domain.model.CategoryColorRegistry
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.X

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomCategorySheet(
    existingCategory: CustomCategoryEntity? = null,
    existingCustomCategories: List<CustomCategoryEntity> = emptyList(),
    isHapticsEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onSaveCategory: (name: String, iconName: String, colorHex: Long) -> Unit,
    onDeleteCategory: ((CustomCategoryEntity) -> Unit)? = null
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var categoryName by remember { mutableStateOf(existingCategory?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(existingCategory?.iconName ?: "ShoppingBag") }
    var selectedColorHex by remember { mutableStateOf(existingCategory?.colorHex ?: CategoryColorRegistry.COLORS.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isEditing = existingCategory != null
    val selectedColor = Color(selectedColorHex.toInt())
    val selectedIcon = CategoryIconRegistry.getIcon(selectedIconName)

    if (showDeleteConfirm && existingCategory != null && onDeleteCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.custom_category_delete_confirm_title, existingCategory.name),
                    style = Typography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.custom_category_delete_confirm_desc),
                    style = Typography.bodyMedium.copy(fontFamily = Lato)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        showDeleteConfirm = false
                        onDeleteCategory(existingCategory)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseExpense)
                ) {
                    Text(
                        text = stringResource(R.string.custom_category_delete),
                        style = Typography.labelLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = Typography.labelLarge.copy(fontFamily = DMSans)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(if (isEditing) R.string.custom_category_edit_title else R.string.custom_category_new_title),
                    style = Typography.titleLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            OutlinedTextField(
                value = categoryName,
                onValueChange = {
                    categoryName = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.custom_category_name_placeholder),
                        style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp, end = 4.dp)
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedColor.copy(alpha = 0.18f))
                            .border(1.dp, selectedColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedIcon,
                            contentDescription = null,
                            tint = selectedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(text = it, color = RoseExpense, style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 11.sp)) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                )
            )

            Text(
                text = stringResource(R.string.custom_category_select_color),
                style = Typography.labelSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryColorRegistry.COLORS.chunked(8).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEach { colorHex ->
                            val isSelected = colorHex == selectedColorHex
                            val itemColor = Color(colorHex.toInt())
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(CircleShape)
                                    .background(itemColor)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else itemColor.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        selectedColorHex = colorHex
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.custom_category_select_icon),
                style = Typography.labelSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryIconRegistry.ICONS.chunked(6).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowIcons.forEach { (iconKey, iconVector) ->
                            val isSelected = iconKey == selectedIconName
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) selectedColor.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) selectedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        selectedIconName = iconKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val errEmpty = stringResource(R.string.custom_category_error_empty)
            val errExists = stringResource(R.string.custom_category_error_exists)

            val btnInteraction = remember { MutableInteractionSource() }
            val btnPressed by btnInteraction.collectIsPressedAsState()
            val btnScale by animateFloatAsState(targetValue = if (btnPressed) 0.97f else 1f, label = "btn_scale")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(btnScale)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(interactionSource = btnInteraction, indication = null) {
                        val trimmed = categoryName.trim()
                        if (trimmed.isEmpty()) {
                            errorMessage = errEmpty
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            return@clickable
                        }

                        val isDuplicateDefault = TransactionCategory.entries.any {
                            it.name.equals(trimmed, ignoreCase = true) || it.displayName.equals(trimmed, ignoreCase = true)
                        }
                        val isDuplicateCustom = existingCustomCategories.any {
                            it.id != (existingCategory?.id ?: -1L) && it.name.equals(trimmed, ignoreCase = true)
                        }

                        if (isDuplicateDefault || isDuplicateCustom) {
                            errorMessage = errExists
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            return@clickable
                        }

                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onSaveCategory(trimmed, selectedIconName, selectedColorHex)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(if (isEditing) R.string.custom_category_save else R.string.custom_category_create),
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (isEditing && onDeleteCategory != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoseExpense.copy(alpha = 0.08f))
                        .clickable {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            showDeleteConfirm = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Trash2,
                            contentDescription = null,
                            tint = RoseExpense,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.custom_category_delete),
                            style = Typography.labelLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = RoseExpense
                        )
                    }
                }
            }
        }
    }
}
