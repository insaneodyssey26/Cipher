package com.masum.cipher.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
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
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.AnimatedNumberTicker
import com.masum.cipher.ui.components.StaggeredEntranceItem
import com.masum.cipher.ui.components.TimeSelectorDropdown
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.components.VaultMotion
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.SpaceGrotesk
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowDown
import compose.icons.lucideicons.ArrowUp
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Bug
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Info
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.RefreshCw
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Star
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.X
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
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val view = androidx.compose.ui.platform.LocalView.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false


    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()
    
    val currentVersionCode = 18
    val lastSeenWhatsNewVersionCode = settings?.lastSeenWhatsNewVersionCode ?: 0
    val shouldShowWhatsNew = settings != null && settings?.hasCompletedOnboarding == true && lastSeenWhatsNewVersionCode < currentVersionCode
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
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxToolbarHeight = androidx.compose.runtime.remember(configuration.screenHeightDp) {
        (configuration.screenHeightDp.dp * 0.32f).coerceIn(240.dp, 300.dp)
    }
    val minToolbarHeight = 140.dp
    val toolbarHeightRangePx = with(androidx.compose.ui.platform.LocalDensity.current) { (maxToolbarHeight - minToolbarHeight).roundToPx().toFloat() }
    val toolbarOffsetHeightPx = androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0f) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val wasAtTopAtFlingStart = remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.geometry.Offset.Zero
                val delta = available.y
                if (delta < 0) {
                    val previousOffset = toolbarOffsetHeightPx.value
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightRangePx, 0f)
                    val consumed = toolbarOffsetHeightPx.value - previousOffset
                    return androidx.compose.ui.geometry.Offset(0f, consumed)
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
                if (delta > 0 && source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput) {
                    val previousOffset = toolbarOffsetHeightPx.value
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightRangePx, 0f)
                    val consumedOffset = toolbarOffsetHeightPx.value - previousOffset
                    return androidx.compose.ui.geometry.Offset(0f, consumedOffset)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                wasAtTopAtFlingStart.value = (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0)
                return androidx.compose.ui.unit.Velocity.Zero
            }

            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity
            ): androidx.compose.ui.unit.Velocity {
                if (state.searchQuery.isNotEmpty()) return androidx.compose.ui.unit.Velocity.Zero
                val velocity = available.y
                if (velocity > 0f && wasAtTopAtFlingStart.value) {
                    animate(
                        initialValue = toolbarOffsetHeightPx.value,
                        targetValue = 0f,
                        initialVelocity = velocity,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { value, _ ->
                        toolbarOffsetHeightPx.value = value.coerceIn(-toolbarHeightRangePx, 0f)
                    }
                    return androidx.compose.ui.unit.Velocity(0f, velocity)
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery.isNotEmpty()) {
            toolbarOffsetHeightPx.value = 0f
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

            val groupedTransactions = remember(state.transactions) {
                state.transactions.groupBy {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
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
                                translationY = toolbarOffsetHeightPx.value
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
                                    val amountStr = "₹${String.format(java.util.Locale.getDefault(), "%.0f", subscription.amount)}"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${subscription.merchant}",
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
                                            androidx.compose.material3.TextButton(
                                                onClick = { viewModel.handleIntent(DashboardContract.Intent.SkipSubscription(subscription)) },
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Skip", color = MaterialTheme.colorScheme.error)
                                            }
                                            androidx.compose.material3.Button(
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
                                    val amountStr = "₹${String.format(java.util.Locale.getDefault(), "%.0f", subscription.amount)}"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${subscription.merchant}",
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
                                            androidx.compose.material3.TextButton(
                                                onClick = { viewModel.handleIntent(DashboardContract.Intent.SkipSubscription(subscription)) },
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Skip", color = MaterialTheme.colorScheme.error)
                                            }
                                            androidx.compose.material3.Button(
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

                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (state.transactions.isEmpty()) {
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
                } else {
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
                }
                }
                
                DashboardHero(
                    totalBalance = state.totalBalance,
                    income = state.totalIncome,
                    expense = state.totalExpenses,
                    selectedPeriod = state.selectedTimePeriod,
                    transactions = state.transactions,
                    onPeriodSelected = { period ->
                        viewModel.handleIntent(DashboardContract.Intent.SetTimePeriod(period))
                    },
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = { viewModel.handleIntent(DashboardContract.Intent.SearchTransactions(it)) },
                    privacyMode = privacyMode,
                    isHapticsEnabled = isHapticsEnabled,
                    expenseComparisonPercent = state.expenseComparisonPercent,
                    expenseComparisonLabel = state.expenseComparisonLabel,
                    onComparisonBadgeClick = { showComparisonExplanation = true },
                    toolbarOffsetHeightPx = toolbarOffsetHeightPx.value,
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
                                translationY = toolbarOffsetHeightPx.value
                            }
                            
                            val progress = if (toolbarHeightRangePx > 0f) {
                                (kotlin.math.abs(toolbarOffsetHeightPx.value) / toolbarHeightRangePx).coerceIn(0f, 1f)
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
            
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch { userPreferences.setHasPromptedReview(true) }
                        showRatingDialog = false
                    },
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Never ask again",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = LucideIcons.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        "Enjoying Cipher?",
                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "If Cipher is helping you manage your money, please take a moment to rate it. It really helps!",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                coroutineScope.launch { 
                                    userPreferences.increaseReviewPromptInterval()
                                    userPreferences.resetAppLaunchCount() 
                                }
                                showRatingDialog = false
                            },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text("Maybe later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        androidx.compose.material3.Button(
                            onClick = {
                                coroutineScope.launch { userPreferences.setHasPromptedReview(true) }
                                showRatingDialog = false
                                try {
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.masum.cipher")))
                                } catch (e: android.content.ActivityNotFoundException) {
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.masum.cipher")))
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Rate App", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
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
        val hasSeen4_1 = lastSeenWhatsNewVersionCode >= 9
        val context = androidx.compose.ui.platform.LocalContext.current
        val versionName = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (_: Exception) {
                "4.8.1"
            }
        }
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
                    text = "Version $versionName is here",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WhatsNewFeatureItem(
                        title = "Settings Search",
                        description = "Added a search bar at the top of the Settings screen to easily find options.",
                        icon = LucideIcons.Search
                    )
                    WhatsNewFeatureItem(
                        title = "Check for Updates",
                        description = "Added a button to check for app updates (found in Settings > About & Support).",
                        icon = LucideIcons.RefreshCw
                    )
                    WhatsNewFeatureItem(
                        title = "App Diagnostics",
                        description = "Added an option to view crash logs. You can copy and send them to the developer using the 'Contact Developer' button below it.",
                        icon = LucideIcons.Bug
                    )
                    WhatsNewFeatureItem(
                        title = "Settings Layout",
                        description = "Minor layout improvements in the Settings screen.",
                        icon = LucideIcons.Settings
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userPreferences.setLastSeenWhatsNewVersionCode(currentVersionCode)
                            showWhatsNewSheet = false
                            if (!hasSeen4_1) {
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
                        text = if (!hasSeen4_1) "Setup Tracking & Continue" else "Awesome",
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${String.format(java.util.Locale.US, "%.0f", currentExp)}",
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
                                    text = "Same days $label",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format(java.util.Locale.US, "%.0f", prevExp)}",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = "$arrow ₹${String.format(java.util.Locale.US, "%.0f", diff)} $actionWord (${String.format(java.util.Locale.US, "%.1f", kotlin.math.abs(percent))}%)",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = color
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
                                    text = "₹${String.format(java.util.Locale.US, "%.0f", currentExp)}",
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
                                    text = "₹${String.format(java.util.Locale.US, "%.0f", state.totalIncome)}",
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
}

@Composable
private fun DashboardHero(
    totalBalance: Double,
    income: Double,
    expense: Double,
    selectedPeriod: com.masum.cipher.core.domain.model.TimePeriod,
    transactions: List<TransactionEntity>,
    onPeriodSelected: (com.masum.cipher.core.domain.model.TimePeriod) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    privacyMode: Boolean,
    isHapticsEnabled: Boolean,
    expenseComparisonPercent: Double? = null,
    expenseComparisonLabel: String? = null,
    onComparisonBadgeClick: () -> Unit = {},
    toolbarOffsetHeightPx: Float = 0f,
    toolbarHeightRangePx: Float = 1f,
    maxToolbarHeight: androidx.compose.ui.unit.Dp = 340.dp
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val scrollProgress = if (toolbarHeightRangePx > 0f) {
        1f - (kotlin.math.abs(toolbarOffsetHeightPx) / toolbarHeightRangePx)
    } else 1f

    val targetHeight = if (searchQuery.isNotEmpty()) 80.dp else maxToolbarHeight + with(androidx.compose.ui.platform.LocalDensity.current) { toolbarOffsetHeightPx.toDp() }
    
    val heroHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = if (searchQuery.isNotEmpty()) 350 else 0),
        label = "HeroHeight"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (searchQuery.isEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = if (searchQuery.isEmpty()) 300 else 150, easing = FastOutSlowInEasing),
        label = "ContentAlpha"
    )

    val balanceScale = 0.6f + (0.4f * scrollProgress)
    val statsScale = 0.85f + (0.15f * scrollProgress)

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
            .clipToBounds()
    ) {
        if (searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 24.dp, end = 24.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = LucideIcons.Calendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${AppFormatters.getPeriodLabel(selectedPeriod, transactions)} BALANCE".uppercase(),
                                    style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (expenseComparisonPercent != null && kotlin.math.abs(expenseComparisonPercent) >= 0.5) {
                                val isLess = expenseComparisonPercent < 0.0
                                val badgeColor = if (isLess) EmeraldIncome else RoseExpense
                                val arrow = if (isLess) "▼" else "▲"
                                val labelSuffix = if (isLess) "less" else "more"
                                val compText = "$arrow ${String.format(java.util.Locale.US, "%.0f", kotlin.math.abs(expenseComparisonPercent))}% $labelSuffix"

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled)
                                            onComparisonBadgeClick()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = compText,
                                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                            } else {
                                val netSaved = income - expense
                                val isPastPeriod = selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_MONTH ||
                                    selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.LAST_WEEK ||
                                    selectedPeriod == com.masum.cipher.core.domain.model.TimePeriod.ALL_TIME
                                val (badgeText, badgeColor) = when {
                                    netSaved > 0 -> Pair("● Positive Flow", EmeraldIncome)
                                    expense > 0 && isPastPeriod -> Pair("● Summary", MaterialTheme.colorScheme.primary)
                                    expense > 0 -> Pair("● Active Flow", MaterialTheme.colorScheme.primary)
                                    else -> Pair("● No Activity", MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled)
                                            onComparisonBadgeClick()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                            }
                        }

                        val balanceStringLength = totalBalance.toLong().toString().length
                        val balanceFontSize = remember(balanceStringLength) {
                            when {
                                balanceStringLength >= 9 -> 36.sp
                                balanceStringLength >= 7 -> 44.sp
                                balanceStringLength >= 5 -> 54.sp
                                else -> 60.sp
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
                            Text(
                                text = "₹",
                                style = Typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            if (privacyMode) {
                                Text(
                                    text = "••••••",
                                    style = Typography.displayLarge.copy(fontSize = balanceFontSize),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                val isHugeAmount = kotlin.math.abs(totalBalance) >= 1_000_000
                                val shouldShorten = isHugeAmount || (scrollProgress < 0.5f && kotlin.math.abs(totalBalance) >= 1000)
                                
                                AnimatedContent(
                                    targetState = shouldShorten,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                    },
                                    label = "main_balance_transition"
                                ) { shortened ->
                                    if (shortened) {
                                        val formattedBalance = remember(totalBalance) {
                                            val absVal = kotlin.math.abs(totalBalance)
                                            when {
                                                absVal >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.2fB", totalBalance / 1_000_000_000f).replace(".00B", "B")
                                                absVal >= 1_000_000 -> String.format(java.util.Locale.US, "%.2fM", totalBalance / 1_000_000f).replace(".00M", "M")
                                                else -> String.format(java.util.Locale.US, "%.1fk", totalBalance / 1000f).replace(".0k", "k")
                                            }
                                        }
                                        Text(
                                            text = formattedBalance,
                                            style = Typography.displayLarge.copy(fontSize = balanceFontSize, letterSpacing = (-1).sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        AnimatedNumberTicker(
                                            value = totalBalance,
                                            textStyle = Typography.displayLarge.copy(fontSize = balanceFontSize, letterSpacing = (-1).sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = statsScale
                                    scaleY = statsScale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                                }
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            val isCollapsed = scrollProgress < 0.5f
                            CashFlowSegmentBar(income = income, expense = expense, privacyMode = privacyMode, isCollapsed = isCollapsed)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { measurables, constraints ->
                    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                    val headerConstraints = looseConstraints.copy(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)
                    val periodLabelPlaceable = measurables[0].measure(headerConstraints)
                    val balancePlaceable = measurables[1].measure(looseConstraints)
                    
                    val statsExpandedWidth = constraints.maxWidth
                    val balanceVisualCollapsedWidth = (balancePlaceable.width * 0.6f).toInt()
                    val gapBetween = 16.dp.roundToPx()
                    val dynamicCollapsedWidth = constraints.maxWidth - balanceVisualCollapsedWidth - gapBetween
                    val statsCollapsedWidth = dynamicCollapsedWidth.coerceIn(
                        (constraints.maxWidth * 0.50f).toInt(),
                        (constraints.maxWidth * 0.74f).toInt()
                    )
                    val currentStatsWidth = statsCollapsedWidth + ((statsExpandedWidth - statsCollapsedWidth) * scrollProgress).toInt()
                    
                    val statsConstraints = looseConstraints.copy(minWidth = currentStatsWidth, maxWidth = currentStatsWidth)
                    val statsPlaceable = measurables[2].measure(statsConstraints)

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        val periodLabelY = 10.dp.roundToPx()
                        if (scrollProgress > 0.4f) {
                            periodLabelPlaceable.placeRelative(0, periodLabelY)
                        }

                        val balanceXProgress = scrollProgress * scrollProgress
                        val statsXProgress = scrollProgress * scrollProgress

                        val expBalanceX = (constraints.maxWidth - balancePlaceable.width) / 2f
                        val colBalanceX = 0f
                        val currentBalanceX = colBalanceX + (expBalanceX - colBalanceX) * balanceXProgress

                        val expBalanceY = (periodLabelY + periodLabelPlaceable.height + 14.dp.roundToPx()).toFloat()
                        val colBalanceY = (constraints.maxHeight - balancePlaceable.height) / 2f
                        val currentBalanceY = colBalanceY + (expBalanceY - colBalanceY) * scrollProgress

                        val balanceVisualRight = currentBalanceX + balancePlaceable.width * balanceScale
                        val balanceVisualBottom = currentBalanceY + balancePlaceable.height * (0.5f + 0.5f * balanceScale)

                        val expStatsX = (constraints.maxWidth - statsPlaceable.width) / 2f
                        val colStatsX = (constraints.maxWidth - statsPlaceable.width).toFloat()
                        val currentStatsX = colStatsX + (expStatsX - colStatsX) * statsXProgress

                        val colStatsY = (constraints.maxHeight - statsPlaceable.height) / 2f
                        val maxAllowedStatsY = (constraints.maxHeight - statsPlaceable.height).toFloat().coerceAtLeast(0f)
                        val expStatsY = (expBalanceY + balancePlaceable.height + 14.dp.toPx()).coerceAtMost(maxAllowedStatsY)
                        val nominalStatsY = colStatsY + (expStatsY - colStatsY) * scrollProgress

                        val horizontalOverlap = (balanceVisualRight + 8.dp.toPx()) - currentStatsX
                        val currentStatsY = if (horizontalOverlap > 0f && scrollProgress > 0.05f) {
                            val safeSeparatedY = (balanceVisualBottom + 8.dp.toPx()).coerceAtMost(maxAllowedStatsY)
                            val overlapRatio = (horizontalOverlap / (balancePlaceable.width * balanceScale).coerceAtLeast(1f)).coerceIn(0f, 1f)
                            (nominalStatsY * (1f - overlapRatio) + safeSeparatedY * overlapRatio).coerceIn(0f, maxAllowedStatsY)
                        } else {
                            nominalStatsY.coerceIn(0f, maxAllowedStatsY)
                        }

                        balancePlaceable.placeRelative(currentBalanceX.toInt(), currentBalanceY.toInt())
                        statsPlaceable.placeRelative(currentStatsX.toInt(), currentStatsY.toInt())
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
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            .width(160.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(LucideIcons.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        
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
                                        Text("Search...", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            }, modifier = Modifier.size(20.dp)) {
                                Icon(LucideIcons.X, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    TimeSelectorDropdown(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = onPeriodSelected,
                        isHapticsEnabled = isHapticsEnabled,
                        iconOnly = true
                    )
                }
            }
        }
    }
}




@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    privacyMode: Boolean,
    onClick: () -> Unit
) {
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
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp)),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = "\"${transaction.note}\"",
                        style = Typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Text(
                text = if (privacyMode) "•••" else (if (transaction.isIncome) "+" else "-") + "₹${String.format(
                    Locale.getDefault(), "%.0f", transaction.amount)}",
                style = Typography.titleMedium,
                color = if (transaction.isIncome) EmeraldIncome else RoseExpense
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try changing the time filter above to see your older data.",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    val total = income + expense
    val incomeFraction = if (total > 0) (income / total).toFloat() else 0.5f
    
    val animatedIncomeRatio by animateFloatAsState(
        targetValue = incomeFraction,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 150f),
        label = "incomeRatio"
    )
    val barHeight by animateDpAsState(
        targetValue = if (isCollapsed) 4.dp else 8.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "barHeight"
    )

    fun formatAmount(value: Double): String {
        val absVal = kotlin.math.abs(value)
        return when {
            absVal >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.1fB", value / 1_000_000_000).replace(".0B", "B")
            absVal >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000).replace(".0M", "M")
            absVal >= 100_000 -> String.format(java.util.Locale.US, "%.1fk", value / 1000).replace(".0k", "k")
            else -> String.format(java.util.Locale.US, "%.0f", value)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // INCOME side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(EmeraldIncome.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(LucideIcons.ArrowDown, contentDescription = null, tint = EmeraldIncome, modifier = Modifier.size(12.dp))
                }
                
                AnimatedVisibility(visible = !isCollapsed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "INCOME",
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.width(6.dp))
                val formattedIncome = formatAmount(income)
                AnimatedContent(
                    targetState = if (privacyMode) "••••" else "₹$formattedIncome",
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "income_anim"
                ) { targetText ->
                    Text(
                        text = targetText,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = EmeraldIncome
                    )
                }
            }

            // EXPENSE side
            Row(verticalAlignment = Alignment.CenterVertically) {
                val formattedExpense = formatAmount(expense)
                AnimatedContent(
                    targetState = if (privacyMode) "••••" else "₹$formattedExpense",
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "expense_anim"
                ) { targetText ->
                    Text(
                        text = targetText,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = RoseExpense
                    )
                }
                
                AnimatedVisibility(visible = !isCollapsed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "EXPENSE",
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(RoseExpense.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(LucideIcons.ArrowUp, contentDescription = null, tint = RoseExpense, modifier = Modifier.size(12.dp))
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
