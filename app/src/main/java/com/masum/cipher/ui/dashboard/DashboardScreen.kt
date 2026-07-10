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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
import com.masum.cipher.core.util.performVibrate
import compose.icons.LucideIcons
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.ChartBar
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Lock
import com.masum.cipher.core.domain.model.TransactionCategory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userPreferences: UserPreferences,
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToManageApps: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by userPreferences.settingsFlow.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val view = androidx.compose.ui.platform.LocalView.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true
    val privacyMode = settings?.isPrivacyModeEnabled ?: false


    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }


    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val shouldShowPopup = settings != null && settings?.hasCompletedOnboarding == true && settings?.hasSeenNotificationFeature == false
    var showNotificationFeatureSheet by remember(shouldShowPopup) {
        mutableStateOf(shouldShowPopup)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {

                item {
                    DashboardHero(
                        totalBalance = state.totalBalance,
                        income = state.totalIncome,
                        expense = state.totalExpenses,
                        selectedPeriod = state.selectedTimePeriod,
                        onPeriodSelected = { period ->
                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                            viewModel.handleIntent(DashboardContract.Intent.SetTimePeriod(period))
                        },
                        privacyMode = privacyMode,
                        isHapticsEnabled = isHapticsEnabled
                    )
                }


                item {
                    BudgetPulseCard(
                        spent = state.thisMonthExpenses,
                        budget = state.monthlyBudget,
                        onSetBudgetClick = {
                            budgetInput = if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else ""
                            showBudgetDialog = true
                        }
                    )
                }


                item {
                    Text(
                        text = "RECENT ACTIVITY",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp)
                    )
                }


                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (state.transactions.isEmpty()) {
                    item {
                        GenesisEmptyState(onAddManual = { showAddSheet = true })
                    }
                } else {
                    itemsIndexed(
                        items = state.transactions,
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
    }

    if (showAddSheet) {
        TransactionDetailsSheet(
            transaction = TransactionEntity(
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
                showAddSheet = false
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Monthly Budget", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) budgetInput = it },
                    label = { Text("Limit (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
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
                androidx.compose.material3.TextButton(onClick = { showBudgetDialog = false }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                }
            }
        )
    }

    if (showNotificationFeatureSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    userPreferences.setHasSeenNotificationFeature(true)
                }
                showNotificationFeatureSheet = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = LucideIcons.BellRing,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                val context = androidx.compose.ui.platform.LocalContext.current
                val versionName = remember {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (e: Exception) {
                        "4.1.0"
                    }
                }
                Text(
                    text = "Version $versionName is here! \uD83C\uDF89\nNew Feature: Notification Tracking",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cipher can now automatically track transactions from your favorite UPI and banking apps using notifications.",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userPreferences.setHasSeenNotificationFeature(true)
                            showNotificationFeatureSheet = false
                            onNavigateToManageApps()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Setup Now", style = Typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            userPreferences.setHasSeenNotificationFeature(true)
                        }
                        showNotificationFeatureSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onPeriodSelected: (com.masum.cipher.core.domain.model.TimePeriod) -> Unit,
    privacyMode: Boolean,
    isHapticsEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        EmeraldIncome.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "TOTAL BALANCE",
                style = Typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))

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
                        style = Typography.displayLarge.copy(fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    AnimatedNumberTicker(
                        value = totalBalance,
                        textStyle = Typography.displayLarge.copy(
                            fontSize = 64.sp,
                            letterSpacing = (-2).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(vertical = 20.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "INCOME", amount = income, color = EmeraldIncome, privacyMode = privacyMode)
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                StatItem(label = "EXPENSES", amount = expense, color = RoseExpense, privacyMode = privacyMode)
            }
        }
        

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "cipher.",
                style = Typography.titleLarge.copy(
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            TimeSelectorDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                isHapticsEnabled = isHapticsEnabled,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, amount: Double, color: Color, privacyMode: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (privacyMode) "••••" else "₹${String.format("%.0f", amount)}",
            style = Typography.titleMedium,
            color = color
        )
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
                    text = if (budget > 0) "₹${String.format("%.0f", spent)} / ₹${String.format("%.0f", budget)}" else "Tap to set a budget",
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
                    progress >= 0.75f -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
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
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(transaction.timestamp)),
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = if (privacyMode) "•••" else (if (transaction.isIncome) "+" else "-") + "₹${String.format("%.0f", transaction.amount)}",
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
