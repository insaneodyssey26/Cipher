package com.example.cipherspend

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.core.security.BiometricAuthenticator
import com.example.cipherspend.ui.dashboard.DashboardScreen
import com.example.cipherspend.ui.dashboard.DashboardViewModel
import com.example.cipherspend.ui.insights.InsightsScreen
import com.example.cipherspend.ui.insights.InsightsViewModel
import com.example.cipherspend.ui.settings.SettingsScreen
import com.example.cipherspend.ui.settings.SettingsViewModel
import com.example.cipherspend.ui.theme.CipherSpendTheme
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

                CipherSpendTheme(darkTheme = darkTheme) {
                    var isAuthenticated by remember { 
                        val shouldLock = userSettings.isBiometricEnabled && biometricAuthenticator.isBiometricAvailable()
                        val timeDiff = System.currentTimeMillis() - userSettings.lastStopTime
                        val isGracePeriodOver = timeDiff > userSettings.autoLockTimeout
                        
                        mutableStateOf(!shouldLock || !isGracePeriodOver)
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_STOP) {
                                scope.launch {
                                    userPreferences.setLastStopTime(System.currentTimeMillis())
                                }
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

                    LaunchedEffect(isAuthenticated) {
                        permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        
                        if (userSettings.isBiometricEnabled && biometricAuthenticator.isBiometricAvailable() && !isAuthenticated) {
                            biometricAuthenticator.authenticate(
                                activity = this@MainActivity,
                                onSuccess = { isAuthenticated = true },
                                onError = { }
                            )
                        }
                    }

                    if (isAuthenticated) {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 3 },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it / 3 },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(500))
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
                    }
                }
            }
        }
    }
}