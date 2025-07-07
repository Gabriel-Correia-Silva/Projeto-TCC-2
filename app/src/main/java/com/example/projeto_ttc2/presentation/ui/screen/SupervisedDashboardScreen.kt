package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.ui.components.*
import com.example.projeto_ttc2.presentation.ui.theme.defaultCard
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import com.example.projeto_ttc2.presentation.viewmodel.EmergencyContactViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisedDashboardScreen(
    userName: String,
    dashboardViewModel: DashboardViewModel,
    emergencyContactViewModel: EmergencyContactViewModel,
    dashboardData: DashboardData,
    heartRateData: List<Long> = emptyList(),
    onSosClick: () -> Unit,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    onBackgroundRefresh: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToHeartRate: () -> Unit,
    onNavigateToSteps: () -> Unit
) {
    val unreadFeedbackCount by dashboardViewModel.unreadFeedbackCount.collectAsStateWithLifecycle(initialValue = 0)
    val showHeartRateAlert by dashboardViewModel.showHeartRateAlert.collectAsStateWithLifecycle()
    val primaryContact by emergencyContactViewModel.primaryContact.collectAsStateWithLifecycle()
    val oxygenationHistory by dashboardViewModel.oxygenationHistory.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (showHeartRateAlert) {
        HeartRateAlertDialog(
            onDismiss = { dashboardViewModel.dismissHeartRateAlert() },
            onConfirm = {
                dashboardViewModel.triggerEmergencyActions(context, primaryContact?.phone)
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            onBackgroundRefresh()
            delay(30000)
        }
    }

    var goalReached by remember { mutableStateOf(false) }
    var showIncentiveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(dashboardData.steps, dashboardData.stepsGoal) {
        if (dashboardData.steps >= dashboardData.stepsGoal && !goalReached) {
            showIncentiveDialog = true
            goalReached = true
        } else if (dashboardData.steps < dashboardData.stepsGoal) {
            goalReached = false
        }
    }

    if (showIncentiveDialog) {
        IncentiveDialog(
            steps = dashboardData.steps,
            onDismiss = { showIncentiveDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onManualRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                UnreadFeedbackCard(
                    unreadCount = unreadFeedbackCount,
                    onClick = onNavigateToFeedback,
                    cardColor = MaterialTheme.colorScheme.tertiary
                )
            }
            item {
                HeartRateCard(
                    bpm = dashboardData.heartRate,
                    heartRateData = heartRateData,
                    onClick = onNavigateToHeartRate,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
            item {
                StepsCard(
                    steps = dashboardData.steps.toInt(),
                    goal = dashboardData.stepsGoal,
                    distanceKm = dashboardData.distanceKm,
                    onClick = onNavigateToSteps,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
            item {
                CaloriesCard(
                    activeKcal = dashboardData.activeCaloriesKcal,
                    totalKcal = dashboardData.caloriesKcal,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
            item {
                OxygenationCard(
                    spo2 = dashboardData.oxygenSaturation,
                    historicalSpo2 = oxygenationHistory,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
            item {
                SleepCard(
                    sleepSession = dashboardData.sleepSession,
                    onClick = onNavigateToSleep,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
        }
    }
}