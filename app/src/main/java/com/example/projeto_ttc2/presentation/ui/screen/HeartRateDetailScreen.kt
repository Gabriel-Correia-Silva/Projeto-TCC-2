package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.presentation.viewmodel.DashboardViewModel
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

    LaunchedEffect(selectedDate) {
        dashboardViewModel.loadHeartRateForDate(selectedDate)
    }

    Scaffold() { paddingValues ->
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

            Text("Batimento Atual", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (currentBpm > 0) "$currentBpm" else "--",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    "bpm",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Média por Hora", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (dailyHeartRateData.isNotEmpty()) {
                HeartRateBarChart(
                    data = dailyHeartRateData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
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
fun HeartRateBarChart(
    data: List<BatimentoCardiaco>,
    modifier: Modifier = Modifier
) {
    val dataByHour = data.groupBy {
        LocalDateTime.ofInstant(it.timestamp, ZoneId.systemDefault()).hour
    }.mapValues { entry ->
        entry.value.map { it.bpm }.average().toLong()
    }

    if (dataByHour.isEmpty()) {
        // Handle empty data case if needed, e.g., show a message
        return
    }

    val maxBpm = (dataByHour.values.maxOrNull() ?: 120L).toFloat()
    val minBpm = (dataByHour.values.minOrNull() ?: 40L).toFloat()
    val range = (maxBpm - minBpm).coerceAtLeast(1f)

    val density = LocalDensity.current

    val primaryColor = colorScheme.primary
    val onSurfaceColor = colorScheme.onSurface

    val textPaint = android.graphics.Paint().apply {
        color = onSurfaceColor.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = with(density) { 12.sp.toPx() }
    }

    Canvas(modifier = modifier) {
        val yAxisSpace = 40.dp.toPx()
        val xAxisSpace = 30.dp.toPx()
        val chartWidth = size.width - yAxisSpace
        val chartHeight = size.height - xAxisSpace

        // Draw Y-axis labels and grid lines
        val numGridLines = 4
        for (i in 0..numGridLines) {
            val value = minBpm + (range / numGridLines) * i
            val y = chartHeight - ((value - minBpm) / range) * chartHeight
            drawLine(
                color = Color.LightGray,
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

        // Draw bars and X-axis labels
        val barWidthWithSpacing = chartWidth / 24
        val barWidth = barWidthWithSpacing * 0.7f

        for (hour in 0..23) {
            val bpm = dataByHour[hour]
            val x = yAxisSpace + (barWidthWithSpacing * hour)

            if (bpm != null) {
                val barHeight = ((bpm - minBpm) / range * chartHeight).coerceAtLeast(0f)
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }

            if (hour % 3 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    hour.toString().padStart(2, '0'),
                    x + barWidth / 2,
                    size.height - (xAxisSpace / 4),
                    textPaint
                )
            }
        }
    }
}