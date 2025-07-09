package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun HeartRateBarChart(
    data: List<BatimentoCardiaco>,
    modifier: Modifier = Modifier
) {
    val dataByHour = data.groupBy {
        val instant = Instant.ofEpochMilli(it.timestamp)
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).hour
    }.mapValues { entry ->
        entry.value.map { it.bpm }.average().toLong()
    }

    if (dataByHour.isEmpty()) {
        return
    }

    val maxBpm = (dataByHour.values.maxOrNull() ?: 120L).toFloat()
    val minBpm = (dataByHour.values.minOrNull() ?: 40L).toFloat()
    val range = (maxBpm - minBpm).coerceAtLeast(1f)

    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

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