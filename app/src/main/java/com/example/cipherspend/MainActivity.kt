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
            
            settings?.let { userSettings ->
                val isSystemDark = isSystemInDarkTheme()
                val darkTheme = when (userSettings.theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemDark
                }

                CipherSpendTheme(darkTheme = darkTheme) {
                    var isAuthenticated by remember { 
                        mutableStateOf(!userSettings.isBiometricEnabled || !biometricAuthenticator.isBiometricAvailable()) 
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }

                    LaunchedEffect(Unit) {
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