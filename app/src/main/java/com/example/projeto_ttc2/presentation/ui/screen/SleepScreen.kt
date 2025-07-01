package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.database.entities.Sono
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.atan2

// Cores
private val DarkTeal = Color(0xFF388E8A)
private val MediumTeal = Color(0xFF4DB6AC)
private val LightTeal = Color(0xFFB2DFDB)
private val BackgroundTeal = Color(0xFFE0F2F1)
private val LightBlue = Color(0xFF4DD0E1)
private val AwakeGray = Color(0xFFBDBDBD)
private val FabRed = Color(0xFFE53935)

// --- SOLUÇÃO APLICADA AQUI ---
// A classe agora é 'internal', visível dentro do módulo do app.
internal data class SleepSlice(
    val label: String,
    val color: Color,
    val proportion: Float,
    var startAngle: Float = 0f,
    var endAngle: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(navController: NavController, sleepData: Sono?) {
    val allSlices = remember(sleepData) {
        val deepMinutes = sleepData?.deepSleepDurationMinutes?.toFloat() ?: 0f
        val remMinutes = sleepData?.remSleepDurationMinutes?.toFloat() ?: 0f
        val lightMinutes = sleepData?.lightSleepDurationMinutes?.toFloat() ?: 0f
        val awakeMinutes = sleepData?.awakeDurationMinutes?.toFloat() ?: 0f
        val totalSleep = (deepMinutes + remMinutes + lightMinutes + awakeMinutes).coerceAtLeast(1f)

        listOf(
            SleepSlice("Sono profundo", DarkTeal, deepMinutes / totalSleep),
            SleepSlice("Sono leve", LightTeal, lightMinutes / totalSleep),
            SleepSlice("Sono REM", LightBlue, remMinutes / totalSleep),
            SleepSlice("Acordado", AwakeGray, awakeMinutes / totalSleep)
        )
    }

    var selectedSlice by remember(allSlices) {
        mutableStateOf(allSlices.firstOrNull())
    }

    Scaffold(
        containerColor = BackgroundTeal,
        topBar = { SleepTopAppBar(onBackClicked = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Lógica SOS */ },
                containerColor = FabRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.Call, "Botão de Emergência", Modifier.size(36.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SleepSummaryCard(sleepData = sleepData)

            SleepDonutChart(
                drawableSlices = allSlices.filter { it.proportion > 0 },
                selectedSlice = selectedSlice,
                onSliceSelected = { slice -> selectedSlice = slice }
            )

            SleepLegend(
                allSlices = allSlices,
                onItemClick = { slice -> selectedSlice = slice }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTopAppBar(onBackClicked: () -> Unit) {
    TopAppBar(
        title = { Text("Detalhes do Sono", color = DarkTeal, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = DarkTeal)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
fun SleepSummaryCard(sleepData: Sono?) {
    fun formatDuration(minutes: Long?): String {
        if (minutes == null || minutes <= 0) return "--"
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}min"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MediumTeal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bedtime, contentDescription = "Ícone de sono", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sono geral", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            SleepBarChart(sleepData = sleepData)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Duração do sono: ${formatDuration(sleepData?.durationMinutes)}", color = Color.White)
                    Text("Sono profundo: ${formatDuration(sleepData?.deepSleepDurationMinutes)}", color = Color.White)
                    Text("Sono leve: ${formatDuration(sleepData?.lightSleepDurationMinutes)}", color = Color.White)
                    Text("Sono REM: ${formatDuration(sleepData?.remSleepDurationMinutes)}", color = Color.White)
                    Text("Acordado: ${formatDuration(sleepData?.awakeDurationMinutes)}", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SleepBarChart(sleepData: Sono?) {
    data class SleepPhase(val duration: Float, val color: Color)
    val deep = sleepData?.deepSleepDurationMinutes?.toFloat() ?: 0f
    val light = sleepData?.lightSleepDurationMinutes?.toFloat() ?: 0f
    val rem = sleepData?.remSleepDurationMinutes?.toFloat() ?: 0f
    val awake = sleepData?.awakeDurationMinutes?.toFloat() ?: 0f

    val validPhases = listOf(
        SleepPhase(deep, DarkTeal),
        SleepPhase(light, LightTeal),
        SleepPhase(rem, LightBlue),
        SleepPhase(awake, AwakeGray)
    ).filter { it.duration > 0f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (validPhases.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
        } else {
            validPhases.forEach { phase ->
                Box(modifier = Modifier.weight(phase.duration).fillMaxHeight().background(phase.color))
            }
        }
    }
}

@Composable
internal fun SleepDonutChart(
    drawableSlices: List<SleepSlice>,
    selectedSlice: SleepSlice?,
    onSliceSelected: (SleepSlice) -> Unit
) {
    val slicesToDraw = remember(drawableSlices) {
        var currentAngle = -90f
        drawableSlices.forEach { slice ->
            val sweepAngle = slice.proportion * 360f
            slice.startAngle = currentAngle
            slice.endAngle = currentAngle + sweepAngle
            currentAngle += sweepAngle
        }
        drawableSlices
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(250.dp)
            .pointerInput(slicesToDraw) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val touchAngle = Math.toDegrees(atan2((offset.y - centerY).toDouble(), (offset.x - centerX).toDouble())).toFloat()
                    val slice = slicesToDraw.find { touchAngle in it.startAngle..it.endAngle }
                    if (slice != null) {
                        onSliceSelected(slice)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            slicesToDraw.forEach { slice ->
                drawArc(
                    color = slice.color,
                    startAngle = slice.startAngle,
                    sweepAngle = slice.endAngle - slice.startAngle,
                    useCenter = false,
                    style = Stroke(width = 50f, cap = StrokeCap.Butt)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val percentageToShow = ((selectedSlice?.proportion ?: 0f) * 100).toInt()
            val labelToShow = selectedSlice?.label ?: "Sem dados"

            Text(
                text = "$percentageToShow%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = selectedSlice?.color ?: DarkTeal
            )
            Text(
                text = labelToShow,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SleepLegend(
    allSlices: List<SleepSlice>,
    onItemClick: (SleepSlice) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        allSlices.forEach { slice ->
            LegendItem(
                color = slice.color,
                text = slice.label,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Selecionar ${slice.label}"
                    ) { onItemClick(slice) }
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.Gray, fontSize = 16.sp)
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun SleepScreenPreview() {
    val previewSleepData = Sono(
        healthConnectId = "preview_id",
        startTime = Instant.now().minus(8, ChronoUnit.HOURS),
        endTime = Instant.now(),
        durationMinutes = 452,
        deepSleepDurationMinutes = 120,
        lightSleepDurationMinutes = 240,
        remSleepDurationMinutes = 70,
        awakeDurationMinutes = 0
    )
    SleepScreen(navController = rememberNavController(), sleepData = previewSleepData)
}