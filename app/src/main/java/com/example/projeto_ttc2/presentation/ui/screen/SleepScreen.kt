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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.database.entities.SleepStage
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.presentation.ui.theme.DarkText
import com.example.projeto_ttc2.presentation.ui.theme.LightTeal
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.atan2

// Cores
private val DarkTeal = DarkText
private val MediumTeal = TealGreen
private val AppLightTeal = LightTeal
private val LightBlue = Color(0xFF4DD0E1)
private val AwakeGray = Color(0xFFBDBDBD)

internal data class SleepSlice(
    val label: String,
    val color: Color,
    val proportion: Float,
    var startAngle: Float = 0f,
    var endAngle: Float = 0f
)

@Composable
fun SleepScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel
) {
    val sleepSessionWithStages by dashboardViewModel.latestSleepSessionWithStages.collectAsStateWithLifecycle()
    val sono = sleepSessionWithStages?.sono
    val stages = sleepSessionWithStages?.stages ?: emptyList()

    val allSlices = remember(sono) {
        val deepMinutes = sono?.deepSleepDurationMinutes?.toFloat() ?: 0f
        val remMinutes = sono?.remSleepDurationMinutes?.toFloat() ?: 0f
        val lightMinutes = sono?.lightSleepDurationMinutes?.toFloat() ?: 0f
        val awakeMinutes = sono?.awakeDurationMinutes?.toFloat() ?: 0f
        val totalSleep = (deepMinutes + remMinutes + lightMinutes + awakeMinutes).coerceAtLeast(1f)

        listOf(
            SleepSlice("Sono profundo", DarkTeal, deepMinutes / totalSleep),
            SleepSlice("Sono leve", AppLightTeal, lightMinutes / totalSleep),
            SleepSlice("Sono REM", LightBlue, remMinutes / totalSleep),
            SleepSlice("Acordado", AwakeGray, awakeMinutes / totalSleep)
        )
    }

    var selectedSlice by remember(allSlices) {
        mutableStateOf(allSlices.firstOrNull { it.proportion > 0 })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SleepSummaryCard(sleepData = sono, stages = stages)

        // Gráfico Donut
        SleepDonutChart(
            drawableSlices = allSlices.filter { it.proportion > 0 },
            allSlices = allSlices,
            selectedSlice = selectedSlice,
            onSliceSelected = { slice -> selectedSlice = slice },
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        )

        SleepLegend(
            allSlices = allSlices,
            selectedSlice = selectedSlice,
            onItemClick = { slice -> selectedSlice = slice }
        )


    }
}

@Composable
fun SleepSummaryCard(sleepData: Sono?, stages: List<SleepStage>) {
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
                Text(
                    "Sono geral",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (stages.isNotEmpty() && sleepData != null) {
                sleepData.startTime?.let {
                    sleepData.endTime?.let { it1 ->
                        SleepTimelineBar(
                            stages = stages,
                            startTime = it,
                            endTime = it1
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Duração do sono: ${formatDuration(sleepData?.durationMinutes)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Sono profundo: ${formatDuration(sleepData?.deepSleepDurationMinutes)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Sono leve: ${formatDuration(sleepData?.lightSleepDurationMinutes)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Sono REM: ${formatDuration(sleepData?.remSleepDurationMinutes)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Acordado: ${formatDuration(sleepData?.awakeDurationMinutes)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SleepTimelineBar(
    stages: List<SleepStage>,
    startTime: Instant,
    endTime: Instant,
    modifier: Modifier = Modifier
) {
    val totalDuration = Duration.between(startTime, endTime).toMillis().coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (stages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
        } else {
            stages.forEach { stage ->
                val stageDuration = Duration.between(stage.startTime, stage.endTime).toMillis()
                val weight = stageDuration.toFloat() / totalDuration
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(getSleepStageColor(stage.type))
                )
            }
        }
    }
}

@Composable
internal fun SleepDonutChart(
    drawableSlices: List<SleepSlice>,
    allSlices: List<SleepSlice>,
    selectedSlice: SleepSlice?,
    onSliceSelected: (SleepSlice) -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .size(250.dp)
            .pointerInput(slicesToDraw) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val touchAngle = Math.toDegrees(atan2((offset.y - centerY).toDouble(), (offset.x - centerX).toDouble())).toFloat()
                    // Normalize angle
                    val normalizedAngle = (touchAngle + 360) % 360
                    val slice = slicesToDraw.find {
                        val start = (it.startAngle + 360) % 360
                        val end = (it.endAngle + 360) % 360
                        if (start <= end) normalizedAngle in start..end else normalizedAngle >= start || normalizedAngle <= end
                    }
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
            val currentSlice = allSlices.find { it.label == selectedSlice?.label }
            val percentageToShow = ((currentSlice?.proportion ?: 0f) * 100).toInt()
            val labelToShow = currentSlice?.label ?: "Sem dados"

            Text(
                text = "$percentageToShow%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = currentSlice?.color ?: DarkTeal
            )
            Text(
                text = labelToShow,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SleepLegend(
    allSlices: List<SleepSlice>,
    selectedSlice: SleepSlice?,
    onItemClick: (SleepSlice) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        allSlices.filter { it.proportion > 0 }.forEach { slice ->
            LegendItem(
                color = slice.color,
                text = slice.label,
                isSelected = slice.label == selectedSlice?.label,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Selecionar ${slice.label}"
                    ) { onItemClick(slice) }
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    text: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Composable
fun SleepTimelineChart(
    stages: List<SleepStage>,
    startTime: Instant,
    endTime: Instant,
    modifier: Modifier = Modifier
) {
    val totalDurationMillis = Duration.between(startTime, endTime).toMillis().coerceAtLeast(1)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
            stages.forEach { stage ->
                val stageStartMillis = Duration.between(startTime, stage.startTime).toMillis()
                val stageDurationMillis = Duration.between(stage.startTime, stage.endTime).toMillis()

                val startX = (stageStartMillis.toFloat() / totalDurationMillis) * size.width
                val barWidth = (stageDurationMillis.toFloat() / totalDurationMillis) * size.width

                drawRect(
                    color = getSleepStageColor(stage.type),
                    topLeft = Offset(x = startX, y = 0f),
                    size = Size(width = barWidth, height = size.height)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(timeFormatter.format(startTime), style = MaterialTheme.typography.labelSmall)
            Text(timeFormatter.format(endTime), style = MaterialTheme.typography.labelSmall)
        }
    }
}


fun getSleepStageColor(stageType: Int): Color {
    return when (stageType) {
        SleepSessionRecord.STAGE_TYPE_AWAKE, SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> AwakeGray
        SleepSessionRecord.STAGE_TYPE_DEEP -> DarkTeal
        SleepSessionRecord.STAGE_TYPE_LIGHT -> AppLightTeal
        SleepSessionRecord.STAGE_TYPE_REM -> LightBlue
        else -> Color.Gray
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun SleepScreenPreview() {
    val now = Instant.now()
    val sono = Sono(
        startTime = now.minus(8, ChronoUnit.HOURS),
        endTime = now,
        durationMinutes = 480,
        deepSleepDurationMinutes = 120,
        lightSleepDurationMinutes = 240,
        remSleepDurationMinutes = 70,
        awakeDurationMinutes = 50
    )

    MaterialTheme {
        SleepScreen(navController = rememberNavController(), dashboardViewModel = hiltViewModel())
    }
}