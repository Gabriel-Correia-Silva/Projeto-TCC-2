package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.local.DashboardData
import com.example.projeto_ttc2.presentation.ui.components.FallDetectionDialog
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

val TealGreen = Color(0xFF4DB6AC)
val LightTeal = Color(0xFFE0F2F1)
val DarkText = Color(0xFF004D40)

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
            containerColor = LightTeal,
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
                item { SleepCard(durationMinutes = dashboardData.sleepDurationMinutes) }
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
                                color = Color.White,
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

@Composable
fun DashboardHeader(userName: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Oi, $userName!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Row {
            IconButton(onClick = { /* TODO: Navegar para tela de segurança */ }) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Status de Segurança", tint = TealGreen)
            }
            IconButton(onClick = { /* TODO: Navegar para tela de notificações */ }) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notificações", tint = TealGreen)
            }
            IconButton(onClick = onLogout) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Configurações", tint = TealGreen)
            }
        }
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TealGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun StepsCard(steps: Long, goal: Long, distanceKm: Double) {
    val progress = if (goal > 0) (steps.toFloat() / goal.toFloat()) else 0f
    DashboardCard {
        Text("Passos", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        Text("$steps / $goal passos", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Distância total: ${"%.1f".format(distanceKm)} km", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun CaloriesCard(activeKcal: Double, totalKcal: Double) {
    val progress = if (totalKcal > 0) (activeKcal.toFloat() / totalKcal.toFloat()) else 0f
    DashboardCard {
        Text("Calorias", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        Text("${activeKcal.roundToInt()} kcal (ativos)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Consumo total: ${totalKcal.roundToInt()} kcal", color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun HeartRateCard(bpm: Long) {
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Frequência", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (bpm > 0) "$bpm" else "--", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("bpm", fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            }
            Image(painter = painterResource(id = R.drawable.icons8_google_logo), contentDescription = "Gráfico de Frequência")
        }
        Text(if (bpm > 0) "Última leitura" else "Nenhum dado", color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun SleepCard(durationMinutes: Long) {
    val hours = TimeUnit.MINUTES.toHours(durationMinutes)
    val minutes = durationMinutes % 60
    val durationText = if (hours > 0 || minutes > 0) "${hours}h ${minutes}min" else "Nenhum dado"
    DashboardCard {
        Text("Sono", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Text("Gráfico de Sono Aqui", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Duração do sono: $durationText", color = Color.White, fontSize = 14.sp)
            Text("Pontuação: --", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun ExerciseCard() {
    DashboardCard {
        Text("Exercícios", color = Color.White, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("01", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Exercícios na semana", color = Color.White, fontSize = 14.sp)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("01h", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Duração", color = Color.White, fontSize = 14.sp)
            }
        }
        Divider(color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
        Text("Último exercício: Caminhada - 1h 10min - 298 kcal", color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun AlertsCard() {
    DashboardCard {
        Text("Meus Alertas Recentes", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("• Alerta de queda detectado (05/02)", color = Color.White)
            Text("• Bateria fraca do sensor (04/02)", color = Color.White)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
@Composable
fun SupervisedDashboardScreenPreview() {
    val sampleData = DashboardData(
        steps = 3521,
        distanceKm = 2.5,
        caloriesKcal = 1890.0,
        activeCaloriesKcal = 310.0,
        heartRate = 68,
        sleepDurationMinutes = 455
    )
    SupervisedDashboardScreen(
        userName = "Maria",
        dashboardData = sampleData,
        isRefreshing = false,
        onRefresh = {},
        onSosClick = {},
        onLogout = {}
    )
}