package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisedDashboardScreen(
    userName: String,
    dashboardData: DashboardData,
    heartRateData: List<Long> = emptyList(),
    onSosClick: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToSleep: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
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
                    heartRateData = heartRateData
                )
            }
            item {
                StepsCard(
                    steps = dashboardData.steps,
                    goal = dashboardData.stepsGoal,
                    distanceKm = dashboardData.distanceKm
                )
            }
            item {
                CaloriesCard(
                    activeKcal = dashboardData.activeCaloriesKcal,
                    totalKcal = dashboardData.caloriesKcal
                )
            }
            item {
                SleepCard(
                    sleepSession = dashboardData.sleepSession,
                    onClick = onNavigateToSleep
                )
            }
        }
    }
}