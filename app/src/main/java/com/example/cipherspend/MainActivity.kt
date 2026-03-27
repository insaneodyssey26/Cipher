package com.example.cipherspend

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
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
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.core.security.BiometricAuthenticator
import com.example.cipherspend.ui.dashboard.DashboardScreen
import com.example.cipherspend.ui.dashboard.DashboardViewModel
import com.example.cipherspend.ui.insights.DayDetailScreen
import com.example.cipherspend.ui.insights.InsightsScreen
import com.example.cipherspend.ui.insights.InsightsViewModel
import com.example.cipherspend.ui.settings.SettingsScreen
import com.example.cipherspend.ui.settings.SettingsViewModel
import com.example.cipherspend.ui.theme.CipherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by userPreferences.settingsFlow.collectAsState(initial = null)
            val scope = rememberCoroutineScope()
            
            settings?.let { userSettings ->
                val isSystemDark = isSystemInDarkTheme()
                val darkTheme = when (userSettings.theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemDark
                }

                CipherTheme(darkTheme = darkTheme) {
                    val currentSettings by rememberUpdatedState(userSettings)
                    var isAuthenticated by remember { 
                        mutableStateOf(!userSettings.isBiometricEnabled || !biometricAuthenticator.isBiometricAvailable()) 
                    }
                    var resumeTrigger by remember { mutableIntStateOf(0) }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> {
                                    resumeTrigger++
                                    val shouldLock = currentSettings.isBiometricEnabled && 
                                                     biometricAuthenticator.isBiometricAvailable()
                                    
                                    if (shouldLock) {
                                        val timeDiff = System.currentTimeMillis() - currentSettings.lastStopTime
                                        val isGracePeriodOver = timeDiff >= currentSettings.autoLockTimeout
                                        
                                        if (isAuthenticated && isGracePeriodOver) {
                                            isAuthenticated = false
                                        }
                                    } else {
                                        isAuthenticated = true
                                    }
                                }
                                Lifecycle.Event.ON_STOP -> {
                                    scope.launch {
                                        userPreferences.setLastStopTime(System.currentTimeMillis())
                                    }
                                }
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }

                    LaunchedEffect(isAuthenticated, resumeTrigger) {
                        if (!isAuthenticated) {
                            if (userSettings.isBiometricEnabled && biometricAuthenticator.isBiometricAvailable()) {
                                biometricAuthenticator.authenticate(
                                    activity = this@MainActivity,
                                    onSuccess = { isAuthenticated = true },
                                    onError = { }
                                )
                            } else {
                                isAuthenticated = true
                            }
                        }
                    }

                    LaunchedEffect(userSettings.isBiometricEnabled) {
                        if (!userSettings.isBiometricEnabled) {
                            isAuthenticated = true
                        }
                    }

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    }

                    if (isAuthenticated) {
                        val navController = rememberNavController()

                        val springSpec = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                        val fadeSpringSpec = spring<Float>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = springSpec
                                ) + fadeIn(animationSpec = fadeSpringSpec)
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 3 },
                                    animationSpec = springSpec
                                ) + fadeOut(animationSpec = fadeSpringSpec)
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it / 3 },
                                    animationSpec = springSpec
                                ) + fadeIn(animationSpec = fadeSpringSpec)
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = springSpec
                                ) + fadeOut(animationSpec = fadeSpringSpec)
                            }
                        ) {
                            composable("dashboard") {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    userPreferences = userPreferences,
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    },
                                    onNavigateToInsights = {
                                        navController.navigate("insights")
                                    }
                                )
                            }

                            composable("insights") {
                                val viewModel: InsightsViewModel = hiltViewModel()
                                InsightsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToDayDetail = { timestamp ->
                                        navController.navigate("day_detail/$timestamp")
                                    }
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
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("settings") {
                                val viewModel: SettingsViewModel = hiltViewModel()
                                SettingsScreen(
                                    viewModel = viewModel,
                                    biometricAuthenticator = biometricAuthenticator,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    } else {
                        com.example.cipherspend.ui.components.LockScreen(
                            onUnlockClick = {
                                resumeTrigger++
                            }
                        )
                    }
                }
            }
        }
    }
}
