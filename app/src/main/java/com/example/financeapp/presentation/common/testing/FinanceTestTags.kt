package com.example.financeapp.presentation.common.testing

import com.example.financeapp.core.theme.AppThemeMode

/** Stable semantics tags used by Compose UI tests. */
object FinanceTestTags {
    const val AppRoot = "finance_app_root"
    const val AnalyticsScreen = "analytics_screen"
    const val SettingsSheet = "settings_sheet"
    const val TopBarAnalytics = "top_bar_analytics"
    const val TopBarSettings = "top_bar_settings"
    const val TransactionCreateAction = "transaction_create_action"
    const val TransactionDateField = "transaction_date_field"
    const val TransactionDatePicker = "transaction_date_picker"
    const val TransactionDatePickerHeadline = "transaction_date_picker_headline"
    const val TransactionDatePickerApply = "transaction_date_picker_apply"
    const val TransactionDatePickerCancel = "transaction_date_picker_cancel"
    const val PinCodeInput = "pin_code_input"
    const val PinCodeSuccess = "pin_code_success"
    const val PinCodeChangeAction = "pin_code_change_action"
    const val PinCodeResetAction = "pin_code_reset_action"

    fun bottomNavigation(route: String): String {
        return "bottom_navigation_$route"
    }

    fun settingsItem(key: String): String {
        return "settings_item_$key"
    }

    fun themeOption(themeMode: AppThemeMode): String {
        return "theme_option_${themeMode.name.lowercase()}"
    }
}
