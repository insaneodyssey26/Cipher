package com.masum.cipher.ui.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Building2
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.CircleDollarSign
import compose.icons.lucideicons.Coins
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.Landmark
import compose.icons.lucideicons.Layers
import compose.icons.lucideicons.PiggyBank
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import java.util.Locale

val AccountPaletteColors = listOf(
    0xFF4F46E5,
    0xFF059669,
    0xFFE11D48,
    0xFFD97706,
    0xFF0891B2,
    0xFF9333EA,
    0xFF1E293B,
    0xFFBE185D,
    0xFF2563EB,
    0xFF0D9488,
    0xFFD946EF,
    0xFFF59E0B,
    0xFF10B981,
    0xFF6366F1,
    0xFF84CC16,
    0xFF0F172A
)

fun getAccountIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Landmark" -> LucideIcons.Landmark
        "Wallet" -> LucideIcons.Wallet
        "CreditCard" -> LucideIcons.CreditCard
        "PiggyBank" -> LucideIcons.PiggyBank
        "TrendingUp" -> LucideIcons.TrendingUp
        "Coins" -> LucideIcons.Coins
        "Building2" -> LucideIcons.Building2
        "CircleDollarSign" -> LucideIcons.CircleDollarSign
        else -> LucideIcons.Layers
    }
}

val AvailableAccountIcons = listOf(
    "Landmark",
    "Wallet",
    "CreditCard",
    "PiggyBank",
    "TrendingUp",
    "Coins",
    "Building2",
    "CircleDollarSign",
    "Layers"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditAccountScreen(
    accountToEdit: AccountEntity? = null,
    currencySymbol: String,
    locale: Locale,
    isHapticsEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onSaveAccount: (name: String, type: String, initialBalance: Double, colorHex: Long, iconName: String, isDefault: Boolean, last4: String?) -> Unit
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val colorScrollState = rememberScrollState()
    val iconScrollState = rememberScrollState()

    var name by remember(accountToEdit) { mutableStateOf(accountToEdit?.name ?: "") }
    var selectedType by remember(accountToEdit) { mutableStateOf(accountToEdit?.type?.let { AccountType.fromKey(it) } ?: AccountType.BANK) }
    var isNegativeBalance by remember(accountToEdit) { mutableStateOf((accountToEdit?.initialBalance ?: 0.0) < 0.0) }
    var balanceInput by remember(accountToEdit) {
        mutableStateOf(
            if (accountToEdit != null && accountToEdit.initialBalance != 0.0) {
                val absVal = kotlin.math.abs(accountToEdit.initialBalance)
                if (absVal % 1.0 == 0.0) absVal.toLong().toString() else absVal.toString()
            } else ""
        )
    }
    var selectedColor by remember(accountToEdit) { mutableLongStateOf(accountToEdit?.colorHex ?: AccountPaletteColors.first()) }
    var selectedIcon by remember(accountToEdit) { mutableStateOf(accountToEdit?.iconName ?: (accountToEdit?.type?.let { AccountType.fromKey(it) } ?: AccountType.BANK).defaultIcon) }
    var isDefault by remember(accountToEdit) { mutableStateOf(accountToEdit?.isDefault ?: false) }
    var last4Input by remember(accountToEdit) { mutableStateOf(accountToEdit?.accountNumberLast4 ?: "") }

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }

    val rawBalanceNumber = balanceInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val parsedBalance = if (isNegativeBalance) -rawBalanceNumber else rawBalanceNumber

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (accountToEdit == null) "Create Account" else "Edit Account",
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customise card theme, balance & details",
                            style = Typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        val finalName = name.trim().ifBlank { selectedType.displayName }
                        val finalLast4 = last4Input.trim().take(4).ifBlank { null }
                        onSaveAccount(
                            finalName,
                            selectedType.key,
                            parsedBalance,
                            selectedColor,
                            selectedIcon,
                            isDefault,
                            finalLast4
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Save",
                        style = Typography.labelMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            LuxuryAccountCard(
                name = name.ifBlank { selectedType.displayName },
                type = selectedType,
                balance = parsedBalance,
                colorHex = selectedColor,
                iconName = selectedIcon,
                isDefault = isDefault,
                last4 = last4Input.ifBlank { null },
                currencySymbol = currencySymbol,
                locale = locale,
                isHapticsEnabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedType == AccountType.CREDIT_CARD) "OUTSTANDING / STARTING BALANCE" else "STARTING BALANCE",
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isNegativeBalance = !isNegativeBalance
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isNegativeBalance) "Negative (-)" else "Positive (+)",
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isNegativeBalance) RoseExpense else EmeraldIncome
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isNegativeBalance) "-$currencySymbol" else currencySymbol,
                        style = Typography.headlineMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = if (isNegativeBalance) RoseExpense else MaterialTheme.colorScheme.primary
                    )

                    BasicTextField(
                        value = balanceInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                balanceInput = input
                            }
                        },
                        textStyle = Typography.headlineMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (balanceInput.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    style = Typography.headlineMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Text(
                    text = "Initial starting balance before recorded transactions",
                    style = Typography.bodySmall.copy(
                        fontFamily = Lato,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACCOUNT NAME",
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = Typography.titleMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text(
                                text = "e.g. HDFC Bank, Cash Wallet, Amex",
                                style = Typography.bodyMedium.copy(
                                    fontFamily = Lato,
                                    fontSize = 14.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACCOUNT TYPE",
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val fullWidth = maxWidth

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isTypeDropdownExpanded = true
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getAccountIconVector(selectedType.defaultIcon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Text(
                                text = selectedType.displayName,
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = LucideIcons.ChevronDown,
                            contentDescription = "Dropdown",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isTypeDropdownExpanded,
                        onDismissRequest = { isTypeDropdownExpanded = false },
                        modifier = Modifier
                            .width(fullWidth)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        AccountType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (selectedType == type) 0.25f else 0.10f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getAccountIconVector(type.defaultIcon),
                                                contentDescription = null,
                                                tint = if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = type.displayName,
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    val previousDefault = selectedType.defaultIcon
                                    selectedType = type
                                    if (selectedIcon == previousDefault) {
                                        selectedIcon = type.defaultIcon
                                    }
                                    isTypeDropdownExpanded = false
                                }
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CARD / ACCOUNT DIGITS (LAST 4)",
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BasicTextField(
                    value = last4Input,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            last4Input = input
                        }
                    },
                    textStyle = Typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        if (last4Input.isEmpty()) {
                            Text(
                                text = "•••• 4821",
                                style = Typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                Text(
                    text = "Matches incoming bank SMS alerts to this account automatically",
                    style = Typography.bodySmall.copy(
                        fontFamily = Lato,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CARD THEME & ACCENT",
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                coroutineScope.launch {
                                    colorScrollState.animateScrollBy(-220f)
                                }
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ChevronLeft,
                                contentDescription = "Scroll Left",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                coroutineScope.launch {
                                    colorScrollState.animateScrollBy(220f)
                                }
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ChevronRight,
                                contentDescription = "Scroll Right",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(colorScrollState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountPaletteColors.forEach { colorLong ->
                        val isSelected = selectedColor == colorLong
                        val color = Color(colorLong)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    selectedColor = colorLong
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CARD ICON",
                        style = Typography.labelSmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                coroutineScope.launch {
                                    iconScrollState.animateScrollBy(-220f)
                                }
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ChevronLeft,
                                contentDescription = "Scroll Left",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                coroutineScope.launch {
                                    iconScrollState.animateScrollBy(220f)
                                }
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ChevronRight,
                                contentDescription = "Scroll Right",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(iconScrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvailableAccountIcons.forEach { iconName ->
                        val isSelected = selectedIcon == iconName

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(selectedColor).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    selectedIcon = iconName
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAccountIconVector(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Set as Primary Account",
                        style = Typography.titleMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Default account for new transactions & ledger",
                        style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isDefault,
                    onCheckedChange = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        isDefault = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(selectedColor)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val finalName = name.trim().ifBlank { selectedType.displayName }
                    val finalLast4 = last4Input.trim().take(4).ifBlank { null }
                    onSaveAccount(
                        finalName,
                        selectedType.key,
                        parsedBalance,
                        selectedColor,
                        selectedIcon,
                        isDefault,
                        finalLast4
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (accountToEdit == null) "Create Account" else "Save Changes",
                    style = Typography.titleMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
