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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projeto_ttc2.database.entities.Sono
import com.example.projeto_ttc2.presentation.ui.theme.TealGreen
import com.example.projeto_ttc2.presentation.ui.theme.LightTeal
import com.example.projeto_ttc2.presentation.ui.theme.DarkText
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.atan2

// Cores seguindo o padrão do theme
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
fun SleepScreen(navController: NavController, sleepData: Sono?) {
    val allSlices = remember(sleepData) {
        val deepMinutes = sleepData?.deepSleepDurationMinutes?.toFloat() ?: 0f
        val remMinutes = sleepData?.remSleepDurationMinutes?.toFloat() ?: 0f
        val lightMinutes = sleepData?.lightSleepDurationMinutes?.toFloat() ?: 0f
        val awakeMinutes = sleepData?.awakeDurationMinutes?.toFloat() ?: 0f
        val totalSleep = (deepMinutes + remMinutes + lightMinutes + awakeMinutes).coerceAtLeast(1f)

        listOf(
            SleepSlice("Sono profundo", DarkTeal, deepMinutes / totalSleep),
            SleepSlice("Sono leve", AppLightTeal, lightMinutes / totalSleep),
            SleepSlice("Sono REM", LightBlue, remMinutes / totalSleep),
            SleepSlice("Acordado", AwakeGray, awakeMinutes / totalSleep)
        )
    }

    var selectedSlice by remember(allSlices) {
        mutableStateOf(allSlices.firstOrNull())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SleepSummaryCard(sleepData = sleepData)

        SleepDonutChart(
            drawableSlices = allSlices.filter { it.proportion > 0 },
            allSlices = allSlices,
            selectedSlice = selectedSlice,
            onSliceSelected = { slice -> selectedSlice = slice },
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
        )

        SleepLegend(
            allSlices = allSlices,
            selectedSlice = selectedSlice,
            onItemClick = { slice -> selectedSlice = slice }
        )
    }
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
                Text(
                    "Sono geral",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            SleepBarChart(sleepData = sleepData)

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
fun SleepBarChart(sleepData: Sono?) {
    data class SleepPhase(val duration: Float, val color: Color)
    val deep = sleepData?.deepSleepDurationMinutes?.toFloat() ?: 0f
    val light = sleepData?.lightSleepDurationMinutes?.toFloat() ?: 0f
    val rem = sleepData?.remSleepDurationMinutes?.toFloat() ?: 0f
    val awake = sleepData?.awakeDurationMinutes?.toFloat() ?: 0f

    val validPhases = listOf(
        SleepPhase(deep, DarkTeal),
        SleepPhase(light, AppLightTeal),
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
        allSlices.forEach { slice ->
            LegendItem(
                color = slice.color,
                text = slice.label,
                isSelected = slice.label == selectedSlice?.label,
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
fun LegendItem(
    color: Color,
    text: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
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