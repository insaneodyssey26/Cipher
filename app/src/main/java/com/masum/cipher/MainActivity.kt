package com.masum.cipher

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.security.BiometricAuthenticator
import com.masum.cipher.ui.MainContract
import com.masum.cipher.ui.MainViewModel
import com.masum.cipher.ui.dashboard.DashboardScreen
import com.masum.cipher.ui.dashboard.DashboardViewModel
import com.masum.cipher.ui.insights.DayDetailScreen
import com.masum.cipher.ui.insights.InsightsScreen
import com.masum.cipher.ui.insights.InsightsViewModel
import com.masum.cipher.ui.onboarding.OnboardingScreen
import com.masum.cipher.ui.privacy.PrivacyPolicyScreen
import com.masum.cipher.ui.settings.SettingsScreen
import com.masum.cipher.ui.settings.SettingsViewModel
import com.masum.cipher.ui.theme.CipherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.preferredDisplayModeId = 0 
        }

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val state by mainViewModel.state.collectAsState()
            
            state.settings?.let { userSettings ->
                val isSystemDark = isSystemInDarkTheme()
                val darkTheme = remember(userSettings.theme, isSystemDark) {
                    when (userSettings.theme) {
                        AppTheme.LIGHT -> false
                        AppTheme.DARK -> true
                        AppTheme.SYSTEM -> isSystemDark
                    }
                }

                CipherTheme(darkTheme = darkTheme) {
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

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = navSpec) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = navSpec) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = navSpec) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = navSpec) }
                        ) {
                            composable("dashboard") {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToInsights = { navController.navigate("insights") }
                                )
                            }
                            composable("insights") {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                InsightsScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDayDetail = { timestamp -> navController.navigate("day_detail/$timestamp") }
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
                                    onNavigateToPrivacy = { navController.navigate("privacy_policy") }
                                )
                            }
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        if (!state.isAuthenticated && !state.isOnboardingRequired) {
                            com.masum.cipher.ui.components.LockScreen(
                                onUnlockClick = { mainViewModel.handleIntent(MainContract.Intent.CheckAuthentication) }
                            )
                        }

                        if (state.isOnboardingRequired) {
                            OnboardingScreen(
                                onComplete = { mainViewModel.handleIntent(MainContract.Intent.SetOnboardingCompleted(true)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
