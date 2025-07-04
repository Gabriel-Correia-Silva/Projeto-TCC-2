package com.example.projeto_ttc2.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateDetailScreen(
    currentBpm: Long,
    dailyHeartRateData: List<BatimentoCardiaco>,
    onBackClick: () -> Unit
) {
    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Text("Histórico do Dia (Média por Hora)", style = MaterialTheme.typography.titleLarge)
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
                    Text("Sem dados de histórico para hoje.")
                }
            }
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

    if (dataByHour.isEmpty()) return

    val maxBpm = (dataByHour.values.maxOrNull() ?: 120L).toFloat()
    val minBpm = (dataByHour.values.minOrNull() ?: 40L).toFloat()
    val range = (maxBpm - minBpm).coerceAtLeast(1f)

    val density = LocalDensity.current

    // Obter as cores fora do contexto Canvas
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

        // Desenha as linhas de grade e os rótulos do eixo Y
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

        // Desenha as barras e os rótulos do eixo X
        val barWidthWithSpacing = chartWidth / 24
        val barWidth = barWidthWithSpacing * 0.7f

        for (hour in 0..23) {
            val bpm = dataByHour[hour]
            val x = yAxisSpace + (barWidthWithSpacing * hour)

            if (bpm != null) {
                val barHeight = ((bpm - minBpm) / range * chartHeight).coerceAtLeast(0f)
                drawRect(
                    color = primaryColor, // Usando a cor obtida fora do Canvas
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