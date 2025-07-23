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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.Calorias
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.database.entities.User
import com.example.projeto_ttc2.presentation.ui.components.HeartRateBarChart
import com.example.projeto_ttc2.presentation.ui.components.InteractiveBarChart
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.ui.theme.caloriesCard
import com.example.projeto_ttc2.presentation.ui.theme.heartRateCard
import com.example.projeto_ttc2.presentation.ui.theme.sleepCard
import com.example.projeto_ttc2.presentation.ui.theme.stepsCard
import com.example.projeto_ttc2.presentation.viewmodel.AuthViewModel
import com.example.projeto_ttc2.presentation.viewmodel.PatientDetailViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun PatientDetailScreen(
    viewModel: PatientDetailViewModel = hiltViewModel()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val stepsData by viewModel.stepsData.collectAsStateWithLifecycle()
    val heartRateData by viewModel.heartRateData.collectAsStateWithLifecycle()
    val sleepData by viewModel.sleepData.collectAsStateWithLifecycle()
    val caloriesData by viewModel.caloriesData.collectAsStateWithLifecycle()
    val feedbackState by viewModel.feedbackState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Visão Geral", "Passos", "Frequência", "Sono", "Alertas")


    LaunchedEffect(feedbackState) {
        when (val state = feedbackState) {
            is PatientDetailViewModel.FeedbackState.Success -> {
                snackbarHostState.showSnackbar("Feedback enviado com sucesso!")
                viewModel.resetFeedbackState()
            }
            is PatientDetailViewModel.FeedbackState.Error -> {
                snackbarHostState.showSnackbar("Erro ao enviar feedback: ${state.message}")
                viewModel.resetFeedbackState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PatientHeader(patient = patient)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            OverviewTab(
                                viewModel = viewModel,
                                authViewModel = authViewModel,
                                stepsData = stepsData,
                                heartRateData = heartRateData,
                                sleepData = sleepData,
                                caloriesData = caloriesData,
                                feedbackState = feedbackState
                            )
                        }
                    }
                    1 -> StepsTab(stepsData)
                    2 -> HeartRateTab(heartRateData)
                    3 -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            SleepTab(sleepData)
                        }
                    }
                    4 -> FallHistoryScreen()
                }
            }
        }
    }
}


@Composable
fun PatientHeader(patient: User?) {
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
    stepsData: List<Passos>,
    heartRateData: List<BatimentoCardiaco>,
    sleepData: List<Sono>,
    caloriesData: List<Calorias>,
    feedbackState: PatientDetailViewModel.FeedbackState
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val lastHeartRate = heartRateData.maxByOrNull { it.timestamp }
        val todaySteps = stepsData.find { it.data == LocalDate.now().toString() }?.contagem ?: 0L
        val lastSleep = sleepData.maxByOrNull { it.endTime!! }

        val lastCalories = caloriesData.filter { it.tipo == "TOTAL" }.maxByOrNull { it.endTime!! }?.kilocalorias ?: 0.0

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                icon = Icons.Default.DirectionsWalk,
                label = "Passos Hoje",
                value = todaySteps.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.stepsCard
            )
            SummaryCard(
                icon = Icons.Default.FavoriteBorder,
                label = "Último BPM",
                value = lastHeartRate?.bpm?.toString() ?: "--",
                unit = if (lastHeartRate != null) "bpm" else null,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.heartRateCard
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
                color = MaterialTheme.colorScheme.sleepCard
            )
            SummaryCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "Calorias",
                value = if (lastCalories > 0) "%.0f".format(lastCalories) else "--",
                unit = "kcal",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.caloriesCard
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FeedbackInputCard(viewModel, authViewModel, feedbackState)
    }
}

@Composable
fun StepsTab(stepsData: List<Passos>) {
    if (stepsData.isEmpty()) {
        EmptyContentState("Sem dados de passos para exibir.")
        return
    }

    val chartData = remember(stepsData) {
        stepsData.takeLast(7).map {
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
        Text("Variação de Batimentos (Hoje)", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
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
    val durationText = if (minutes != null && minutes > 0) {
        "${minutes / 60}h ${minutes % 60}m"
    } else {
        "--"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
    color: Color
) {
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Column {
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
}

@Composable
fun EmptyContentState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Sem dados",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FeedbackInputCard(
    viewModel: PatientDetailViewModel,
    authViewModel: AuthViewModel,
    feedbackState: PatientDetailViewModel.FeedbackState
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
                modifier = Modifier.align(Alignment.End),

                enabled = feedbackText.isNotBlank() && feedbackState != PatientDetailViewModel.FeedbackState.Loading
            ) {

                if (feedbackState == PatientDetailViewModel.FeedbackState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PatientHeaderPreview() {
    ProjetoTTC2Theme {
        PatientHeader(patient = User(id = "2", name = "Maria Oliveira", email = "maria@example.com"))
    }
}

