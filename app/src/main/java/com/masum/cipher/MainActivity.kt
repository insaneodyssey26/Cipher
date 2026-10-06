package com.masum.cipher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.model.SplitParticipant
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.core.security.DatabaseRecovery
import com.masum.cipher.core.security.SecurityManager
import com.masum.cipher.core.updates.UpdateManager
import com.masum.cipher.core.worker.NotificationScheduler
import com.masum.cipher.ui.MainContract
import com.masum.cipher.ui.MainViewModel
import com.masum.cipher.ui.accounts.AccountsContract
import com.masum.cipher.ui.accounts.AccountsScreen
import com.masum.cipher.ui.accounts.AccountsViewModel
import com.masum.cipher.ui.accounts.CreateEditAccountScreen
import com.masum.cipher.ui.accounts.analytics.AccountAnalyticsScreen
import com.masum.cipher.ui.accounts.details.AccountDetailsScreen
import com.masum.cipher.ui.categories.CategoriesScreen
import com.masum.cipher.ui.components.FloatingNavBar
import com.masum.cipher.ui.debts.CreateEditDebtScreen
import com.masum.cipher.ui.debts.DebtsContract
import com.masum.cipher.ui.debts.DebtsViewModel
import com.masum.cipher.ui.goals.CreateEditGoalScreen
import com.masum.cipher.ui.goals.GoalsContract
import com.masum.cipher.ui.goals.GoalsViewModel
import com.masum.cipher.ui.components.LicenseRevokedDialog
import com.masum.cipher.ui.components.LockScreen
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.components.TransactionSplitSheet
import com.masum.cipher.ui.dashboard.DashboardScreen
import com.masum.cipher.ui.dashboard.DashboardViewModel
import com.masum.cipher.ui.insights.DayDetailScreen
import com.masum.cipher.ui.insights.InsightsScreen
import com.masum.cipher.ui.insights.InsightsViewModel
import com.masum.cipher.ui.onboarding.AppSelectionScreen
import com.masum.cipher.ui.onboarding.OnboardingScreen
import com.masum.cipher.ui.privacy.PrivacyPolicyScreen
import com.masum.cipher.ui.recovery.DatabaseRecoveryScreen
import com.masum.cipher.ui.settings.CurrencySelectionScreen
import com.masum.cipher.ui.settings.SettingsScreen
import com.masum.cipher.ui.settings.SettingsViewModel
import com.masum.cipher.ui.settings.rules.SmartRulesScreen
import com.masum.cipher.ui.settings.rules.SmartRulesViewModel
import com.masum.cipher.ui.splits.SplitExpensesScreen
import com.masum.cipher.ui.theme.CipherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import javax.inject.Inject
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var licenseSyncScheduler: com.masum.cipher.core.worker.LicenseSyncScheduler

    @Inject
    lateinit var appUpdateScheduler: com.masum.cipher.core.worker.AppUpdateScheduler

    @Inject
    lateinit var licenseEngine: com.masum.cipher.core.security.LicenseEngine

    @Inject
    lateinit var securityManager: SecurityManager

    @Inject
    lateinit var databaseRecovery: DatabaseRecovery

    private val currentIntentFlow = MutableStateFlow<Intent?>(null)
    private val updateReady = MutableStateFlow(false)

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = UserPreferences(newBase).getCachedLanguageCode()
        val wrapped = com.masum.cipher.core.util.LocaleHelper.wrapContext(newBase, lang)
        super.attachBaseContext(wrapped)
        val config = com.masum.cipher.core.util.LocaleHelper.getOverrideConfiguration(newBase, lang)
        if (config != null) {
            applyOverrideConfiguration(config)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentFlow.value = intent
    }

    private fun showDatabaseRecovery() {
        enableEdgeToEdge()
        setContent {
            val darkTheme = when (userPreferences.getCachedAppTheme()) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            CipherTheme(
                darkTheme = darkTheme,
                accentColor = Color(userPreferences.getCachedAccentColor().colorValue)
            ) {
                DatabaseRecoveryScreen(
                    onRetry = {
                        val recovered = !securityManager.isDatabaseKeyUnavailable()
                        if (recovered) recreate()
                        recovered
                    },
                    onErase = {
                        databaseRecovery.eraseEncryptedData()
                        recreate()
                    }
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (securityManager.isDatabaseKeyUnavailable()) {
            showDatabaseRecovery()
            return
        }
        currentIntentFlow.value = intent
        
        notificationScheduler.scheduleDailyNotifications()
        licenseSyncScheduler.schedulePeriodicLicenseSync()
        licenseSyncScheduler.syncLicenseOnLaunch()
        appUpdateScheduler.schedulePeriodicUpdateCheck()
        UpdateManager.checkForUpdates(this) {
            updateReady.value = true
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.preferredDisplayModeId = 0 
        }

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val state by mainViewModel.state.collectAsStateWithLifecycle()
            val showUpdateReady by updateReady.collectAsStateWithLifecycle()
            
            state.settings?.let { userSettings ->
                val isSystemDark = isSystemInDarkTheme()
                val darkTheme = when (userSettings.theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemDark
                }

                LaunchedEffect(darkTheme) {
                    val style = if (darkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }

                CipherTheme(
                    darkTheme = darkTheme,
                    accentColor = Color(userSettings.accentColor.colorValue)
                ) {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> mainViewModel.handleIntent(MainContract.Intent.CheckAuthentication)
                                Lifecycle.Event.ON_STOP -> mainViewModel.handleIntent(MainContract.Intent.OnAppStop)
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    LaunchedEffect(Unit) {
                        mainViewModel.effect.collect { effect ->
                            if (effect is MainContract.Effect.TriggerBiometricPrompt) {
                                biometricAuthenticator.authenticate(
                                    activity = this@MainActivity,
                                    onSuccess = { mainViewModel.handleIntent(MainContract.Intent.Authenticate) },
                                    onError = { }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        val navSpec = remember { tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing) }
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        var showAddSheet by remember { mutableStateOf(false) }

                        val incomingIntent by currentIntentFlow.collectAsStateWithLifecycle()

                        LaunchedEffect(incomingIntent) {
                            val activeIntent = incomingIntent ?: return@LaunchedEffect
                            
                            val dataUri = activeIntent.data
                            if (dataUri != null && dataUri.scheme == "cipher") {
                                if (dataUri.host == "activate") {
                                    val licenseKey = dataUri.getQueryParameter("key")?.trim()
                                    val email = dataUri.getQueryParameter("email")?.trim()
                                    if (!licenseKey.isNullOrBlank()) {
                                        val deviceId = android.provider.Settings.Secure.getString(
                                            this@MainActivity.contentResolver,
                                            android.provider.Settings.Secure.ANDROID_ID
                                        ) ?: "device_${System.currentTimeMillis()}"
                                        val validation = licenseEngine.activateLicenseRemote(licenseKey, email, deviceId)
                                        if (validation.isValid) {
                                            userPreferences.setProStatus(
                                                isPro = true,
                                                tier = validation.tier.identifier,
                                                token = licenseKey,
                                                orderId = validation.orderId,
                                                expiresAt = validation.expiresAtEpochMs
                                            )
                                        }
                                    }
                                } else if (dataUri.host == "open") {
                                    val targetScreen = dataUri.getQueryParameter("screen")
                                    if (targetScreen == "pro") {
                                        navController.navigate("cipher_pro")
                                    }
                                }
                            }

                            if (activeIntent.getStringExtra("navigate_to") == "manage_apps") {
                                navController.navigate("manage_apps")
                                activeIntent.removeExtra("navigate_to")
                            }
                            val quickLogCat = activeIntent.getStringExtra("quick_log_category")
                            if (!quickLogCat.isNullOrBlank()) {
                                val defaultCurrency = state.settings?.currencyCode ?: "INR"
                                val mappedCat = when (quickLogCat.uppercase()) {
                                    "FOOD" -> "FOOD"
                                    "SHOP", "SHOPPING" -> "SHOPPING"
                                    "RIDE", "TRANSPORT" -> "TRANSPORT"
                                    "BILLS", "BILL" -> "BILLS"
                                    "FUN", "ENTERTAINMENT" -> "ENTERTAINMENT"
                                    "HEALTH" -> "HEALTH"
                                    "INVEST", "INVESTMENT" -> "INVESTMENT"
                                    "MORE", "OTHERS" -> "OTHERS"
                                    else -> quickLogCat
                                }
                                mainViewModel.handleIntent(
                                    MainContract.Intent.UpdateDraftTransaction(
                                        TransactionEntity(
                                            amount = 0.0,
                                            merchant = "",
                                            currency = defaultCurrency,
                                            timestamp = System.currentTimeMillis(),
                                            category = mappedCat,
                                            rawSms = null,
                                            isIncome = false
                                        )
                                    )
                                )
                                showAddSheet = true
                                activeIntent.removeExtra("quick_log_category")
                            }
                        }

                        val isTopLevel = currentRoute in listOf("dashboard", "insights", "split_expenses", "settings")
                        val modalRoutes = setOf("create_debt", "edit_debt/{debtId}", "create_goal", "edit_goal/{goalId}")

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = { 
                                val targetRoute = targetState.destination.route
                                val initialRoute = initialState.destination.route
                                if (targetRoute in modalRoutes) {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                } else if (targetRoute in listOf("dashboard", "insights", "split_expenses", "settings") && initialRoute in listOf("dashboard", "insights", "split_expenses", "settings")) {
                                    fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { (it * 0.25f).toInt() }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                }
                            },
                            exitTransition = { 
                                val targetRoute = targetState.destination.route
                                val initialRoute = initialState.destination.route
                                if (targetRoute in modalRoutes) {
                                    fadeOut(tween(250)) + scaleOut(targetScale = 0.96f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else if (targetRoute in listOf("dashboard", "insights", "split_expenses", "settings") && initialRoute in listOf("dashboard", "insights", "split_expenses", "settings")) {
                                    fadeOut(tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { -(it * 0.25f).toInt() }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            },
                            popEnterTransition = { 
                                val targetRoute = targetState.destination.route
                                val initialRoute = initialState.destination.route
                                if (initialRoute in modalRoutes) {
                                    fadeIn(tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else if (targetRoute in listOf("dashboard", "insights", "split_expenses", "settings") && initialRoute in listOf("dashboard", "insights", "split_expenses", "settings")) {
                                    fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { -(it * 0.25f).toInt() }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                }
                            },
                            popExitTransition = { 
                                val targetRoute = targetState.destination.route
                                val initialRoute = initialState.destination.route
                                if (initialRoute in modalRoutes) {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                } else if (targetRoute in listOf("dashboard", "insights", "split_expenses", "settings") && initialRoute in listOf("dashboard", "insights", "split_expenses", "settings")) {
                                    fadeOut(tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { (it * 0.25f).toInt() }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            }
                        ) {
                            composable("dashboard") {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateToManageApps = { navController.navigate("manage_apps") },
                                    onNavigateToAccounts = { navController.navigate("accounts") },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable("split_expenses") {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                SplitExpensesScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { 
                                        if (navController.previousBackStackEntry != null) {
                                            navController.popBackStack()
                                        } else {
                                            navController.navigate("dashboard") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onNavigateToPro = { navController.navigate("cipher_pro") },
                                    onNavigateToCreateGoal = { navController.navigate("create_goal") },
                                    onNavigateToEditGoal = { goalId -> navController.navigate("edit_goal/$goalId") },
                                    onNavigateToCreateDebt = { navController.navigate("create_debt") },
                                    onNavigateToEditDebt = { debtId -> navController.navigate("edit_debt/$debtId") }
                                )
                            }
                            composable("insights") {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                InsightsScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateToDayDetail = { timestamp -> navController.navigate("day_detail/$timestamp") },
                                    onNavigateToCategories = { navController.navigate("categories") },
                                    onNavigateToAccounts = { navController.navigate("accounts") }
                                )
                            }
                            composable("categories") {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                CategoriesScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable(
                                route = "day_detail/{timestamp}",
                                arguments = listOf(navArgument("timestamp") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: 0L
                                val viewModel: InsightsViewModel = hiltViewModel()
                                DayDetailScreen(
                                    timestamp = timestamp,
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                val viewModel: SettingsViewModel = hiltViewModel()
                                SettingsScreen(
                                    viewModel = viewModel,
                                    biometricAuthenticator = biometricAuthenticator,
                                    onNavigateToPrivacy = { navController.navigate("privacy_policy") },
                                    onNavigateToManageApps = { navController.navigate("manage_apps") },
                                    onNavigateToSmartRules = { navController.navigate("smart_rules") },
                                    onNavigateToCategories = { navController.navigate("categories") },
                                    onNavigateToAccounts = { navController.navigate("accounts") },
                                    onNavigateToCurrency = { navController.navigate("currency_selection") },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable("accounts") {
                                AccountsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPro = { navController.navigate("cipher_pro") },
                                    onNavigateToCreateAccount = { navController.navigate("create_account") },
                                    onNavigateToEditAccount = { accountId -> navController.navigate("edit_account/$accountId") },
                                    onNavigateToAccountDetails = { accountId -> navController.navigate("account_details/$accountId") }
                                )
                            }
                            composable(
                                route = "account_details/{accountId}",
                                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                                AccountDetailsScreen(
                                    accountId = accountId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEditAccount = { accId -> navController.navigate("edit_account/$accId") },
                                    onNavigateToAccountAnalytics = { accId -> navController.navigate("account_analytics/$accId") },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable(
                                route = "account_analytics/{accountId}",
                                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                                AccountAnalyticsScreen(
                                    accountId = accountId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable("create_account") {
                                val viewModel: AccountsViewModel = hiltViewModel()
                                val accountsState by viewModel.state.collectAsStateWithLifecycle()
                                val locale = Locale.getDefault()
                                CreateEditAccountScreen(
                                    accountToEdit = null,
                                    currencySymbol = accountsState.currencySymbol,
                                    locale = locale,
                                    isHapticsEnabled = accountsState.isHapticsEnabled,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSaveAccount = { name, type, initialBalance, colorHex, iconName, isDefault, last4 ->
                                        viewModel.handleIntent(
                                            AccountsContract.Intent.SaveAccount(
                                                accountId = null,
                                                name = name,
                                                type = type,
                                                initialBalance = initialBalance,
                                                colorHex = colorHex,
                                                iconName = iconName,
                                                isDefault = isDefault,
                                                last4 = last4
                                            )
                                        )
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(
                                route = "edit_account/{accountId}",
                                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                                val viewModel: AccountsViewModel = hiltViewModel()
                                val accountsState by viewModel.state.collectAsStateWithLifecycle()
                                val accountToEdit = accountsState.accounts.find { it.id == accountId }?.entity
                                val locale = Locale.getDefault()
                                CreateEditAccountScreen(
                                    accountToEdit = accountToEdit,
                                    currencySymbol = accountsState.currencySymbol,
                                    locale = locale,
                                    isHapticsEnabled = accountsState.isHapticsEnabled,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSaveAccount = { name, type, initialBalance, colorHex, iconName, isDefault, last4 ->
                                        viewModel.handleIntent(
                                            AccountsContract.Intent.SaveAccount(
                                                accountId = accountId,
                                                name = name,
                                                type = type,
                                                initialBalance = initialBalance,
                                                colorHex = colorHex,
                                                iconName = iconName,
                                                isDefault = isDefault,
                                                last4 = last4
                                            )
                                        )
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("cipher_pro") {
                                com.masum.cipher.ui.pro.CipherProScreen(
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("currency_selection") {
                                CurrencySelectionScreen(
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("smart_rules") {
                                val viewModel: SmartRulesViewModel = hiltViewModel()
                                SmartRulesScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPro = { navController.navigate("cipher_pro") }
                                )
                            }
                            composable("manage_apps") {
                                AppSelectionScreen(
                                    initialSelectedApps = state.settings?.trackedApps ?: emptySet(),
                                    onComplete = { apps ->
                                        mainViewModel.handleIntent(MainContract.Intent.SaveTrackedApps(apps))
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(
                                route = "create_goal",
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(300))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            ) {
                                val viewModel: GoalsViewModel = hiltViewModel()
                                val goalsState by viewModel.state.collectAsStateWithLifecycle()
                                if (!goalsState.isPro && goalsState.goals.size >= goalsState.freeGoalLimit) {
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                        viewModel.handleIntent(GoalsContract.Intent.ShowProGate)
                                    }
                                }
                                CreateEditGoalScreen(
                                    goalToEdit = null,
                                    currencySymbol = goalsState.currencySymbol,
                                    isHapticsEnabled = goalsState.isHapticsEnabled,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSaveGoal = { name, targetAmount, initialSaved, colorHex, iconName ->
                                        viewModel.handleIntent(
                                            GoalsContract.Intent.CreateGoal(
                                                name = name,
                                                targetAmount = targetAmount,
                                                initialSaved = initialSaved,
                                                colorHex = colorHex,
                                                iconName = iconName
                                            )
                                        )
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(
                                route = "edit_goal/{goalId}",
                                arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(300))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            ) { backStackEntry ->
                                val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                                val viewModel: GoalsViewModel = hiltViewModel()
                                val goalsState by viewModel.state.collectAsStateWithLifecycle()
                                val goalToEdit = goalsState.goals.find { it.id == goalId }

                                if (goalToEdit != null) {
                                    CreateEditGoalScreen(
                                        goalToEdit = goalToEdit,
                                        currencySymbol = goalsState.currencySymbol,
                                        isHapticsEnabled = goalsState.isHapticsEnabled,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSaveGoal = { name, targetAmount, savedAmount, colorHex, iconName ->
                                            viewModel.handleIntent(
                                                GoalsContract.Intent.UpdateGoal(
                                                    id = goalId,
                                                    name = name,
                                                    targetAmount = targetAmount,
                                                    savedAmount = savedAmount,
                                                    colorHex = colorHex,
                                                    iconName = iconName
                                                )
                                            )
                                            navController.popBackStack()
                                        },
                                        onDeleteGoal = {
                                            viewModel.handleIntent(GoalsContract.Intent.DeleteGoal(it))
                                            navController.popBackStack()
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background)
                                    )
                                }
                            }
                            composable(
                                route = "create_debt",
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(300))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            ) {
                                val viewModel: DebtsViewModel = hiltViewModel()
                                val debtsState by viewModel.state.collectAsStateWithLifecycle()
                                CreateEditDebtScreen(
                                    debtToEdit = null,
                                    accounts = debtsState.accounts,
                                    currencySymbol = debtsState.currencySymbol,
                                    isHapticsEnabled = debtsState.isHapticsEnabled,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSaveDebt = { personName, amount, type, dueDate, note, accountId, syncLedger ->
                                        viewModel.handleIntent(
                                            DebtsContract.Intent.CreateDebt(
                                                personName = personName,
                                                amount = amount,
                                                type = type,
                                                dueDate = dueDate,
                                                note = note,
                                                accountId = accountId,
                                                syncLedger = syncLedger
                                            )
                                        )
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(
                                route = "edit_debt/{debtId}",
                                arguments = listOf(navArgument("debtId") { type = NavType.LongType }),
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(300))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
                                }
                            ) { backStackEntry ->
                                val debtId = backStackEntry.arguments?.getLong("debtId") ?: 0L
                                val viewModel: DebtsViewModel = hiltViewModel()
                                val debtsState by viewModel.state.collectAsStateWithLifecycle()
                                val debtItemToEdit = debtsState.debts.find { it.debt.id == debtId }

                                if (debtItemToEdit != null) {
                                    CreateEditDebtScreen(
                                        debtToEdit = debtItemToEdit.debt,
                                        accounts = debtsState.accounts,
                                        currencySymbol = debtsState.currencySymbol,
                                        isHapticsEnabled = debtsState.isHapticsEnabled,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSaveDebt = { personName, amount, type, dueDate, note, accountId, syncLedger ->
                                            viewModel.handleIntent(
                                                DebtsContract.Intent.UpdateDebt(
                                                    debtId = debtId,
                                                    personName = personName,
                                                    amount = amount,
                                                    type = type,
                                                    dueDate = dueDate,
                                                    note = note,
                                                    accountId = accountId,
                                                    syncLedger = syncLedger
                                                )
                                            )
                                            navController.popBackStack()
                                        },
                                        onDeleteDebt = {
                                            viewModel.handleIntent(DebtsContract.Intent.DeleteDebt(it))
                                            navController.popBackStack()
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background)
                                    )
                                }
                            }
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        var activeSplittingTx by remember { mutableStateOf<Pair<TransactionEntity, List<SplitParticipant>>?>(null) }

                        if (isTopLevel) {
                            FloatingNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAddClick = { showAddSheet = true },
                                modifier = Modifier.align(Alignment.BottomCenter),
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true,
                                isCompressed = state.settings?.isNavBarCompressed ?: false,
                                onToggleCompress = { compressed ->
                                    mainViewModel.handleIntent(MainContract.Intent.SetNavBarCompressed(compressed))
                                }
                            )
                        }

                        if (showAddSheet) {
                            TransactionDetailsSheet(
                                transaction = state.draftTransaction ?: TransactionEntity(
                                    amount = 0.0,
                                    merchant = "",
                                    currency = state.settings?.currencyCode ?: "INR",
                                    timestamp = System.currentTimeMillis(),
                                    category = "OTHERS",
                                    rawSms = null,
                                    isIncome = false
                                ),
                                accounts = state.accounts,
                                currencySymbol = state.settings?.currencySymbol ?: "₹",
                                customCategories = state.customCategories,
                                onDismiss = { showAddSheet = false },
                                onConfirm = { newTx ->
                                    mainViewModel.handleIntent(MainContract.Intent.AddTransaction(newTx))
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(null))
                                    showAddSheet = false
                                },
                                onConfirmWithSplits = { newTx, splits ->
                                    mainViewModel.handleIntent(MainContract.Intent.AddTransaction(newTx, splits))
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(null))
                                    showAddSheet = false
                                },
                                onOpenSplitSheet = { draftTx, splits ->
                                    activeSplittingTx = Pair(draftTx, splits)
                                    showAddSheet = false
                                },
                                onDraftChange = { updatedDraft ->
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(updatedDraft))
                                },
                                onCreateCustomCategory = { name, iconName, colorHex ->
                                    mainViewModel.handleIntent(MainContract.Intent.CreateCustomCategory(name, iconName, colorHex))
                                },
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true
                            )
                        }

                        activeSplittingTx?.let { (tx, participants) ->
                            TransactionSplitSheet(
                                expenseName = tx.merchant.ifBlank { "Expense" },
                                totalAmount = tx.amount,
                                currencySymbol = state.settings?.currencySymbol ?: "₹",
                                initialParticipants = participants,
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true,
                                onDismiss = { activeSplittingTx = null },
                                onSaveSplits = { updatedSplits ->
                                    mainViewModel.handleIntent(MainContract.Intent.AddTransaction(tx, updatedSplits))
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(null))
                                    activeSplittingTx = null
                                }
                            )
                        }

                        val shouldShowLock = state.settings?.isBiometricEnabled == true
                            && biometricAuthenticator.isBiometricAvailable()
                            && !state.isAuthenticated
                            && !state.isOnboardingRequired

                        if (shouldShowLock) {
                            LockScreen(
                                onUnlockClick = { mainViewModel.handleIntent(MainContract.Intent.CheckAuthentication) }
                            )
                        }

                        if (state.isOnboardingRequired) {
                            OnboardingScreen(
                                userPreferences = userPreferences,
                                currentAccentColor = state.settings?.accentColor ?: com.masum.cipher.core.data.local.pref.AccentColor.INDIGO,
                                onAccentColorSelected = { color -> mainViewModel.handleIntent(MainContract.Intent.SaveAccentColor(color)) },
                                currentTheme = state.settings?.theme ?: AppTheme.SYSTEM,
                                onThemeSelected = { theme -> mainViewModel.handleIntent(MainContract.Intent.SaveTheme(theme)) },
                                currentLanguageCode = state.settings?.appLanguage ?: "system",
                                onLanguageSelected = { langCode -> mainViewModel.handleIntent(MainContract.Intent.SaveLanguage(langCode)) },
                                currentCurrencyCode = state.settings?.currencyCode ?: com.masum.cipher.core.domain.model.AppCurrency.detectDefault().code,
                                currentCurrencySymbol = state.settings?.currencySymbol ?: com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
                                onCurrencySelected = { code, symbol -> mainViewModel.handleIntent(MainContract.Intent.SaveCurrency(code, symbol)) },
                                onComplete = { mainViewModel.handleIntent(MainContract.Intent.SetOnboardingCompleted(true)) },
                                onSaveApps = { apps -> mainViewModel.handleIntent(MainContract.Intent.SaveTrackedApps(apps)) }
                            )
                        }

                        if (state.settings?.showLicenseRevokedDialog == true) {
                            LicenseRevokedDialog(
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true,
                                onReactivate = {
                                    mainViewModel.handleIntent(MainContract.Intent.DismissLicenseRevokedDialog)
                                    navController.navigate("cipher_pro")
                                },
                                onDismiss = {
                                    mainViewModel.handleIntent(MainContract.Intent.DismissLicenseRevokedDialog)
                                }
                            )
                        }

                        if (showUpdateReady) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                                Snackbar(
                                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                                    action = {
                                        TextButton(onClick = { UpdateManager.completeUpdate(this@MainActivity) }) {
                                            Text("RESTART", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                ) {
                                    Text("An update is ready to install.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
