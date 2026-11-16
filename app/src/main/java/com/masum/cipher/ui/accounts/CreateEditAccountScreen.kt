package com.masum.cipher.ui.accounts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.domain.model.CategoryColorRegistry
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.FormEyebrow
import com.masum.cipher.ui.components.FormGroup
import com.masum.cipher.ui.components.FormHairline
import com.masum.cipher.ui.components.FormHeroAmount
import com.masum.cipher.ui.components.FormPrimaryButton
import com.masum.cipher.ui.components.FormRow
import com.masum.cipher.ui.components.FormSelectableChip
import com.masum.cipher.ui.components.FormTextRow
import com.masum.cipher.ui.components.FormTopBar
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.RoseExpense
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.Hash
import compose.icons.lucideicons.Wallet
import java.util.Locale

val AccountPaletteColors = CategoryColorRegistry.COLORS

fun getAccountIconVector(iconName: String): ImageVector {
    return CategoryIconRegistry.getIcon(iconName)
}

val AvailableAccountIcons = CategoryIconRegistry.ICONS.map { it.first }

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
    var isIconsExpanded by remember { mutableStateOf(false) }

    val rawBalanceNumber = balanceInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val parsedBalance = if (isNegativeBalance) -rawBalanceNumber else rawBalanceNumber
    val defaultAccountName = stringResource(selectedType.labelRes)

    val accentColor = Color(selectedColor.toInt())
    val glow by animateColorAsState(targetValue = accentColor, animationSpec = tween(500), label = "account_glow")
    val balanceAccent = if (isNegativeBalance) RoseExpense else accentColor
    val hideIcons = !isIconsExpanded

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .background(Brush.verticalGradient(listOf(glow.copy(alpha = 0.18f), Color.Transparent), endY = 1200f)),
        containerColor = Color.Transparent,
        topBar = {
            FormTopBar(
                title = if (accountToEdit == null) stringResource(R.string.account_create_title) else stringResource(R.string.account_edit_title),
                onBack = {
                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                    onNavigateBack()
                }
            )
        },
        bottomBar = {
            FormPrimaryButton(
                text = if (accountToEdit == null) stringResource(R.string.account_create_title) else stringResource(R.string.account_save_changes),
                enabled = true,
                accent = accentColor,
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val finalName = name.trim().ifBlank { defaultAccountName }
                    val finalLast4 = last4Input.trim().take(4).ifBlank { null }
                    onSaveAccount(finalName, selectedType.key, parsedBalance, selectedColor, selectedIcon, isDefault, finalLast4)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            LuxuryAccountCard(
                name = name.ifBlank { defaultAccountName },
                type = selectedType,
                balance = parsedBalance,
                colorHex = selectedColor,
                iconName = selectedIcon,
                isDefault = isDefault,
                last4 = last4Input.ifBlank { null },
                currencySymbol = currencySymbol,
                locale = locale,
                isHapticsEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormEyebrow(text = stringResource(R.string.account_type_label), modifier = Modifier.padding(start = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountType.entries.forEach { type ->
                            FormSelectableChip(
                                label = stringResource(type.labelRes),
                                selected = selectedType == type,
                                accent = accentColor,
                                onClick = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    val previousDefault = selectedType.defaultIcon
                                    selectedType = type
                                    if (selectedIcon == previousDefault) selectedIcon = type.defaultIcon
                                },
                                leading = {
                                    Icon(
                                        imageVector = getAccountIconVector(type.defaultIcon),
                                        contentDescription = null,
                                        tint = if (selectedType == type) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormEyebrow(
                        text = if (selectedType == AccountType.CREDIT_CARD) stringResource(R.string.account_outstanding_balance_label) else stringResource(R.string.account_starting_balance_label)
                    )
                    FormHeroAmount(
                        value = balanceInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) balanceInput = input
                        },
                        currencySymbol = if (isNegativeBalance) "-$currencySymbol" else currencySymbol,
                        accent = balanceAccent,
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormSelectableChip(
                            label = stringResource(R.string.account_balance_positive),
                            selected = !isNegativeBalance,
                            accent = EmeraldIncome,
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isNegativeBalance = false
                            }
                        )
                        FormSelectableChip(
                            label = stringResource(R.string.account_balance_negative),
                            selected = isNegativeBalance,
                            accent = RoseExpense,
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isNegativeBalance = true
                            }
                        )
                    }
                    Text(
                        text = stringResource(R.string.account_starting_balance_desc),
                        style = TextStyle(fontFamily = DMSans, fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormGroup {
                        FormTextRow(
                            icon = LucideIcons.Wallet,
                            label = stringResource(R.string.account_name_label).lowercase().replaceFirstChar { it.uppercase() },
                            value = name,
                            onValueChange = { name = it },
                            placeholder = stringResource(R.string.account_name_placeholder),
                            accent = accentColor,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                        )
                        FormHairline()
                        FormTextRow(
                            icon = LucideIcons.Hash,
                            label = stringResource(R.string.account_last4_label).lowercase().replaceFirstChar { it.uppercase() },
                            value = last4Input,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) last4Input = it },
                            placeholder = stringResource(R.string.account_last4_placeholder),
                            accent = accentColor,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                        )
                        FormHairline()
                        FormRow(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isDefault = !isDefault
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = stringResource(R.string.account_set_primary_title),
                                        style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.account_set_primary_desc),
                                        style = TextStyle(fontFamily = DMSans, fontSize = 11.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
                                        checkedTrackColor = accentColor,
                                        checkedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.account_last4_desc),
                        style = TextStyle(fontFamily = DMSans, fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormEyebrow(text = stringResource(R.string.goals_select_color), modifier = Modifier.padding(start = 4.dp))
                    FormGroup {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CategoryColorRegistry.COLORS.forEach { colorLong ->
                                val isSelected = selectedColor == colorLong
                                val c = Color(colorLong.toInt())
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .then(if (isSelected) Modifier.border(1.5.dp, c, CircleShape).padding(4.dp) else Modifier)
                                        .clip(CircleShape)
                                        .background(c)
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedColor = colorLong
                                        }
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormEyebrow(text = stringResource(R.string.goals_select_icon))
                        Text(
                            text = if (isIconsExpanded) stringResource(R.string.less) else stringResource(R.string.more),
                            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    isIconsExpanded = !isIconsExpanded
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    FormGroup {
                        val visibleIcons = if (hideIcons) CategoryIconRegistry.ICONS.take(18) else CategoryIconRegistry.ICONS
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            visibleIcons.forEach { (iconName, iconVector) ->
                                val isSelected = selectedIcon == iconName
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        .then(if (isSelected) Modifier.border(1.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp)) else Modifier)
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedIcon = iconName
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = iconName,
                                        tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
