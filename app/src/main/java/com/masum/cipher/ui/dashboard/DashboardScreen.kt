package com.masum.cipher.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.masum.cipher.core.data.local.entity.TransactionEntity
import androidx.compose.ui.text.style.TextOverflow
import com.masum.cipher.core.data.local.pref.UserPreferences
import kotlinx.coroutines.launch
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.core.util.AppFormatters
import compose.icons.LucideIcons
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Lock
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.ArrowDown
import compose.icons.lucideicons.ArrowUp
import com.masum.cipher.core.domain.model.TransactionCategory
import kotlinx.coroutines.flow.collectLatest
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.X
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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


    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val currentVersionCode = 13
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
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(settings) {
        if (settings != null && !hasCheckedReview) {
            hasCheckedReview = true
            userPreferences.incrementAppLaunchCount()
            val currentLaunchCount = settings!!.appLaunchCount + 1
            
            // Trigger review prompt after 5 app launches
            if (currentLaunchCount >= 5 && !settings!!.hasPromptedReview) {
                val reviewManager = com.google.android.play.core.review.ReviewManagerFactory.create(context)
                val request = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                            flow.addOnCompleteListener { _ ->
                                coroutineScope.launch {
                                    userPreferences.setHasPromptedReview(true)
                                }
                            }
                        }
                    }
                }
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
    
    val maxToolbarHeight = 340.dp
    val toolbarHeightRangePx = with(androidx.compose.ui.platform.LocalDensity.current) { 180.dp.roundToPx().toFloat() }
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
                    item {
                        AnimatedVisibility(
                            visible = state.searchQuery.isEmpty(),
                            enter = fadeIn(tween(350, easing = FastOutSlowInEasing)) + expandVertically(tween(350, easing = FastOutSlowInEasing)),
                            exit = fadeOut(tween(250, easing = FastOutSlowInEasing)) + shrinkVertically(tween(250, easing = FastOutSlowInEasing))
                        ) {
                            val progress = if (toolbarHeightRangePx > 0f) {
                                1f - (kotlin.math.abs(toolbarOffsetHeightPx.value) / toolbarHeightRangePx)
                            } else 1f
                            Box(modifier = Modifier
                                .graphicsLayer { alpha = progress }
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val newHeight = (placeable.height * progress).toInt()
                                    layout(placeable.width, newHeight) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }
                                .clipToBounds()
                            ) {
                                BudgetPulseCard(
                                    spent = state.thisMonthExpenses,
                                    budget = state.monthlyBudget,
                                    onSetBudgetClick = {
                                        budgetInput = if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else ""
                                        showBudgetDialog = true
                                    }
                                )
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
                            GenesisEmptyState(onAddManual = { showAddSheet = true })
                        }
                    }
                } else {
                    groupedTransactions.forEach { (monthYear, transactions) ->
                        item(key = "header_$monthYear") {
                            Text(
                                text = monthYear.uppercase(),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
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

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Monthly Budget", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) budgetInput = it },
                    label = { Text("Limit (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        coroutineScope.launch {
                            userPreferences.setMonthlyBudget(amount)
                        }
                        showBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
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
                "4.7.0"
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
                    text = "Version $versionName is here! \uD83C\uDF89",
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
                    if (!hasSeen4_1) {
                        WhatsNewFeatureItem(
                            title = "Notification Tracking",
                            description = "Cipher can now automatically track transactions from your favorite UPI and banking apps using notifications.",
                            icon = LucideIcons.BellRing
                        )
                    }
                    WhatsNewFeatureItem(
                        title = "Push Notifications",
                        description = "Get notified when your spending crosses your budget limit, and get nudged to categorize 'Others' transactions.",
                        icon = LucideIcons.BellRing
                    )
                    WhatsNewFeatureItem(
                        title = "Daily & Monthly Summaries",
                        description = "A quick evening recap on days you spend money, and a full snapshot on the 1st of every month.",
                        icon = LucideIcons.Calendar
                    )
                    WhatsNewFeatureItem(
                        title = "Transaction Drafts",
                        description = "Accidentally switched apps while adding a transaction? Your amounts and text will be waiting for you.",
                        icon = LucideIcons.Plus
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
    toolbarOffsetHeightPx: Float = 0f,
    toolbarHeightRangePx: Float = 1f,
    maxToolbarHeight: androidx.compose.ui.unit.Dp = 340.dp
) {
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = balanceScale
                                    scaleY = balanceScale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .graphicsLayer { alpha = (scrollProgress - 0.5f).coerceAtLeast(0f) * 2f }
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        val currentHeight = (placeable.height * scrollProgress).toInt()
                                        layout(placeable.width, currentHeight) {
                                            placeable.placeRelative(0, currentHeight - placeable.height)
                                        }
                                    }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = LucideIcons.Calendar,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${AppFormatters.getPeriodLabel(selectedPeriod, transactions)} BALANCE".uppercase(),
                                        style = Typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            val balanceStringLength = totalBalance.toLong().toString().length
                            val balanceFontSize = remember(balanceStringLength) {
                                when {
                                    balanceStringLength >= 9 -> 36.sp
                                    balanceStringLength >= 7 -> 44.sp
                                    balanceStringLength >= 5 -> 54.sp
                                    else -> 64.sp
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    style = Typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                if (privacyMode) {
                                    Text(
                                        text = "••••••",
                                        style = Typography.displayLarge.copy(fontSize = balanceFontSize),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    AnimatedContent(
                                        targetState = scrollProgress < 0.5f && kotlin.math.abs(totalBalance) >= 1000,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                                        },
                                        label = "main_balance_transition"
                                    ) { isShortened ->
                                        if (isShortened) {
                                            val formattedBalance = remember(totalBalance) {
                                                when {
                                                    kotlin.math.abs(totalBalance) >= 1_000_000 -> String.format(
                                                        Locale.US, "%.1fM", totalBalance / 1_000_000f).replace(".0M", "M")
                                                    else -> String.format(Locale.US, "%.1fk", totalBalance / 1000f).replace(".0k", "k")
                                                }
                                            }
                                            Text(
                                                text = formattedBalance,
                                                style = Typography.displayLarge.copy(fontSize = balanceFontSize, letterSpacing = (-2).sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        } else {
                                            AnimatedNumberTicker(
                                                value = totalBalance,
                                                textStyle = Typography.displayLarge.copy(fontSize = balanceFontSize, letterSpacing = (-2).sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Row(
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
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isCollapsed = scrollProgress < 0.5f
                            StatItem(label = "INCOME", amount = income, color = EmeraldIncome, icon = LucideIcons.ArrowDown, privacyMode = privacyMode, isCollapsed = isCollapsed, modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.padding(horizontal = 8.dp).width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                            StatItem(label = "SPENT", amount = expense, color = RoseExpense, icon = LucideIcons.ArrowUp, privacyMode = privacyMode, isCollapsed = isCollapsed, modifier = Modifier.weight(1f))
                        }


                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f * (1f - scrollProgress)))
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { measurables, constraints ->
                    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                    val balancePlaceable = measurables[0].measure(looseConstraints)
                    
                    val statsExpandedWidth = constraints.maxWidth
                    val statsCollapsedWidth = (constraints.maxWidth * 0.62f).toInt()
                    val currentStatsWidth = statsCollapsedWidth + ((statsExpandedWidth - statsCollapsedWidth) * scrollProgress).toInt()
                    
                    val statsConstraints = looseConstraints.copy(minWidth = currentStatsWidth, maxWidth = currentStatsWidth)
                    val statsPlaceable = measurables[1].measure(statsConstraints)

                    val dividerPlaceable = measurables[2].measure(looseConstraints)

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        val topPadding = 32.dp.roundToPx()
                        val expBalanceY = topPadding.toFloat()
                        
                        val gap = (constraints.maxHeight - expBalanceY - balancePlaceable.height - statsPlaceable.height) / 2f

                        val expBalanceX = (constraints.maxWidth - balancePlaceable.width) / 2f
                        
                        val expStatsX = (constraints.maxWidth - statsPlaceable.width) / 2f
                        val expStatsY = expBalanceY + balancePlaceable.height + gap

                        val colBalanceX = 0f
                        val colBalanceY = (constraints.maxHeight - balancePlaceable.height) / 2f
                        
                        val colStatsX = (constraints.maxWidth - statsPlaceable.width).toFloat()
                        val colStatsY = (constraints.maxHeight - statsPlaceable.height) / 2f

                        val horizontalProgress = scrollProgress * scrollProgress
                        val verticalProgress = 1f - (1f - scrollProgress) * (1f - scrollProgress)

                        val currentBalanceX = colBalanceX + (expBalanceX - colBalanceX) * horizontalProgress
                        val currentBalanceY = colBalanceY + (expBalanceY - colBalanceY) * verticalProgress
                        
                        val currentStatsX = colStatsX + (expStatsX - colStatsX) * horizontalProgress
                        val currentStatsY = colStatsY + (expStatsY - colStatsY) * verticalProgress

                        balancePlaceable.placeRelative(currentBalanceX.toInt(), currentBalanceY.toInt())
                        statsPlaceable.placeRelative(currentStatsX.toInt(), currentStatsY.toInt())


                        if (scrollProgress < 0.8f) {
                            val balanceRightEdge = currentBalanceX + (balancePlaceable.width * balanceScale)
                            val dividerX = balanceRightEdge + 12.dp.toPx()
                            val dividerY = currentBalanceY + (balancePlaceable.height - dividerPlaceable.height) / 2f
                            dividerPlaceable.placeRelative(dividerX.toInt(), dividerY.toInt())
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
private fun StatItem(
    label: String, 
    amount: Double, 
    color: Color, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    privacyMode: Boolean, 
    isCollapsed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val formattedAmount = remember(amount, isCollapsed) {
        if (isCollapsed && amount >= 1000) {
            String.format(Locale.US, "%.1fk", amount / 1000f).replace(".0k", "k")
        } else {
            String.format(Locale.US, "%.0f", amount)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp).background(color.copy(alpha = 0.15f), shape = CircleShape).padding(2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label, 
                style = Typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedContent(
            targetState = if (privacyMode) "••••" else "₹$formattedAmount",
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "stat_amount_transition"
        ) { targetText ->
            Text(
                text = targetText,
                style = Typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetPulseCard(
    spent: Double,
    budget: Double,
    onSetBudgetClick: () -> Unit
) {
    VaultCard(
        modifier = Modifier.padding(horizontal = 24.dp),
        onClick = onSetBudgetClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = Typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (budget > 0) "₹${String.format(Locale.getDefault(), "%.0f", spent)} / ₹${String.format(
                        Locale.getDefault(), "%.0f", budget)}" else "Tap to set a budget",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (budget > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val progress = (spent / budget).toFloat().coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    label = "budgetProgress"
                )
                
                val barColor = when {
                    progress >= 0.9f -> RoseExpense
                    progress >= 0.75f -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(White10, RoundedCornerShape(4.dp))
                ) {
                    if (animatedProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(barColor, RoundedCornerShape(4.dp))
                        )
                    }
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
private fun GenesisEmptyState(onAddManual: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            LucideIcons.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your financial vault is ready.",
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cipher securely monitors your tracked apps and automatically adds your transactions here whenever you get a payment notification.\n\nMake a digital payment, or tap below to record one manually!",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddManual,
            modifier = Modifier,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Manual Transaction", style = Typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
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
