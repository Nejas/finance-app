package com.example.financeapp.presentation.main

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.accounts.AccountsRoute
import com.example.financeapp.presentation.analytics.AnalyticsRoute
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
import com.example.financeapp.presentation.common.network.NetworkStatusBanner
import com.example.financeapp.presentation.common.network.PendingSyncStatusBanner
import com.example.financeapp.presentation.expenses.ExpensesRoute
import com.example.financeapp.presentation.income.IncomeRoute
import com.example.financeapp.presentation.navigation.AppNavGraph
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import androidx.navigation.NavHostController
import kotlin.math.abs

internal class MainNavigationActions(
    val onTransactionClick: (Long, TransactionType) -> Unit,
    val onTransactionDeleteRequest: (Long) -> Unit,
    val onAccountClick: (Long) -> Unit,
    val onAccountDeleteRequest: (Long) -> Unit
)

@Composable
internal fun MainNavigationContent(
    navController: NavHostController,
    selectedRoute: AppRoute,
    actions: MainNavigationActions,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel()
) {
    val mainState by mainViewModel.state.collectAsState()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()
    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    val edgeGuardPx = with(density) { spacing.contentSwipeEdgeGuard.toPx() }
    val swipeThresholdPx = with(density) { spacing.contentSwipeThreshold.toPx() }

    Column(modifier = modifier.fillMaxSize()) {
        if (!isOnline) NetworkStatusBanner(modifier = Modifier.fillMaxWidth())
        if (mainState.hasPendingSync) PendingSyncStatusBanner(modifier = Modifier.fillMaxWidth())

        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(edgeGuardPx, swipeThresholdPx, selectedRoute) {
                    var dragOffset = 0f
                    var canHandleSwipe = false
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragOffset = 0f
                            canHandleSwipe = selectedRoute.isMainRoute() &&
                                offset.x > edgeGuardPx && offset.x < size.width - edgeGuardPx
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (canHandleSwipe) {
                                dragOffset += dragAmount
                                change.consume()
                            }
                        },
                        onDragCancel = {
                            dragOffset = 0f
                            canHandleSwipe = false
                        },
                        onDragEnd = {
                            if (canHandleSwipe && abs(dragOffset) >= swipeThresholdPx) {
                                val nextRoute = if (dragOffset < 0) {
                                    selectedRoute.nextMainRoute()
                                } else {
                                    selectedRoute.previousMainRoute()
                                }
                                navController.navigateToRoute(nextRoute)
                            }
                            dragOffset = 0f
                            canHandleSwipe = false
                        }
                    )
                },
            expensesContent = { modifier ->
                ExpensesRoute(
                    modifier = modifier,
                    state = mainState.expensesState,
                    onRetry = { mainViewModel.onIntent(MainIntent.Retry) },
                    onTransactionClick = { id -> actions.onTransactionClick(id, TransactionType.EXPENSE) },
                    onTransactionDeleteRequest = actions.onTransactionDeleteRequest
                )
            },
            incomeContent = { modifier ->
                IncomeRoute(
                    modifier = modifier,
                    state = mainState.incomeState,
                    onRetry = { mainViewModel.onIntent(MainIntent.Retry) },
                    onTransactionClick = { id -> actions.onTransactionClick(id, TransactionType.INCOME) },
                    onTransactionDeleteRequest = actions.onTransactionDeleteRequest
                )
            },
            accountsContent = { modifier ->
                AccountsRoute(
                    modifier = modifier,
                    state = mainState.accountsState,
                    onRetry = { mainViewModel.onIntent(MainIntent.Retry) },
                    onAccountClick = actions.onAccountClick,
                    onAccountDeleteRequest = actions.onAccountDeleteRequest
                )
            },
            analyticsContent = { modifier ->
                AnalyticsRoute(modifier = modifier, onBack = navController::navigateBackToMain)
            }
        )
    }
}
