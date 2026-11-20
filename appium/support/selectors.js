const Tags = Object.freeze({
    appRoot: "finance_app_root",
    analyticsScreen: "analytics_screen",
    settingsSheet: "settings_sheet",
    topBarAnalytics: "top_bar_analytics",
    topBarSettings: "top_bar_settings",
    transactionCreateAction: "transaction_create_action",
    transactionDateField: "transaction_date_field",
    transactionDatePicker: "transaction_date_picker",
    transactionDatePickerHeadline: "transaction_date_picker_headline",
    transactionDatePickerApply: "transaction_date_picker_apply",
    transactionDatePickerCancel: "transaction_date_picker_cancel",
    pinCodeInput: "pin_code_input",
    pinCodeSuccess: "pin_code_success",
    bottomNavigation: (route) => `bottom_navigation_${route}`,
    settingsItem: (key) => `settings_item_${key}`,
    themeOption: (theme) => `theme_option_${theme}`
});

/**
 * Compose maps testTag to the Android resource-id without an application package.
 * UiAutomator's resourceId selector therefore receives the tag unchanged.
 */
function byTag(tag) {
    return `android=new UiSelector().resourceId("${tag}")`;
}

module.exports = { Tags, byTag };
