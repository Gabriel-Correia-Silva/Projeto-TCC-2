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
import androidx.compose.ui.unit.dp
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.ui.components.*
import com.example.projeto_ttc2.presentation.ui.theme.defaultCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisedDashboardScreen(
    userName: String,
    dashboardData: DashboardData,
    heartRateData: List<Long> = emptyList(),
    onSosClick: () -> Unit,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    onBackgroundRefresh: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToHeartRate: () -> Unit,
    onNavigateToSteps: () -> Unit
) {
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
                HeartRateCard(
                    bpm = dashboardData.heartRate,
                    heartRateData = heartRateData,
                    onClick = onNavigateToHeartRate,
                    cardColor = MaterialTheme.colorScheme.defaultCard
                )
            }
            item {
                StepsCard(
                    steps = dashboardData.steps,
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