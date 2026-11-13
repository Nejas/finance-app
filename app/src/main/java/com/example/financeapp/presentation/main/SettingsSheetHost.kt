package com.example.financeapp.presentation.main

import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.domain.model.Category
import com.example.financeapp.presentation.auth.BiometricPromptController
import com.example.financeapp.presentation.settings.UserSettingsViewModel
import com.example.financeapp.presentation.bottomSheets.components.categories.FinanceCategorySelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.components.currency.FinanceCurrencySelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.settings.BiometricSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.LanguageSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.PinCodeSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsBottomSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsListItem
import com.example.financeapp.presentation.bottomSheets.settings.ThemeSettingsSheet
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType

@Composable
internal fun SettingsSheetHost(
    isVisible: Boolean,
    activeItem: SettingsListItem?,
    mainState: MainState,
    biometricPromptController: BiometricPromptController,
    onDismissSettings: () -> Unit,
    onDismissActiveItem: () -> Unit,
    onItemSelected: (SettingsListItem) -> Unit,
    onBiometricFailure: () -> Unit,
    settingsViewModel: UserSettingsViewModel = hiltViewModel()
) {
    if (!isVisible) return
    val userSettings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isBiometricAvailable = remember(context) {
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
    val applicationLocales = AppCompatDelegate.getApplicationLocales()
    val languageTag = if (applicationLocales.isEmpty) configuration.locales[0].language
        else applicationLocales[0]?.language
    val selectedLanguage = AppLanguage.fromLanguageTag(languageTag.orEmpty()) ?: AppLanguage.RUSSIAN
    when (activeItem?.takeUnless { it == SettingsListItem.Biometrics && !isBiometricAvailable }) {
        SettingsListItem.Articles -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            FinanceCategorySelectionSheetContent(
                categories = mainState.settingsCategories(),
                selectedCategoryIds = emptySet(),
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onCategoryClick = null
            )
        }
        SettingsListItem.Language -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            LanguageSettingsSheet(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language ->
                    settingsViewModel.setLanguage(language)
                    onDismissActiveItem()
                }
            )
        }
        SettingsListItem.Theme -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            ThemeSettingsSheet(
                selectedThemeMode = userSettings.themeMode,
                onThemeModeSelected = settingsViewModel::setThemeMode
            )
        }
        SettingsListItem.PinCode -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            PinCodeSettingsRoute(
                onPinCodeChanged = onDismissActiveItem,
                settingsViewModel = settingsViewModel
            )
        }
        SettingsListItem.Biometrics -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            BiometricSettingsSheet(
                isEnabled = userSettings.isBiometricLoginEnabled,
                onEnabledChange = { isEnabled ->
                    if (isEnabled) {
                        biometricPromptController.authenticate(
                            { settingsViewModel.setBiometricLoginEnabled(true) },
                            { isFailedAttempt -> if (isFailedAttempt) onBiometricFailure() }
                        )
                    } else {
                        settingsViewModel.setBiometricLoginEnabled(false)
                    }
                }
            )
        }
        SettingsListItem.Currency -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            FinanceCurrencySelectionSheetContent(
                selectedCurrency = userSettings.selectedCurrency,
                onCurrencyClick = { currency ->
                    settingsViewModel.setCurrency(currency)
                    onDismissActiveItem()
                }
            )
        }
        null -> SettingsBottomSheet(
            onDismissRequest = onDismissSettings,
            isBiometricAvailable = isBiometricAvailable,
            onItemClick = onItemSelected
        )
    }
}

private fun MainState.settingsCategories(): List<Category> =
    (expensesState.categoriesById.values + incomeState.categoriesById.values)
        .distinctBy(Category::id)
        .sortedWith(compareBy<Category> { it.type }.thenBy { it.name })

@Composable
private fun PinCodeSettingsRoute(
    onPinCodeChanged: () -> Unit,
    settingsViewModel: UserSettingsViewModel
) {
    val hasPinCode by settingsViewModel.hasPinCode.collectAsState()
    val authProtectionState by settingsViewModel.authProtectionState.collectAsState()
    PinCodeSettingsSheet(
        hasPinCode = hasPinCode == true,
        authProtectionState = authProtectionState,
        onVerifyPinCode = settingsViewModel::verifyPinCode,
        onSetPinCode = settingsViewModel::setPinCode,
        onClearPinCode = settingsViewModel::clearPinProtection,
        onCanAttemptPin = settingsViewModel::canAttemptPin,
        onPinFailure = settingsViewModel::registerPinFailure,
        onAuthSuccess = settingsViewModel::registerAuthSuccess,
        onPinCodeChanged = onPinCodeChanged
    )
}
