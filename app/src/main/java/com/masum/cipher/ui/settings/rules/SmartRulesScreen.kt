package com.masum.cipher.ui.settings.rules

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.entity.MerchantAliasEntity
import com.masum.cipher.core.domain.model.CategoryColorRegistry
import com.masum.cipher.core.domain.model.CategoryHelper
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowRight
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.Store
import compose.icons.lucideicons.Tag
import compose.icons.lucideicons.Trash2
import compose.icons.lucideicons.X

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRulesScreen(
    viewModel: SmartRulesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = state.selectedTab) { 2 }

    var isSearchOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editCategoryRule by remember { mutableStateOf<CategoryRuleEntity?>(null) }

    var showMerchantDialog by remember { mutableStateOf(false) }
    var editMerchantRule by remember { mutableStateOf<MerchantAliasEntity?>(null) }

    BackHandler(enabled = isSearchOpen) {
        isSearchOpen = false
        viewModel.handleIntent(SmartRulesContract.Intent.SetSearchQuery(""))
    }

    LaunchedEffect(isSearchOpen) {
        if (isSearchOpen) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.handleIntent(SmartRulesContract.Intent.SelectTab(pagerState.currentPage))
    }

    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab) {
            pagerState.animateScrollToPage(state.selectedTab)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SmartRulesContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SmartRulesContract.Effect.ShowUndoDeleteCategoryRule -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Rule deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(SmartRulesContract.Intent.RestoreCategoryRule(effect.rule))
                    }
                }
                is SmartRulesContract.Effect.ShowUndoDeleteMerchantRule -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Rule deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(SmartRulesContract.Intent.RestoreMerchantRule(effect.alias))
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchOpen) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.smart_rules_search_hint),
                                    style = Typography.bodyMedium.copy(fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.handleIntent(SmartRulesContract.Intent.SetSearchQuery(it)) },
                                singleLine = true,
                                textStyle = Typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }
                    } else {
                        Text(
                            stringResource(R.string.smart_rules_title), 
                            style = Typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performVibrate(state.isHapticsEnabled)
                        if (isSearchOpen) {
                            isSearchOpen = false
                            viewModel.handleIntent(SmartRulesContract.Intent.SetSearchQuery(""))
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (isSearchOpen) {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                view.performVibrate(state.isHapticsEnabled)
                                viewModel.handleIntent(SmartRulesContract.Intent.SetSearchQuery(""))
                            }) {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            view.performVibrate(state.isHapticsEnabled)
                            isSearchOpen = true
                        }) {
                            Icon(
                                imageVector = LucideIcons.Search,
                                contentDescription = stringResource(R.string.smart_rules_search_hint),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    view.performVibrate(state.isHapticsEnabled)
                    if (pagerState.currentPage == 0) {
                        editCategoryRule = null
                        showCategoryDialog = true
                    } else {
                        editMerchantRule = null
                        showMerchantDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(LucideIcons.Plus, contentDescription = stringResource(R.string.action_add_rule))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.smart_rules_tab_categories),
                            style = Typography.titleSmall.copy(
                                fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = LucideIcons.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        view.performVibrate(state.isHapticsEnabled)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.smart_rules_tab_merchants),
                            style = Typography.titleSmall.copy(
                                fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = LucideIcons.Store,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        val categoryRules = state.filteredCategoryRules
                        if (state.categoryRules.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.smart_rules_empty_categories),
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else if (categoryRules.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.smart_rules_no_search_results),
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(categoryRules, key = { it.merchantName }) { rule ->
                                    CategoryRuleItem(
                                        rule = rule,
                                        customCategories = state.customCategories,
                                        onClick = {
                                            view.performVibrate(state.isHapticsEnabled)
                                            editCategoryRule = rule
                                            showCategoryDialog = true
                                        },
                                        onDelete = {
                                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                                            viewModel.handleIntent(SmartRulesContract.Intent.DeleteCategoryRule(rule))
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        val merchantRules = state.filteredMerchantRules
                        if (state.merchantRules.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.smart_rules_empty_merchants),
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else if (merchantRules.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.smart_rules_no_search_results),
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(merchantRules, key = { it.rawName }) { alias ->
                                    MerchantRuleItem(
                                        alias = alias,
                                        onClick = {
                                            view.performVibrate(state.isHapticsEnabled)
                                            editMerchantRule = alias
                                            showMerchantDialog = true
                                        },
                                        onDelete = {
                                            view.performVibrate(state.isHapticsEnabled, isLongPress = true)
                                            viewModel.handleIntent(SmartRulesContract.Intent.DeleteMerchantRule(alias))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryRuleEditDialog(
            initialMerchant = editCategoryRule?.merchantName ?: "",
            initialCategory = editCategoryRule?.customCategory ?: "OTHERS",
            customCategories = state.customCategories,
            onDismiss = { showCategoryDialog = false },
            onSave = { merchant, category ->
                viewModel.handleIntent(SmartRulesContract.Intent.AddOrUpdateCategoryRule(merchant, category))
                showCategoryDialog = false
            }
        )
    }

    if (showMerchantDialog) {
        MerchantRuleEditDialog(
            initialRawMerchant = editMerchantRule?.rawName ?: "",
            initialCleanMerchant = editMerchantRule?.cleanName ?: "",
            onDismiss = { showMerchantDialog = false },
            onSave = { rawName, cleanName ->
                viewModel.handleIntent(SmartRulesContract.Intent.AddOrUpdateMerchantRule(rawName, cleanName))
                showMerchantDialog = false
            }
        )
    }
}

@Composable
private fun CategoryRuleItem(
    rule: CategoryRuleEntity,
    customCategories: List<CustomCategoryEntity> = emptyList(),
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryItem = remember(rule.customCategory, customCategories) {
        CategoryHelper.resolveCategory(rule.customCategory, customCategories)
    }
    
    VaultCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(categoryItem.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryItem.icon,
                    contentDescription = null,
                    tint = categoryItem.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.merchantName,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                val categoryDisplayName = if (categoryItem.titleRes != null) stringResource(categoryItem.titleRes) else categoryItem.displayName
                Text(
                    text = stringResource(R.string.smart_rules_always_as, categoryDisplayName),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Trash2,
                    contentDescription = stringResource(R.string.action_delete_rule),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MerchantRuleItem(
    alias: MerchantAliasEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val initialLetter = remember(alias.cleanName) {
        alias.cleanName.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "M"
    }
    val accentColor = remember(alias.cleanName) {
        val hash = alias.cleanName.trim().lowercase().hashCode()
        val colorHex = CategoryColorRegistry.COLORS[Math.abs(hash) % CategoryColorRegistry.COLORS.size]
        Color(colorHex)
    }

    VaultCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter,
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = accentColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alias.rawName,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowRight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = alias.cleanName,
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = accentColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Trash2,
                    contentDescription = stringResource(R.string.action_delete_rule),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRuleEditDialog(
    initialMerchant: String,
    initialCategory: String,
    customCategories: List<CustomCategoryEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var merchantName by remember { mutableStateOf(initialMerchant) }
    var selectedCategory by remember { mutableStateOf(CategoryHelper.resolveCategory(initialCategory, customCategories)) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMerchant.isEmpty()) stringResource(R.string.smart_rules_dialog_new) else stringResource(R.string.smart_rules_dialog_edit),
                style = Typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text(stringResource(R.string.merchant_name_label)) },
                    supportingText = { Text(stringResource(R.string.smart_rules_case_insensitive_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = initialMerchant.isEmpty()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { 
                        focusManager.clearFocus()
                        expanded = !expanded 
                    }
                ) {
                    val categoryTitle = selectedCategory.titleRes?.let { stringResource(it) } ?: selectedCategory.displayName
                    OutlinedTextField(
                        value = categoryTitle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        leadingIcon = {
                            Icon(
                                imageVector = selectedCategory.icon,
                                contentDescription = null,
                                tint = selectedCategory.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(
                            surface = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(4.dp)
                        ) {
                            val allCategories = remember(customCategories) {
                                CategoryHelper.getAllCategories(customCategories, includeIncome = false)
                            }
                            allCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (cat.titleRes != null) stringResource(cat.titleRes) else cat.displayName,
                                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (cat.isCustom) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(cat.color.copy(alpha = 0.15f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.custom_category_badge),
                                                        style = Typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = cat.color
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(cat.color.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                tint = cat.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (merchantName.isNotBlank()) {
                        onSave(merchantName, selectedCategory.name)
                    }
                },
                enabled = merchantName.isNotBlank()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
private fun MerchantRuleEditDialog(
    initialRawMerchant: String,
    initialCleanMerchant: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var rawMerchant by remember { mutableStateOf(initialRawMerchant) }
    var cleanMerchant by remember { mutableStateOf(initialCleanMerchant) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRawMerchant.isEmpty()) stringResource(R.string.smart_rules_dialog_new) else stringResource(R.string.smart_rules_dialog_edit),
                style = Typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = rawMerchant,
                    onValueChange = { rawMerchant = it },
                    label = { Text(stringResource(R.string.smart_rules_raw_merchant_label)) },
                    supportingText = { Text(stringResource(R.string.smart_rules_case_insensitive_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = initialRawMerchant.isEmpty()
                )

                OutlinedTextField(
                    value = cleanMerchant,
                    onValueChange = { cleanMerchant = it },
                    label = { Text(stringResource(R.string.smart_rules_clean_merchant_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (rawMerchant.isNotBlank() && cleanMerchant.isNotBlank()) {
                        onSave(rawMerchant, cleanMerchant)
                    }
                },
                enabled = rawMerchant.isNotBlank() && cleanMerchant.isNotBlank()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
