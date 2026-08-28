package com.masum.cipher

import android.os.Build
import android.os.Bundle
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
import androidx.compose.animation.slideOutHorizontally
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
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.core.worker.NotificationScheduler
import com.masum.cipher.ui.MainContract
import com.masum.cipher.ui.MainViewModel
import com.masum.cipher.ui.components.FloatingNavBar
import com.masum.cipher.ui.components.TransactionDetailsSheet
import com.masum.cipher.ui.dashboard.DashboardScreen
import com.masum.cipher.ui.dashboard.DashboardViewModel
import com.masum.cipher.ui.insights.DayDetailScreen
import com.masum.cipher.ui.insights.InsightsScreen
import com.masum.cipher.ui.insights.InsightsViewModel
import com.masum.cipher.ui.onboarding.AppSelectionScreen
import com.masum.cipher.ui.onboarding.OnboardingScreen
import com.masum.cipher.ui.privacy.PrivacyPolicyScreen
import com.masum.cipher.ui.settings.SettingsScreen
import com.masum.cipher.ui.settings.SettingsViewModel
import com.masum.cipher.ui.settings.rules.SmartRulesScreen
import com.masum.cipher.ui.settings.rules.SmartRulesViewModel
import com.masum.cipher.ui.theme.CipherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    private val updateReady = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        notificationScheduler.scheduleDailyNotifications()
        com.masum.cipher.core.updates.UpdateManager.checkForUpdates(this) {
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
                val darkTheme = remember(userSettings.theme, isSystemDark) {
                    when (userSettings.theme) {
                        AppTheme.LIGHT -> false
                        AppTheme.DARK -> true
                        AppTheme.SYSTEM -> isSystemDark
                    }
                }

                LaunchedEffect(darkTheme) {
                    val style = if (darkTheme) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }

                CipherTheme(
                    darkTheme = darkTheme,
                    accentColor = androidx.compose.ui.graphics.Color(userSettings.accentColor.colorValue)
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

                        LaunchedEffect(intent) {
                            if (intent.getStringExtra("navigate_to") == "manage_apps") {
                                navController.navigate("manage_apps")
                                intent.removeExtra("navigate_to")
                            }
                        }

                        val isTopLevel = currentRoute in listOf("dashboard", "insights", "settings")

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = { 
                                if (targetState.destination.route in listOf("dashboard", "insights", "settings") && initialState.destination.route in listOf("dashboard", "insights", "settings")) {
                                    fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = navSpec)
                                }
                            },
                            exitTransition = { 
                                if (targetState.destination.route in listOf("dashboard", "insights", "settings") && initialState.destination.route in listOf("dashboard", "insights", "settings")) {
                                    fadeOut(tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = navSpec)
                                }
                            },
                            popEnterTransition = { 
                                if (targetState.destination.route in listOf("dashboard", "insights", "settings") && initialState.destination.route in listOf("dashboard", "insights", "settings")) {
                                    fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = navSpec)
                                }
                            },
                            popExitTransition = { 
                                if (targetState.destination.route in listOf("dashboard", "insights", "settings") && initialState.destination.route in listOf("dashboard", "insights", "settings")) {
                                    fadeOut(tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = navSpec)
                                }
                            }
                        ) {
                            composable("dashboard") {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateToManageApps = { navController.navigate("manage_apps") }
                                )
                            }
                            composable("insights") {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                InsightsScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDayDetail = { timestamp -> navController.navigate("day_detail/$timestamp") },
                                    onNavigateToCategories = { navController.navigate("categories") }
                                )
                            }
                            composable(
                                route = "categories",
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
                            ) {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                com.masum.cipher.ui.categories.CategoriesScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() }
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
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPrivacy = { navController.navigate("privacy_policy") },
                                    onNavigateToManageApps = { navController.navigate("manage_apps") },
                                    onNavigateToSmartRules = { navController.navigate("smart_rules") }
                                )
                            }
                            
                            composable(
                                route = "smart_rules",
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
                            ) {
                                val viewModel: SmartRulesViewModel = hiltViewModel()
                                SmartRulesScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
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
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        var showAddSheet by remember { mutableStateOf(false) }

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
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true
                            )
                        }

                        if (showAddSheet) {
                            TransactionDetailsSheet(
                                transaction = state.draftTransaction ?: com.masum.cipher.core.data.local.entity.TransactionEntity(
                                    amount = 0.0,
                                    merchant = "",
                                    currency = "INR",
                                    timestamp = System.currentTimeMillis(),
                                    category = "OTHERS",
                                    rawSms = null,
                                    isIncome = false
                                ),
                                onDismiss = { showAddSheet = false },
                                onConfirm = { newTx ->
                                    mainViewModel.handleIntent(MainContract.Intent.AddTransaction(newTx))
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(null))
                                    showAddSheet = false
                                },
                                onDraftChange = { updatedDraft ->
                                    mainViewModel.handleIntent(MainContract.Intent.UpdateDraftTransaction(updatedDraft))
                                },
                                isHapticsEnabled = state.settings?.isHapticsEnabled ?: true
                            )
                        }

                        if (!state.isAuthenticated && !state.isOnboardingRequired) {
                            com.masum.cipher.ui.components.LockScreen(
                                onUnlockClick = { mainViewModel.handleIntent(MainContract.Intent.CheckAuthentication) }
                            )
                        }

                        if (state.isOnboardingRequired) {
                            OnboardingScreen(
                                currentAccentColor = state.settings?.accentColor ?: com.masum.cipher.core.data.local.pref.AccentColor.INDIGO,
                                onAccentColorSelected = { color -> mainViewModel.handleIntent(MainContract.Intent.SaveAccentColor(color)) },
                                onComplete = { mainViewModel.handleIntent(MainContract.Intent.SetOnboardingCompleted(true)) },
                                onSaveApps = { apps -> mainViewModel.handleIntent(MainContract.Intent.SaveTrackedApps(apps)) }
                            )
                        }

                        if (showUpdateReady) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                                Snackbar(
                                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                                    action = {
                                        TextButton(onClick = { com.masum.cipher.core.updates.UpdateManager.completeUpdate(this@MainActivity) }) {
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
