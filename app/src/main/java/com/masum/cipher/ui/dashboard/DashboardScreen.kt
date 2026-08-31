package com.masum.cipher.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.BuildConfig
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.AnimatedNumberTicker
import com.masum.cipher.ui.components.StaggeredEntranceItem
import com.masum.cipher.ui.components.TimeSelectorDropdown
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.TransactionListSkeleton
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.components.VaultMotion
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Activity
import compose.icons.lucideicons.ArrowDown
import compose.icons.lucideicons.ArrowUp
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.CalendarClock
import compose.icons.lucideicons.Info
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.SlidersHorizontal
import compose.icons.lucideicons.Star
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.X
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.core.net.toUri
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userPreferences: UserPreferences,
    onNavigateToManageApps: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locale = LocalLocale.current.platformLocale
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val view = androidx.compose.ui.platform.LocalView.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false


    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()
    
    val currentVersionCode = BuildConfig.VERSION_CODE
    val lastSeenWhatsNewVersionCode = settings?.lastSeenWhatsNewVersionCode ?: 0
    val shouldShowWhatsNew = settings != null && settings?.hasCompletedOnboarding == true && lastSeenWhatsNewVersionCode < 24
    var showWhatsNewSheet by remember(shouldShowWhatsNew) {
        mutableStateOf(shouldShowWhatsNew)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            if (effect is DashboardContract.Effect.ShowUndoDelete) {
                val result = snackbarHostState.showSnackbar(
                    message = "Transaction deleted",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    viewModel.handleIntent(DashboardContract.Intent.RestoreTransaction(effect.transaction))
                }
            }
        }
    }

    var hasCheckedReview by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showComparisonExplanation by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.transactions) {
        val activity = context as? android.app.Activity
        val intent = activity?.intent
        if (intent?.getStringExtra("navigate_to") == "transaction_details") {
            val transactionId = intent.getLongExtra("transaction_id", -1L)
            if (transactionId != -1L) {
                val tx = state.transactions.find { it.id == transactionId }
                if (tx != null) {
                    editingTransaction = tx
                    intent.removeExtra("navigate_to")
                    intent.removeExtra("transaction_id")
                }
            }
        }
    }
    LaunchedEffect(settings) {
        if (settings != null && !hasCheckedReview) {
            hasCheckedReview = true
            userPreferences.incrementAppLaunchCount()
            val currentLaunchCount = settings!!.appLaunchCount + 1
            
            if (currentLaunchCount >= settings!!.reviewPromptInterval && !settings!!.hasPromptedReview) {
                showRatingDialog = true
            }
        }
    }

    BackHandler(enabled = state.searchQuery.isNotEmpty()) {
        viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(""))
    }

    val shouldShowSkeleton by produceState(initialValue = false, key1 = state.isLoading, key2 = state.transactions.isEmpty()) {
        if (state.isLoading && state.transactions.isEmpty()) {
            kotlinx.coroutines.delay(200)
            value = true
        } else {
            value = false
        }
    }

    val mainScale by animateFloatAsState(
        targetValue = if (showAddSheet || editingTransaction != null) 0.93f else 1f,
        animationSpec = VaultMotion.LayoutSpring,
        label = "MainScale"
    )
    val mainCorner by animateDpAsState(
        targetValue = if (showAddSheet || editingTransaction != null) 32.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "MainCorner"
    )

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxToolbarHeight = remember(windowInfo.containerSize.height, density) {
        val heightDp = with(density) { windowInfo.containerSize.height.toDp() }
        (heightDp * 0.32f).coerceIn(240.dp, 300.dp)
    }
    val minToolbarHeight = 154.dp
    val toolbarHeightRangePx = with(density) { (maxToolbarHeight - minToolbarHeight).roundToPx().toFloat() }
    val toolbarOffsetHeightPx = androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val animatedToolbarOffset by animateFloatAsState(
        targetValue = toolbarOffsetHeightPx.floatValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "AnimatedToolbarOffset"
    )

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val nestedScrollConnection = remember(toolbarHeightRangePx) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.geometry.Offset.Zero
                val delta = available.y
                if (delta < 0) {
                    val previousOffset = toolbarOffsetHeightPx.floatValue
                    val newOffset = (previousOffset + delta).coerceIn(-toolbarHeightRangePx, 0f)
                    val consumed = newOffset - previousOffset
                    toolbarOffsetHeightPx.floatValue = newOffset
                    return androidx.compose.ui.geometry.Offset(0f, consumed)
                } else if (delta > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    val previousOffset = toolbarOffsetHeightPx.floatValue
                    val newOffset = (previousOffset + delta).coerceIn(-toolbarHeightRangePx, 0f)
                    val consumedOffset = newOffset - previousOffset
                    toolbarOffsetHeightPx.floatValue = newOffset
                    return androidx.compose.ui.geometry.Offset(0f, consumedOffset)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.geometry.Offset.Zero
                val delta = available.y
                if (delta > 0) {
                    return androidx.compose.ui.geometry.Offset(0f, delta)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.unit.Velocity.Zero
                val velocity = available.y
                if (velocity < 0f && toolbarOffsetHeightPx.floatValue > -toolbarHeightRangePx) {
                    toolbarOffsetHeightPx.floatValue = -toolbarHeightRangePx
                    return available
                }
                if (velocity > 0f && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 && toolbarOffsetHeightPx.floatValue < 0f) {
                    toolbarOffsetHeightPx.floatValue = 0f
                    return available
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }

            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity
            ): androidx.compose.ui.unit.Velocity {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.unit.Velocity.Zero
                val velocity = available.y
                if (velocity > 0f) {
                    return available
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery.isNotEmpty()) {
            toolbarOffsetHeightPx.floatValue = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { 
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 100.dp)
                ) 
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(mainCorner.coerceAtLeast(0.dp)))
                .graphicsLayer {
                    this.scaleX = mainScale
                    this.scaleY = mainScale
                }
        ) { padding ->
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            
            val searchTransition by animateFloatAsState(
                targetValue = if (state.searchQuery.isEmpty()) 0f else 1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "SearchTransition"
            )

            val groupedTransactions = remember(state.transactions, locale) {
                state.transactions.groupBy {
                    SimpleDateFormat("MMMM yyyy", locale).format(Date(it.timestamp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())
                        .layout { measurable, constraints ->
                            val extraHeight = toolbarHeightRangePx.toInt()
                            val placeable = measurable.measure(
                                constraints.copy(
                                    minHeight = constraints.maxHeight + extraHeight,
                                    maxHeight = constraints.maxHeight + extraHeight
                                )
                            )
                            layout(placeable.width, constraints.maxHeight) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                        .graphicsLayer {
                            if (searchTransition > 0f) {
                                val offsetPx = (maxToolbarHeight.toPx() - 80.dp.toPx())
                                translationY = -(offsetPx * searchTransition)
                            } else {
                                translationY = animatedToolbarOffset
                            }
                        },
                    contentPadding = PaddingValues(
                        top = maxToolbarHeight + statusBarHeight, 
                        bottom = 140.dp
                    )
                ) {
                    if (state.pendingSubscriptions.isNotEmpty() && state.searchQuery.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.BellRing,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Action Needed",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                
                                state.pendingSubscriptions.forEach { subscription ->
                                    val amountStr = "₹${String.format(locale, "%.0f", subscription.amount)}"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = subscription.merchant,
                                                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "Due for $amountStr",
                                                style = Typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TextButton(
                                                onClick = { viewModel.handleIntent(DashboardContract.Intent.SkipSubscription(subscription)) },
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Skip", color = MaterialTheme.colorScheme.error)
                                            }
                                            Button(
                                                onClick = { viewModel.handleIntent(DashboardContract.Intent.ApproveSubscription(subscription)) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                contentPadding = PaddingValues(horizontal = 16.dp)
                                            ) {
                                                Text("Log it", color = MaterialTheme.colorScheme.onError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                if (state.transactions.isNotEmpty()) {
                    if (state.filter.isActive) {
                        item(key = "active_filter_summary") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.SlidersHorizontal,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = buildFilterSummary(state.filter),
                                        style = Typography.labelMedium.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "Reset",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            viewModel.handleIntent(DashboardContract.Intent.ResetDashboardFilter)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    groupedTransactions.entries.forEachIndexed { groupIndex, (monthYear, transactions) ->
                        item(key = "header_$monthYear") {
                            Text(
                                text = monthYear.uppercase(),
                                style = Typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.padding(start = 24.dp, top = if (groupIndex == 0) 14.dp else 24.dp, bottom = 8.dp)
                            )
                        }
                        itemsIndexed(
                            items = transactions,
                            key = { _, t -> t.id }
                        ) { index, transaction ->
                            StaggeredEntranceItem(index = index) {
                                TransactionItem(
                                    transaction = transaction,
                                    privacyMode = privacyMode,
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled)
                                        editingTransaction = transaction
                                    }
                                )
                            }
                        }
                    }
                } else if (state.isLoading && shouldShowSkeleton) {
                    item(key = "dashboard_skeleton_loader") {
                        TransactionListSkeleton(
                            count = 6,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else if (!state.isLoading && state.transactions.isEmpty()) {
                    if (state.searchQuery.isNotEmpty()) {
                        item {
                            SearchEmptyState(query = state.searchQuery)
                        }
                    } else if (state.hasAnyTransactions) {
                        item {
                            FilterEmptyState(period = state.selectedTimePeriod)
                        }
                    } else {
                        item {
                            GenesisEmptyState()
                        }
                    }
                }
                }
                
                DashboardHero(
                    totalBalance = state.totalBalance,
                    income = state.totalIncome,
                    expense = state.totalExpenses,
                    selectedPeriod = state.selectedTimePeriod,
                    selectedTimeRange = state.selectedTimeRange,
                    transactions = state.transactions,
                    onPeriodSelected = { period, start, end ->
                        viewModel.handleIntent(DashboardContract.Intent.SetTimePeriod(period, start, end))
                    },
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = { viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(it)) },
                    filter = state.filter,
                    onOpenFilter = { showFilterSheet = true },
                    privacyMode = privacyMode,
                    isHapticsEnabled = isHapticsEnabled,
                    expenseComparisonPercent = state.expenseComparisonPercent,
                    onComparisonBadgeClick = { showComparisonExplanation = true },
                    toolbarOffsetHeightPx = animatedToolbarOffset,
                    toolbarHeightRangePx = toolbarHeightRangePx,
                    maxToolbarHeight = maxToolbarHeight
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .offset(y = maxToolbarHeight + statusBarHeight)
                        .graphicsLayer {
                            if (searchTransition > 0f) {
                                val offsetPx = (maxToolbarHeight.toPx() - 80.dp.toPx())
                                translationY = -(offsetPx * searchTransition)
                            } else {
                                translationY = animatedToolbarOffset
                            }
                            
                            val progress = if (toolbarHeightRangePx > 0f) {
                                (kotlin.math.abs(animatedToolbarOffset) / toolbarHeightRangePx).coerceIn(0f, 1f)
                            } else 0f
                            
                            alpha = if (searchTransition > 0f) 1f else progress
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }

    if (showAddSheet) {
        TransactionDetailsSheet(
            transaction = state.draftTransaction ?: TransactionEntity(
                amount = 0.0,
                merchant = "",
                currency = "INR",
                timestamp = System.currentTimeMillis(),
                category = "OTHERS",
                rawSms = null,
                isIncome = false
            ),
            onDismiss = { showAddSheet = false },
            onConfirm = { newTransaction ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                viewModel.handleIntent(DashboardContract.Intent.AddTransaction(newTransaction))
                viewModel.handleIntent(DashboardContract.Intent.UpdateDraftTransaction(null))
                showAddSheet = false
            },
            onDraftChange = { updatedDraft ->
                viewModel.handleIntent(DashboardContract.Intent.UpdateDraftTransaction(updatedDraft))
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    editingTransaction?.let { transaction ->
        TransactionDetailsSheet(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                view.performVibrate(isHapticsEnabled, isLongPress = true)
                viewModel.handleIntent(DashboardContract.Intent.UpdateTransaction(updated))
                editingTransaction = null
            },
            onDelete = {
                viewModel.handleIntent(DashboardContract.Intent.DeleteTransaction(transaction))
                editingTransaction = null
            },
            isHapticsEnabled = isHapticsEnabled
        )
    }

    if (showRatingDialog) {
        var animateIn by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            animateIn = true
        }
        
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                coroutineScope.launch { 
                    userPreferences.increaseReviewPromptInterval()
                    userPreferences.resetAppLaunchCount() 
                }
                showRatingDialog = false
            }
        ) {
            val scale by animateFloatAsState(
                targetValue = if (animateIn) 1f else 0.8f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "rating_scale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (animateIn) 1f else 0f,
                animationSpec = tween(300),
                label = "rating_alpha"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch { userPreferences.setHasPromptedReview(true) }
                        showRatingDialog = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Enjoying Cipher?",
                        style = Typography.titleLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "If Cipher helps you manage your spending, please consider leaving a review on the Play Store. Your support means the world!",
                        style = Typography.bodyMedium.copy(fontFamily = DMSans),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch { userPreferences.setHasPromptedReview(true) }
                            showRatingDialog = false
                            try {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, "market://details?id=com.masum.cipher".toUri()))
                            } catch (_: android.content.ActivityNotFoundException) {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.masum.cipher".toUri()))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Rate on Google Play",
                            style = Typography.labelLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                userPreferences.increaseReviewPromptInterval()
                                userPreferences.resetAppLaunchCount()
                            }
                            showRatingDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Maybe later",
                            style = Typography.labelLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    state.promptCategoryRuleFor?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(DashboardContract.Intent.DismissCategoryRulePrompt) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text(
                    text = "Save Category Rule?",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Do you want to always categorize future transactions from '${tx.merchant}' as '${tx.category}'?",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleIntent(DashboardContract.Intent.SaveCategoryRule(tx.merchant, tx.category))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yes, always", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.handleIntent(DashboardContract.Intent.DismissCategoryRulePrompt)
                }) {
                    Text("No, just this once", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showWhatsNewSheet) {
        val hasSeenV41 = lastSeenWhatsNewVersionCode >= 9
        val versionName = BuildConfig.VERSION_NAME
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    userPreferences.setHasSeenNotificationFeature(true)
                    userPreferences.setLastSeenWhatsNewVersionCode(currentVersionCode)
                    showWhatsNewSheet = false
                }
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = LucideIcons.BellRing,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Version $versionName is here 🎉",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WhatsNewFeatureItem(
                        title = "Domain Tabs in Insights",
                        description = "Explore spending, habits, and recurring bills across three focused tabs.",
                        icon = LucideIcons.Activity
                    )
                    WhatsNewFeatureItem(
                        title = "Financial Flow Trends",
                        description = "Track cash flow over time with interactive expense, income, and net trend lines.",
                        icon = LucideIcons.TrendingUp
                    )
                    WhatsNewFeatureItem(
                        title = "Advanced Filter Ledger",
                        description = "Filter by transaction type, multiple categories, and custom amount ranges.",
                        icon = LucideIcons.SlidersHorizontal
                    )
                    WhatsNewFeatureItem(
                        title = "Subscriptions Hub",
                        description = "Manage recurring bills with monthly estimates, due dates, and manual entry.",
                        icon = LucideIcons.CalendarClock
                    )
                    WhatsNewFeatureItem(
                        title = "Custom Date Ranges",
                        description = "Select custom timeframes and date intervals directly from the time picker.",
                        icon = LucideIcons.Calendar
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userPreferences.setLastSeenWhatsNewVersionCode(currentVersionCode)
                            showWhatsNewSheet = false
                            if (!hasSeenV41) {
                                onNavigateToManageApps()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (!hasSeenV41) "Setup Tracking & Continue" else "Cool Stuff",
                        style = Typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    if (showComparisonExplanation) {
        val comparisonSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showComparisonExplanation = false },
            sheetState = comparisonSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
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
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val percent = state.expenseComparisonPercent
                    val isLess = (percent ?: 0.0) < 0.0
                    val iconTint = if (percent != null) {
                        if (isLess) EmeraldIncome else RoseExpense
                    } else MaterialTheme.colorScheme.primary

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconTint.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLess) LucideIcons.ArrowDown else LucideIcons.ArrowUp,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Spending Trend",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.selectedTimePeriod.label,
                            style = Typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                VaultCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val percent = state.expenseComparisonPercent
                        val prevExp = state.previousPeriodExpenses
                        val currentExp = state.totalExpenses
                        val label = state.expenseComparisonLabel ?: "last period"

                        if (percent != null && kotlin.math.abs(percent) >= 0.5 && prevExp != null && prevExp > 0) {
                            val isLess = percent < 0.0
                            val diff = kotlin.math.abs(currentExp - prevExp)
                            val color = if (isLess) EmeraldIncome else RoseExpense
                            val arrow = if (isLess) "▼" else "▲"
                            val actionWord = if (isLess) "less" else "more"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "This period (so far)",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = AppFormatters.formatCompactCurrency(currentExp),
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Same days $label",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = AppFormatters.formatCompactCurrency(prevExp),
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(14.dp))

                            val percentStr = if (kotlin.math.abs(percent) > 9999.0) ">999%" else "${String.format(Locale.US, "%.1f", kotlin.math.abs(percent))}%"
                            Text(
                                text = "$arrow ${AppFormatters.formatCompactCurrency(diff)} $actionWord ($percentStr)",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = color,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isLess) {
                                    "You've spent less compared to the exact same days in $label."
                                } else {
                                    "You've spent more compared to the exact same days in $label."
                                },
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Spent",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%.0f", currentExp)}",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Income",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%.0f", state.totalIncome)}",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldIncome
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(12.dp))

                            val isPastPeriod = state.selectedTimePeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH ||
                                state.selectedTimePeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK ||
                                state.selectedTimePeriod == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME
                            Text(
                                text = if (isPastPeriod) "${state.selectedTimePeriod.label} Summary" else "Active Period Overview",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isPastPeriod) {
                                    "No transactions found in the preceding period to compare against."
                                } else {
                                    "No previous records found to compare against yet."
                                },
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Compares the exact same elapsed days (e.g. Day 1 to today) for a fair comparison.",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        coroutineScope.launch {
                            comparisonSheetState.hide()
                        }.invokeOnCompletion {
                            if (!comparisonSheetState.isVisible) {
                                showComparisonExplanation = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got it", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    if (showFilterSheet) {
        DashboardFilterSheet(
            currentFilter = state.filter,
            onApplyFilter = { newFilter ->
                viewModel.handleIntent(DashboardContract.Intent.SetDashboardFilter(newFilter))
            },
            onDismiss = { showFilterSheet = false },
            isHapticsEnabled = isHapticsEnabled
        )
    }
}

@Composable
private fun DashboardHero(
    totalBalance: Double,
    income: Double,
    expense: Double,
    selectedPeriod: com.masum.cipher.core.domain.model.TimePeriod,
    selectedTimeRange: com.masum.cipher.core.domain.model.TimeRange? = null,
    transactions: List<TransactionEntity>,
    onPeriodSelected: (com.masum.cipher.core.domain.model.TimePeriod, Long?, Long?) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    filter: DashboardFilter = DashboardFilter(),
    onOpenFilter: () -> Unit = {},
    privacyMode: Boolean,
    isHapticsEnabled: Boolean,
    expenseComparisonPercent: Double? = null,
    onComparisonBadgeClick: () -> Unit = {},
    toolbarOffsetHeightPx: Float = 0f,
    toolbarHeightRangePx: Float = 1f,
    maxToolbarHeight: androidx.compose.ui.unit.Dp = 340.dp
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val locale = LocalLocale.current.platformLocale
    val scrollProgress = if (toolbarHeightRangePx > 0f) {
        1f - (kotlin.math.abs(toolbarOffsetHeightPx) / toolbarHeightRangePx)
    } else 1f

    val searchHeroHeight by animateDpAsState(
        targetValue = if (searchQuery.isNotEmpty()) 80.dp else maxToolbarHeight,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "SearchHeroHeight"
    )
    val heroHeight = if (searchQuery.isNotEmpty()) searchHeroHeight else (maxToolbarHeight + with(LocalDensity.current) { toolbarOffsetHeightPx.toDp() })

    val contentAlpha by animateFloatAsState(
        targetValue = if (searchQuery.isEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = if (searchQuery.isEmpty()) 300 else 150, easing = FastOutSlowInEasing),
        label = "ContentAlpha"
    )

    val balanceScale = 0.6f + (0.4f * scrollProgress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .height(heroHeight)
    ) {
        if (searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp, start = 24.dp, end = 24.dp)
                    .graphicsLayer { alpha = contentAlpha }
            ) {
                androidx.compose.ui.layout.Layout(
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = ((scrollProgress - 0.4f) / 0.6f).coerceIn(0f, 1f)
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Calendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                val periodLabel = if (selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.CUSTOM && selectedTimeRange != null && selectedTimeRange.startTime > 0L) {
                                    val sdf = SimpleDateFormat("MMM d", locale)
                                    "${sdf.format(Date(selectedTimeRange.startTime))} – ${sdf.format(Date(selectedTimeRange.endTime))}"
                                } else {
                                    AppFormatters.getPeriodLabel(selectedPeriod, transactions)
                                }
                                Text(
                                    text = "$periodLabel BALANCE".uppercase(),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (expenseComparisonPercent != null && kotlin.math.abs(expenseComparisonPercent) >= 0.5) {
                                val isLess = expenseComparisonPercent < 0.0
                                val badgeColor = if (isLess) EmeraldIncome else RoseExpense
                                val labelSuffix = if (isLess) "less" else "more"
                                val percentFormatted = if (kotlin.math.abs(expenseComparisonPercent) > 999) {
                                    ">999"
                                } else {
                                    String.format(Locale.US, "%.0f", kotlin.math.abs(expenseComparisonPercent))
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(badgeColor.copy(alpha = 0.12f))
                                        .border(
                                            width = 1.dp,
                                            color = badgeColor.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            onComparisonBadgeClick()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLess) LucideIcons.ArrowDown else LucideIcons.ArrowUp,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "$percentFormatted% $labelSuffix",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = badgeColor
                                    )
                                }
                            } else {
                                val netSaved = income - expense
                                val isPastPeriod = selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH ||
                                    selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK ||
                                    selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME
                                val (badgeText, badgeColor) = when {
                                    netSaved > 0 -> Pair("Positive Flow", EmeraldIncome)
                                    expense > 0 && isPastPeriod -> Pair("Summary", MaterialTheme.colorScheme.primary)
                                    expense > 0 -> Pair("Active Flow", MaterialTheme.colorScheme.primary)
                                    else -> Pair("No Activity", MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(badgeColor.copy(alpha = 0.10f))
                                        .border(
                                            width = 1.dp,
                                            color = badgeColor.copy(alpha = 0.20f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            onComparisonBadgeClick()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Text(
                                        text = badgeText,
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = badgeColor
                                    )
                                }
                            }
                        }

                        val formattedBalance = remember(totalBalance, locale) {
                            val absVal = kotlin.math.abs(totalBalance)
                            val sign = if (totalBalance < 0) "-" else ""
                            when {
                                absVal >= 1_000_000_000_000_000.0 -> "₹$sign" + String.format(Locale.US, "%.2fQ", absVal / 1_000_000_000_000_000.0).replace(".00Q", "Q")
                                absVal >= 1_000_000_000_000.0 -> "₹$sign" + String.format(Locale.US, "%.2fT", absVal / 1_000_000_000_000.0).replace(".00T", "T")
                                absVal >= 1_000_000_000.0 -> "₹$sign" + String.format(Locale.US, "%.2fB", absVal / 1_000_000_000.0).replace(".00B", "B")
                                absVal >= 1_000_000.0 -> "₹$sign" + String.format(Locale.US, "%.2fM", absVal / 1_000_000.0).replace(".00M", "M")
                                absVal >= 100_000.0 -> "₹$sign" + String.format(Locale.US, "%.1fk", absVal / 1000.0).replace(".0k", "k")
                                else -> "₹$sign" + String.format(locale, "%,.0f", absVal)
                            }
                        }

                        val balanceFontSize = remember(formattedBalance.length) {
                            when {
                                formattedBalance.length > 14 -> 28.sp
                                formattedBalance.length > 10 -> 36.sp
                                formattedBalance.length > 8 -> 44.sp
                                else -> 52.sp
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer {
                                scaleX = balanceScale
                                scaleY = balanceScale
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                            }
                        ) {
                            if (privacyMode) {
                                Text(
                                    text = "₹••••••",
                                    style = Typography.displayLarge.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = balanceFontSize,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                val isHugeAmount = kotlin.math.abs(totalBalance) >= 100_000

                                if (isHugeAmount) {
                                    AnimatedContent(
                                        targetState = formattedBalance,
                                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                                        label = "huge_balance_fade"
                                    ) { targetText ->
                                        Text(
                                            text = targetText,
                                            style = Typography.displayLarge.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = balanceFontSize,
                                                letterSpacing = (-1.2).sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹",
                                            style = Typography.headlineMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                        if (totalBalance < 0) {
                                            Text(
                                                text = "-",
                                                style = Typography.displayLarge.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = balanceFontSize,
                                                    letterSpacing = (-1.2).sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        AnimatedNumberTicker(
                                            value = kotlin.math.abs(totalBalance),
                                            textStyle = Typography.displayLarge.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = balanceFontSize,
                                                letterSpacing = (-1.2).sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        val exitAlpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                        val exitProgress = exitAlpha * exitAlpha * (3f - 2f * exitAlpha)

                        val enterAlpha = ((0.65f - scrollProgress) / 0.65f).coerceIn(0f, 1f)
                        val enterProgress = enterAlpha * enterAlpha * (3f - 2f * enterAlpha)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = exitProgress
                                    translationX = -(1f - exitProgress) * 70.dp.toPx()
                                }
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            CashFlowSegmentBar(
                                income = income,
                                expense = expense,
                                privacyMode = privacyMode,
                                isCollapsed = false
                            )
                        }

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = enterProgress
                                    translationX = (1f - enterProgress) * 70.dp.toPx()
                                }
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 7.dp, horizontal = 12.dp)
                        ) {
                            CashFlowSegmentBar(
                                income = income,
                                expense = expense,
                                privacyMode = privacyMode,
                                isCollapsed = true
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { measurables, constraints ->
                    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                    val headerConstraints = looseConstraints.copy(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)
                    val periodLabelPlaceable = measurables[0].measure(headerConstraints)
                    val balancePlaceable = measurables[1].measure(looseConstraints)
                    val expandedStatsPlaceable = measurables[2].measure(looseConstraints.copy(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth))
                    
                    val balanceVisualCollapsedWidth = (balancePlaceable.width * 0.62f).toInt()
                    val gapBetween = 16.dp.roundToPx()
                    val maxSafeCollapsedWidth = (constraints.maxWidth - balanceVisualCollapsedWidth - gapBetween).coerceAtLeast(100)
                    val collapsedStatsConstraints = looseConstraints.copy(
                        minWidth = (maxSafeCollapsedWidth * 0.85f).toInt(),
                        maxWidth = maxSafeCollapsedWidth
                    )
                    val collapsedStatsPlaceable = measurables[3].measure(collapsedStatsConstraints)

                    val progress = scrollProgress.coerceIn(0f, 1f)

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        val periodLabelY = 10.dp.roundToPx()
                        if (progress > 0.4f) {
                            periodLabelPlaceable.placeRelative(0, periodLabelY)
                        }

                        val expBalanceX = (constraints.maxWidth - balancePlaceable.width) / 2f
                        val colBalanceX = 0f
                        val currentBalanceX = colBalanceX + (expBalanceX - colBalanceX) * progress

                        val expBalanceY = (periodLabelY + periodLabelPlaceable.height + 14.dp.roundToPx()).toFloat()
                        val colBalanceY = (constraints.maxHeight - balancePlaceable.height) / 2f
                        val currentBalanceY = colBalanceY + (expBalanceY - colBalanceY) * progress

                        balancePlaceable.placeRelative(currentBalanceX.toInt(), currentBalanceY.toInt())

                        val balanceVisualHeight = balancePlaceable.height * balanceScale
                        val balanceBottom = expBalanceY + balanceVisualHeight
                        val remainingSpace = constraints.maxHeight - balanceBottom
                        val maxAllowedExpStatsY = (constraints.maxHeight - expandedStatsPlaceable.height).toFloat().coerceAtLeast(0f)
                        val expStatsY = (balanceBottom + (remainingSpace - expandedStatsPlaceable.height) / 2f).coerceIn(0f, maxAllowedExpStatsY)

                        val exitAlpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                        if (exitAlpha > 0.01f) {
                            expandedStatsPlaceable.placeRelative(0, expStatsY.toInt())
                        }

                        val enterAlpha = ((0.65f - scrollProgress) / 0.65f).coerceIn(0f, 1f)
                        if (enterAlpha > 0.01f) {
                            val colStatsX = (constraints.maxWidth - collapsedStatsPlaceable.width)
                            val colStatsY = (constraints.maxHeight - collapsedStatsPlaceable.height) / 2f
                            collapsedStatsPlaceable.placeRelative(colStatsX, colStatsY.toInt())
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "cipher.",
                    style = Typography.titleLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var textFieldValue by remember { 
                        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(searchQuery)) 
                    }
                    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

                    LaunchedEffect(searchQuery) {
                        if (searchQuery.isEmpty() && textFieldValue.text.isNotEmpty()) {
                            textFieldValue = androidx.compose.ui.text.input.TextFieldValue("")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(135.dp)
                            .height(38.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(19.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(19.dp))
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(LucideIcons.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        androidx.compose.foundation.text.BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onSearchQueryChanged(newValue.text)
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (textFieldValue.text.isEmpty()) {
                                        Text("Search...", style = Typography.bodyMedium.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        if (textFieldValue.text.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { 
                                textFieldValue = androidx.compose.ui.text.input.TextFieldValue("")
                                onSearchQueryChanged("") 
                                focusManager.clearFocus()
                            }, modifier = Modifier.size(18.dp)) {
                                Icon(LucideIcons.X, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    if (filter.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (filter.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onOpenFilter()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.SlidersHorizontal,
                                contentDescription = "Filter",
                                tint = if (filter.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (filter.isActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 1.dp, end = 1.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                            )
                        }
                    }

                    TimeSelectorDropdown(
                        selectedPeriod = selectedPeriod,
                        selectedTimeRange = selectedTimeRange,
                        onPeriodSelected = onPeriodSelected,
                        isHapticsEnabled = isHapticsEnabled,
                        iconOnly = true
                    )
                }
            }
        }
    }
}

private fun buildFilterSummary(filter: DashboardFilter): String {
    val parts = mutableListOf<String>()
    when (filter.type) {
        DashboardContract.FilterType.EXPENSE -> parts.add("Expenses")
        DashboardContract.FilterType.INCOME -> parts.add("Income")
        DashboardContract.FilterType.ALL -> {}
    }
    if (filter.selectedCategories.isNotEmpty()) {
        if (filter.selectedCategories.size == 1) {
            parts.add(filter.selectedCategories.first())
        } else {
            parts.add("${filter.selectedCategories.size} Categories")
        }
    }
    if (filter.minAmount != null && filter.maxAmount != null) {
        parts.add("₹${filter.minAmount.toInt()} – ₹${filter.maxAmount.toInt()}")
    } else if (filter.minAmount != null) {
        parts.add("> ₹${filter.minAmount.toInt()}")
    } else if (filter.maxAmount != null) {
        parts.add("< ₹${filter.maxAmount.toInt()}")
    }
    return parts.joinToString(" • ")
}




@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    privacyMode: Boolean,
    onClick: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    VaultCard(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        onClick = onClick,
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            val category = TransactionCategory.fromString(transaction.category)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(category.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("d MMM, HH:mm", locale).format(Date(transaction.timestamp)),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = "\"${transaction.note}\"",
                        style = Typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            val amountFormatted = String.format(locale, "%,.0f", transaction.amount)
            Text(
                text = if (privacyMode) "•••" else "${if (transaction.isIncome) "+₹" else "₹-"}$amountFormatted",
                style = Typography.titleMedium.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold
                ),
                color = if (transaction.isIncome) EmeraldIncome else RoseExpense,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun GenesisEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VaultCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        LucideIcons.BellRing,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Listening for payments",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Go ahead, make a digital payment. Cipher will catch the notification and log it here instantly.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tap + below to add a past transaction manually",
            style = Typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            LucideIcons.Search,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No transactions found for '$query'",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilterEmptyState(period: com.masum.cipher.core.domain.model.TimePeriod) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            LucideIcons.Calendar,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No transactions found for ${AppFormatters.getPeriodLabel(period, emptyList()).lowercase()}",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try changing the time filter above to see your older data.",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WhatsNewFeatureItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp).size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}



@Composable
private fun CashFlowSegmentBar(
    income: Double,
    expense: Double,
    privacyMode: Boolean,
    isCollapsed: Boolean
) {
    val locale = LocalLocale.current.platformLocale
    val total = income + expense
    val incomeFraction = if (total > 0) (income / total).toFloat() else 0.5f
    
    val animatedIncomeRatio by animateFloatAsState(
        targetValue = incomeFraction,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 150f),
        label = "incomeRatio"
    )
    val barHeight = if (isCollapsed) 4.dp else 8.dp

    fun formatAmount(value: Double): String {
        val absVal = kotlin.math.abs(value)
        return when {
            absVal >= 1_000_000_000_000_000.0 -> String.format(Locale.US, "%.1fQ", absVal / 1_000_000_000_000_000.0).replace(".0Q", "Q")
            absVal >= 1_000_000_000_000.0 -> String.format(Locale.US, "%.1fT", absVal / 1_000_000_000_000.0).replace(".0T", "T")
            absVal >= 1_000_000_000.0 -> String.format(Locale.US, "%.1fB", absVal / 1_000_000_000.0).replace(".0B", "B")
            absVal >= 1_000_000.0 -> String.format(Locale.US, "%.1fM", absVal / 1_000_000.0).replace(".0M", "M")
            absVal >= 100_000.0 -> String.format(Locale.US, "%.1fk", absVal / 1000.0).replace(".0k", "k")
            else -> String.format(locale, "%,.0f", absVal)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isCollapsed) 5.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCollapsed) 17.dp else 18.dp)
                        .background(EmeraldIncome.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowDown,
                        contentDescription = null,
                        tint = EmeraldIncome,
                        modifier = Modifier.size(if (isCollapsed) 11.5.dp else 12.dp)
                    )
                }
                
                if (!isCollapsed) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "INCOME",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Spacer(Modifier.width(if (isCollapsed) 4.dp else 6.dp))
                val formattedIncome = formatAmount(income)
                AnimatedContent(
                    targetState = if (privacyMode) "••••" else "₹$formattedIncome",
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "income_fade"
                ) { targetText ->
                    Text(
                        text = targetText,
                        style = Typography.titleMedium.copy(
                            fontSize = if (isCollapsed) 14.5.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = EmeraldIncome,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                val formattedExpense = formatAmount(expense)
                AnimatedContent(
                    targetState = if (privacyMode) "••••" else "₹$formattedExpense",
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "expense_fade"
                ) { targetText ->
                    Text(
                        text = targetText,
                        style = Typography.titleMedium.copy(
                            fontSize = if (isCollapsed) 14.5.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = RoseExpense,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (!isCollapsed) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "EXPENSE",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Spacer(Modifier.width(if (isCollapsed) 4.dp else 6.dp))
                Box(
                    modifier = Modifier
                        .size(if (isCollapsed) 17.dp else 18.dp)
                        .background(RoseExpense.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowUp,
                        contentDescription = null,
                        tint = RoseExpense,
                        modifier = Modifier.size(if (isCollapsed) 11.5.dp else 12.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            if (total > 0) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedIncomeRatio.coerceAtLeast(0.01f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(EmeraldIncome.copy(alpha = 0.7f), EmeraldIncome)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - animatedIncomeRatio).coerceAtLeast(0.01f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(RoseExpense, RoseExpense.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                )
            }
        }
    }
}
