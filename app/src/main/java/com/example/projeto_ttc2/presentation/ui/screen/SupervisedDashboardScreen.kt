package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.ui.components.*
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisedDashboardScreen(
    userName: String,
    dashboardData: DashboardData,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSosClick: () -> Unit,
    onLogout: () -> Unit,
) {
    var showFallDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onSosClick,
                    shape = CircleShape,
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Botão de Emergência SOS", modifier = Modifier.size(36.dp))
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                item {
                    DashboardHeader(userName = userName, onLogout = onLogout)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item { StepsCard(steps = dashboardData.steps, goal = dashboardData.stepsGoal, distanceKm = dashboardData.distanceKm) }
                item { CaloriesCard(activeKcal = dashboardData.activeCaloriesKcal, totalKcal = dashboardData.caloriesKcal) }
                item { HeartRateCard(bpm = dashboardData.heartRate) }
                item { SleepCard(sleepSession = dashboardData.sleepSession) }
                item { ExerciseCard() }
                item { AlertsCard() }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFallDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simular Detecção de Queda")
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Atualizando...")
                        } else {
                            Text("Atualizar Dados")
                        }
                    }
                }
            }
        }

        FallDetectionDialog(
            showDialog = showFallDialog,
            onDismiss = {
                println("Alerta de queda cancelado pelo usuário.")
                showFallDialog = false
            },
            onTimerFinished = {
                println("TEMPO ESGOTADO! LIGANDO PARA EMERGÊNCIA...")
                showFallDialog = false
                // TODO: Chamar a lógica de emergência do seu ViewModel
            }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SupervisedDashboardScreenPreview() {
    val sampleSleepSession = Sono(
        healthConnectId = "preview_id",
        startTime = Instant.now().minus(8, ChronoUnit.HOURS),
        endTime = Instant.now(),
        durationMinutes = 455,
        awakeDurationMinutes = 25,
        remSleepDurationMinutes = 90,
        lightSleepDurationMinutes = 240,
        deepSleepDurationMinutes = 100
    )
    val sampleData = DashboardData(
        steps = 3521,
        distanceKm = 2.5,
        heartRate = 68,
        sleepSession = sampleSleepSession
    )
    ProjetoTTC2Theme {
        SupervisedDashboardScreen(
            userName = "Maria",
            dashboardData = sampleData,
            isRefreshing = false,
            onRefresh = {},
            onSosClick = {},
            onLogout = {}
        )
    }
}