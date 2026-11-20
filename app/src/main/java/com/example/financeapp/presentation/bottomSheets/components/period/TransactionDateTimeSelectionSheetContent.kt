package com.example.financeapp.presentation.bottomSheets.components.period

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.core.theme.CustomPeriodActionTextStyle
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.TransactionTimePickerTitleTextStyle
import com.example.financeapp.presentation.common.components.icons.FinanceClockIcon
import com.example.financeapp.presentation.common.testing.FinanceTestTags
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDateSelectionScreen(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    val initialSelectedDateMillis = selectedDate.toEpochMillisUtc()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        initialDisplayedMonthMillis = initialSelectedDateMillis
    )
    val spacing = LocalSpacing.current
    val datePickerContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val datePickerAccentColor = MaterialTheme.colorScheme.primary
    val datePickerColors = DatePickerDefaults.colors(
        containerColor = datePickerContainerColor,
        titleContentColor = MaterialTheme.colorScheme.outline,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurface,
        subheadContentColor = MaterialTheme.colorScheme.outline,
        navigationContentColor = MaterialTheme.colorScheme.outline,
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedDayContainerColor = datePickerAccentColor,
        todayContentColor = datePickerAccentColor,
        todayDateBorderColor = datePickerAccentColor,
        dividerColor = MaterialTheme.colorScheme.outlineVariant
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = datePickerContainerColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DatePicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(FinanceTestTags.TransactionDatePicker),
                state = datePickerState,
                title = {
                    Text(
                        modifier = Modifier.padding(
                            start = spacing.transactionDatePickerActionsHorizontal,
                            top = spacing.transactionDatePickerCloseTop,
                            end = spacing.transactionDatePickerActionsHorizontal
                        ),
                        text = stringResource(R.string.transaction_date_picker_title),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                headline = {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = spacing.transactionDatePickerActionsHorizontal,
                            vertical = spacing.s
                        ).testTag(FinanceTestTags.TransactionDatePickerHeadline),
                        text = datePickerState.selectedDateMillis
                            ?.toLocalDateUtc()
                            ?.formatDatePickerHeadline()
                            .orEmpty(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                showModeToggle = false,
                colors = datePickerColors
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = spacing.hairline
            )
            TransactionDatePickerActions(
                containerColor = datePickerContainerColor,
                contentColor = datePickerAccentColor,
                onCancelClick = onDismissRequest,
                onApplyClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDateUtc()
                        ?.let(onDateSelected)
                }
            )
        }
    }
}

@Composable
private fun TransactionDatePickerActions(
    containerColor: Color,
    contentColor: Color,
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionDatePickerActionBarHeight)
            .navigationBarsPadding()
            .background(containerColor)
            .padding(horizontal = spacing.transactionDatePickerActionsHorizontal),
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.transactionDatePickerActionsGap,
            alignment = Alignment.End
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onCancelClick,
            modifier = Modifier.testTag(FinanceTestTags.TransactionDatePickerCancel)
        ) {
            Text(
                text = stringResource(R.string.picker_cancel),
                style = CustomPeriodActionTextStyle,
                color = contentColor
            )
        }
        TextButton(
            onClick = onApplyClick,
            modifier = Modifier.testTag(FinanceTestTags.TransactionDatePickerApply)
        ) {
            Text(
                text = stringResource(R.string.picker_apply),
                style = CustomPeriodActionTextStyle,
                color = contentColor
            )
        }
    }
}

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

private fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun LocalDate.formatDatePickerHeadline(): String =
    format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionTimeSelectionSheetContent(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismissRequest: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )
    val timePickerContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val timePickerColors = TimePickerDefaults.colors(
        containerColor = timePickerContainerColor,
        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionTimePickerSheetHeight),
        color = timePickerContainerColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing.transactionTimePickerContentGap)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.transactionTimePickerHeaderHeight)
                    .padding(
                        start = spacing.transactionTimePickerHeaderHorizontal,
                        top = spacing.transactionTimePickerHeaderTop,
                        end = spacing.transactionTimePickerHeaderHorizontal
                    )
            ) {
                Text(
                    text = stringResource(R.string.transaction_time_picker_title),
                    style = TransactionTimePickerTitleTextStyle,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.transactionTimePickerInputHeight)
                    .padding(
                        start = spacing.transactionTimePickerInputHorizontal,
                        end = spacing.transactionTimePickerInputHorizontal
                    ),
                contentAlignment = Alignment.Center
            ) {
                TimeInput(
                    state = timePickerState,
                    colors = timePickerColors
                )
            }
            TransactionTimePickerActions(
                onCancelClick = onDismissRequest,
                onApplyClick = {
                    onTimeSelected(
                        LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun TransactionTimePickerActions(
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(sizing.transactionTimePickerActionsHeight)
            .padding(
                start = spacing.transactionTimePickerActionsStart,
                end = spacing.transactionTimePickerActionsEnd,
                bottom = spacing.transactionTimePickerActionsBottom
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(sizing.topBarActionSize),
            contentAlignment = Alignment.Center
        ) {
            FinanceClockIcon(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(sizing.icon)
            )
        }
        Row(
            modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight),
            horizontalArrangement = Arrangement.spacedBy(
                space = spacing.transactionTimePickerActionsGap,
                alignment = Alignment.End
            )
        ) {
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.picker_cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            TextButton(
                onClick = onApplyClick,
                modifier = Modifier.height(sizing.transactionTimePickerActionButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.picker_apply),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
