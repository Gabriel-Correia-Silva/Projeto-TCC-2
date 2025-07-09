package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateDetailScreen(
    currentBpm: Long,
    dashboardViewModel: DashboardViewModel,
    onBackClick: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dailyHeartRateData by dashboardViewModel.heartRateForDate.collectAsStateWithLifecycle()
    var selectedHeartRate by remember { mutableStateOf<BatimentoCardiaco?>(null) }

    val lastValidHeartRate = dailyHeartRateData.lastOrNull()

    LaunchedEffect(selectedDate) {
        dashboardViewModel.loadHeartRateForDate(selectedDate)
        selectedHeartRate = null
    }

    val displayBpm = selectedHeartRate?.bpm ?: currentBpm
    val displayTime = selectedHeartRate?.timestamp?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    } ?: LocalDateTime.now()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateSelector(
                selectedDate = selectedDate,
                onDateChange = { newDate ->
                    selectedDate = newDate
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (selectedHeartRate != null) "Batimento Selecionado" else "Batimento Atual",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (displayBpm > 0) "$displayBpm" else "--",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    "bpm",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }
            Text(
                displayTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )


            Spacer(modifier = Modifier.height(32.dp))

            if (dailyHeartRateData.isNotEmpty()) {
                HeartRateLineChart(
                    data = dailyHeartRateData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    onHeartRateSelected = { selectedHeartRate = it },
                    selectedHeartRate = selectedHeartRate ?: lastValidHeartRate
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sem dados de histórico para este dia.")
                }
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "BR"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior")
        }

        Text(
            text = if (selectedDate.isEqual(LocalDate.now())) "Hoje" else selectedDate.format(formatter),
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(
            onClick = { onDateChange(selectedDate.plusDays(1)) },
            enabled = selectedDate.isBefore(LocalDate.now())
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia")
        }
    }
}

@Composable
fun HeartRateLineChart(
    data: List<BatimentoCardiaco>,
    modifier: Modifier = Modifier,
    onHeartRateSelected: (BatimentoCardiaco) -> Unit,
    selectedHeartRate: BatimentoCardiaco?
) {
    if (data.isEmpty()) return

    val maxBpm = (data.maxOfOrNull { it.bpm } ?: 120L).toFloat()
    val minBpm = (data.minOfOrNull { it.bpm } ?: 40L).toFloat()
    val range = (maxBpm - minBpm).coerceAtLeast(1f)

    val density = LocalDensity.current
    val primaryColor = colorScheme.error
    val onSurfaceColor = colorScheme.onSurface

    val textPaint = remember {
        android.graphics.Paint().apply {
            color = onSurfaceColor.hashCode()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
        }
    }

    val startOfDay = data.first().timestamp - (data.first().timestamp % (24 * 60 * 60 * 1000))
    val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

    Canvas(modifier = modifier.pointerInput(data) {
        detectHorizontalDragGestures(
            onDragEnd = {},
            onHorizontalDrag = { change, _ ->
                val touchX = change.position.x
                val closestPoint = data.minByOrNull {
                    val timeOfDayMillis = it.timestamp - startOfDay
                    val pointX = (size.width * timeOfDayMillis / (endOfDay - startOfDay))
                    kotlin.math.abs(pointX - touchX)
                }
                closestPoint?.let { onHeartRateSelected(it) }
            }
        )
    }) {
        val yAxisSpace = 40.dp.toPx()
        val xAxisSpace = 30.dp.toPx()
        val chartWidth = size.width - yAxisSpace
        val chartHeight = size.height - xAxisSpace

        // Eixo Y e linhas de grade
        (0..4).forEach { i ->
            val value = minBpm + (range / 4) * i
            val y = chartHeight - ((value - minBpm) / range) * chartHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(yAxisSpace, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${value.toInt()}",
                yAxisSpace / 2,
                y + textPaint.textSize / 2,
                textPaint
            )
        }

        // Eixo X
        (0..23 step 6).forEach { hour ->
            val x = yAxisSpace + (chartWidth / 24 * hour)
            drawContext.canvas.nativeCanvas.drawText(
                "${hour.toString().padStart(2, '0')}:00",
                x,
                size.height - (xAxisSpace / 4),
                textPaint
            )
        }

        val points = data.map {
            val timeOfDayMillis = it.timestamp - startOfDay
            val x = yAxisSpace + (chartWidth * timeOfDayMillis / (endOfDay - startOfDay))
            val y = chartHeight - ((it.bpm - minBpm) / range) * chartHeight
            Offset(x.toFloat(), y)
        }

        // Preenchimento (área)
        val fillPath = Path().apply {
            moveTo(yAxisSpace, chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, chartHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                endY = chartHeight
            )
        )

        // Linha do gráfico
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.forEach { lineTo(it.x, it.y) }
        }
        drawPath(path = linePath, color = primaryColor, style = Stroke(width = 2.dp.toPx()))

        // Indicador do ponto selecionado
        selectedHeartRate?.let {
            val timeOfDayMillis = it.timestamp - startOfDay
            val pointX = yAxisSpace + (chartWidth * timeOfDayMillis / (endOfDay - startOfDay))
            val pointY = chartHeight - ((it.bpm - minBpm) / range) * chartHeight

            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = Offset(pointX.toFloat(), 0f),
                end = Offset(pointX.toFloat(), chartHeight),
                strokeWidth = 1.dp.toPx()
            )
            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(pointX.toFloat(), pointY))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(pointX.toFloat(), pointY))
        }
    }
}