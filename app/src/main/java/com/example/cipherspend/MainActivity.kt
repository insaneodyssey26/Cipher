package com.example.cipherspend

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cipherspend.core.data.local.pref.AppTheme
import com.example.cipherspend.core.data.local.pref.ThemePreferences
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
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeState) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemDark
            }

            CipherSpendTheme(darkTheme = darkTheme) {
                var isAuthenticated by remember { 
                    mutableStateOf(!biometricAuthenticator.isBiometricAvailable()) 
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    
                    if (biometricAuthenticator.isBiometricAvailable() && !isAuthenticated) {
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
                        startDestination = "dashboard"
                    ) {
                        composable("dashboard") {
                            val viewModel: DashboardViewModel = hiltViewModel()
                            DashboardScreen(
                                viewModel = viewModel,
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