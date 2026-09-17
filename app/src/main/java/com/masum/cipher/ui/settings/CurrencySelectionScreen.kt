package com.masum.cipher.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.AppCurrency
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Pencil
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.X
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CurrencySelectionScreen(
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val cachedSettings = remember { userPreferences.getCachedSettings() }
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = cachedSettings)
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isHapticsEnabled = settings.isHapticsEnabled
    val currentCode = settings.currencyCode
    val currentSymbol = settings.currencySymbol

    var searchQuery by remember { mutableStateOf("") }
    var showCustomCreatorSheet by remember { mutableStateOf(false) }
    var editingCustomCurrency by remember { mutableStateOf<AppCurrency?>(null) }

    val defaultCurrency = remember { AppCurrency.detectDefault() }
    val allCurrencies = remember { AppCurrency.SUPPORTED_CURRENCIES }
    val savedCustomCurrencies = settings.customCurrencies

    val combinedCustomCurrencies = remember(savedCustomCurrencies, currentCode, currentSymbol, allCurrencies) {
        val list = savedCustomCurrencies.toMutableList()
        val isCurrentCustom = currentCode.isNotBlank() && allCurrencies.none { it.code.equals(currentCode, ignoreCase = true) }
        if (isCurrentCustom && list.none { it.code.equals(currentCode, ignoreCase = true) }) {
            list.add(0, AppCurrency.fromCode(currentCode, currentSymbol.ifBlank { currentCode }))
        }
        list
    }

    val activeCustom = combinedCustomCurrencies.firstOrNull { it.code.equals(currentCode, ignoreCase = true) }

    val sampleFormatted = remember(currentCode, currentSymbol, activeCustom) {
        if (activeCustom != null) {
            activeCustom.formatSample("14,250.75")
        } else {
            AppFormatters.formatCurrency(14250.75, currentSymbol)
        }
    }

    val sampleCompact = remember(currentCode, currentSymbol, activeCustom) {
        if (activeCustom != null) {
            activeCustom.formatSample("14.3k")
        } else {
            AppFormatters.formatCompactCurrency(14250.75, currentSymbol)
        }
    }

    val filteredCurrencies = remember(searchQuery, allCurrencies) {
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        if (query.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter {
                it.name.lowercase(Locale.ROOT).contains(query) ||
                it.code.lowercase(Locale.ROOT).contains(query) ||
                it.symbol.lowercase(Locale.ROOT).contains(query) ||
                it.countryCode.lowercase(Locale.ROOT).contains(query)
            }
        }
    }

    val filteredCustomCurrencies = remember(searchQuery, combinedCustomCurrencies) {
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        if (query.isEmpty()) {
            combinedCustomCurrencies
        } else {
            combinedCustomCurrencies.filter {
                it.name.lowercase(Locale.ROOT).contains(query) ||
                it.code.lowercase(Locale.ROOT).contains(query) ||
                it.symbol.lowercase(Locale.ROOT).contains(query)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dialog_currency_title),
                        style = Typography.titleMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(1.dp, White10, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIEW",
                            style = Typography.labelSmall.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$currentCode • $currentSymbol",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Standard",
                                style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                            Text(
                                text = sampleFormatted,
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Compact",
                                style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                            Text(
                                text = sampleCompact,
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = EmeraldIncome
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, White10, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = Typography.bodyMedium.copy(
                        fontFamily = DMSans,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search by currency, ISO code, or country...",
                                style = Typography.bodyMedium.copy(
                                     fontFamily = DMSans,
                                     fontSize = 14.sp,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        inner()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .clickable {
                                searchQuery = ""
                                focusManager.clearFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item(key = "create_action_card") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    editingCustomCurrency = null
                                    showCustomCreatorSheet = true
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Plus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Create Custom Currency",
                                        style = Typography.titleSmall.copy(
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Define your own ISO symbol, Crypto, or Unit",
                                        style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = LucideIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (filteredCustomCurrencies.isNotEmpty()) {
                    items(filteredCustomCurrencies, key = { "custom_${it.code}" }) { item ->
                        val isSelected = item.code.equals(currentCode, ignoreCase = true)
                        CurrencyScreenRow(
                            currency = item,
                            isSelected = isSelected,
                            isCustom = true,
                            onEdit = {
                                view.performVibrate(isHapticsEnabled)
                                editingCustomCurrency = item
                                showCustomCreatorSheet = true
                            },
                            onDelete = {
                                view.performVibrate(isHapticsEnabled)
                                coroutineScope.launch {
                                    userPreferences.removeCustomCurrency(item.code)
                                    if (isSelected) {
                                        userPreferences.setCurrency(defaultCurrency.code, defaultCurrency.symbol, defaultCurrency.isSuffix, defaultCurrency.hasSpace)
                                    }
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted ${item.name}",
                                        actionLabel = "UNDO",
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        userPreferences.addCustomCurrency(item)
                                        if (isSelected) {
                                            userPreferences.setCurrency(item.code, item.symbol, item.isSuffix, item.hasSpace)
                                        }
                                    }
                                }
                            },
                            onClick = {
                                view.performVibrate(isHapticsEnabled)
                                coroutineScope.launch {
                                    userPreferences.setCurrency(item.code, item.symbol, item.isSuffix, item.hasSpace)
                                }
                            }
                        )
                    }
                }

                items(filteredCurrencies, key = { it.code }) { item ->
                    val isSelected = item.code.equals(currentCode, ignoreCase = true)
                    val isDeviceDefault = item.code.equals(defaultCurrency.code, ignoreCase = true)
                    CurrencyScreenRow(
                        currency = item,
                        isSelected = isSelected,
                        isDeviceDefault = isDeviceDefault,
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            coroutineScope.launch {
                                userPreferences.setCurrency(item.code, item.symbol, item.isSuffix, item.hasSpace)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCustomCreatorSheet) {
        CreateCustomCurrencySheet(
            initialCurrency = editingCustomCurrency,
            onDismiss = {
                showCustomCreatorSheet = false
                editingCustomCurrency = null
            },
            onApply = { code, symbol, name, isSuffix, hasSpace ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                val newCustom = AppCurrency(
                    code = code,
                    symbol = symbol,
                    name = name.ifBlank { "$code Currency" },
                    countryCode = "CUSTOM",
                    isSuffix = isSuffix,
                    hasSpace = hasSpace
                )
                coroutineScope.launch {
                    val oldCurrency = editingCustomCurrency
                    if (oldCurrency != null) {
                        userPreferences.updateCustomCurrency(oldCurrency.code, newCustom)
                    } else {
                        userPreferences.addCustomCurrency(newCustom)
                        userPreferences.setCurrency(code, symbol, isSuffix, hasSpace)
                    }
                    showCustomCreatorSheet = false
                    editingCustomCurrency = null
                }
            }
        )
    }
}

@Composable
private fun CurrencyScreenRow(
    currency: AppCurrency,
    isSelected: Boolean,
    isCustom: Boolean = false,
    isDeviceDefault: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "currency_screen_row_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        label = "currency_screen_row_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else White10,
        label = "currency_screen_row_border"
    )

    val symbolFontSize = remember(currency.symbol) {
        when {
            currency.symbol.length > 4 -> 12.sp
            currency.symbol.length > 2 -> 13.5.sp
            else -> 17.sp
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 46.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.40f) else White10,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.symbol,
                    style = Typography.titleMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = symbolFontSize
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currency.name,
                        style = Typography.titleSmall.copy(
                            fontFamily = DMSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (isDeviceDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldIncome.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldIncome
                            )
                        }
                    }
                }
                Text(
                    text = "${currency.code} • ${currency.countryCode}",
                    style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isCustom && onEdit != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Pencil,
                        contentDescription = "Edit Custom Currency",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (isCustom && onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Trash2,
                        contentDescription = "Delete Custom Currency",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CreateCustomCurrencySheet(
    initialCurrency: AppCurrency? = null,
    onDismiss: () -> Unit,
    onApply: (code: String, symbol: String, name: String, isSuffix: Boolean, hasSpace: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    var customCodeInput by remember(initialCurrency) { mutableStateOf(initialCurrency?.code ?: "") }
    var customSymbolInput by remember(initialCurrency) { mutableStateOf(initialCurrency?.symbol ?: "") }
    var customNameInput by remember(initialCurrency) { mutableStateOf(initialCurrency?.name ?: "") }
    var isSuffix by remember(initialCurrency) { mutableStateOf(initialCurrency?.isSuffix ?: false) }
    var hasSpace by remember(initialCurrency) { mutableStateOf(initialCurrency?.hasSpace ?: false) }

    val finalCode = customCodeInput.trim().uppercase(Locale.ROOT)
    val finalSymbol = customSymbolInput.trim().ifEmpty { finalCode }
    val finalName = customNameInput.trim()
    val isValid = finalCode.isNotBlank()

    val previewText = remember(finalSymbol, isSuffix, hasSpace) {
        val sym = if (finalSymbol.isNotBlank()) finalSymbol else "¤"
        val space = if (hasSpace) " " else ""
        if (isSuffix) {
            "1,450.00$space$sym"
        } else {
            "$sym$space" + "1,450.00"
        }
    }

    val consumeOverscrollConnection = remember {
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
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .imeNestedScroll()
                .nestedScroll(consumeOverscrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialCurrency != null) "Edit Custom Currency" else "Create Custom Currency",
                    style = Typography.titleLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customCodeInput,
                    onValueChange = { if (it.length <= 6) customCodeInput = it.uppercase(Locale.ROOT) },
                    label = { Text("ISO Code (e.g. BTC, AED)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = customSymbolInput,
                    onValueChange = { if (it.length <= 6) customSymbolInput = it },
                    label = { Text("Symbol (e.g. ₿, د.إ)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                )
            }

            OutlinedTextField(
                value = customNameInput,
                onValueChange = { if (it.length <= 30) customNameInput = it },
                label = { Text("Full Name (Optional, e.g. Bitcoin)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )

            Text(
                text = "Symbol Position",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .border(1.dp, White10, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CurrencyOptionPill(
                    title = "Before (${if (finalSymbol.isNotBlank()) finalSymbol else "$"}100)",
                    isSelected = !isSuffix,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performVibrate(true)
                        isSuffix = false
                    }
                )
                CurrencyOptionPill(
                    title = "After (100${if (finalSymbol.isNotBlank()) finalSymbol else "$"})",
                    isSelected = isSuffix,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performVibrate(true)
                        isSuffix = true
                    }
                )
            }

            Text(
                text = "Symbol Spacing",
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .border(1.dp, White10, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CurrencyOptionPill(
                    title = "No Space",
                    isSelected = !hasSpace,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performVibrate(true)
                        hasSpace = false
                    }
                )
                CurrencyOptionPill(
                    title = "With Space",
                    isSelected = hasSpace,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performVibrate(true)
                        hasSpace = true
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Preview:",
                        style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = previewText,
                        style = Typography.titleMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = EmeraldIncome
                    )
                }
            }

            Button(
                onClick = {
                    if (isValid) {
                        onApply(finalCode, finalSymbol, finalName, isSuffix, hasSpace)
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (initialCurrency != null) "Save Currency Changes" else "Apply Currency to Vault",
                    style = Typography.titleSmall.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CurrencyOptionPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "pill_txt"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Typography.labelMedium.copy(
                fontFamily = DMSans,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
