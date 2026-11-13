package com.example.financeapp.presentation.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceBiometricIconContainer
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.settings.UserSettingsViewModel
import com.example.financeapp.presentation.common.components.PinCodeEntryContent
import com.example.financeapp.presentation.common.components.PinCodeLength
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsBiometryIcon
import kotlinx.coroutines.launch

@Composable
fun AuthSetupScreen(
    biometricPromptController: BiometricPromptController,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: UserSettingsViewModel = hiltViewModel()
) {
    val userSettings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
    val isBiometricAvailable = remember(context) {
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
    val isBiometricOfferShown = userSettings.isBiometricLoginOfferShown
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val coroutineScope = rememberCoroutineScope()
    var step by rememberSaveable { mutableStateOf(AuthSetupStep.NewPin) }
    var input by rememberSaveable(step) { mutableStateOf("") }
    var newPinCode by rememberSaveable { mutableStateOf("") }
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var isPinMismatch by rememberSaveable { mutableStateOf(false) }
    val shouldOfferBiometrics = isBiometricAvailable && !isBiometricOfferShown

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = spacing.securitySheetHorizontal,
                end = spacing.securitySheetHorizontal,
                bottom = spacing.authContentLift
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            AuthSetupStep.NewPin,
            AuthSetupStep.ConfirmPin -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Text(
                        text = stringResource(R.string.auth_setup_pin_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    PinCodeEntryContent(
                        value = input,
                        message = when {
                            isPinMismatch -> stringResource(R.string.settings_pin_mismatch)
                            step == AuthSetupStep.NewPin -> stringResource(R.string.settings_pin_new_description)
                            else -> stringResource(R.string.settings_pin_confirm_description)
                        },
                        isError = isPinMismatch,
                        isEnabled = !isProcessing,
                        onValueChange = { value ->
                            if (!isProcessing) {
                                input = value
                                isPinMismatch = false
                                if (value.length == PinCodeLength) {
                                    when (step) {
                                        AuthSetupStep.NewPin -> {
                                            newPinCode = value
                                            input = ""
                                            step = AuthSetupStep.ConfirmPin
                                        }
                                        AuthSetupStep.ConfirmPin -> {
                                            if (value == newPinCode) {
                                                isProcessing = true
                                                coroutineScope.launch {
                                                    settingsViewModel.setPinCode(value)
                                                    if (shouldOfferBiometrics) {
                                                        input = ""
                                                        isProcessing = false
                                                        step = AuthSetupStep.BiometricOffer
                                                    } else {
                                                        settingsViewModel.registerAuthSuccess()
                                                        onCompleted()
                                                    }
                                                }
                                            } else {
                                                input = ""
                                                newPinCode = ""
                                                isPinMismatch = true
                                                step = AuthSetupStep.NewPin
                                            }
                                        }
                                        AuthSetupStep.BiometricOffer -> Unit
                                    }
                                }
                            }
                        },
                        footer = {
                            TextButton(
                                enabled = !isProcessing,
                                onClick = {
                                    coroutineScope.launch {
                                        settingsViewModel.setPinCodeSetupOnboardingShown(true)
                                        settingsViewModel.registerAuthSuccess()
                                        onCompleted()
                                    }
                                }
                            ) {
                                Text(text = stringResource(R.string.action_skip))
                            }
                        }
                    )
                }
            }
            AuthSetupStep.BiometricOffer -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.biometricContentGap)
                ) {
                    RoundFrame(
                        size = sizing.biometricIconContainer,
                        backgroundColor = FinanceBiometricIconContainer,
                        content = {
                            FinanceSettingsBiometryIcon(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(sizing.biometricIcon)
                            )
                        }
                    )

                    Text(
                        text = stringResource(R.string.auth_biometric_offer_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = spacing.biometricDescriptionHorizontal),
                        text = stringResource(R.string.auth_biometric_offer_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    settingsViewModel.setBiometricLoginOfferShown(true)
                                    settingsViewModel.registerAuthSuccess()
                                    onCompleted()
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.action_skip))
                        }

                        TextButton(
                            onClick = {
                                biometricPromptController.authenticate(
                                    {
                                        coroutineScope.launch {
                                            settingsViewModel.setBiometricLoginEnabledSuspend(true)
                                            settingsViewModel.setBiometricLoginOfferShown(true)
                                            settingsViewModel.registerAuthSuccess()
                                            onCompleted()
                                        }
                                    },
                                    {}
                                )
                            }
                        ) {
                            Text(text = stringResource(R.string.auth_biometric_enable))
                        }
                    }
                }
            }
        }
    }
}

private enum class AuthSetupStep {
    NewPin,
    ConfirmPin,
    BiometricOffer
}
