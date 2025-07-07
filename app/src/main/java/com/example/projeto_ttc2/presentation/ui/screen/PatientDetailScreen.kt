package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.presentation.ui.components.DashboardCard
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.PatientDetailViewModel

@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel = hiltViewModel()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val stepsData by viewModel.stepsData.collectAsStateWithLifecycle()
    val heartRateData by viewModel.heartRateData.collectAsStateWithLifecycle()
    val sleepData by viewModel.sleepData.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Visão Geral", "Passos", "Frequência", "Sono")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        patient?.let { user ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = user.profileImageUrl,
                        fallback = painterResource(id = R.drawable.ic_launcher_foreground)
                    ),
                    contentDescription = "Foto do Perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user.name ?: "Paciente",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email ?: "Email não disponível",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } ?: Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> OverviewTab(viewModel, authViewModel, stepsData, heartRateData, sleepData)
            1 -> StepsTab(stepsData)
            2 -> HeartRateTab(heartRateData)
            3 -> SleepTab(sleepData)
        }
    }
}

@Composable
fun OverviewTab(
    viewModel: PatientDetailViewModel,
    authViewModel: AuthViewModel,
    steps: List<Passos>,
    heartRate: List<BatimentoCardiaco>,
    sleep: List<Sono>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                icon = Icons.Default.DirectionsWalk,
                label = "Passos Hoje",
                value = steps.sumOf { it.contagem }.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Favorite,
                label = "Último BPM",
                value = "${heartRate.firstOrNull()?.bpm ?: "--"}",
                unit = "bpm",
                modifier = Modifier.weight(1f)
            )
        }
        val lastSleep = sleep.firstOrNull()
        val sleepHours = lastSleep?.durationMinutes?.div(60) ?: 0
        val sleepMinutes = lastSleep?.durationMinutes?.rem(60) ?: 0
        SummaryCard(
            icon = Icons.Default.Bedtime,
            label = "Último Sono",
            value = if (lastSleep != null) "${sleepHours}h ${sleepMinutes}m" else "--",
            isFullWidth = true
        )
        FeedbackInputCard(viewModel, authViewModel)
    }
}

@Composable
fun StepsTab(stepsData: List<Passos>) {
    Text("Detalhes de Passos")
    Text("Total de passos hoje: ${stepsData.sumOf { it.contagem }}")
}

@Composable
fun HeartRateTab(heartRateData: List<BatimentoCardiaco>) {
    if (heartRateData.isNotEmpty()) {
        val minBpm = heartRateData.minOfOrNull { it.bpm } ?: "--"
        val maxBpm = heartRateData.maxOfOrNull { it.bpm } ?: "--"
        Text("Variação de BPM hoje: $minBpm - $maxBpm bpm")
        Spacer(modifier = Modifier.height(16.dp))
    } else {
        EmptyState("Sem dados de frequência cardíaca para exibir.")
    }
}

@Composable
fun SleepTab(sleepData: List<Sono>) {
    val lastSleep = sleepData.firstOrNull()
    if(lastSleep != null) {
        Text("Detalhes da última noite de sono:")
        Text("Duração total: ${lastSleep.durationMinutes} minutos")
        Text("Sono profundo: ${lastSleep.deepSleepDurationMinutes ?: 0} minutos")
        Text("Sono leve: ${lastSleep.lightSleepDurationMinutes ?: 0} minutos")
        Text("Sono REM: ${lastSleep.remSleepDurationMinutes ?: 0} minutos")
    } else {
        EmptyState("Sem dados de sono para exibir.")
    }
}

@Composable
fun SummaryCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    DashboardCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    unit?.let {
                        Text(
                            text = " $it",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun FeedbackInputCard(
    viewModel: PatientDetailViewModel,
    authViewModel: AuthViewModel
) {
    var feedbackText by remember { mutableStateOf("") }
    val currentUser = authViewModel.getCurrentUser()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Enviar Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Digite o feedback para o paciente") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    currentUser?.uid?.let { senderId ->
                        // Acessando o método através da instância do viewModel
                        viewModel.sendFeedback(senderId, feedbackText)
                        feedbackText = "" // Limpa o campo após o envio
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Enviar")
            }
        }
    }
}