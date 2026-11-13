package com.example.financeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.core.theme.AppThemeMode.Companion.resolveDarkTheme
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.presentation.auth.AuthGateScreen
import com.example.financeapp.presentation.auth.AuthSetupScreen
import com.example.financeapp.presentation.auth.BiometricPromptController
import com.example.financeapp.presentation.main.FinanceApp
import com.example.financeapp.presentation.settings.UserSettingsViewModel
import com.example.financeapp.presentation.splash.DotLottieSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var biometricPromptController: BiometricPromptController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.view
                .animate()
                .alpha(0f)
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(300L)
                .withEndAction {
                    splashScreenViewProvider.remove()
                }
                .start()
        }
        super.onCreate(savedInstanceState)
        biometricPromptController = BiometricPromptController(this)
        setContent {
            val settingsViewModel: UserSettingsViewModel = hiltViewModel()
            val userSettings by settingsViewModel.settings.collectAsState()
            val hasPinCode by settingsViewModel.hasPinCode.collectAsState()
            val isPinCodeSetupOnboardingShown by settingsViewModel
                .isPinCodeSetupOnboardingShown
                .collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = userSettings.themeMode.resolveDarkTheme(systemDarkTheme)

            FinanceAppTheme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }
                var isAuthSetupInProgress by rememberSaveable { mutableStateOf(false) }
                val shouldStartAuthSetup = !isAuthenticated &&
                    hasPinCode == false &&
                    isPinCodeSetupOnboardingShown == false
                val shouldShowAuthSetup = !isAuthenticated &&
                    (isAuthSetupInProgress || shouldStartAuthSetup)

                LaunchedEffect(shouldStartAuthSetup) {
                    if (shouldStartAuthSetup) {
                        isAuthSetupInProgress = true
                    }
                }

                when {
                    showSplash ||
                        hasPinCode == null ||
                        isPinCodeSetupOnboardingShown == null -> {
                        DotLottieSplashScreen(
                            onFinished = { showSplash = false }
                        )
                    }
                    shouldShowAuthSetup -> {
                        AuthSetupScreen(
                            biometricPromptController = biometricPromptController,
                            onCompleted = {
                                isAuthSetupInProgress = false
                                isAuthenticated = true
                            }
                        )
                    }
                    hasPinCode == true && !isAuthenticated -> {
                        AuthGateScreen(
                            biometricPromptController = biometricPromptController,
                            onAuthenticated = {
                                isAuthenticated = true
                            }
                        )
                    }
                    else -> FinanceApp(
                        biometricPromptController = biometricPromptController
                    )
                }
            }
        }
    }

}
