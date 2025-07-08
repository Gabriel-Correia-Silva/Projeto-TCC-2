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
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.PatientDetailViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Cabeçalho do Paciente
        PatientHeader(patient = patient)

        // Abas de Navegação
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = if(selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Conteúdo da Aba
        Box(modifier = Modifier.padding(16.dp)) {
            when (selectedTabIndex) {
                0 -> OverviewTab(viewModel, authViewModel, stepsData, heartRateData, sleepData)
                1 -> StepsTab(stepsData)
                2 -> HeartRateTab(heartRateData)
                3 -> SleepTab(sleepData)
            }
        }
    }
}

@Composable
fun PatientHeader(patient: com.example.projeto_ttc2.database.entities.User?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        if (patient == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = patient.profileImageUrl,
                        fallback = painterResource(id = R.drawable.ic_launcher_foreground)
                    ),
                    contentDescription = "Foto do Perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = patient.name ?: "Paciente",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = patient.email ?: "Email não disponível",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
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
        val lastHeartRate = heartRate.maxByOrNull { it.timestamp }
        val todaySteps = steps.find { it.data == LocalDate.now().toString() }?.contagem ?: 0L
        val lastSleep = sleep.maxByOrNull { it.endTime }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                icon = Icons.Default.DirectionsWalk,
                label = "Passos Hoje",
                value = todaySteps.toString(),
                modifier = Modifier.weight(1f),
                color = Color(0xFF64B5F6) // StepsBlue
            )
            SummaryCard(
                icon = Icons.Default.FavoriteBorder,
                label = "Último BPM",
                value = lastHeartRate?.bpm?.toString() ?: "--",
                unit = if (lastHeartRate != null) "bpm" else null,
                modifier = Modifier.weight(1f),
                color = Color(0xFFE57373) // HeartRateRed
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val sleepHours = lastSleep?.durationMinutes?.div(60) ?: 0
            val sleepMinutes = lastSleep?.durationMinutes?.rem(60) ?: 0
            SummaryCard(
                icon = Icons.Default.Bedtime,
                label = "Último Sono",
                value = if (lastSleep != null) "${sleepHours}h ${sleepMinutes}m" else "--",
                modifier = Modifier.weight(1f),
                color = Color(0xFF81C784) // SleepGreen
            )
            SummaryCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "Calorias",
                value = "--", // Dado não disponível no ViewModel
                unit = "kcal",
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFB74D) // CaloriesOrange
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FeedbackInputCard(viewModel, authViewModel)
    }
}

@Composable
fun StepsTab(stepsData: List<Passos>) {
    if (stepsData.isEmpty()) {
        EmptyContentState("Sem dados de passos para exibir.")
        return
    }

    val chartData = remember(stepsData) {
        stepsData.map {
            val date = LocalDate.parse(it.data)
            BarData(
                value = it.contagem.toFloat(),
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Passos na Última Semana", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        InteractiveBarChart(
            data = chartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Composable
fun HeartRateTab(heartRateData: List<BatimentoCardiaco>) {
    if (heartRateData.isEmpty()) {
        EmptyContentState("Sem dados de frequência cardíaca para exibir.")
        return
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Batimentos Hoje", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        HeartRateBarChart(
            data = heartRateData,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Composable
fun SleepTab(sleepData: List<Sono>) {
    val lastSleep = sleepData.firstOrNull()
    if (lastSleep == null) {
        EmptyContentState("Sem dados de sono para exibir.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Detalhes da Última Noite de Sono", style = MaterialTheme.typography.titleLarge)
        SleepInfoRow("Duração total", lastSleep.durationMinutes)
        SleepInfoRow("Sono profundo", lastSleep.deepSleepDurationMinutes)
        SleepInfoRow("Sono leve", lastSleep.lightSleepDurationMinutes)
        SleepInfoRow("Sono REM", lastSleep.remSleepDurationMinutes)
        SleepInfoRow("Tempo acordado", lastSleep.awakeDurationMinutes)
    }
}

@Composable
fun SleepInfoRow(label: String, minutes: Long?) {
    val durationText = if (minutes != null) {
        "${minutes / 60}h ${minutes % 60}m"
    } else {
        "--"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(durationText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun SummaryCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    color: Color = TealGreen
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                unit?.let {
                    Text(
                        text = " $it",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyContentState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
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
                        viewModel.sendFeedback(senderId, feedbackText)
                        feedbackText = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar")
            }
        }
    }
}